package com.capdevon.control;

import java.util.LinkedList;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class TimekeeperControl extends AbstractControl implements ActionListener {

	public static final String INPUT_TOGGLE_REWIND = "INPUT_TOGGLE_REWIND";

	private boolean isRewinding;
	private LinkedList<TrasformPoint> points;
	private RigidBodyControl rgb;

	public TimekeeperControl() {
		this.points = new LinkedList<>();
	}

	@Override
	public void setSpatial(Spatial sp) {
		super.setSpatial(sp);
		if (spatial != null) {
			this.rgb = spatial.getControl(RigidBodyControl.class);
		}
	}

	@Override
	public void onAction(String action, boolean isPressed, float tpf) {
		// TODO Auto-generated method stub
		if (action.equals(TimekeeperControl.INPUT_TOGGLE_REWIND)) {
			if (isPressed) {
				startRewind();
			} else {
				stopRewind();
			}
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
		// TODO Auto-generated method stub
		if (isRewinding) {
			rewind();
		} else {
			record(tpf);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
		// TODO Auto-generated method stub
	}

	private void rewind() {
		if (!points.isEmpty()) {
			TrasformPoint point = points.removeFirst();
			spatial.setLocalTranslation(point.position);
			spatial.setLocalRotation(point.rotation);

		} else {
//			stopRewind();
//			System.out.println(spatial + " -> stop");
		}
	}

	private void record(float tpf) {
		if (points.size() > Math.round(5f / tpf)) {
			points.removeLast();
		}

		points.addFirst(new TrasformPoint(spatial.getLocalTranslation(), spatial.getLocalRotation()));
	}

	public boolean isRewinding() {
		return isRewinding;
	}

	public void startRewind() {
		this.isRewinding = true;
		rgb.setKinematic(true);
	}

	public void stopRewind() {
		this.isRewinding = false;
		rgb.setKinematic(false);
	}

	private class TrasformPoint {

		Vector3f position;
		Quaternion rotation;

		public TrasformPoint(Vector3f position, Quaternion rotation) {
			this.position = new Vector3f(position);
			this.rotation = new Quaternion(rotation);
		}
	}

}