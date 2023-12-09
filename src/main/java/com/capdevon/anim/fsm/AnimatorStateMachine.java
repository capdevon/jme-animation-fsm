package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.capdevon.anim.fsm.BlendTree.BlendTreeType;
import com.jme3.anim.tween.action.Action;
import com.jme3.anim.tween.action.BlendAction;
import com.jme3.anim.tween.action.LinearBlendSpace;

/**
 * A graph controlling the interaction of states. Each state references a motion.
 * 
 * @author capdevon
 */
public class AnimatorStateMachine {

    private static final Logger logger = Logger.getLogger(AnimatorStateMachine.class.getName());

    private AnimatorController animator;

    // The name of the state machine.
    protected String layerName;
    // The anyState, not a proper state but used as dummy.
    protected AnimatorState anyState;
    // The state that the state machine will be in when it starts.
    protected AnimatorState currentState;
    // The list of states.
    protected Map<String, AnimatorState> states = new HashMap<>();
    // The list of listeners.
    protected List<StateMachineListener> listeners = new ArrayList<>();

    /**
     * Constructor.
     * @param animator
     */
    protected AnimatorStateMachine(AnimatorController animator) {
        this.animator = animator;
        anyState = new AnimatorState("AnyState", animator);
        currentState = anyState;
    }

    /**
     * Adds a new listener.
     * @param listener The listener to add.
     */
    public void addListener(StateMachineListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException(
                    "The given listener is already registed at this AnimatorStateMachine");
        }

        listeners.add(listener);
    }

    /**
     * Removes the given listener from listening to events.
     * @param listener
     */
    public void removeListener(StateMachineListener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException(
                    "The given listener is not registed at this AnimatorStateMachine");
        }
    }

    /**
     * Sets the initial state of this StateMachine.
     * @param state the initial state
     */
    public void setDefaultState(AnimatorState state) {
        anyState.transitions.clear();
        anyState.addTransition(state);
    }

    /**
     * Creates a BlendTree in a new AnimatorState.
     * @param stateName
     * @param blendTree
     * @return
     */
    public AnimatorState createBlendTree(String stateName, BlendTree blendTree) {

        if (blendTree.blendType == BlendTreeType.Simple1D) {

            if (blendTree.motions.size() < 2) {
                throw new IllegalArgumentException("BlendTree requires at least 2 animations");
            }

            blendTree.name = stateName;

            LinearBlendSpace blendSpace = new LinearBlendSpace(blendTree.minThreshold, blendTree.maxThreshold);
            String[] clips = blendTree.getAnimMotionsNames();
            BlendAction action = animator.animComposer.actionBlended(blendTree.name, blendSpace, clips);
            action.clearSpeedFactors();
            logger.log(Level.INFO, "BlendAction created: {0}", blendTree.name);

        } else if (blendTree.blendType == BlendTreeType.SimpleDirectional2D) {

            for (ChildMotion childMotion : blendTree.motions) {
                Action action = animator.animComposer.action(childMotion.animName);
                logger.log(Level.INFO, "ActionClip created: {0}", action);
            }
        }

        return addState(stateName, blendTree);
    }

    /**
     * Utility function to add a state to the state machine.
     * @param stateName
     * @param animName
     * @return
     */
    public AnimatorState addState(String stateName, String animName) {
        
    	Action action = animator.animComposer.action(animName);
    	logger.log(Level.INFO, "ActionClip created: {0}", action);
        
        Motion motion = new Motion();
        motion.name = animName;
        return addState(stateName, motion);
    }
    
    /**
     * Utility function to add a state to the state machine.
     * @param stateName
     * @return
     */
    public AnimatorState addState(String stateName) {
    	return addState(stateName, new Motion());
    }
    
    /**
     * InternalCall.
     * @param stateName
     * @param motion
     * @return
     */
    private AnimatorState addState(String stateName, Motion motion) {
    	if (states.containsKey(stateName)) {
            String error = String.format("State '%s' already exists in state machine", stateName);
            throw new IllegalArgumentException(error);
        }
    	
    	AnimatorState state = new AnimatorState(stateName, animator);
        state.motion = motion;
        states.put(stateName, state);
        return state;
    }

    /**
     * Utility function to remove a state from the state machine.
     * @param stateName
     */
    public void removeState(String stateName) {
        AnimatorState state = findState(stateName);
        String animName = state.motion.name;
        if (animName != null) {
            animator.animComposer.removeAction(animName);
        }
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
     * @param stateName the name of the state
     * @return the state.
     */
    public AnimatorState getState(String stateName) {
        return states.get(stateName);
    }

    /**
     * Returns a read only collection of the states.
     * @return the states.
     */
    public Collection<AnimatorState> getStates() {
        return states.values();
    }

    /**
     * InternalCall
     * @param tpf
     */
    protected void update(float tpf) {
        AnimatorState nextState = currentState.checkTransitions(layerName);

        if (currentState != nextState) {

            listeners.forEach(listener -> listener.onStateChanged(currentState, nextState));

            logger.log(Level.INFO, "onStateExit: {0}", currentState);
            currentState.behaviours.forEach(behaviour -> behaviour.onStateExit(animator));

            currentState = nextState;

            logger.log(Level.INFO, "onStateEnter: {0}", currentState);
            currentState.behaviours.forEach(behaviour -> behaviour.onStateEnter(animator));
        }

        currentState.update(layerName, tpf);
        currentState.behaviours.forEach(behaviour -> behaviour.onStateUpdate(animator, tpf));
    }
    
}
