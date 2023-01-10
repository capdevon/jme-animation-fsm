package com.capdevon.anim;

import java.util.Objects;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.TransformTrack;
import com.jme3.anim.util.HasLocalTransform;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.SafeArrayList;

/**
 *
 * @author capdevon
 */
public class AnimUtils {

    private AnimUtils() {}

    /**
     * @param from
     * @param to
     */
    public static void copyAnimation(Spatial from, Spatial to) {

        AnimComposer source = getAnimComposer(from);
        AnimComposer target = getAnimComposer(to);
        Armature targetArmature = getSkinningControl(to).getArmature();

        copyAnimation(source, target, targetArmature);
    }

    /**
     *
     * @param source
     * @param target
     * @param targetArmature
     */
    public static void copyAnimation(AnimComposer source, AnimComposer target, Armature targetArmature) {
        for (String animName : source.getAnimClipsNames()) {
            if (!target.getAnimClipsNames().contains(animName)) {
                System.out.println("Copying Animation: " + animName);

                AnimClip clip = new AnimClip(animName);
                clip.setTracks(copyAnimTracks(source.getAnimClip(animName), targetArmature));
                target.addAnimClip(clip);
            }
        }
    }

    /**
     *
     * @param sourceClip
     * @param targetArmature
     * @return
     */
    private static AnimTrack[] copyAnimTracks(AnimClip sourceClip, Armature targetArmature) {

        SafeArrayList<AnimTrack> tracks = new SafeArrayList<>(AnimTrack.class);

        for (AnimTrack track : sourceClip.getTracks()) {

            TransformTrack tt = (TransformTrack) track;
            HasLocalTransform target = null;

            if (tt.getTarget() instanceof Node) {
                Node node = (Node) tt.getTarget();
                target = targetArmature.getJoint(node.getName());

            } else if (tt.getTarget() instanceof Joint) {
                Joint joint = (Joint) tt.getTarget();
                target = targetArmature.getJoint(joint.getName());
            }

            if (target != null) {
                //TransformTrack newTrack = new TransformTrack(target, tt.getTimes(), tt.getTranslations(), tt.getRotations(), tt.getScales());
                TransformTrack newTrack = tt.jmeClone(); //optimization
                newTrack.setTarget(target);
                tracks.add(newTrack);

            } else {
                throw new IllegalStateException("Joint not found in the target Armature: " + tt.getTarget());
            }
        }

        System.out.println("Copied tracks " + tracks.size() + " of " + sourceClip.getTracks().length);
        return tracks.getArray();
    }

    public static AnimComposer getAnimComposer(Spatial sp) {
        AnimComposer control = findControl(sp, AnimComposer.class);
        return Objects.requireNonNull(control, "AnimComposer not found: " + sp);
    }

    public static SkinningControl getSkinningControl(Spatial sp) {
        SkinningControl control = findControl(sp, SkinningControl.class);
        return Objects.requireNonNull(control, "SkinningControl not found: " + sp);
    }

    /**
     * @param <T>
     * @param sp
     * @param clazz
     * @return
     */
    private static <T extends Control> T findControl(Spatial sp, Class<T> clazz) {
        T control = sp.getControl(clazz);
        if (control != null) {
            return control;
        }
        if (sp instanceof Node) {
            for (Spatial child : ((Node) sp).getChildren()) {
                control = findControl(child, clazz);
                if (control != null) {
                    return control;
                }
            }
        }
        return null;
    }

}
