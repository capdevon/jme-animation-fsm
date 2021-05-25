package com.capdevon.anim.fsm;

/**
 * Motions are used by animation States in the animator StateMachines.
 * 
 * @author capdevon
 */
public class Motion {

    //The name of the object.
    protected String name;
    
    /**
     * Constructor.
     */
    public Motion() {
    	//default empty.
    }

    public String getName() {
        return name;
    }

}
