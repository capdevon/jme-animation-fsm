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
    
    private final AnimatorController animator;
    
    //The duration of the transition.
    public float duration = 0.25f;
    //Determines whether the duration of the transition is reported in a fixed duration in seconds or as a normalized time.
    public boolean hasFixedDuration = true;
    //If AnimatorStateTransition.hasExitTime is true, exitTime represents the exact time at which the transition can take effect
    public float exitTime = 0.75f;
    //When active the transition will have an exit time condition.
    public boolean hasExitTime = false;
    //The destination state of the transition.
    public AnimatorState destinationState;
    //AnimatorCondition conditions that need to be met for a transition to happen.
    public List<AnimatorCondition> conditions = new ArrayList<>();
    
    AnimatorStateTransition(AnimatorController animator) {
        // default constructor.
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