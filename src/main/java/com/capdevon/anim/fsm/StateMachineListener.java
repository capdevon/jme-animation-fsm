package com.capdevon.anim.fsm;

/**
 * 
 * @author capdevon
 */
public interface StateMachineListener {
	
	/**
	 * These listeners will be invoked any time a state transition occurs and can be utilized for things such as logging or other.
	 * @param from
	 * @param to
	 */
	public void onStateChanged(AnimatorState from, AnimatorState to);

}
