package com.capdevon.animation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jme3.anim.Armature;
import com.jme3.anim.Joint;
import com.jme3.anim.SkinningControl;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.SkeletonDebugger;

/**
 *
 * @author capdevon
 */
public class AnimUtils {

    private AnimUtils() {}

    /**
     * Running Mixamo Armature Renaming Script.
     */
    public static void renameMixamoArmature(Spatial sp) {
        Skeleton skeleton = getSkeletonControl(sp).getSkeleton();
        for (int i = 0; i < skeleton.getBoneCount(); ++i) {
            Bone bone = skeleton.getBone(i);

            String replacement = StringUtils.substringAfterLast(bone.getName(), ":");
            if (!replacement.isBlank()) {
                renameBone(bone, replacement);
            }
        }
    }

    public static void renameBone(Bone bone, String newName) {
        try {
            System.out.println("Renaming Bone= " + bone.getName() + " to= " + newName);

            Field fieldName = Bone.class.getDeclaredField("name");
            fieldName.setAccessible(true);
            fieldName.set(bone, newName);

            Field fieldAttachNode = Bone.class.getDeclaredField("attachNode");
            fieldAttachNode.setAccessible(true);

            Node node = (Node) fieldAttachNode.get(bone);
            if (node != null) {
                node.setName(newName + "_attachNode");
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
        AnimControl acFrom = getAnimControl(from);
        AnimControl acTo = getAnimControl(to);

        for (String animName : acFrom.getAnimationNames()) {
            if (!acTo.getAnimationNames().contains(animName)) {
                System.out.println("Copying Animation: " + animName);
                Animation anim = acFrom.getAnim(animName);
                acTo.addAnim(anim);
            }
        }
    }

    public static void addSkeletonDebugger(AssetManager asm, SkeletonControl skControl) {
        Node animRoot = (Node) skControl.getSpatial();
        SkeletonDebugger skDebugger = new SkeletonDebugger(animRoot.getName() + "_Skeleton", skControl.getSkeleton());
        Material mat = new Material(asm, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        mat.getAdditionalRenderState().setDepthTest(false);
        skDebugger.setMaterial(mat);
        animRoot.attachChild(skDebugger);
    }
    
    public static AnimControl getAnimControl(Spatial sp) {
        AnimControl control = findControl(sp, AnimControl.class);
        if (control == null) {
            throw new IllegalArgumentException("AnimControl not found: " + sp);
        }
        return control;
    }

    public static SkeletonControl getSkeletonControl(Spatial sp) {
        SkeletonControl control = findControl(sp, SkeletonControl.class);
        if (control == null) {
            throw new IllegalArgumentException("SkeletonControl not found: " + sp);
        }
        return control;
    }

    public static List<String> listBones(Spatial sp) {
        SkeletonControl skControl = getSkeletonControl(sp);
        return listBones(skControl);
    }

    public static List<String> listBones(SkeletonControl skControl) {
        Skeleton skeleton = skControl.getSkeleton();
        int boneCount = skeleton.getBoneCount();
        List<String> lst = new ArrayList<>(boneCount);
        for (int i = 0; i < boneCount; ++i) {
            lst.add(skeleton.getBone(i).getName());
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
