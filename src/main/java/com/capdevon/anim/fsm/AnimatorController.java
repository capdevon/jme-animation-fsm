/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.control.AdapterControl;
import com.jme3.anim.AnimComposer;
import com.jme3.scene.Spatial;

/**
 * The Animator Controller controls animation with state machine, controlled by parameters.
 * 
 * @author capdevon
 */
public class AnimatorController extends AdapterControl {
    
    private static final Logger logger = Logger.getLogger(AnimatorController.class.getName());

    protected AnimComposer animComposer;
    protected AnimatorStateMachine stateMachine;
    protected List<AnimatorControllerParameter> parameters = new ArrayList<>();
    
    /**
     * Constructor.
     */
    public AnimatorController() {
        stateMachine = new AnimatorStateMachine(this);
    }
    
    public AnimatorStateMachine getStateMachine() {
        return stateMachine;
    }
    	
    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        if (spatial != null) {
            animComposer = getComponentInChild(AnimComposer.class);
            Objects.requireNonNull(animComposer, "AnimComposer not found in subtree: " + spatial);
            logger.log(Level.INFO, "AnimatorController initialized for " + spatial);
        }
    }
        
    @Override
    protected void controlUpdate(float tpf) {
        stateMachine.update(tpf);
    }
    
    /**
     * Utility function to remove a parameter from the controller.
     * @param param
     */
    public void removeParameter(AnimatorControllerParameter param) {
    	parameters.remove(param);
    }

    /**
     * Utility function to add a parameter to the controller.
     * @param name
     * @param type 
     */
    public void addParameter(String name, AnimatorControllerParameterType type) {
        AnimatorControllerParameter param = new AnimatorControllerParameter();
        param.name = name;
        param.type = type;
        param.nameHash = name.hashCode();
        parameters.add(param);
    }
    
    /**
     * Returns the parameter with the given name or null if the parameter is not found.
     * @param name
     * @return the parameter
     */
	public AnimatorControllerParameter getParameter(String name) {
		for (AnimatorControllerParameter param : parameters) {
			if (param.nameHash == name.hashCode()) {
				return param;
			}
		}
		return null;
	}
    
    //--------------------------------------------------------------------------
    // Float
    //--------------------------------------------------------------------------
    public float getFloat(String name) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Float);
    	return param.defaultFloat;
    }

    public void setFloat(String name, float value) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Float);
    	param.defaultFloat = value;
    }
 
    //--------------------------------------------------------------------------
    // Integer
    //--------------------------------------------------------------------------
    public int getInt(String name) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Int);
    	return param.defaultInt;
    }

    public void setInt(String name, int value) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Int);
    	param.defaultInt = value;
    }
    
    //--------------------------------------------------------------------------
    // Boolean
    //--------------------------------------------------------------------------
    public boolean getBool(String name) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Bool);
    	return param.defaultBool;
    }
    
    public void setBool(String name, boolean value) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Bool);
    	param.defaultBool = value;
    }
    
    //--------------------------------------------------------------------------
    // Trigger
    //--------------------------------------------------------------------------
    public void setTrigger(String name) {
    	AnimatorControllerParameter param = findParameter(name, AnimatorControllerParameterType.Trigger);
    	param.defaultBool = true;
    }
    
    /**
     * Find a parameter with the given name. 
     * Throws an exception if the parameter is not found.
     */
    private AnimatorControllerParameter findParameter(String name, AnimatorControllerParameterType type) {
    	return findParameter(name.hashCode(), type);
    }
    
    /**
     * Find a parameter with the given id. 
     * Throws an exception if the parameter is not found.
     */
    private AnimatorControllerParameter findParameter(int id, AnimatorControllerParameterType type) {
    	for (AnimatorControllerParameter param : parameters) {
            if (param.nameHash == id && param.type == type) {
                return param;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + id);
    }
    
}
