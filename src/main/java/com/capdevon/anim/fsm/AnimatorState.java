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
 * a Motion (AnimClip or BlendTree) which will play while the character is
 * in that state. When an event in the game triggers a state transition, the
 * character will be left in a new state whose animation sequence will then take
 * over.
 *
 * @author capdevon
 */
public class AnimatorState {

    private AnimatorController animator;
    
    //The animation clip assigned to this state
    protected String action;
    //A name can be used to identify a state.
    protected String name;
    //The default speed of the motion.
    protected float speed = 1f;
    //The transitions that are going out of the state.
    protected List<AnimatorStateTransition> transitions = new ArrayList<>();
    //The behaviour list assigned to this state.
    protected List<StateMachineBehaviour> behaviours = new ArrayList<>();
        
    /**
     * Constructor.
     * @param animator
     */
    protected AnimatorState(String name, AnimatorController animator) {
    	this.name = name;
        this.animator = animator;
    }
    
    /**
     * Adds a state machine behaviour class to the AnimatorState.
     * @param behaviour The state machine behaviour to add.
     */
    public void addStateMachineBehaviour(StateMachineBehaviour behaviour) {
    	behaviours.add(behaviour);
    }
    
    /**
     * Utility function to remove a transition from the state.
     * @param transition Transition to remove.
     */
    public void removeTransition(AnimatorStateTransition transition) {
        transitions.remove(transition);
    }
    
    /**
     * Utility function to add an outgoing transition to the destination state.
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
		if (animator != null) {
			return animator.animComposer.getTime() / animator.animComposer.getAnimClip(action).getLength();
		}
		return 0;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getName() {
		return name;
	}
	
	public float getSpeed() {
		return speed;
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
