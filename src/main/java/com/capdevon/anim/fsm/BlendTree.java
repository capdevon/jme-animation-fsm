package com.capdevon.anim.fsm;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.Vector2f;

/**
 * Blend trees are used to blend continuously animation between their childs.
 * They can either be 1D or 2D.
 * 
 * @author capdevon
 */
public class BlendTree extends Motion {

    public enum BlendTreeType {
        Simple1D,
        SimpleDirectional2D,
//        FreeformDirectional2D,
//        FreeformCartesian2D,
//        Direct
    }

    // Parameter that is used to compute the blending weight of the childs in 1D
    // blend trees or on the X axis of a 2D blend tree.
    protected String blendParameter;
    // Parameter that is used to compute the blending weight of the childs on the Y
    // axis of a 2D blend tree.
    protected String blendParameterY;
    // The Blending type can be either 1D or different types of 2D.
    protected BlendTreeType blendType = BlendTreeType.Simple1D;
    // Sets the maximum threshold that will be used by the ChildMotion.
    protected float maxThreshold = 1f;
    // Sets the minimum threshold that will be used by the ChildMotion.
    protected float minThreshold = 0f;
    // The list of the blend tree child motions.
    protected List<ChildMotion> motions = new ArrayList<>();
    
    /**
     * Constructor.
     */
    public BlendTree() {
    	//default empty.
    }

    /**
     * Create blend tree with the minimum and maximum threshold for the
     * LinearBlendSpace.
     * 
     * @param minThreshold
     * @param maxThreshold
     */
    public BlendTree(int minThreshold, int maxThreshold) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }
    
    /**
     * Utility function to remove the child of a blend tree.
     * 
     * @param index - The index of the blend tree to remove.
     */
    public void removeChild(int index) {
        motions.remove(index);
    }

    /**
     * Utility function to add a child motion to a blend trees.
     * 
     * @param animName - The motion to add as child.
     * @param position - The position of the child. When using 2D blend trees.
     * @return 
     */
    public ChildMotion addChild(String animName, Vector2f position) {
        return addChild(animName, position, 0f);
    }

    /**
     * Utility function to add a child motion to a blend trees.
     * 
     * @param animName  - The motion to add as child.
     * @param threshold - The threshold of the child. When using 1D blend trees.
     * @return 
     */
    public ChildMotion addChild(String animName, float threshold) {
        return addChild(animName, Vector2f.ZERO.clone(), threshold);
    }

    private ChildMotion addChild(String animName, Vector2f position, float threshold) {
        ChildMotion motion = new ChildMotion();
        motion.animName = animName;
        motion.position = position;
        motion.threshold = threshold;
        motions.add(motion);
        return motion;
    }

    public String[] getAnimMotionsNames() {
        String[] clips = new String[motions.size()];
        for (int i = 0; i < clips.length; i++) {
            String animName = motions.get(i).animName;
            clips[i] = animName;
        }
        return clips;
    }

    public BlendTreeType getBlendType() {
        return blendType;
    }

    public void setBlendType(BlendTreeType blendType) {
        this.blendType = blendType;
    }

    public String getBlendParameter() {
        return blendParameter;
    }

    public void setBlendParameter(String blendParameter) {
        this.blendParameter = blendParameter;
    }

    public String getBlendParameterY() {
        return blendParameterY;
    }

    public void setBlendParameterY(String blendParameterY) {
        this.blendParameterY = blendParameterY;
    }

    public float getMaxThreshold() {
        return maxThreshold;
    }

    public float getMinThreshold() {
        return minThreshold;
    }
    
    /**
     * InternalCall.
     * @param blendPos
     * @return
     */
    protected ChildMotion getBlendMotion(Vector2f blendPos) {
        ChildMotion closestChild = null;
        float minDistance = Float.MAX_VALUE;

        for (ChildMotion childMotion : motions) {
            float d = childMotion.position.distanceSquared(blendPos);
            if (d < minDistance) {
                minDistance = d;
                closestChild = childMotion;
            }
        }

        return closestChild;
    }

    /**
     * InternalCall.
     * @param blendPos
     * @return
     */
    protected ChildMotion getBlendMotion(float blendPos) {

        int point_lower = -1;
        float pos_lower = 0.0f;
        int point_higher = -1;
        float pos_higher = 0.0f;
        float[] weights = new float[motions.size()];

        for (int i = 0; i < motions.size(); i++) {
            float pos = motions.get(i).threshold;

            if (pos <= blendPos) {
                if (point_lower == -1) {
                    point_lower = i;
                    pos_lower = pos;
                } else if ((blendPos - pos) < (blendPos - pos_lower)) {
                    point_lower = i;
                    pos_lower = pos;
                }
            } else {
                if (point_higher == -1) {
                    point_higher = i;
                    pos_higher = pos;
                } else if ((pos - blendPos) < (pos_higher - blendPos)) {
                    point_higher = i;
                    pos_higher = pos;
                }
            }
        }

        if (point_lower == -1 && point_higher != -1) {
            // we are on the left side, no other point to the left
            // we just play the next point.
            weights[point_higher] = 1.0f;

        } else if (point_higher == -1) {
            // we are on the right side, no other point to the right
            // we just play the previous point
            weights[point_lower] = 1.0f;

        } else {
            // we are between two points.
            // figure out weights, then blend the animations
            float distance_between_points = pos_higher - pos_lower;
            float current_pos_inbetween = blendPos - pos_lower;
            float blend_percentage = current_pos_inbetween / distance_between_points;

            float blend_lower = 1.0f - blend_percentage;
            float blend_higher = blend_percentage;

            weights[point_lower] = blend_lower;
            weights[point_higher] = blend_higher;
        }

        return motions.get(getIndexOfMax(weights));
    }

    private int getIndexOfMax(float[] array) {
        if (array == null || array.length == 0)
            return -1; // null or empty

        int max = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[max])
                max = i;
        }

        // position of the first largest found
        return max;
    }
    
}
