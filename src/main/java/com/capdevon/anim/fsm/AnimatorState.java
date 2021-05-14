/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import com.jme3.math.FastMath;
import java.util.ArrayList;
import java.util.List;

/**
 * States are the basic building blocks of a state machine. Each state contains
 * a Motion (AnimationClip or BlendTree) which will play while the character is
 * in that state. When an event in the game triggers a state transition, the
 * character will be left in a new state whose animation sequence will then take
 * over.
 *
 * @author capdevon
 */
public class AnimatorState {

    private final AnimatorController animator;
    
    // The animation clip assigned to this state
    public String action;
    //A name can be used to identify a state.
    public String name;
    //The default speed of the motion.
    public float speed = 1f;
    //The transitions that are going out of the state.
    public List<AnimatorStateTransition> transitions = new ArrayList<>();
    
    AnimatorState(AnimatorController animator) {
        // default constructor.
        this.animator = animator;
    }
    
    /**
     * Utility function to remove a transition from the state.
     * 
     * @param transition Transition to remove.
     */
    public void removeTransition(AnimatorStateTransition transition) {
        transitions.remove(transition);
    }
    
    /**
     * Utility function to add an outgoing transition to the destination state.
     * 
     * @param destinationState The destination state.
     * @return 
     */
    public AnimatorStateTransition addTransition(AnimatorState destinationState) {
        return addTransition(destinationState, 0);
    }

    public AnimatorStateTransition addTransition(AnimatorState destinationState, float exitTime) {
        AnimatorStateTransition transition = new AnimatorStateTransition(animator);
        transition.destinationState = destinationState;
        if (exitTime > 0) {
            transition.hasExitTime = true;
            transition.exitTime = FastMath.clamp(exitTime, 0, 1);
        }
        
        transitions.add(transition);
        return transition;
    }

    protected AnimatorState checkTransitions() {
        double animPercent = getAnimPercent();
        for (AnimatorStateTransition transition : transitions) {
            if (transition.checkConditions(animPercent)) {
                return transition.destinationState;
            }
        }
        return this;
    }
    
    private double getAnimPercent() {
        return animator.animComposer.getTime() / animator.animComposer.getAnimClip(action).getLength();
    }

    @Override
    public String toString() {
        return "AnimatorState{" 
                + "action=" + action 
                + ", name=" + name 
                + ", speed=" + speed 
                + '}';
    }

}
