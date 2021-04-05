/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.demo;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.capdevon.animation.AnimUtils;
import com.capdevon.animation.Animation3;
import com.capdevon.animation.MixamoBodyBones;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author capdevon
 */
public class ZombieMocap extends AbstractControl implements AnimEventListener {
    
    private static final Logger logger = Logger.getLogger(ZombieMocap.class.getName());

    public enum AnimMask {
        Upper, Lower, All
    }

    private AnimControl animControl;
    private AnimChannel upperChannel, lowerChannel;
    private String pfxMixamo = "Armature_mixamorig:";
    private boolean renameArmature;
    
    /**
     * 
     * @param pfxMixamo 
     */
    public ZombieMocap(String pfxMixamo) {
        this.pfxMixamo = pfxMixamo;
    }

    /**
     * 
     * @param renameArmature 
     */
    public ZombieMocap(boolean renameArmature) {
        this.renameArmature = renameArmature;
    }
    
    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        
        if (spatial != null) {
            // find AnimControl
            animControl = AnimUtils.getAnimControl(sp);
            animControl.addListener(this);
            System.out.println(spatial.getName() + " --Animations: " + animControl.getAnimationNames());
            
            if (renameArmature) {
                pfxMixamo = "";
                AnimUtils.renameMixamoArmature(animControl.getSpatial());
            }

            // create AnimChannels
            upperChannel = animControl.createChannel();
            upperChannel.addFromRootBone(pfxMixamo + MixamoBodyBones.Spine);

            lowerChannel = animControl.createChannel();
            lowerChannel.addFromRootBone(pfxMixamo + MixamoBodyBones.RightUpLeg);
            lowerChannel.addFromRootBone(pfxMixamo + MixamoBodyBones.LeftUpLeg);
            lowerChannel.addBone(pfxMixamo + MixamoBodyBones.Hips);
        }
    }

    public void setAnimation(Animation3 newAnim, AnimMask channel) {
        switch (channel) {
            case Upper:
                setAnimation(newAnim, upperChannel);
                break;
            case Lower:
                setAnimation(newAnim, lowerChannel);
                break;
            default:
                setAnimation(newAnim, upperChannel);
                setAnimation(newAnim, lowerChannel);
        }
    }

    private void setAnimation(Animation3 anim, AnimChannel channel) {
        if (hasAnimation(anim.getName())) {
            if (!anim.getName().equals(channel.getAnimationName())) {
                channel.setAnim(anim.getName(), anim.getBlendTime());
                channel.setLoopMode(anim.getLoopMode());
                channel.setSpeed(anim.getSpeed());
            }
        }
    }
    
    private boolean hasAnimation(String animName) {
        boolean result = animControl.getAnimationNames().contains(animName);
        if (!result) {
            logger.log(Level.WARNING, "Cannot find animation named: {0}", animName);
        }
        return result;
    }
    
    /**
     * Synchronize animation channels.
     *
     * @param from  - the source animation channel.
     * @param to    - the target animation channel.
     */
    private void synchAnimation(AnimChannel from, AnimChannel to) {
        try {
            to.setAnim(from.getAnimationName());
            to.setLoopMode(from.getLoopMode());
            to.setSpeed(from.getSpeed());
            to.setTime(from.getTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        // Use this listener to trigger something after an animation is done.
        if (channel.getLoopMode() == LoopMode.DontLoop && channel == upperChannel) {
            synchAnimation(lowerChannel, upperChannel);
        }
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // Use this listener to trigger something between two animations.
        System.out.println("onAnimChange[channel=" + channel.hashCode() + ", animName=" + animName + "]");
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //To change body of generated methods, choose Tools | Templates.
    }

}
