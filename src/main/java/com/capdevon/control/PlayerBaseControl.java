package com.capdevon.control;

import com.capdevon.engine.FRotator;
import com.jme3.app.Application;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author capdevon
 */
public class PlayerBaseControl extends AbstractControl implements ActionListener {

    private interface InputMapping {

        final String MOVE_LEFT = "MOVE_LEFT";
        final String MOVE_RIGHT = "MOVE_RIGHT";
        final String MOVE_FORWARD = "MOVE_FORWARD";
        final String MOVE_BACKWARD = "MOVE_BACKWARD";
        final String RUNNING = "RUNNING";
        final String FIRE = "FIRE";
    }

    public float m_MoveSpeed = 4.5f;
    public float m_TurnSpeed = 10f;

    private Camera camera;
    private InputManager inputManager;
    private BetterCharacterControl bcc;

    private final Quaternion lookRotation = new Quaternion();
    private final Vector3f cameraDir = new Vector3f();
    private final Vector3f cameraLeft = new Vector3f();
    private final Vector3f walkDirection = new Vector3f(0, 0, 0);
    private final Vector3f viewDirection = new Vector3f(0, 0, 1);
    private boolean _MoveForward, _MoveBackward, _TurnLeft, _TurnRight;
    private boolean isRunning;

    /**
     *
     * @param app
     */
    public PlayerBaseControl(Application app) {
        this.camera = app.getCamera();
        this.inputManager = app.getInputManager();
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

        camera.getDirection(cameraDir).setY(0);
        camera.getLeft(cameraLeft).setY(0);

        walkDirection.set(0, 0, 0);

        if (_MoveForward) {
            walkDirection.addLocal(cameraDir);
        } else if (_MoveBackward) {
            walkDirection.subtractLocal(cameraDir);
        }
        if (_TurnLeft) {
            walkDirection.addLocal(cameraLeft);
        } else if (_TurnRight) {
            walkDirection.subtractLocal(cameraLeft);
        }

        walkDirection.normalizeLocal();
        boolean isMoving = walkDirection.lengthSquared() > 0;

        if (isMoving) {
            // smooth rotation
            float angle = FastMath.atan2(walkDirection.x, walkDirection.z);
            lookRotation.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
            FRotator.smoothDamp(spatial.getWorldRotation(), lookRotation, m_TurnSpeed * tpf, viewDirection);
            bcc.setViewDirection(viewDirection);
        }

        bcc.setWalkDirection(walkDirection.multLocal(m_MoveSpeed));
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        //To change body of generated methods, choose Tools | Templates.
        if (name.equals(InputMapping.MOVE_FORWARD)) {
            _MoveForward = isPressed;
        } else if (name.equals(InputMapping.MOVE_BACKWARD)) {
            _MoveBackward = isPressed;
        } else if (name.equals(InputMapping.MOVE_LEFT)) {
            _TurnLeft = isPressed;
        } else if (name.equals(InputMapping.MOVE_RIGHT)) {
            _TurnRight = isPressed;
        } else if (name.equals(InputMapping.RUNNING) && isPressed) {
            isRunning = isPressed;
        }
    }

    public void stop() {
        isRunning       = false;
        _MoveForward    = false;
        _MoveBackward   = false;
        _TurnLeft       = false;
        _TurnRight      = false;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Custom Keybinding: Map named actions to inputs.
     */
    private void registerInputs() {
        addMapping(InputMapping.MOVE_FORWARD,   new KeyTrigger(KeyInput.KEY_W));
        addMapping(InputMapping.MOVE_BACKWARD,  new KeyTrigger(KeyInput.KEY_S));
        addMapping(InputMapping.MOVE_LEFT,      new KeyTrigger(KeyInput.KEY_A));
        addMapping(InputMapping.MOVE_RIGHT,     new KeyTrigger(KeyInput.KEY_D));
        addMapping(InputMapping.RUNNING,        new KeyTrigger(KeyInput.KEY_SPACE));
        addMapping(InputMapping.FIRE,           new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    }

    private void addMapping(String mapping, Trigger... triggers) {
        inputManager.addMapping(mapping, triggers);
        inputManager.addListener(this, mapping);
    }

}
