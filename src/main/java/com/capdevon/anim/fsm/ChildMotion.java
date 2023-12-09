package com.capdevon.anim.fsm;

import com.jme3.math.Vector2f;

/**
 * Structure that represents a motion in the context of its parent blend tree.
 *
 * @author capdevon
 */
public class ChildMotion {

    // The motion itself.
    protected String animName;
    // The position of the child. Used in 2D blend trees.
    protected Vector2f position;
    // The threshold of the child. Used in 1D blend trees.
    protected float threshold;
    // The relative speed of the child.
    protected float timeScale = 1f;
    // Normalized time offset of the child.
    protected float cycleOffset = 0;

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

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public float getCycleOffset() {
        return cycleOffset;
    }

    public void setCycleOffset(float cycleOffset) {
        this.cycleOffset = cycleOffset;
    }

}
