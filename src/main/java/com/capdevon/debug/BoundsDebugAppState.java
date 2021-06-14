package com.capdevon.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.debug.WireSphere;

/**
 * 
 * @author capdevon
 */
public class BoundsDebugAppState extends BaseAppState {

    protected static final Logger logger = Logger.getLogger(BoundsDebugAppState.class.getName());

    protected Application app;
    protected AssetManager assetManager;
    protected Node rootNode;
    /**
     * scene-graph node to parent the geometries
     */
    protected final Node boundsDebugRootNode = new Node("Bounds Debug Root Node");
    /**
     * view port in which to render (not null)
     */
    protected ViewPort viewPort;
    protected RenderManager rm;
    /**
     * map bounding volumes to visualizations
     */
    protected HashMap<Node, Spatial> bounds = new HashMap<>();
    /**
     * limit which objects are visualized, or null to visualize all objects
     */
    protected Function<Node, Boolean> filter;

    protected Material wireMaterial;

    public BoundsDebugAppState(Node rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    protected void initialize(Application app) {
        this.app = app;
        this.rm = app.getRenderManager();
        this.assetManager = app.getAssetManager();
        setupMaterials(app);
        boundsDebugRootNode.setCullHint(Spatial.CullHint.Never);
        viewPort = rm.createMainView("Bounds Debug Overlay", app.getCamera());
        viewPort.setClearFlags(false, true, false);
        viewPort.attachScene(boundsDebugRootNode);
    }

    @Override
    protected void cleanup(Application app) {
        rm.removeMainView(viewPort);
    }

    @Override
    public void update(float tpf) {
        updateBounds();
        // update our debug root node
        boundsDebugRootNode.updateLogicalState(tpf);
        boundsDebugRootNode.updateGeometricState();
    }

    @Override
    public void render(RenderManager rm) {
        super.render(rm);
        if (viewPort != null) {
            rm.renderScene(boundsDebugRootNode, viewPort);
        }
    }

    @Override
    protected void onDisable() {}

    @Override
    protected void onEnable() {}

    /**
     * Alter which objects are visualized.
     *
     * @param filter the desired filter, or null to visualize all objects
     */
    public void setFilter(Function<Node, Boolean> filter) {
        this.filter = filter;
    }

    /**
     * Alter the color of all lines.
     *
     * @param newColor (not null, unaffected)
     */
    public void setColor(ColorRGBA newColor) {
        ColorRGBA colorClone = newColor.clone();
        wireMaterial.setColor("Color", colorClone);
    }

    /**
     * Alter the depth test setting. The test provides depth cues, but might
     * hide portions of the visualization.
     *
     * @param newSetting true to enable test, false to disable it
     */
    public void setDepthTest(boolean newSetting) {
        wireMaterial.getAdditionalRenderState().setDepthTest(newSetting);
    }

    /**
     * Alter the effective line width of the visualization.
     *
     * @param newWidth (in pixels, &ge;0, values &lt;1 hide the lines)
     */
    public void setLineWidth(float newWidth) {
        wireMaterial.getAdditionalRenderState().setLineWidth(newWidth);
    }

    /**
     * Initialize the materials.
     *
     * @param app the application which owns this state (not null)
     */
    private void setupMaterials(Application app) {
        AssetManager manager = app.getAssetManager();
        wireMaterial = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        wireMaterial.getAdditionalRenderState().setWireframe(true);
        wireMaterial.setColor("Color", ColorRGBA.Green);
    }

    private void updateBounds() {

        HashMap<Node, Spatial> oldObjects = bounds;
        bounds = new HashMap<Node, Spatial>();

        // create new map
        rootNode.depthFirstTraversal(new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node geo) {
                // copy existing spatials
                if (oldObjects.containsKey(geo)) {
                    Spatial spat = oldObjects.get(geo);
                    bounds.put(geo, spat);
                    oldObjects.remove(geo);
                } else {
                    if (filter == null || filter.apply(geo)) {
                        if (geo.getWorldBound() != null) {
                            logger.log(Level.FINE, "Create new debug BoundingVolume");
                            // create new spatial
                            Node node = new Node("Bounds." + geo.toString());
                            node.addControl(new BoundingVolumeDebugControl(BoundsDebugAppState.this, geo));
                            bounds.put(geo, node);
                            boundsDebugRootNode.attachChild(node);
                        }
                    }
                }
            }
        });

        // remove leftover spatials
        for (Map.Entry<Node, Spatial> entry : oldObjects.entrySet()) {
            Spatial spatial = entry.getValue();
            spatial.removeFromParent();
        }
    }

    /**
     * --------------------------------------------------------
     * @class BoundingVolumeDebugControl
     * --------------------------------------------------------
     */
    public class BoundingVolumeDebugControl extends AbstractControl {

        Node source;
        Geometry wire;

        public BoundingVolumeDebugControl(BoundsDebugAppState debugAppState, Node source) {
            this.source = source;

            wire = makeGeometry(source.getWorldBound());
            wire.setName(source.toString());
            wire.setMaterial(debugAppState.wireMaterial);
            //wire.setShadowMode(RenderQueue.ShadowMode.Off);
            //wire.setQueueBucket(RenderQueue.Bucket.Inherit);
        }

        private Geometry makeGeometry(BoundingVolume bv) {
            Mesh mesh = null;

            if (bv.getType() == BoundingVolume.Type.AABB) {
                BoundingBox bb = (BoundingBox) bv;
                mesh = new WireBox(bb.getXExtent(), bb.getYExtent(), bb.getZExtent());
            }
            if (bv.getType() == BoundingVolume.Type.Sphere) {
                BoundingSphere bs = (BoundingSphere) bv;
                mesh = new WireSphere(bs.getRadius());
            }

            Geometry debug = new Geometry();
            debug.setMesh(mesh);
            debug.updateModelBound();
            debug.updateGeometricState();
            return debug;
        }

        @Override
        public void setSpatial(Spatial spatial) {
            if (spatial != null && spatial instanceof Node) {
                Node node = (Node) spatial;
                node.attachChild(wire);

            } else if (spatial == null && this.spatial != null) {
                Node node = (Node) this.spatial;
                node.detachChild(wire);
            }
            super.setSpatial(spatial);
        }

        @Override
        protected void controlUpdate(float tpf) {
            wire.setLocalTransform(source.getWorldTransform());
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {}
    }

}
