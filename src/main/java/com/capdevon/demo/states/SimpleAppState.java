package com.capdevon.demo.states;

import java.util.Objects;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public abstract class SimpleAppState extends BaseAppState {
    
    // variables
    public AppSettings      settings;
    public AppStateManager  stateManager;
    public AssetManager     assetManager;
    public InputManager     inputManager;
    public RenderManager    renderManager;
    public ViewPort         viewPort;
    public Camera           camera;
    public Node             rootNode;
    public Node             guiNode;
    public BitmapFont       guiFont;
    
    @Override
    public void initialize(Application app) {
        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("application should be a SimpleApplication");
        }

        refreshCacheFields();
    }
    
    protected void refreshCacheFields() {
    	SimpleApplication app = (SimpleApplication) getApplication();
        this.settings       = app.getContext().getSettings();
        this.stateManager   = app.getStateManager();
        this.assetManager   = app.getAssetManager();
        this.inputManager   = app.getInputManager();
        this.renderManager  = app.getRenderManager();
        this.viewPort       = app.getViewPort();
        this.camera         = app.getCamera();
        this.rootNode       = app.getRootNode();
        this.guiNode        = app.getGuiNode();
        this.guiFont        = assetManager.loadFont("Interface/Fonts/Default.fnt");
    }

    public PhysicsSpace getPhysicsSpace() {
        return getState(BulletAppState.class, true).getPhysicsSpace();
    }

    /**
     * Attempts to find a child spatial with the specified 
     * name within the scene's root node.
     *
     * @param childName The name of the child spatial to search for.
     * @return The found child spatial if successful.
     * @throws NullPointerException if no child with the specified name is found.
     */
    public Spatial find(String childName) {
        Spatial child = rootNode.getChild(childName);
        return Objects.requireNonNull(child, "The spatial could not be found: " + childName);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

}
