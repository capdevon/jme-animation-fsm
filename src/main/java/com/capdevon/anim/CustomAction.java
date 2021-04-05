package com.capdevon.anim;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.action.BaseAction;

public class CustomAction extends BaseAction {

	private ActionAnimEventListener animListener;
	private AnimComposer animComposer;
	private String animName;
	private String layer;
	private boolean loop;

	/**
	 * @param delegate
	 * @param animComposer
	 * @param animName
	 * @param layer
	 */
	public CustomAction(Tween delegate, AnimComposer animComposer, String animName, String layer) {
		super(delegate);
		this.animComposer = animComposer;
		this.animName = animName;
		this.layer = layer;
	}
	
	public CustomAction(Tween delegate, AnimComposer animComposer, String animName) {
		this(delegate, animComposer, animName, AnimComposer.DEFAULT_LAYER);
	}

	public boolean isLooping() {
		return loop;
	}

	public void setLooping(boolean loop) {
		this.loop = loop;
	}

	public ActionAnimEventListener getAnimEventListener() {
		return animListener;
	}

	public void setAnimEventListener(ActionAnimEventListener animListener) {
		this.animListener = animListener;
	}

	public void playAnimation() {
		animComposer.setCurrentAction(animName);
		notifyAnimChange(animName);
	}

	@Override
	public boolean interpolate(double t) {
		boolean running = super.interpolate(t);
		if (!loop && !running) {
			// animation done running...
			// now we can remove this action from the layer it is attached to
			animComposer.removeCurrentAction(layer);
			notifyAnimCycleDone(animName);
//				System.out.println(animComposer.getTime(layer) + " " + this.getLength());
		}
		return running;
	}

	void notifyAnimChange(String name) {
		if (animListener != null)
			animListener.onAnimChange(this, animComposer, name);
	}

	void notifyAnimCycleDone(String name) {
		if (animListener != null)
			animListener.onAnimCycleDone(this, animComposer, name);
	}

	@Override
	public String toString() {
		return "CustomAction [animName=" + animName + ", layer=" + layer + ", loop=" + loop + "]";
	}

}