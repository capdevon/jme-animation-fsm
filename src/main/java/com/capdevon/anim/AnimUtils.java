package com.capdevon.anim;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(AnimUtils.class.getName());

    private AnimUtils() {
    }

    /**
     * Copies all animations from the source spatial to the target spatial, adapting
     * them to the target's armature.
     *
     * This method retrieves the {@link AnimComposer} from both the source and
     * target spatial using the {@link #getAnimComposer(Spatial)} method.
     *
     * @param from The source spatial containing the animations to copy.
     * @param to   The target spatial where the animations will be added.
     */
    public static void copyAnimation(Spatial from, Spatial to) {

        AnimComposer source = getAnimComposer(from);
        AnimComposer target = getAnimComposer(to);
        Armature targetArmature = getSkinningControl(to).getArmature();

        copyAnimation(source, target, targetArmature);
    }

    /**
     * Copies animations from a source {@link AnimComposer} to a target
     * {@link AnimComposer}, applying the animations to the specified target
     * armature.
     * 
     * @param source         The source AnimComposer containing the animations to copy.
     * @param target         The target AnimComposer where the animations will be added.
     * @param targetArmature The armature in the target composer to which the animations should be applied.
     */
    public static void copyAnimation(AnimComposer source, AnimComposer target, Armature targetArmature) {
        for (AnimClip sourceClip : source.getAnimClips()) {
            String clipName = sourceClip.getName();
            if (!target.getAnimClipsNames().contains(clipName)) {
                logger.log(Level.INFO, "Copying Animation: {0}", clipName);

                AnimClip copy = new AnimClip(clipName);
                copy.setTracks(copyAnimTracks(sourceClip, targetArmature));
                target.addAnimClip(copy);
            }
        }
    }

    /**
     * Copies animation tracks from a source animation clip, adapting them to the
     * target armature.
     *
     * @param sourceClip     The source animation clip containing the tracks to copy.
     * @param targetArmature The target armature to which the tracks should be adapted.
     * @return An array of copied {@link TransformTrack} objects targeting joints in
     *         the target armature, or an empty array if no tracks were copied.
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
                logger.log(Level.WARNING, "Joint not found in the target Armature: {0}", tt.getTarget());
            }
        }

        logger.log(Level.INFO, "Copied tracks {0} of {1}", new Object[]{tracks.size(), sourceClip.getTracks().length});
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

    public static Spatial getAnimRoot(Spatial sp) {
        return getAnimComposer(sp).getSpatial();
    }

    /**
     * Finds a control of the specified type within a given spatial and its children
     * recursively.
     *
     * @param <T>   The type of the control to find.
     * @param sp    The spatial to search for the control.
     * @param clazz The class object representing the type of control to find.
     * @return The first encountered control of the specified type within the
     *         spatial and its children, or null if no such control is found.
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
