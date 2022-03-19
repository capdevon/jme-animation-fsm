package com.capdevon.anim.fsm;

/**
 * StateMachineBehaviour is a component that can be added to an AnimatorState.
 *
 * @author capdevon
 */
public interface StateMachineBehaviour {

    /**
     * Called on the first Update frame when a state machine evaluate this
     * state.
     *
     * @param animator
     */
    public void onStateEnter(AnimatorController animator);

    /**
     * Called at each Update frame except for the first and last frame.
     *
     * @param animator
     * @param tpf
     */
    public void onStateUpdate(AnimatorController animator, float tpf);

    /**
     * Called on the last update frame when a state machine evaluate this state.
     *
     * @param animator
     */
    public void onStateExit(AnimatorController animator);

}
