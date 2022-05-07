package com.capdevon.anim;

import java.util.BitSet;

import com.jme3.anim.AnimationMask;
import com.jme3.anim.Joint;

/**
 *
 * @author capdevon
 */
public class AvatarMask implements AnimationMask {

    private final BitSet affectedJoints = new BitSet();

    /**
     * Instantiate a mask that affects no joints.
     */
    public AvatarMask() {
        // do nothing
    }

    public void addJoint(int jointId) {
        affectedJoints.set(jointId);
    }

    public void removeJoint(int jointId) {
        affectedJoints.clear(jointId);
    }

    @Override
    public boolean contains(Object target) {
        Joint joint = (Joint) target;
        return affectedJoints.get(joint.getId());
    }

}
