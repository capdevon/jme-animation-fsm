/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capdevon.control;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 * 
 * @author capdevon
 */
public class PlayerBaseControl extends AbstractControl implements ActionListener {
    
    private InputManager inputManager;
    private BetterCharacterControl bcc;
    
    boolean _StrafeLeft, _StrafeRight;
    boolean _MoveForward;
    boolean _MoveBackward;
    boolean _TurnLeft;
    boolean _TurnRight;
    boolean ducked;
    float m_MoveSpeed = 2.5f;
    
    Vector3f walkDirection = new Vector3f(0, 0, 0);
    Vector3f viewDirection = new Vector3f(0, 0, 1);
    Vector3f dz = new Vector3f();
    Vector3f dx = new Vector3f();
    Quaternion tempRot = new Quaternion();
    
    /**
     * 
     * @param inputManager 
     */
    public PlayerBaseControl(InputManager inputManager) {
        this.inputManager = inputManager;
    }
    
    @Override
    public void setSpatial(Spatial sp) {
        super.setSpatial(sp);
        if (spatial != null) {
            this.bcc = spatial.getControl(BetterCharacterControl.class);
            registerInputs();
        }
    }
    
    @Override
    public void controlUpdate(float tpf) {
        
        walkDirection.set(Vector3f.ZERO);

        if (_StrafeLeft || _StrafeRight) {
//            float k = _StrafeLeft ? 1f : -1;
//            spatial.getWorldRotation().mult(Vector3f.UNIT_X, dx);
//            walkDirection.addLocal(dx.multLocal(k * m_MoveSpeed));
        }
        if (_MoveForward || _MoveBackward) {
            float k = _MoveForward ? 1 : -1;
            spatial.getWorldRotation().mult(Vector3f.UNIT_Z, dz);
            walkDirection.addLocal(dz.multLocal(k * m_MoveSpeed));
        }
        if (_TurnLeft || _TurnRight) {
            float k = _TurnLeft ? FastMath.PI : -FastMath.PI;
            tempRot.fromAngleNormalAxis(tpf * k, Vector3f.UNIT_Y).multLocal(viewDirection);
            bcc.setViewDirection(viewDirection); //Turn!
        }

        bcc.setWalkDirection(walkDirection); //Walk!
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("StrafeLeft")) {
            _StrafeLeft = isPressed;
        } else if (name.equals("StrafeRight")) {
            _StrafeRight = isPressed;
        } else if (name.equals("MoveForward")) {
            _MoveForward = isPressed;
        } else if (name.equals("MoveBackward")) {
            _MoveBackward = isPressed;
        } else if (name.equals("RotateLeft")) {
            _TurnLeft = isPressed;
        } else if (name.equals("RotateRight")) {
            _TurnRight = isPressed;
        } else if (name.equals("Ducked") && isPressed) {
            ducked = !ducked;
            bcc.setDucked(ducked);
        } else if (name.equals("Jump") && isPressed) {
            bcc.jump();
        }
    }
    
    public void stop() {
//        _RunForward   = false;
        _MoveForward  = false;
        _MoveBackward = false;
        _TurnLeft     = false;
        _TurnRight    = false;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //To change body of generated methods, choose Tools | Templates.
    }
            
    /**
     * Custom Keybinding: Map named actions to inputs.
     */
    private void registerInputs() {
        addMapping("StrafeLeft",      new KeyTrigger(KeyInput.KEY_Q));
        addMapping("StrafeRight",     new KeyTrigger(KeyInput.KEY_E));
        addMapping("RunForward",      new KeyTrigger(KeyInput.KEY_SPACE));
        addMapping("MoveForward",     new KeyTrigger(KeyInput.KEY_W));
        addMapping("MoveBackward",    new KeyTrigger(KeyInput.KEY_S));
        addMapping("RotateLeft",      new KeyTrigger(KeyInput.KEY_A));
        addMapping("RotateRight",     new KeyTrigger(KeyInput.KEY_D));
        addMapping("Ducked",          new KeyTrigger(KeyInput.KEY_Z));
    }
    
    private void addMapping(String mapping, Trigger... triggers) {
        inputManager.addMapping(mapping, triggers);
        inputManager.addListener(this, mapping);
    }
    
}
