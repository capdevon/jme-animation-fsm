package com.capdevon.anim;

import java.util.HashMap;
import java.util.Map;

import com.capdevon.animation.Animation3;
import com.capdevon.control.AdapterControl;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.Tween;
import com.jme3.animation.LoopMode;
import com.jme3.scene.Spatial;

/**
 * 
 * @author capdevon
 */
public class Animator extends AdapterControl {

    public AnimComposer animComposer;
    public SkinningControl skinningControl;

    private final Map<String, CustomAction> animationMap = new HashMap<>();
    private String currentAnim;

    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        if (spatial != null) {
            animComposer = getComponentInChild(AnimComposer.class);
            skinningControl = getComponentInChild(SkinningControl.class);

            System.out.println("--Animations: " + animComposer.getAnimClipsNames());
            System.out.println("--List Bones: " + AnimUtils.listBones(skinningControl.getArmature()));
        }
    }

    /**
     * @param anim
     */
    public void addActions(Animation3... animations) {
    	for (Animation3 anim : animations) {
    		addAction(anim, null);
    	}
    }

    /**
     * @param anim
     * @param animListener
     */
    public void addAction(Animation3 anim, ActionAnimEventListener animListener) {
        String animName = anim.getName();
        float speed = anim.getSpeed();
        boolean isLooping = (anim.getLoopMode() == LoopMode.Loop);

        // Get action registered with specified name. It will make a new action if there isn't any.
        Tween delegate = animComposer.action(animName);
        // Configure custom action with specified name, layer, loop, speed and listener.
        CustomAction action = new CustomAction(delegate, animComposer, animName);
        action.setLooping(isLooping);
        action.setSpeed(speed);
        action.setAnimEventListener(animListener);
        // Register custom action with specified name.
        animComposer.addAction(animName, action);

        // Add custom action to map
        animationMap.put(animName, action);
    }

    /**
     * Run animation
     *
     * @param anim
     */
    public void setAnimation(Animation3 anim) {
        setAnimation(anim.getName(), false);
    }
    
    public void setAnimation(String animName) {
        setAnimation(animName, false);
    }

    /**
     * 
     * @param animName
     * @param overwrite 
     */
    public void setAnimation(String animName, boolean overwrite) {

        if (overwrite || !animName.equals(currentAnim)) {
            CustomAction action = animationMap.get(animName);
            if (action != null) {
                // play animation mapped on custom action.
                action.playAnimation();
            } else {
                // play animation in a traditional way.
                animComposer.setCurrentAction(animName);
            }
            currentAnim = animName;
        }
    }

    public Spatial getAnimRoot() {
        return animComposer.getSpatial();
    }
}
