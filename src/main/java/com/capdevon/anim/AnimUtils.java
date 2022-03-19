package com.capdevon.anim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import java.lang.reflect.Field;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author capdevon
 */
public class AnimUtils {

    private AnimUtils() {
    }

    /**
     * Running Mixamo Armature Renaming Script.
     */
    public static void renameMixamoArmature(Spatial sp) {
        Armature skeleton = getSkinningControl(sp).getArmature();
        for (int i = 0; i < skeleton.getJointCount(); ++i) {
            Joint joint = skeleton.getJoint(i);

            String replacement = StringUtils.substringAfterLast(joint.getName(), ":");
            if (StringUtils.isNotBlank(replacement)) {
                renameJoint(joint, replacement);
            }
        }
    }

    public static void renameJoint(Joint joint, String newName) {
        try {
            System.out.println("Renaming Joint= " + joint.getName() + " to= " + newName);

            Field fieldName = Joint.class.getDeclaredField("name");
            fieldName.setAccessible(true);
            fieldName.set(joint, newName);

            Field fieldAttachNode = Joint.class.getDeclaredField("attachedNode");
            fieldAttachNode.setAccessible(true);

            Node node = (Node) fieldAttachNode.get(joint);
            if (node != null) {
                node.setName(newName + "_attachedNode");
            }

        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

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

    @SuppressWarnings("rawtypes")
    public static AnimClip retargetClip(String name, AnimClip sourceClip, Spatial target) {
        Spatial animRoot = findAnimRoot(target);
        if (animRoot == null) {
            System.err.println("Anim root is null!");
            return null;
        }

        SkinningControl sc = animRoot.getControl(SkinningControl.class);
        AnimClip copy = new AnimClip(name);
        AnimTrack[] tracks = copyAnimTracks(sourceClip, sc.getArmature());
        copy.setTracks(tracks);
        return copy;
    }

    private static Spatial findAnimRoot(Spatial s) {
        if (s.getControl(AnimComposer.class) != null) {
            return s;
        }
        if (s instanceof Node) {
            for (Spatial child : ((Node) s).getChildren()) {
                Spatial result = findAnimRoot(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static AnimComposer getAnimComposer(Spatial sp) {
        AnimComposer control = findControl(sp, AnimComposer.class);
        return Objects.requireNonNull(control, "AnimComposer not found: " + sp);
    }

    public static SkinningControl getSkinningControl(Spatial sp) {
        SkinningControl control = findControl(sp, SkinningControl.class);
        return Objects.requireNonNull(control, "SkinningControl not found: " + sp);
    }

    public static List<String> listJoints(Spatial sp) {
        SkinningControl sc = getSkinningControl(sp);
        return listJoints(sc.getArmature());
    }

    public static List<String> listJoints(Armature skeleton) {
        int boneCount = skeleton.getJointCount();
        List<String> lst = new ArrayList<>(boneCount);
        for (Joint bone : skeleton.getJointList()) {
            lst.add(bone.getName());
        }
        Collections.sort(lst);
        return lst;
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
