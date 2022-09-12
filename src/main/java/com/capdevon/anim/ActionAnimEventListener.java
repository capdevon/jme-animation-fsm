package com.capdevon.anim;

/**
 *
 * @author capdevon
 */
public interface ActionAnimEventListener {

    /**
     * Invoked when an animation "cycle" is done. For non-looping animations,
     * this event is invoked when the animation is finished playing. For looping
     * animations, this event is invoked each time the animation is restarted.
     */
    public void onAnimCycleDone(Animator animator, String animName, boolean loop);

    /**
     * Invoked when an animation is set to play 
     * by the user on the given AnimComposer.
     */
    public void onAnimChange(Animator animator, String animName);
}
