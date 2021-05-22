package com.capdevon.anim.fsm;

/**
 * Motions are used by animation States in the animator StateMachines.
 * 
 * @author capdevon
 */
public class Motion {
	
	//The name of the object.
	protected String name;
	
	public Motion(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
