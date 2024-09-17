package com.capdevon.anim;

import java.io.IOException;
import java.util.BitSet;

import com.jme3.anim.AnimationMask;
import com.jme3.anim.Joint;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;

/**
 *
 * @author capdevon
 */
public class AvatarMask implements AnimationMask, Savable {

    private BitSet affectedJoints = new BitSet();

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

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(affectedJoints, "affectedJoints", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);
        affectedJoints = ic.readBitSet("affectedJoints", null);
    }

}
