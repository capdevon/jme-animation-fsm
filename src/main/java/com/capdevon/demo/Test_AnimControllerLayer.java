package com.capdevon.demo;

import com.capdevon.anim.AnimMaskBuilder;
import com.capdevon.anim.AnimUtils;
import com.capdevon.anim.MixamoBodyBones;
import com.capdevon.anim.fsm.AnimatorConditionMode;
import com.capdevon.anim.fsm.AnimatorController;
import com.capdevon.anim.fsm.AnimatorControllerLayer;
import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.anim.fsm.AnimatorState;
import com.capdevon.anim.fsm.AnimatorStateMachine;
import com.capdevon.anim.fsm.AnimatorStateTransition;
import com.capdevon.demo.util.DefaultSceneAppState;
import com.capdevon.engine.FRotator;
import com.capdevon.engine.SimpleAppState;
import com.capdevon.physx.TogglePhysicsDebugState;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimationMask;
import com.jme3.anim.SkinningControl;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
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
import com.jme3.math.Quaternion;
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
public class Test_AnimControllerLayer extends SimpleApplication {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Test_AnimControllerLayer app = new Test_AnimControllerLayer();
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
        stateManager.attach(new PlayerInputAppState());
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
        bcc.getRigidBody().setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        bcc.getRigidBody().setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);

        setupAnimator(player);

        // setup Player Movement Control
        player.addControl(new PlayerMovementControl(this));
    }

    private void setupAnimator(Spatial player) {
        AnimComposer animComposer = AnimUtils.getAnimComposer(player);
        SkinningControl skeleton = AnimUtils.getSkinningControl(player);

        // Create the controller and the parameters
        //------------------------------------------------------------------------
        AnimatorController animator = new AnimatorController(animComposer);
        animator.addParameter("isRunning", AnimatorControllerParameterType.Bool);
        animator.addParameter("isReloading", AnimatorControllerParameterType.Trigger);
        player.addControl(animator);

        // Define states for layer 0
        //------------------------------------------------------------------------
        AnimatorStateMachine sm = animator.getLayer(0).getStateMachine();
        AnimatorState idle = sm.addState("Idle", AnimDefs.RifleAimingIdle);
        AnimatorState walk = sm.addState("Walk", AnimDefs.WalkWithRifle);

        AnimatorStateTransition idleToWalk = idle.addTransition(walk);
        idleToWalk.addCondition(AnimatorConditionMode.If, 0f, "isRunning");

        AnimatorStateTransition walkToIdle = walk.addTransition(idle);
        walkToIdle.addCondition(AnimatorConditionMode.IfNot, 0f, "isRunning");

        // set the initial state for state machine of layer0
        sm.setDefaultState(idle);

        // Define states for layer 1
        //------------------------------------------------------------------------
        AnimationMask animMask = new AnimMaskBuilder(skeleton.getArmature())
        		.addFromJoint("Armature_mixamorig:" + MixamoBodyBones.Spine)
        		.build();

        // Define a layer that acts on an AnimationMask
        AnimatorControllerLayer layer1 = animator.addLayer("Torso", animMask);
        AnimatorStateMachine sm1 = layer1.getStateMachine();
        AnimatorState empty = sm1.addState("Empty");
        AnimatorState reload = sm1.addState("Reload", AnimDefs.Reloading);

        AnimatorStateTransition emptyToReload = empty.addTransition(reload);
        emptyToReload.addCondition(AnimatorConditionMode.If, 0f, "isReloading");

        // execute 95% of the reloading animation before returning to idle state
        AnimatorStateTransition reloadToEmpty = reload.addTransition(empty, 0.95f);

        // set the initial state for state machine of layer1
        sm1.setDefaultState(empty);
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

        private float moveSpeed = 3.6f;
        private float turnSpeed = 10f;

        private Camera camera;
        private AnimatorController animator;
        private BetterCharacterControl bcc;

        private final Quaternion lookRotation = new Quaternion();
        private final Vector3f cameraDir = new Vector3f();
        private final Vector3f cameraLeft = new Vector3f();
        private final Vector3f walkDirection = new Vector3f();
        private final Vector3f viewDirection = new Vector3f(0, 0, 1);
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
            } else if (name.equals(InputMapping.RELOAD) && isPressed) {
                animator.setTrigger("isReloading");
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
                float angle = FastMath.atan2(walkDirection.x, walkDirection.z);
                lookRotation.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
                float smoothTime = turnSpeed * tpf;
                FRotator.smoothDamp(spatial.getWorldRotation(), lookRotation, smoothTime, viewDirection);
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

    private class PlayerInputAppState extends SimpleAppState implements AnalogListener, ActionListener {

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

        private void addMapping(String bindingName, Trigger... triggers) {
            inputManager.addMapping(bindingName, triggers);
            inputManager.addListener(this, bindingName);
        }

        @Override
        public void onAnalog(String name, float value, float tpf) {
            if (isEnabled()) {
                playerControl.onAnalog(name, value, tpf);
            }
        }

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isEnabled()) {
                playerControl.onAction(name, isPressed, tpf);
            }
        }
    }

}
