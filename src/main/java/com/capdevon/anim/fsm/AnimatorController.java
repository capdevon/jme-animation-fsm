package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.control.AdapterControl;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimationMask;

/**
 * The Animator Controller controls animation with state machine, controlled by parameters.
 * 
 * @author capdevon
 */
public class AnimatorController extends AdapterControl {

    private static final Logger logger = Logger.getLogger(AnimatorController.class.getName());

    protected AnimComposer animComposer;
    //The layers in the controller.
    protected List<AnimatorControllerLayer> layers = new ArrayList<>();
    //Parameters are used to communicate between scripting and the controller. They are used to drive transitions and blendtrees for example.
    protected List<AnimatorControllerParameter> parameters = new ArrayList<>();

    /**
     * Constructor.
     * @param animComposer
     */
    public AnimatorController(AnimComposer animComposer) {
        this.animComposer = animComposer;
        addLayer(AnimComposer.DEFAULT_LAYER, null);
    }

    @Override
    protected void controlUpdate(float tpf) {
        layers.forEach(layer -> layer.stateMachine.update(tpf));
    }

    /**
     * Utility function to add a layer to the controller.
     * @param name - The name of the Layer.
     * @param mask - The desired mask for the new layer (alias created)
     */
    public AnimatorControllerLayer addLayer(String name, AnimationMask mask) {
        AnimatorControllerLayer layer = new AnimatorControllerLayer();
        layer.name = name;
        layer.avatarMask = mask;
        layer.stateMachine = new AnimatorStateMachine(this);
        layer.stateMachine.name = layer.name;
        layers.add(layer);
        animComposer.makeLayer(name, mask);
        return layer;
    }

    /**
     * Utility function to remove a layer from the controller.
     * @param index - The AnimatorLayer.
     */
    public void removeLayer(AnimatorControllerLayer layer) {
        layers.remove(layer);
        animComposer.removeLayer(layer.name);
    }
    
    public AnimatorControllerLayer getLayer(String name) {
    	for (AnimatorControllerLayer layer : layers) {
    		if (layer.name.equals(name)) {
    			return layer;
    		}
    	}
    	return null;
    }

    /**
     * Returns an unmodifiable collection of all available layers. When an attempt
     * is made to modify the collection, an UnsupportedOperationException is thrown.
     *
     * @return the unmodifiable collection of layers
     */
    public Collection<AnimatorControllerLayer> getLayers() {
        return Collections.unmodifiableCollection(layers);
    }

    /**
     * Utility function to add a parameter to the controller.
     * @param name - The name of the parameter.
     * @param type - The type of the parameter.
     */
    public void addParameter(String name, AnimatorControllerParameterType type) {
        AnimatorControllerParameter param = new AnimatorControllerParameter();
        param.name = name;
        param.type = type;
        param.nameHash = name.hashCode();
        parameters.add(param);
    }

    /**
     * Utility function to remove a parameter from the controller.
     * @param param - The AnimatorParameter.
     */
    public void removeParameter(AnimatorControllerParameter param) {
        parameters.remove(param);
    }

    /**
     * Returns the parameter with the given name or null if the parameter is not found.
     * @param name
     * @return the parameter
     */
    public AnimatorControllerParameter getParameter(String name) {
        for (AnimatorControllerParameter param: parameters) {
            if (param.nameHash == name.hashCode()) {
                return param;
            }
        }
        return null;
    }
    
    /**
     * Returns an unmodifiable collection of all available parameters. When an attempt
     * is made to modify the collection, an UnsupportedOperationException is thrown.
     *
     * @return the unmodifiable collection of parameters
     */
    public Collection<AnimatorControllerParameter> getParameters() {
    	return Collections.unmodifiableCollection(parameters);
    }

    public float getFloat(String name) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Float);
        return param.defaultFloat;
    }

    public void setFloat(String name, float value) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Float);
        param.defaultFloat = value;
    }

    public int getInt(String name) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Int);
        return param.defaultInt;
    }

    public void setInt(String name, int value) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Int);
        param.defaultInt = value;
    }

    public boolean getBool(String name) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Bool);
        return param.defaultBool;
    }

    public void setBool(String name, boolean value) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Bool);
        param.defaultBool = value;
    }

    public void setTrigger(String name) {
        AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Trigger);
        param.defaultBool = true;
    }

    /**
     * Find a parameter with the given name. 
     * Throws an exception if the parameter is not found.
     */
    private AnimatorControllerParameter findParameter(String name, AnimatorControllerParameterType type) {
        for (AnimatorControllerParameter param: parameters) {
            if (param.nameHash == name.hashCode() && param.type == type) {
                return param;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + name);
    }

}
