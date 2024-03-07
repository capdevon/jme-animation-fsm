package com.capdevon.demo;

import com.capdevon.anim.AnimUtils;
import com.capdevon.anim.fsm.AnimatorConditionMode;
import com.capdevon.anim.fsm.AnimatorController;
import com.capdevon.anim.fsm.AnimatorControllerLayer;
import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.anim.fsm.AnimatorState;
import com.capdevon.anim.fsm.AnimatorStateMachine;
import com.capdevon.anim.fsm.AnimatorStateTransition;
import com.capdevon.anim.fsm.StateMachineBehaviour;
import com.capdevon.anim.fsm.StateMachineListener;
import com.capdevon.demo.control.PlayerBaseControl;
import com.capdevon.demo.states.DefaultSceneAppState;
import com.capdevon.demo.states.TogglePhysicsDebugState;
import com.capdevon.demo.util.MeshBuilder;
import com.jme3.anim.AnimComposer;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.ChaseCamera;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.LightControl;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public class Test_StateMachineBehaviour extends SimpleApplication {

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Test_StateMachineBehaviour app = new Test_StateMachineBehaviour();
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

        MeshBuilder.init(assetManager);
        initPhysics();
        setupPlayer();
        setupChaseCamera();
        setupEnemyAI();
        
        stateManager.attach(new DefaultSceneAppState());
    }

    /**
     * Initialize the physics simulation
     */
    public void initPhysics() {
        physics = new BulletAppState();
        stateManager.attach(physics);
        physics.setDebugAxisLength(1);
        stateManager.attach(new TogglePhysicsDebugState());
    }

    private void setupEnemyAI() {
        // setup enemy model
        Node soldier = (Node) assetManager.loadModel(AnimDefs.MODEL);
        soldier.setName("SoldierAI");
        soldier.setLocalTranslation(4, 0, 4);
        rootNode.attachChild(soldier);
        
        // setup flashlight
        PointLight pl = new PointLight(new Vector3f(0, 2.5f, 0), ColorRGBA.Yellow, 4f);
        rootNode.addLight(pl);
        soldier.addControl(new LightControl(pl, LightControl.ControlDirection.SpatialToLight));
        
        // setup Physics Character
        BetterCharacterControl bcc = new BetterCharacterControl(.4f, 1.8f, 40f);
        soldier.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        bcc.getRigidBody().setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        bcc.getRigidBody().setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        
        // Create the controller and the parameters
        AnimatorController animator = new AnimatorController(AnimUtils.getAnimComposer(soldier));
        animator.addParameter("distance", AnimatorControllerParameterType.Float);
        soldier.addControl(animator);
        
        // Define states for animations.
        AnimatorControllerLayer layer0 = animator.getLayer(AnimComposer.DEFAULT_LAYER);
        AnimatorStateMachine sm = layer0.getStateMachine();
        
        AnimatorState patrol = sm.addState("Patrol", AnimDefs.WalkWithRifle);
        patrol.addStateMachineBehaviour(new PatrolState());
        
        AnimatorState chase = sm.addState("Chase", AnimDefs.RifleRun);
        chase.addStateMachineBehaviour(new ChaseState());
        
        AnimatorState attack = sm.addState("Attack", AnimDefs.FiringRifleAuto);
        attack.addStateMachineBehaviour(new AttackState());
        
        // Define the transitions and conditions for each state
        AnimatorStateTransition patrolToChase = patrol.addTransition(chase);
        patrolToChase.addCondition(AnimatorConditionMode.Less, 12f, "distance");
        
        AnimatorStateTransition chaseToPatrol = chase.addTransition(patrol);
        chaseToPatrol.addCondition(AnimatorConditionMode.Greater, 12f, "distance");
        
        AnimatorStateTransition chaseToAttack = chase.addTransition(attack);
        chaseToAttack.addCondition(AnimatorConditionMode.Less, 5f, "distance");
        
        AnimatorStateTransition attackToChase = attack.addTransition(chase);
        attackToChase.addCondition(AnimatorConditionMode.Greater, 6f, "distance");
        
        // set the initial state.
        sm.setDefaultState(patrol);
        
        BitmapText bmp = createBitmap(patrol.getName(), 0.5f, ColorRGBA.Red);
        bmp.setName("Status");
        bmp.setLocalTranslation(0, 2.5f, 0);
        soldier.attachChild(bmp);
        
        SoldierAI aiControl = new SoldierAI();
        aiControl.player = player;
        aiControl.bmp = bmp;
        soldier.addControl(aiControl);
        sm.addListener(aiControl);
    }
    
    private BitmapText createBitmap(String text, float size, ColorRGBA color) {
        BitmapText bmp = new BitmapText(guiFont);
        bmp.setText(text);
        bmp.setColor(color);
        bmp.setBox(new Rectangle((-bmp.getLineWidth() / 2) * bmp.getSize(), 0f, bmp.getLineWidth() * bmp.getSize(), bmp.getLineHeight()));
        bmp.setShadowMode(ShadowMode.Off);
        bmp.setQueueBucket(RenderQueue.Bucket.Transparent);
        bmp.setAlignment(BitmapFont.Align.Center);
        bmp.setSize(size);
        bmp.addControl(new BillboardControl());

        return bmp;
    }
    
    private void setupPlayer() {
        player = new Node("Player");
        player.attachChild(MeshBuilder.createCapsule(ColorRGBA.Green, .5f, 1.4f));
        player.attachChild(MeshBuilder.createAxes("MyAxes"));
        player.setLocalTranslation(0, 0, -10f);
        rootNode.attachChild(player);

        BetterCharacterControl bcc = new BetterCharacterControl(.5f, 1.6f, 40f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        //bcc.getRigidBody().setDebugMaterial(getUnshadedMaterial(ColorRGBA.Green));

        PlayerBaseControl baseControl = new PlayerBaseControl(this);
        player.addControl(baseControl);
    }

//    	private Material getUnshadedMaterial(ColorRGBA color) {
//    		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//    		mat.setColor("Color", color);
//    		return mat;
//    	}

    private void setupChaseCamera() {
        // disable the default 1st-person flyCam!
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setLookAtOffset(Vector3f.UNIT_Y.mult(1.5f));
        chaseCam.setMinDistance(15);
        chaseCam.setMaxDistance(18);
        chaseCam.setRotationSpeed(2);
        chaseCam.setMinVerticalRotation(-FastMath.QUARTER_PI);
        chaseCam.setMaxVerticalRotation(FastMath.QUARTER_PI);
        chaseCam.setDownRotateOnCloseViewOnly(false);

        chaseCam.setDefaultDistance(chaseCam.getMinDistance());
    }

    /**
     * -----------------------------------------------
     * SoldierAI
     * -----------------------------------------------
     */
    private class SoldierAI extends AbstractControl implements StateMachineListener {

        public Spatial player;
        public BitmapText bmp;

        AnimatorController animator;
        BetterCharacterControl bcc;
        float fireTime = 0;
        float fireRate = .6f;
        boolean isFiring;

        @Override
        public void setSpatial(Spatial sp) {
            super.setSpatial(sp);
            if (spatial != null) {
                this.animator = spatial.getControl(AnimatorController.class);
                this.bcc = spatial.getControl(BetterCharacterControl.class);
            }
        }

        @Override
        protected void controlUpdate(float tpf) {
            float distance = player.getWorldTranslation().distance(spatial.getWorldTranslation());
            animator.setFloat("distance", distance);
            
            if (isFiring) {
                fireTime += tpf;
                if (fireTime > fireRate) {
                    fireTime = 0;
                    System.out.println("Firing");
                }
            }
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }

        public void rotateTo(Vector3f direction, float angularSpeed) {
            float angle = FastMath.atan2(direction.x, direction.z);
            Quaternion lookRotation = new Quaternion().fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
            spatial.getWorldRotation().slerp(lookRotation, angularSpeed);
            bcc.setViewDirection(spatial.getWorldRotation().mult(Vector3f.UNIT_Z));
        }

        public void startFiring() {
            isFiring = true;
            System.out.println("FireTimer schedule");
        }

        public void stopFiring() {
            isFiring = false;
            fireTime = 0;
            System.out.println("FireTimer cancel");
        }

        @Override
        public void onStateChanged(AnimatorState from, AnimatorState to) {
            bmp.setText(to.getName());
        }

    }
    
    /**
     * -----------------------------------------------
     * SoldierBaseFSM
     * -----------------------------------------------
     */
    private abstract class SoldierBaseFSM implements StateMachineBehaviour {

        public SoldierAI aiControl;
        public Spatial spatial;
        public float moveSpeed = 2f;
        public float runSpeed = 3f;
        public float rotSpeed = 10f;
        public float accuracy = 0.5f;
        
        private boolean initialized = false;

        @Override
        public void onStateEnter(AnimatorController animator) {
            if (!initialized) {
                spatial = animator.getSpatial();
                aiControl = spatial.getControl(SoldierAI.class);
                initialized = true;
            }
        }
    }

    /**
     * -----------------------------------------------
     * PatrolState
     * -----------------------------------------------
     */
    private class PatrolState extends SoldierBaseFSM {

        private int currentWP;
        private Vector3f[] waypoints = {
            new Vector3f(5, 0, 5),
            new Vector3f(-5, 0, 5),
            new Vector3f(-5, 0, -5),
            new Vector3f(5, 0, -5)
        };

        @Override
        public void onStateEnter(AnimatorController animator) {
            super.onStateEnter(animator);
            currentWP = 0;
        }

        @Override
        public void onStateUpdate(AnimatorController animator, float tpf) {
            if (waypoints.length == 0)
                return;

            if (waypoints[currentWP].distance(spatial.getWorldTranslation()) < accuracy) {
                currentWP++;
                if (currentWP >= waypoints.length) {
                    currentWP = 0;
                }
            }

            // rotate towards target
            Vector3f walkDirection = waypoints[currentWP].subtract(spatial.getWorldTranslation());
            walkDirection.y = 0;
            walkDirection.normalizeLocal();
            aiControl.rotateTo(walkDirection, rotSpeed * tpf);
            aiControl.bcc.setWalkDirection(walkDirection.multLocal(moveSpeed));
        }

        @Override
        public void onStateExit(AnimatorController animator) {
        }

    }

    /**
     * -----------------------------------------------
     * ChaseState
     * -----------------------------------------------
     */
    private class ChaseState extends SoldierBaseFSM {

        @Override
        public void onStateEnter(AnimatorController animator) {
            super.onStateEnter(animator);
        }

        @Override
        public void onStateUpdate(AnimatorController animator, float tpf) {
            // rotate towards target
            Vector3f walkDirection = aiControl.player.getWorldTranslation().subtract(spatial.getWorldTranslation());
            walkDirection.y = 0;
            walkDirection.normalizeLocal();
            aiControl.rotateTo(walkDirection, rotSpeed * tpf);
            aiControl.bcc.setWalkDirection(walkDirection.multLocal(runSpeed));
        }

        @Override
        public void onStateExit(AnimatorController animator) {
        }

    }

    /**
     * -----------------------------------------------
     * AttackState
     * -----------------------------------------------
     */
    private class AttackState extends SoldierBaseFSM {

        @Override
        public void onStateEnter(AnimatorController animator) {
            super.onStateEnter(animator);
            aiControl.startFiring();
        }

        @Override
        public void onStateUpdate(AnimatorController animator, float tpf) {
            Vector3f walkDirection = aiControl.player.getWorldTranslation().subtract(spatial.getWorldTranslation());
            walkDirection.y = 0;
            walkDirection.normalizeLocal();
            aiControl.rotateTo(walkDirection, rotSpeed * tpf);
            aiControl.bcc.setWalkDirection(Vector3f.ZERO);
        }

        @Override
        public void onStateExit(AnimatorController animator) {
            aiControl.stopFiring();
        }

    }

}
