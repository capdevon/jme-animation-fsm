package com.capdevon.debug;

import com.jme3.asset.AssetManager;
import com.jme3.environment.util.BoundingSphereDebug;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.debug.WireSphere;
import com.jme3.shadow.ShadowUtil;

/**
 * https://wiki.jmonkeyengine.org/jme3/advanced/debugging.html#debug-shapes
 */
public class DebugShapes {

    protected final AssetManager assetManager;

    // Node for attaching debug geometries.
    public Node debugNode = new Node("Debug Shape Node");

    public float lineWidth = 1f;

    public DebugShapes(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    /**
     * Use a wireframe grid (com.jme3.scene.debug.Grid) as a ruler or simple
     * floor.
     *
     * @param pos
     * @param size
     * @param color
     * @return
     */
    public Geometry drawGrid(Vector3f pos, int size, ColorRGBA color) {
        Grid grid = new Grid(size, size, 0.2f);
        Geometry geo = new Geometry("wireframe grid", grid);
        geo.setMaterial(createWireMat(color));
        geo.setShadowMode(ShadowMode.Off);
        geo.center().move(pos);
        debugNode.attachChild(geo);
        return geo;
    }

    /**
     * The coordinate axes (com.jme3.scene.debug.Arrow) help you see the
     * cardinal directions (X,Y,Z) from their center point. Scale the arrows to
     * use them as a "ruler" for a certain length.
     *
     * @return
     */
    public Node drawAxis() {
        Node node = new Node("AxisCoords");
        node.attachChild(drawArrow("AX", Vector3f.UNIT_X, ColorRGBA.Red));
        node.attachChild(drawArrow("AY", Vector3f.UNIT_Y, ColorRGBA.Green));
        node.attachChild(drawArrow("AZ", Vector3f.UNIT_Z, ColorRGBA.Blue));
        return node;
    }

    public Geometry drawArrow(String name, Vector3f dir, ColorRGBA color) {
        Arrow arrow = new Arrow(dir);
        Geometry geo = new Geometry(name, arrow);
        geo.setMaterial(createWireMat(color));
        geo.setShadowMode(ShadowMode.Off);
        debugNode.attachChild(geo);
        return geo;
    }

    public Geometry drawMesh(String name, Mesh shape, ColorRGBA color) {
        Geometry geo = new Geometry(name, shape);
        geo.setMaterial(createColorMat(color));
        geo.setShadowMode(ShadowMode.Off);
        debugNode.attachChild(geo);
        return geo;
    }

    /**
     * Use a wireframe cube (com.jme3.scene.debug.WireBox) as a stand-in object
     * to see whether your code scales, positions, or orients, loaded models
     * right.
     *
     * @param size
     * @param color
     * @return
     */
    public Geometry drawWireCube(float size, ColorRGBA color) {
        WireBox box = new WireBox(size, size, size);
        Geometry geo = new Geometry("WireBox", box);
        geo.setMaterial(createWireMat(color));
        geo.setShadowMode(ShadowMode.Off);
        debugNode.attachChild(geo);
        return geo;
    }

    /**
     * Use a wireframe sphere (com.jme3.scene.debug.WireSphere) as a stand-in
     * object to see whether your code scales, positions, or orients, loaded
     * models right.
     *
     * @param radius
     * @param color
     * @return
     */
    public Geometry drawWireSphere(float radius, ColorRGBA color) {
        WireSphere sphere = new WireSphere(radius);
        Geometry geo = new Geometry("WireSphere", sphere);
        geo.setMaterial(createWireMat(color));
        geo.setShadowMode(ShadowMode.Off);
        debugNode.attachChild(geo);
        return geo;
    }

    public Geometry drawSphereDebug(float radius, ColorRGBA color) {
        BoundingSphereDebug sphere = new BoundingSphereDebug();
        Geometry geo = new Geometry("SphereDebug", sphere);
        geo.setMaterial(createWireMat(color));
        geo.setShadowMode(RenderQueue.ShadowMode.Off);
        geo.setLocalScale(radius);
        debugNode.attachChild(geo);
        return geo;
    }

    public Geometry drawCameraFrustum(Camera cam) {

        Vector3f[] points = new Vector3f[8];
        for (int i = 0; i < 8; i++) {
            points[i] = new Vector3f();
        }

        Camera frustumCam = cam.clone();
        frustumCam.setLocation(new Vector3f(0, 0, 0));
        frustumCam.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
        ShadowUtil.updateFrustumPoints2(frustumCam, points);
        Mesh mesh = new WireFrustum(points);

        Geometry frustumGeo = new Geometry("Viewing.Frustum", mesh);
        Material mat = new Material(assetManager, Materials.UNSHADED);
        mat.setColor("Color", ColorRGBA.White);
        frustumGeo.setMaterial(mat);
        frustumGeo.setCullHint(Spatial.CullHint.Never);
        frustumGeo.setShadowMode(RenderQueue.ShadowMode.Off);

        debugNode.attachChild(frustumGeo);
        return frustumGeo;
    }

    private Material createColorMat(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    private Material createWireMat(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setLineWidth(lineWidth);
        return mat;
    }

    /**
     * Render all the debug geometries to the specified view port.
     *
     * @param rm the render manager (not null)
     * @param vp the view port (not null)
     */
    public void show(RenderManager rm, ViewPort vp) {
        debugNode.updateLogicalState(0);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, vp);
    }
}
