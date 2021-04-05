package com.capdevon.animation;

import com.jme3.animation.LoopMode;
import java.util.Objects;

public class Animation3 {

    String name;
    LoopMode loopMode;
    float blendTime = 0.15f;
    float speed = 1f;

    public Animation3(String name, LoopMode loopMode) {
        this.name = name;
        this.loopMode = loopMode;
    }

    public Animation3(String name, LoopMode loopMode, float blendTime) {
        this.name = name;
        this.loopMode = loopMode;
        this.blendTime = blendTime;
    }

    public Animation3(String name, LoopMode loopMode, float blendTime, float speed) {
        this.name = name;
        this.loopMode = loopMode;
        this.blendTime = blendTime;
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public float getBlendTime() {
        return blendTime;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return "Animation3[" + "name=" + name
                + ", loopMode=" + loopMode
                + ", blendTime=" + blendTime
                + ", speed=" + speed
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Animation3 other = (Animation3) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}
