package com.capdevon.anim;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.capdevon.animation.Animation3;
import com.capdevon.control.AdapterControl;
import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.Armature;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
import com.jme3.anim.tween.action.BaseAction;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.custom.ArmatureDebugger;

/**
 *
 * @author capdevon
 */
public class Animator extends AdapterControl {

    private static final Logger logger = Logger.getLogger(Animator.class.getName());

    private AnimComposer animComposer;
    private SkinningControl skinningControl;
    private String currentAnim;
    private ArrayList<ActionAnimEventListener> listeners = new ArrayList<>();
    private ArmatureDebugger debugger;

    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);

        if (spatial != null) {
            animComposer = getComponentInChildren(AnimComposer.class);
            skinningControl = getComponentInChildren(SkinningControl.class);

            initActions();
        }
    }

    private void initActions() {
        StringBuilder sb = new StringBuilder();
        String r = String.format("Owner: %s, AnimRoot: %s", spatial, animComposer.getSpatial());
        sb.append(r);

        for (AnimClip clip : animComposer.getAnimClips()) {
            AnimTrack[] tracks = clip.getTracks();
            String s = String.format("%n * %s (%d), Length: %f", clip.getName(), tracks.length, clip.getLength());
            sb.append(s);
            setAnimCallback(clip.getName(), true);
        }

        logger.log(Level.INFO, sb.toString());
    }

    public void setAnimCallback(String animName, boolean loop) {
        Action action = animComposer.action(animName);
        Tween callback = Tweens.callMethod(this, "notifyAnimCycleDone", animName, loop);
        action = new BaseAction(Tweens.sequence(action, callback));
        animComposer.addAction(animName, action);
    }

    /**
     * @param anim (not null)
     */
    public void setAnimCallback(Animation3 anim) {
        String animName = anim.getName();
        boolean isLooping = (anim.getLoopMode() == LoopMode.Loop);
        setAnimCallback(animName, isLooping);

        /*
        // Get action registered with specified name. It will make a new action if there isn't any.
        Tween delegate = animComposer.action(animName);
        // Configure custom action with specified name, layer, loop, speed and listener.
        CustomAction action = new CustomAction(delegate, animComposer, animName, AnimComposer.DEFAULT_LAYER);
        action.setLooping(isLooping);
        action.setSpeed(speed);
        // Register custom action with specified name.
        animComposer.addAction(animName, action);
         */
    }

    /**
     * Run an action on the default layer.
     *
     * @param name The name of the action to run.
     */
    public void setAnimation(Animation3 anim) {
        setAnimation(anim.getName(), false);
    }

    /**
     * Run an action on the default layer.
     *
     * @param name The name of the action to run.
     */
    public void setAnimation(String animName, boolean override) {
        if (override || !animName.equals(currentAnim)) {
            animComposer.setCurrentAction(animName);
            notifyAnimChange(animName);
        }
    }

    public String getCurrentAnimation() {
        return currentAnim;
    }

    public Spatial getAnimRoot() {
        return animComposer.getSpatial();
    }

    public AnimComposer getAnimComposer() {
        return animComposer;
    }

    public SkinningControl getSkinningControl() {
        return skinningControl;
    }

    public void disableArmatureDebug() {
        debugger.removeFromParent();
        debugger = null;
    }
    
    public void enableArmatureDebug(AssetManager asm) {
        if (debugger == null) {
            Node animRoot = (Node) skinningControl.getSpatial();
            String name = animRoot.getName() + "_Armature";
            Armature armature = skinningControl.getArmature();
            debugger = new ArmatureDebugger(name, armature, armature.getJointList());
            debugger.setMaterial(createWireMaterial(asm));
            animRoot.attachChild(debugger);
        }
    }

    private Material createWireMaterial(AssetManager asm) {
        Material mat = new Material(asm, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setDepthTest(false);
        return mat;
    }

    /**
     * Adds a new listener to receive animation related events.
     */
    public void addListener(ActionAnimEventListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("The given listener is already "
                    + "registered at this Animator");
        }

        listeners.add(listener);
    }

    /**
     * Removes the given listener from listening to events.
     */
    public void removeListener(ActionAnimEventListener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("The given listener is not "
                    + "registered at this Animator");
        }
    }

    /**
     * Clears all the listeners added to this <code>Animator</code>
     */
    public void clearListeners() {
        listeners.clear();
    }

    void notifyAnimChange(String name) {
        currentAnim = name;
        for (ActionAnimEventListener listener : listeners) {
            listener.onAnimChange(animComposer, name);
        }
    }

    void notifyAnimCycleDone(String name, boolean loop) {
        for (ActionAnimEventListener listener : listeners) {
            listener.onAnimCycleDone(animComposer, name, loop);
        }
    }

}
