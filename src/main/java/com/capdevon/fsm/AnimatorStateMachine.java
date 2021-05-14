/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A graph controlling the interaction of states. Each state references a motion.
 * 
 * @author capdevon
 */
public class AnimatorStateMachine {
    
    private static final Logger logger = Logger.getLogger(AnimatorStateMachine.class.getName());

    private final AnimatorController animator;

    //The state that the state machine will be in when it starts
    protected AnimatorState defaultState;
    //The list of states.
    protected Map<String, AnimatorState> states = new HashMap<>();

    AnimatorStateMachine(AnimatorController animator) {
        this.animator = animator;
    }

    public void setDefaultState(AnimatorState state) {
        defaultState = state;
        animator.animComposer.setCurrentAction(defaultState.action);
    }

    public AnimatorState addState(String stateName, String animName) {
        if (animator.animComposer.getAnimClip(animName) == null) {
            throw new IllegalArgumentException("Can't find an animation clip with name " + animName);
        }
        
        AnimatorState state = new AnimatorState(animator);
        state.name = stateName;
        state.action = animName;
        states.put(stateName, state);
        return state;
    }

    protected void update(float tpf) {
        AnimatorState nextState = defaultState.checkTransitions();
        if (defaultState != nextState) {
            animator.animComposer.setCurrentAction(nextState.action);
            animator.animComposer.setGlobalSpeed(nextState.speed);
            defaultState = nextState;
            logger.log(Level.INFO, "onAnimChange: {0}", defaultState);
        }
    }

}
