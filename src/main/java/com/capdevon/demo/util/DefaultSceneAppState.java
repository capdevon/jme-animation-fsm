package com.capdevon.demo.util;

import com.capdevon.engine.SimpleAppState;
import com.jme3.app.Application;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * 
 * @author capdevon
 */
public class DefaultSceneAppState extends SimpleAppState {
    
    @Override
    public void initialize(Application app) {
        super.initialize(app);
        
        setupLights();
        setupSky();
        createFloor();
    }
    
    /**
     * An ambient light and a directional sun light
     */
    private void setupLights() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));
        rootNode.addLight(ambient);

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 2_048, 3);
        dlsf.setLight(sun);
        dlsf.setShadowIntensity(0.4f);
        dlsf.setShadowZExtend(256);
        fpp.addFilter(dlsf);
        
        FXAAFilter fxaa = new FXAAFilter();
        fpp.addFilter(fxaa);
    }

    /**
     * a sky as background
     */
    private void setupSky() {
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);
    }

    private void createFloor() {
        rootNode.setShadowMode(ShadowMode.CastAndReceive);

        Box box = new Box(20, .1f, 20);
        box.scaleTextureCoordinates(new Vector2f(20, 20));
        Geometry floorGeo = new Geometry("Floor.GeoMesh", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/white_grid.jpg");
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        floorGeo.setMaterial(mat);
        rootNode.attachChild(floorGeo);

        CollisionShape collShape = CollisionShapeFactory.createMeshShape(floorGeo);
        RigidBodyControl rBody = new RigidBodyControl(collShape, 0f);
        floorGeo.addControl(rBody);
        getPhysicsSpace().add(rBody);
    }

}
