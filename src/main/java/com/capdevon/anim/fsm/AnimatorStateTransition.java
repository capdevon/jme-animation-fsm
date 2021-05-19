/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;

/**
 * Transitions define when and how the state machine switches from one state to another.
 * A transition happens when all its conditions are met.
 *
 * @author capdevon
 */
public class AnimatorStateTransition {
    
    private AnimatorController animator;
    
    //The duration of the transition.
    protected float duration = 0.25f;
    //Determines whether the duration of the transition is reported in a fixed duration in seconds or as a normalized time.
    protected boolean hasFixedDuration = true;
    //If AnimatorStateTransition.hasExitTime is true, exitTime represents the exact time at which the transition can take effect.
    protected float exitTime = 0.75f;
    //When active the transition will have an exit time condition.
    protected boolean hasExitTime = false;
    //The destination state of the transition.
    protected AnimatorState destinationState;
    //AnimatorCondition conditions that need to be met for a transition to happen.
    protected List<AnimatorCondition> conditions = new ArrayList<>();
    
    /**
     * Constructor.
     * @param animator
     */
    protected AnimatorStateTransition(AnimatorController animator) {
        this.animator = animator;
    }
    
    /**
     * Utility function to remove a condition from the transition.
     * 
     * @param condition 
     */
    public void removeCondition(AnimatorCondition condition) {
        conditions.remove(condition);
    }

    /**
     * Utility function to add a condition to a transition.
     * 
     * @param mode
     * @param threshold
     * @param parameter 
     */
    public void addCondition(AnimatorConditionMode mode, float threshold, String parameter) {
        if (animator.getParameter(parameter) == null) {
            throw new IllegalArgumentException("AnimatorControllerParameter not found: " + parameter);
        }

        AnimatorCondition condition = new AnimatorCondition();
        condition.mode = mode;
        condition.threshold = threshold;
        condition.parameter = parameter;
        conditions.add(condition);
    }

    protected boolean checkConditions(double animPercent) {
        if (hasExitTime) {
            return animPercent >= exitTime;
        }
        
        for (AnimatorCondition condition : conditions) {
            if (!condition.evalute(animator.parameters)) {
                return false;
            }
        }
        return true;
    }

}
