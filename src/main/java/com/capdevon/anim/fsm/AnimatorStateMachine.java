/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    private AnimatorController animator;

    //The any state name
    public static final String ANY_STATE = "Any State";
    //The anyState, not a proper state but used as dummy
    protected AnimatorState anyState = new AnimatorState(ANY_STATE, null);
    //The state that the state machine will be in when it starts
    protected AnimatorState currentState = anyState;
    //The list of states
    protected Map<String, AnimatorState> states = new HashMap<>();
    //The list of listeners
    protected List<StateMachineListener> listeners = new ArrayList<>();

    /**
     * Constructor.
     * @param animator
     */
    protected AnimatorStateMachine(AnimatorController animator) {
        this.animator = animator;
    }

    /**
     * Adds a new listener.
     * @param listener The listener to add.
     */
    public void addListener(StateMachineListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("The given listener is already registed at this AnimatorStateMachine");
        }

        listeners.add(listener);
    }

    /**
     * Removes the given listener from listening to events.
     * @param listener
     */
    public void removeListener(StateMachineListener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("The given listener is not registed at this AnimatorStateMachine");
        }
    }

    public void setDefaultState(AnimatorState state) {
        anyState.transitions.clear();
        anyState.addTransition(state);
    }

    /**
     * Utility function to add a state to the state machine.
     * @param stateName
     * @param animName
     * @return
     */
    public AnimatorState addState(String stateName, String animName) {
        if (animator.animComposer.getAnimClip(animName) == null) {
            throw new IllegalArgumentException("Cannot find an animation clip with name " + animName);
        }

        AnimatorState state = new AnimatorState(stateName, animator);
        state.action = animName;
        states.put(stateName, state);
        return state;
    }

    /**
     * Utility function to remove a state from the state machine.
     * @param stateName
     */
    public void removeState(String stateName) {
        states.remove(stateName);
    }

    /**
     * Find a state with the given name. 
     * Throws an exception if the state is not found.
     * 
     * @param stateName
     * @return
     */
    public AnimatorState findState(String stateName) {
        AnimatorState state = getState(stateName);
        if (state == null) {
            throw new IllegalArgumentException("Cannot find state with name " + stateName);
        }
        return state;
    }

    /**
     * Returns the state with the given name or null if the state is not found.
     *
     * @param stateName the name of the state
     * @return the state.
     */
    public AnimatorState getState(String stateName) {
        if (stateName.equals(ANY_STATE)) {
            return anyState;
        }
        return states.get(stateName);
    }

    /**
     * Returns a read only collection of the states.
     *
     * @return the states.
     */
    public Collection<AnimatorState> getStates() {
        return states.values();
    }

    protected void update(float tpf) {
        AnimatorState nextState = currentState.checkTransitions();
        if (currentState != nextState) {

            listeners.forEach(listener -> listener.onStateChanged(currentState, nextState));

            logger.log(Level.INFO, "onStateExit: {0}", currentState);
            currentState.behaviours.forEach(behaviour -> behaviour.onStateExit(animator));

            animator.animComposer.setCurrentAction(nextState.action);
            animator.animComposer.setGlobalSpeed(nextState.speed);
            currentState = nextState;

            logger.log(Level.INFO, "onStateEnter: {0}", currentState);
            currentState.behaviours.forEach(behaviour -> behaviour.onStateEnter(animator));
        }

        currentState.behaviours.forEach(behaviour -> behaviour.onStateUpdate(animator, tpf));
    }

}
