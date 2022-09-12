package com.capdevon.anim;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.capdevon.control.AdapterControl;
import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.Armature;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
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
    private ArrayList<ActionAnimEventListener> listeners = new ArrayList<>();
    private ArmatureDebugger debugger;
    private String currentAnim;

    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);

        if (spatial != null) {
            animComposer = getComponentInChildren(AnimComposer.class);
            skinningControl = getComponentInChildren(SkinningControl.class);

            printInfo();
        }
    }

    private void printInfo() {
        System.out.printf("Owner: %s, AnimRoot: %s", spatial, getAnimRoot());
        for (AnimClip clip : animComposer.getAnimClips()) {
            System.out.printf("%n * Clip=%s Tracks=%d, Length=%.2f sec",
                    clip.getName(), clip.getTracks().length, clip.getLength());
        }
    }

    public void createDefaultActions() {
        for (AnimClip clip : animComposer.getAnimClips()) {
            actionCallback(clip.getName(), true);
        }
    }

    /**
     * @param def (not null)
     */
    public void actionCallback(Animation3 def) {
        Action action = actionCallback(def.getName(), def.isLooping());
        action.setSpeed(def.speed);
    }

    public Action actionCallback(String animName, boolean loop) {
        // Get action registered with specified name. It will make a new action if there isn't any.
        Action action = animComposer.action(animName);
        Tween callback = Tweens.callMethod(this, "notifyAnimCycleDone", animName, loop);
        // Register custom action with specified name.
        return animComposer.actionSequence(animName, action, callback);
    }

    /**
     * @param def (not null)
     */
    public void actionNoLoop(Animation3 def) {
        Action action = actionNoLoop(def.name, def.layer);
        action.setSpeed(def.speed);
    }

    public Action actionNoLoop(String animName, String layerName) {
        // Get action registered with specified name. It will make a new action if there isn't any.
        Action action = animComposer.action(animName);
        Tween remove = Tweens.callMethod(animComposer, "removeCurrentAction", layerName);
        // Register custom action with specified name.
        return animComposer.actionSequence(animName, action, remove);
    }

    public void playAnimation(Animation3 def) {
        playAnimation(def.name, def.layer);
    }

    public void playAnimation(String animName, String layerName) {
        if (!animName.equals(currentAnim)) {
            animComposer.setCurrentAction(animName, layerName);
            currentAnim = animName;
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
        for (ActionAnimEventListener listener : listeners) {
            listener.onAnimChange(this, name);
        }
    }

    void notifyAnimCycleDone(String name, boolean loop) {
        for (ActionAnimEventListener listener : listeners) {
            listener.onAnimCycleDone(this, name, loop);
        }
    }

}
