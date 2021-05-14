/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.control.AdapterControl;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.scene.Spatial;

/**
 * The Animator Controller controls animation with state machine, controlled by parameters.
 * 
 * @author capdevon
 */
public class AnimatorController extends AdapterControl {
    
    private static final Logger logger = Logger.getLogger(AnimatorController.class.getName());

    AnimComposer animComposer;
    SkinningControl skinningControl;
    AnimatorStateMachine stateMachine;
    List<AnimatorControllerParameter> parameters = new ArrayList<>();
    
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
            skinningControl = getComponentInChild(SkinningControl.class);
            animComposer = getComponentInChild(AnimComposer.class);
            logger.log(Level.INFO, "{0} --Animations: {1}", new Object[]{spatial.getName(), animComposer.getAnimClipsNames()});
        }
    }
        
    @Override
    protected void controlUpdate(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
        stateMachine.update(tpf);
    }

    /**
     * 
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

    //--------------------------------------------------------------------------
    // Boolean
    //--------------------------------------------------------------------------
    public boolean getBool(int id) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.nameHash == id && param.type == AnimatorControllerParameterType.Bool) {
                return param.defaultBool;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + id);
    }

    public boolean getBool(String paramName) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(paramName) && param.type == AnimatorControllerParameterType.Bool) {
                return param.defaultBool;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + paramName);
    }

    public void setBool(String paramName, boolean value) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(paramName) && param.type == AnimatorControllerParameterType.Bool) {
                param.defaultBool = value;
                return;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + paramName);
    }
    
    //--------------------------------------------------------------------------
    // Float
    //--------------------------------------------------------------------------
    public float getFloat(int id) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.nameHash == id && param.type == AnimatorControllerParameterType.Float) {
                return param.defaultFloat;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + id);
    }

    public float getFloat(String paramName) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(paramName) && param.type == AnimatorControllerParameterType.Float) {
                return param.defaultFloat;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + paramName);
    }

    public void setFloat(String paramName, float value) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(paramName) && param.type == AnimatorControllerParameterType.Float) {
                param.defaultFloat = value;
                return;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + paramName);
    }
 
    //--------------------------------------------------------------------------
    // Integer
    //--------------------------------------------------------------------------
    public int getInt(int id) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.nameHash == id && param.type == AnimatorControllerParameterType.Int) {
                return param.defaultInt;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + id);
    }

    public int getInt(String paramName) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(paramName) && param.type == AnimatorControllerParameterType.Int) {
                return param.defaultInt;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + paramName);
    }

    public void setInt(String paramName, int value) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(paramName) && param.type == AnimatorControllerParameterType.Int) {
                param.defaultInt = value;
                return;
            }
        }
        throw new IllegalArgumentException("AnimatorControllerParameter not found: " + paramName);
    }
}
