package com.capdevon.anim;

import java.util.BitSet;

import com.jme3.anim.AnimationMask;
import com.jme3.anim.Armature;
import com.jme3.anim.Joint;

/**
 * 
 * @author capdevon
 */
public class AnimMaskBuilder implements AnimationMask {
	
	private final BitSet affectedJoints;
	private final Armature armature;
	
	/**
     * Instantiate a builder that affects no joints.
     */
	public AnimMaskBuilder(Armature armature) {
		this.armature = armature;
		this.affectedJoints = new BitSet(armature.getJointCount());
	}
	
	/**
     * Add joints to be influenced by this animation mask.
     */
	public AnimMaskBuilder addJoints(String... jointNames) {
		for (String jointName : jointNames) {
            Joint joint = findJoint(jointName);
            affectedJoints.set(joint.getId());
        }
		return this;
	}
	
    private Joint findJoint(String jointName) {
        Joint joint = armature.getJoint(jointName);
        if (joint == null) {
            throw new IllegalArgumentException("Cannot find joint " + jointName);
        }
        return joint;
    }
    
    /**
     * Add a joint and all its sub armature joints to be influenced by this animation mask.
     */
    public AnimMaskBuilder addFromJoint(String jointName) {
        Joint joint = findJoint(jointName);
        recurseAddJoint(joint);
        return this;
    }

    private void recurseAddJoint(Joint joint) {
        affectedJoints.set(joint.getId());
        for (Joint j : joint.getChildren()) {
            recurseAddJoint(j);
        }
    }
    
    /**
     * Add the specified Joint and all its ancestors.
     *
     * @param start the starting point (may be null, unaffected)
     * @return this
     */
	public AnimMaskBuilder addAncestors(String jointName) {
		Joint joint = findJoint(jointName);
		addAncestors(joint);
		return this;
	}

	private void addAncestors(Joint start) {
		for (Joint joint = start; joint != null; joint = joint.getParent()) {
			int jointId = joint.getId();
			affectedJoints.set(jointId);
		}
	}
    
    /**
     * Add all the bones of the model's armature to be
     * influenced by this animation mask.
     */
	public AnimMaskBuilder addAllBones() {
		int numJoints = armature.getJointCount();
		affectedJoints.set(0, numJoints);
		return this;
	}
    
	/**
	 * Build ArmatureMask
	 * 
	 * @return
	 */
//	public ArmatureMask buildMask() {
//		ArmatureMask mask = new ArmatureMask();
//		for (int i = 0; i < affectedJoints.length(); i++) {
//
//			if (affectedJoints.get(i) == true) {
//				String jointName = armature.getJoint(i).getName();
//				mask.addBones(armature, jointName);
//				System.out.println("ArmatureMask Joint: " + jointName);
//			}
//		}
//		return mask;
//	}
	
    @Override
    public boolean contains(Object target) {
        return affectedJoints.get(((Joint) target).getId());
    }

}
