package com.capdevon.demo;

import com.capdevon.anim.AnimUtils;
import com.capdevon.anim.fsm.AnimatorConditionMode;
import com.capdevon.anim.fsm.AnimatorController;
import com.capdevon.anim.fsm.AnimatorControllerLayer;
import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.anim.fsm.AnimatorState;
import com.capdevon.anim.fsm.AnimatorStateMachine;
import com.capdevon.anim.fsm.AnimatorStateTransition;
import com.capdevon.demo.util.DefaultSceneAppState;
import com.capdevon.engine.SimpleAppState;
import com.capdevon.physx.TogglePhysicsDebugState;
import com.jme3.anim.AnimComposer;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.LightControl;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public class Test_AnimStateController extends SimpleApplication {

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Test_AnimStateController app = new Test_AnimStateController();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setSamples(4);
        settings.setBitsPerPixel(32);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }
    
    private BulletAppState physics;
    private Node player;
    
    @Override
    public void simpleInitApp() {
        
        initPhysics();
        setupPlayer();
        setupChaseCamera();
        
        stateManager.attach(new DefaultSceneAppState());
        stateManager.attach(new TPSInputAppState());
    }
    
    /**
     * Initialize the physics simulation
     */
    public void initPhysics() {
        physics = new BulletAppState();
        stateManager.attach(physics);
        stateManager.attach(new TogglePhysicsDebugState());
    }

    private void setupPlayer() {
        // setup Player model
        player = (Node) assetManager.loadModel(AnimDefs.MODEL);
        player.setName("Player");
        rootNode.attachChild(player);
        
        // setup flashlight
        PointLight pl = new PointLight(new Vector3f(0, 2.5f, 0), ColorRGBA.Yellow, 4f);
        rootNode.addLight(pl);
        player.addControl(new LightControl(pl, LightControl.ControlDirection.SpatialToLight));
        
        // setup Physics Character
        BetterCharacterControl bcc = new BetterCharacterControl(.4f, 1.8f, 10f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        
        // Create the controller and the parameters
        AnimatorController animator = new AnimatorController(AnimUtils.getAnimComposer(player));
        animator.addParameter("isRunning", AnimatorControllerParameterType.Bool);
        player.addControl(animator);
        
        // Define states for animations.
        AnimatorControllerLayer layer0 = animator.getLayer(AnimComposer.DEFAULT_LAYER);
        AnimatorStateMachine sm = layer0.getStateMachine();
        AnimatorState idle = sm.addState("Idle", AnimDefs.RifleIdle);
        AnimatorState walk = sm.addState("Run", AnimDefs.RifleRun);
        
        AnimatorStateTransition idleToWalk = idle.addTransition(walk);
        idleToWalk.addCondition(AnimatorConditionMode.If, 0f, "isRunning");
        
        AnimatorStateTransition walkToIdle = walk.addTransition(idle);
        walkToIdle.addCondition(AnimatorConditionMode.IfNot, 0f, "isRunning");
        
        // set the initial state.
        sm.setDefaultState(idle);
        
        // setup Player Movement Control
        PlayerMovementControl playerControl = new PlayerMovementControl(this);
        player.addControl(playerControl);
    }
    
    private void setupChaseCamera() {
        // disable the default 1st-person flyCam!
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        flyCam.setEnabled(false);
        
        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setLookAtOffset(Vector3f.UNIT_Y.mult(1.5f));
        chaseCam.setMinDistance(5);
        chaseCam.setMaxDistance(8);
        chaseCam.setRotationSpeed(2f);
        chaseCam.setMinVerticalRotation(-FastMath.QUARTER_PI);
        chaseCam.setMaxVerticalRotation(FastMath.QUARTER_PI);
        chaseCam.setDownRotateOnCloseViewOnly(false);
        
        chaseCam.setDefaultDistance(chaseCam.getMinDistance());
    }
    
    private class PlayerMovementControl extends AbstractControl implements ActionListener, AnalogListener {

        private float moveSpeed = 4.5f;
        private float turnSpeed = 6f;

        private Camera camera;
        private AnimatorController animator;
        private BetterCharacterControl bcc;

        private final Vector3f cameraDir = new Vector3f();
        private final Vector3f cameraLeft = new Vector3f();
        private final Vector3f walkDirection = new Vector3f();
        private boolean moveForward, moveBackward, turnLeft, turnRight;
        
        public PlayerMovementControl(Application app) {
            this.camera = app.getCamera();
        }

        @Override
        public void setSpatial(Spatial sp) {
            super.setSpatial(sp);
            if (spatial != null) {
                this.animator = spatial.getControl(AnimatorController.class);
                this.bcc = spatial.getControl(BetterCharacterControl.class);
            }
        }

        @Override
        public void onAnalog(String name, float value, float tpf) {
        }

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(InputMapping.MOVE_FORWARD)) {
                moveForward = isPressed;
            } else if (name.equals(InputMapping.MOVE_BACKWARD)) {
                moveBackward = isPressed;
            } else if (name.equals(InputMapping.MOVE_LEFT)) {
                turnLeft = isPressed;
            } else if (name.equals(InputMapping.MOVE_RIGHT)) {
                turnRight = isPressed;
            }
        }

        @Override
        public void controlUpdate(float tpf) {

            camera.getDirection(cameraDir).setY(0);
            camera.getLeft(cameraLeft).setY(0);

            walkDirection.set(0, 0, 0);

            if (moveForward) {
                walkDirection.addLocal(cameraDir);
            } else if (moveBackward) {
                walkDirection.subtractLocal(cameraDir);
            }
            if (turnLeft) {
                walkDirection.addLocal(cameraLeft);
            } else if (turnRight) {
                walkDirection.subtractLocal(cameraLeft);
            }

            walkDirection.normalizeLocal();
            boolean isMoving = walkDirection.lengthSquared() > 0;

            if (isMoving) {
                // smooth rotation
                Vector3f viewDirection = bcc.getViewDirection().interpolateLocal(walkDirection, turnSpeed * tpf);
                bcc.setViewDirection(viewDirection);
            }

            bcc.setWalkDirection(walkDirection.multLocal(moveSpeed));

            animator.setBool("isRunning", isMoving);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
            //To change body of generated methods, choose Tools | Templates.
        }

    }
	
    private interface InputMapping {

        final String MOVE_LEFT      = "MOVE_LEFT";
        final String MOVE_RIGHT     = "MOVE_RIGHT";
        final String MOVE_FORWARD   = "MOVE_FORWARD";
        final String MOVE_BACKWARD  = "MOVE_BACKWARD";
        final String RUNNING        = "RUNNING";
        final String RELOAD         = "RELOAD";
        final String FIRE           = "FIRE";
    }
	
    private class TPSInputAppState extends SimpleAppState implements AnalogListener, ActionListener {

        private PlayerMovementControl playerControl;

        @Override
        public void initialize(Application app) {
            super.initialize(app);
            playerControl = find("Player").getControl(PlayerMovementControl.class);
            addInputMappings();
        }

        private void addInputMappings() {

            addMapping(InputMapping.MOVE_FORWARD,   new KeyTrigger(KeyInput.KEY_W));
            addMapping(InputMapping.MOVE_BACKWARD,  new KeyTrigger(KeyInput.KEY_S));
            addMapping(InputMapping.MOVE_LEFT,      new KeyTrigger(KeyInput.KEY_A));
            addMapping(InputMapping.MOVE_RIGHT,     new KeyTrigger(KeyInput.KEY_D));
            addMapping(InputMapping.RUNNING,        new KeyTrigger(KeyInput.KEY_SPACE));
            addMapping(InputMapping.FIRE,           new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
            addMapping(InputMapping.RELOAD,         new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        }

        private void addMapping(String bindingName, Trigger...triggers) {
            inputManager.addMapping(bindingName, triggers);
            inputManager.addListener(this, bindingName);
        }

        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isEnabled() && playerControl != null) {
                playerControl.onAnalog(name, value, tpf);
            }
        }

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isEnabled() && playerControl != null) {
                playerControl.onAction(name, isPressed, tpf);
            }
        }

        public void setPlayerControl(PlayerMovementControl playerControl) {
            this.playerControl = playerControl;
        }
    }

}
