package com.capdevon.anim.fsm;

import com.jme3.anim.AnimationMask;

/**
 * The Animation Layer contains a state machine that controls animations of a model or part of it.
 * 
 * @author capdevon
 */
public class AnimatorControllerLayer {

    //The name of the layer.
    protected String name;
    //The state machine for the layer.
    protected AnimatorStateMachine stateMachine;
    //The AvatarMask that is used to mask the animation on the given layer.
    protected AnimationMask avatarMask;
    //Specifies the index of the Synced Layer. (not yet supported)
    protected int syncedLayerIndex = -1;

    /**
     * Constructor.
     */
    public AnimatorControllerLayer() {
    }

    public String getName() {
        return name;
    }

    public AnimatorStateMachine getStateMachine() {
        return stateMachine;
    }

    public AnimationMask getAvatarMask() {
        return avatarMask;
    }

}