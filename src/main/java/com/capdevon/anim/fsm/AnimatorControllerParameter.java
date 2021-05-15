/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

/**
 *
 * @author capdevon
 */
public class AnimatorControllerParameter {

    public enum AnimatorControllerParameterType {
        Float, Int, Bool, Trigger
    }

    //The default bool value for the parameter.
    protected boolean defaultBool = false;
    //The default float value for the parameter.
    protected float defaultFloat = 0f;
    //The default int value for the parameter.
    protected int defaultInt = 0;
    //The name of the parameter.
    protected String name;
    //Returns the hash of the parameter based on its name.
    protected int nameHash;
    //The type of the parameter.
    protected AnimatorControllerParameterType type;
    
}
