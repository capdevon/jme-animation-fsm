package com.capdevon.anim;

import com.jme3.anim.AnimComposer;

/**
 * 
 * @author capdevon
 */
public interface ActionAnimEventListener {

    public void onAnimCycleDone(CustomAction action, AnimComposer animComposer, String animName);

    public void onAnimChange(CustomAction action, AnimComposer animComposer, String animName);
}
