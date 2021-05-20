package com.capdevon.anim.fsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jme3.anim.tween.action.Action;
import com.jme3.anim.tween.action.BlendableAction;
import com.jme3.anim.util.HasLocalTransform;
import com.jme3.math.Transform;

public class MyBlendAction extends BlendableAction {

    private int firstActiveIndex;
    private int secondActiveIndex;
    private final MyBlendSpace blendSpace;
    private final BlendMode blendMode;
    private float blendWeight;
    private double lastTime;
    // In stretch mode it contains time factor and in loop mode it contains the time
    final private double[] timeData;
    final private Map<HasLocalTransform, Transform> targetMap = new HashMap<>();

    public enum BlendMode {Stretch, Loop}

    public MyBlendAction(MyBlendSpace blendSpace, BlendableAction... actions) {
        this(blendSpace, BlendMode.Loop, actions);
    }

    public MyBlendAction(MyBlendSpace blendSpace, BlendMode blendMode, BlendableAction... actions) {
        super(actions);
        this.blendMode = blendMode;
        timeData = new double[actions.length];
        this.blendSpace = blendSpace;
        blendSpace.setBlendAction(this);

        for (BlendableAction action : actions) {
            if (action.getLength() > getLength()) {
                setLength(action.getLength());
            }
            Collection<HasLocalTransform> targets = action.getTargets();
            for (HasLocalTransform target : targets) {
                Transform t = targetMap.get(target);
                if (t == null) {
                    t = new Transform();
                    targetMap.put(target, t);
                }
            }
        }

        if (blendMode == BlendMode.Stretch) {
            //Blending effect maybe unexpected when blended animation don't have the same length
            //Stretching any action that doesn't have the same length.
            for (int i = 0; i < this.actions.length; i++) {
                this.timeData[i] = 1;
                if (this.actions[i].getLength() != getLength()) {
                    double actionLength = this.actions[i].getLength();
                    if (actionLength > 0 && getLength() > 0) {
                        this.timeData[i] = this.actions[i].getLength() / getLength();
                    }
                }
            }
        }
    }
    
    @Override
	public boolean interpolate(double t) {
		boolean interpolate = super.interpolate(t);
		if (!interpolate) {
			lastTime = 0;
		}
		return interpolate;
	}

    @Override
    public void doInterpolate(double t) {
        blendWeight = blendSpace.getWeight();
        BlendableAction firstActiveAction = (BlendableAction) actions[firstActiveIndex];
        BlendableAction secondActiveAction = (BlendableAction) actions[secondActiveIndex];
        firstActiveAction.setCollectTransformDelegate(this);
        secondActiveAction.setCollectTransformDelegate(this);

        //only interpolate the first action if the weight if below 1.
        if (blendWeight < 1f) {
            firstActiveAction.setWeight(1f);
            //firstActiveAction.interpolate(t * timeFactor[firstActiveIndex]);
            interpolate(firstActiveAction, firstActiveIndex, t);
            if (blendWeight == 0) {
                for (HasLocalTransform target : targetMap.keySet()) {
                    collect(target, targetMap.get(target));
                }
            }
        }

        //Second action should be interpolated
        secondActiveAction.setWeight(blendWeight);
        //secondActiveAction.interpolate(t * timeFactor[secondActiveIndex]);
        interpolate(secondActiveAction, secondActiveIndex, t);

        firstActiveAction.setCollectTransformDelegate(null);
        secondActiveAction.setCollectTransformDelegate(null);

        lastTime = t;
    }

	private void interpolate(BlendableAction action, int index, double time) {
		if (blendMode == BlendMode.Stretch) {
			// In stretch mode timeData represents time factor
			action.interpolate(time * timeData[index]);
		} else { // Loop mode
			double tpf = time - lastTime;
			timeData[index] += tpf;
			// In loop mode timeData represents time
			if (!action.interpolate(timeData[index])) {
				timeData[index] = 0;
			}
		}
	}

    protected Action[] getActions() {
        return actions;
    }

    public MyBlendSpace getBlendSpace() {
        return blendSpace;
    }

    protected void setFirstActiveIndex(int index) {
        this.firstActiveIndex = index;
    }

    protected void setSecondActiveIndex(int index) {
        this.secondActiveIndex = index;
    }

    @Override
    public Collection<HasLocalTransform> getTargets() {
        return targetMap.keySet();
    }

    @Override
    public void collectTransform(HasLocalTransform target, Transform t, float weight, BlendableAction source) {

        Transform tr = targetMap.get(target);
        if (weight == 1) {
            tr.set(t);
        } else if (weight > 0) {
            tr.interpolateTransforms(tr, t, weight);
        }

        if (source == actions[secondActiveIndex]) {
            collect(target, tr);
        }
    }

    private void collect(HasLocalTransform target, Transform tr) {
        if (collectTransformDelegate != null) {
            collectTransformDelegate.collectTransform(target, tr, this.getWeight(), this);
        } else {
            if (getTransitionWeight() == 1) {
                target.setLocalTransform(tr);
            } else {
                Transform trans = target.getLocalTransform();
                trans.interpolateTransforms(trans, tr, getTransitionWeight());
                target.setLocalTransform(trans);
            }
        }
    }

}