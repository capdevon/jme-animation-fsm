/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.anim.fsm;

import com.jme3.math.Vector2f;

/**
 * Structure that represents a motion in the context of its parent blend tree.
 * 
 * @author capdevon
 */
public class ChildMotion {

	//The relative speed of the child.
    public float timeScale = 1f;
    //The motion itself.
    public String animName;
    //The position of the child. Used in 2D blend trees.
    public Vector2f position;
    //The threshold of the child. Used in 1D blend trees.
    public float threshold;
    //Normalized time offset of the child.
    public float cycleOffset;
    //The parameter used by the child when used in a BlendTree of type BlendTreeType.Direct.
    public String directBlendParameter = "Blend";
    
	@Override
	public String toString() {
		return "ChildMotion [timeScale=" + timeScale + ", animName=" + animName + ", threshold=" + threshold + "]";
	}
    
}
