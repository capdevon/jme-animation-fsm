/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import java.util.List;

/**
 * Condition that is used to determine if a transition must be taken.
 *
 * @author capdevon
 */
public class AnimatorCondition {

    //The mode of the condition.
    public AnimatorConditionMode mode;
    //The name of the parameter used in the condition.
    public String parameter;
    //The AnimatorParameter's threshold value for the condition to be true.
    public float threshold;
    
    AnimatorCondition() {
        // default constructor.
    }

    protected boolean evalute(List<AnimatorControllerParameter> parameters) {
        for (AnimatorControllerParameter param : parameters) {
            if (param.name.equals(parameter)) {
                switch (param.type) {
                    case Int:
                        return evaluateInt(param);
                    case Float:
                        return evaluateFloat(param);
                    case Bool:
                        return evaluateBool(param);
                    case Trigger:
                    	return evaluateTrigger(param);
                }
            }
        }
        
        return false;
    }
    
    private boolean evaluateInt(AnimatorControllerParameter param) {
        switch (mode) {
            case Greater:
                return param.defaultInt > threshold;
            case Less:
                return param.defaultInt < threshold;
            case Equals:
                return param.defaultInt == threshold;
            case NotEqual:
                return param.defaultInt != threshold;
            default:
                return false;
        }
    }
    
    private boolean evaluateFloat(AnimatorControllerParameter param) {
        switch (mode) {
            case Greater:
                return param.defaultFloat > threshold;
            case Less:
                return param.defaultFloat < threshold;
            case Equals:
                return param.defaultFloat == threshold;
            case NotEqual:
                return param.defaultFloat != threshold;
            default:
                return false;
        }
    }
    
    private boolean evaluateBool(AnimatorControllerParameter param) {
        switch (mode) {
            case If:
                return param.defaultBool;
            case IfNot:
                return !param.defaultBool;
            default:
                return false;
        }
    }
    
    private boolean evaluateTrigger(AnimatorControllerParameter param) {
    	boolean triggered = param.defaultBool;
    	if (triggered) {
    		param.defaultBool = false;
    	}
        return triggered;
    }

}
