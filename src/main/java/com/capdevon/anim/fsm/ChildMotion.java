package com.capdevon.anim.fsm;

import com.jme3.math.Vector2f;

/**
 * Structure that represents a motion in the context of its parent blend tree.
 * 
 * @author capdevon
 */
public class ChildMotion {

    //The relative speed of the child.
    protected float timeScale = 1f;
    //The motion itself.
    protected String animName;
    //The position of the child. Used in 2D blend trees. (not yet supported)
    protected Vector2f position;
    //The threshold of the child. Used in 1D blend trees.
    protected float threshold;
    //Normalized time offset of the child.
    protected float cycleOffset;
    //The parameter used by the child when used in a BlendTree of type BlendTreeType.Direct. (not yet supported)
    protected String directBlendParameter = "Blend";


    public String getAnimName() {
        return animName;
    }

    public float getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    @Override
    public String toString() {
        return "ChildMotion [timeScale=" + timeScale + ", animName=" + animName + ", threshold=" + threshold + "]";
    }

}
