package com.capdevon.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.capdevon.engine.GameObject;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

/**
 *
 * @author capdevon
 */
public class AdapterControl extends AbstractControl {

    /**
     * Returns the first child found with exactly the given name
     * (case-sensitive).
     *
     * @param name - the name of the child to retrieve
     * @return
     */
    public Spatial getChild(String name) {
        Spatial child = ((Node) spatial).getChild(name);
        if (child == null) {
            String error = "The child %s could not be found";
            throw new NullPointerException(String.format(error, name));
        }
        return child;
    }

    public <T> T getUserData(String key, boolean failOnMiss) {
        T objValue = spatial.getUserData(key);
        if (failOnMiss) {
            String error = "The UserData %s could not be found";
            return Objects.requireNonNull(objValue, String.format(error, key));
        }
        return objValue;
    }

    /**
     * Returns all components of Type type in the GameObject.
     */
    public <T extends Control> T[] getComponents(Class<T> clazz) {
        return GameObject.getComponents(spatial, clazz);
    }

    /**
     * Returns the component of Type type if the game object has one attached,
     * null if it doesn't.
     */
    public <T extends Control> T getComponent(Class<T> type) {
        return spatial.getControl(type);
    }

    /**
     * Returns the component of Type type in the GameObject or any of its
     * children using depth first search.
     */
    public <T extends Control> T getComponentInChildren(Class<T> type) {
        return GameObject.getComponentInChildren(spatial, type);
    }

    /**
     * Retrieves the component of Type type in the GameObject or any of its
     * parents.
     */
    public <T extends Control> T getComponentInParent(Class<T> type) {
        return GameObject.getComponentInParent(spatial, type);
    }

    @Override
    protected void controlUpdate(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //To change body of generated methods, choose Tools | Templates.
    }

}
