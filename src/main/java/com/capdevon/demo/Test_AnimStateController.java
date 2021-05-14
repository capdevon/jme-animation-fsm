package com.capdevon.demo;

import com.capdevon.anim.fsm.AnimatorConditionMode;
import com.capdevon.anim.fsm.AnimatorController;
import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.anim.fsm.AnimatorState;
import com.capdevon.anim.fsm.AnimatorStateMachine;
import com.capdevon.anim.fsm.AnimatorStateTransition;
import com.capdevon.animation.Animation3;
import com.capdevon.physx.PhysxDebugAppState;
import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;

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
        settings.setResolution(1024, 768);
        settings.setFrameRate(60);
        settings.setSamples(4);
        settings.setBitsPerPixel(32);
        settings.setGammaCorrection(true);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }
    
    private BulletAppState physics;
    private Node player;
    private PlayerMovementControl playerControl;
    
    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        
        initPhysics();
        createFloor();
        setupPlayer();
        setupChaseCamera();
        setupSky();
        setupLights();
        
        TPSInputAppState input = new TPSInputAppState();
        input.setPlayerControl(playerControl);
        stateManager.attach(input);
    }
    
    /**
     * Initialize the physics simulation
     *
     */
    public void initPhysics() {
        physics = new BulletAppState();
        //physics.setThreadingType(ThreadingType.SEQUENTIAL);
        stateManager.attach(physics);
        stateManager.attach(new PhysxDebugAppState());

        physics.getPhysicsSpace().setAccuracy(0.01f); // 10-msec timestep
        physics.getPhysicsSpace().getSolverInfo().setNumIterations(15);
        physics.setDebugAxisLength(1);
        physics.setDebugEnabled(false);
    }
    
    /**
     * An ambient light and a directional sun light
     */
    private void setupLights() {
    	DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        rootNode.addLight(sun);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.25f, 0.25f, 0.25f, 1));
        rootNode.addLight(ambient);
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 2_048, 3);
        shadowFilter.setLight(sun);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);
        fpp.addFilter(shadowFilter);
    }
    
    private Material getUnshadedMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }
    
    private Material getShinyMat() {
        Material mat = new Material(assetManager, Materials.LIGHTING);
        mat.setColor("Diffuse", ColorRGBA.Green);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setFloat("Shininess", 0);
        mat.setBoolean("UseMaterialColors", true);
        return mat;
    }
    
    /**
     * a sky as background
     */
    private void setupSky() {
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);
    }
    
    private void createFloor() {
        Box box = new Box(20, 0.2f, 20);
        Geometry floorGeo = new Geometry("Floor.GeoMesh", box);
        floorGeo.setMaterial(getShinyMat());
        rootNode.attachChild(floorGeo);
        rootNode.setShadowMode(ShadowMode.CastAndReceive);

        CollisionShape collShape = CollisionShapeFactory.createMeshShape(floorGeo);
        RigidBodyControl rBody = new RigidBodyControl(collShape, 0f);
        floorGeo.addControl(rBody);
        physics.getPhysicsSpace().add(rBody);
    }
    
    private void setupPlayer() {
    	// setup Player model
        player = (Node) assetManager.loadModel(AnimDefs.MODEL);
        player.setName("Player.Character");
        rootNode.attachChild(player);
        
        // setup flashlight
        PointLight pl = new PointLight(new Vector3f(0, 2.5f, 0), ColorRGBA.Yellow, 4f);
        rootNode.addLight(pl);
        player.addControl(new LightControl(pl, LightControl.ControlDirection.SpatialToLight));
        
        // setup Physics Character
        BetterCharacterControl bcc = new BetterCharacterControl(.4f, 1.8f, 10f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        bcc.getRigidBody().setCollisionGroup(RigidBodyControl.COLLISION_GROUP_02);
        bcc.getRigidBody().setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_01);
        
        // setup AnimState
        AnimatorController animator = new AnimatorController();
        animator.addParameter("moveSpeed", AnimatorControllerParameterType.Float);
        player.addControl(animator);
        
        AnimatorStateMachine sm = animator.getStateMachine();
        AnimatorState idle = sm.addState("Idle", AnimDefs.rifleIdle.getName());
        AnimatorState walk = sm.addState("Run", AnimDefs.rifleRun.getName());
        
        AnimatorStateTransition idleToWalk = idle.addTransition(walk);
        idleToWalk.addCondition(AnimatorConditionMode.Greater, 0.3f, "moveSpeed");
        
        AnimatorStateTransition walkToIdle = walk.addTransition(idle);
        walkToIdle.addCondition(AnimatorConditionMode.Less, 0.3f, "moveSpeed");
        
        sm.setDefaultState(idle);
        
        // setup Player Movement Control
        playerControl = new PlayerMovementControl(this);
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
    
    private interface AnimDefs {
    	
      final String MODEL = "Models/Rifle/soldier.gltf";
      final Animation3 rifleIdle  = new Animation3("RifleIdle", LoopMode.Loop);
      final Animation3 rifleRun   = new Animation3("RifleRun", LoopMode.Loop);
  }
    
    private class PlayerMovementControl extends AbstractControl implements ActionListener, AnalogListener {

        public float m_MoveSpeed = 4.5f;
        public float m_TurnSpeed = 10f;
        
        private Camera camera;
        private AnimatorController animator;
        private BetterCharacterControl bcc;
        
        private final Quaternion dr = new Quaternion();
        private final Vector3f cameraDir = new Vector3f();
        private final Vector3f cameraLeft = new Vector3f();
        private final Vector3f walkDirection = new Vector3f();
        private boolean _MoveForward, _MoveBackward, _TurnLeft, _TurnRight;
        
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
			// TODO Auto-generated method stub
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
            } else if (name.equals(InputMapping.TOGGLE_CROUCH) && isPressed) {
                boolean isDucked = bcc.isDucked();
                bcc.setDucked(!isDucked);
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
                dr.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
                spatial.getWorldRotation().slerp(dr, m_TurnSpeed * tpf);
                bcc.setViewDirection(spatial.getWorldRotation().mult(Vector3f.UNIT_Z));
            }
            
            bcc.setWalkDirection(walkDirection.multLocal(m_MoveSpeed));
            
            animator.setFloat("moveSpeed", bcc.getVelocity().length());

            // update animation
            //----------------------------------------------------------------
//            if (isMoving) {
//                mocap.setAnimation(bcc.isDucked() ? YBot.WalkCrouching_FW : YBot.Walk_FW);
//            } else {
//                mocap.setAnimation(bcc.isDucked() ? YBot.IdleCrouching : YBot.IdleAiming);
//            }
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
            //To change body of generated methods, choose Tools | Templates.
        }

    }
    
	private interface InputMapping {

		final String MOVE_LEFT 		= "MOVE_LEFT";
		final String MOVE_RIGHT 	= "MOVE_RIGHT";
		final String MOVE_FORWARD 	= "MOVE_FORWARD";
		final String MOVE_BACKWARD 	= "MOVE_BACKWARD";
		final String TOGGLE_CROUCH 	= "TOGGLE_CROUCH";
		final String SWITCH_WEAPON 	= "SWITCH_WEAPON";
		final String RELOAD_WEAPON 	= "RELOAD_WEAPON";
		final String FIRE 			= "FIRE";
	}
    
    private class TPSInputAppState extends BaseAppState implements AnalogListener, ActionListener {

        private InputManager inputManager;
        private PlayerMovementControl playerControl;

        @Override
        protected void initialize(Application app) {
            this.inputManager = app.getInputManager();
            addInputMappings();
        }

        @Override
        protected void cleanup(Application app) {
        }

        @Override
        protected void onEnable() {
        }

        @Override
        protected void onDisable() {
        }
        
		private void addInputMappings() {

			addMapping(InputMapping.MOVE_FORWARD, 	new KeyTrigger(KeyInput.KEY_W));
			addMapping(InputMapping.MOVE_BACKWARD, 	new KeyTrigger(KeyInput.KEY_S));
			addMapping(InputMapping.MOVE_LEFT, 		new KeyTrigger(KeyInput.KEY_A));
			addMapping(InputMapping.MOVE_RIGHT, 	new KeyTrigger(KeyInput.KEY_D));
			addMapping(InputMapping.RELOAD_WEAPON, 	new KeyTrigger(KeyInput.KEY_R));
			addMapping(InputMapping.SWITCH_WEAPON, 	new KeyTrigger(KeyInput.KEY_F));
			addMapping(InputMapping.TOGGLE_CROUCH, 	new KeyTrigger(KeyInput.KEY_LSHIFT));
			addMapping(InputMapping.FIRE, 			new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		}

		private void addMapping(String bindingName, Trigger... triggers) {
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
