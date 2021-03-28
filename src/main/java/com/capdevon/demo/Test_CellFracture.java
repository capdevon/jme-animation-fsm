/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.demo;

import com.capdevon.control.TimekeeperControl;
import com.capdevon.physx.PhysxDebugAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;

/**
 *
 * @author capdevon
 */
public class Test_CellFracture extends SimpleApplication {

    /**
     * @param args 
     */
    public static void main(String[] args) {
        Test_CellFracture app = new Test_CellFracture();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setFrameRate(30);
        settings.setBitsPerPixel(32);
        settings.setSamples(4);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }
    
    private static final String SCENE_MODEL = "Models/gltf2/CellFracture/cube-cell-fracture.j3o";
    
    @Override
    public void simpleInitApp() {
        cam.setLocation(Vector3f.UNIT_XYZ.mult(10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(20f);
        
        stateManager.attach(new BulletAppState());
        stateManager.attach(new PhysxDebugAppState());
        
        setupScene();
        setupLights();
    }
    
    private void setupLights() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.clone());
        rootNode.addLight(ambient);
        ambient.setName("ambient");
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.clone());
        rootNode.addLight(sun);
        sun.setName("sun");
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 2_048, 3);
        shadowFilter.setLight(sun);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);
        fpp.addFilter(shadowFilter);
    }

    private void setupScene() {
        
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        inputManager.addMapping(TimekeeperControl.INPUT_TOGGLE_REWIND, new KeyTrigger(KeyInput.KEY_RETURN));
        
        Node scene = (Node) getAssetManager().loadModel(SCENE_MODEL);
        rootNode.attachChild(scene);
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        
        // setup scene
        for (Spatial sp : scene.getChildren()) {
            System.out.println("$> ChildName: " + sp);

            if (sp.getName().contains("Cube_cell")) {
                addRigidBody(sp, 10);

                TimekeeperControl timekeeper = new TimekeeperControl();
                sp.addControl(timekeeper);
                inputManager.addListener(timekeeper, TimekeeperControl.INPUT_TOGGLE_REWIND);
                
            } else if (sp.getName().contains("Plane")) {
                addRigidBody(sp, 0);
                sp.setMaterial(getShinyMat());
            }
        }
    }
    
    /**
     * https://wiki.jmonkeyengine.org/docs/3.3/physics/physics.html#specify-physical-properties
     * @param sp
     * @param mass
     */
    private void addRigidBody(Spatial sp, float mass) {
        BoundingBox vol = (BoundingBox) sp.getWorldBound();
        CollisionShape shape = new BoxCollisionShape(vol.getExtent(null));
        RigidBodyControl rgb = new RigidBodyControl(shape, mass);
        rgb.setCcdMotionThreshold(0.001f);
        sp.addControl(rgb);
        getPhysicsSpace().add(rgb);
        rgb.setFriction(.5f); 		// Ice: 0.0f - Rock: 1.0f
        rgb.setRestitution(.1f);	// Brick: 0.0f - Rubber ball: 1.0f
    }
    
    private Material getShinyMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);  // needed for shininess
        mat.setColor("Specular", ColorRGBA.White); 	// needed for shininess
        mat.setColor("Diffuse", ColorRGBA.White); 	// needed for shininess
        mat.setFloat("Shininess", 0); 				// shininess from 1-128
        return mat;
    }
    
    private PhysicsSpace getPhysicsSpace() {
        return stateManager.getState(BulletAppState.class).getPhysicsSpace();
    }
}
