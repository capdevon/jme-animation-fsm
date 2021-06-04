package com.capdevon.demo;

import com.capdevon.anim.AnimUtils;
import com.capdevon.anim.fsm.AnimatorController;
import com.capdevon.anim.fsm.AnimatorControllerParameter.AnimatorControllerParameterType;
import com.capdevon.anim.fsm.AnimatorState;
import com.capdevon.anim.fsm.AnimatorStateMachine;
import com.capdevon.anim.fsm.BlendTree;
import com.capdevon.anim.fsm.BlendTree.BlendTreeType;
import com.capdevon.animation.MixamoBodyBones;
import com.capdevon.engine.FRotator;
import com.capdevon.physx.PhysxDebugAppState;
import com.jme3.anim.AnimComposer;
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
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
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
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

/**
 * 
 * @author capdevon
 */
public class Test_BlendTree2D extends SimpleApplication {

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Test_BlendTree2D app = new Test_BlendTree2D();
        AppSettings settings = new AppSettings(true);
        //settings.setResolution(1024, 768);
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
        createFloor();
        setupPlayer();
        setupChaseCamera();
        setupSky();
        setupLights();

        PlayerInputAppState input = new PlayerInputAppState();
        input.setPlayerControl(player.getControl(PlayerMovementControl.class));
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

    /**
     * a sky as background
     */
    private void setupSky() {
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);
    }

    private void createFloor() {
        rootNode.setShadowMode(ShadowMode.CastAndReceive);

        Box box = new Box(20, .1f, 20);
        box.scaleTextureCoordinates(new Vector2f(20, 20));
        Geometry floorGeo = new Geometry("Floor.GeoMesh", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Textures/white_grid.jpg");
        tex.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("ColorMap", tex);
        floorGeo.setMaterial(mat);
        rootNode.attachChild(floorGeo);

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
        PointLight pl = new PointLight();
        pl.setColor(ColorRGBA.Yellow);
        pl.setRadius(4f);
        rootNode.addLight(pl);
        Node rshoulder = AnimUtils.getAttachments(player, "Armature_mixamorig:" + MixamoBodyBones.RightShoulder);
        rshoulder.addControl(new LightControl(pl, LightControl.ControlDirection.SpatialToLight));

        // setup Physics Character
        BetterCharacterControl bcc = new BetterCharacterControl(.4f, 1.8f, 10f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);

        setupAnimator(player);

        // setup Player Movement Control
        PlayerMovementControl pControl = new PlayerMovementControl(this);
        player.addControl(pControl);
    }

    private void setupAnimator(Spatial player) {
        // setup Animation System
        AnimComposer animComposer = AnimUtils.getAnimControl(player);

        AnimatorController animator = new AnimatorController(animComposer);
        animator.addParameter("vSpeed", AnimatorControllerParameterType.Float);
        animator.addParameter("hSpeed", AnimatorControllerParameterType.Float);
        player.addControl(animator);

        AnimatorStateMachine sm = animator.getLayer(0).getStateMachine();

        BlendTree tree = new BlendTree();
        tree.setBlendType(BlendTreeType.SimpleDirectional2D);
        // Configure the name of the parameter that controls the mixing of animations.
        tree.setBlendParameter("hSpeed");
        tree.setBlendParameterY("vSpeed");
        // Configure the animations and their position in 2D space
        tree.addChild(AnimDefs.IdleAiming, new Vector2f(0, 0));
        tree.addChild(AnimDefs.Walk_FW, new Vector2f(0, 1));
        tree.addChild(AnimDefs.Walk_BW, new Vector2f(0, -1));
        tree.addChild(AnimDefs.Walk_L, new Vector2f(-1, 0));
        tree.addChild(AnimDefs.Walk_R, new Vector2f(1, 0));
        tree.addChild(AnimDefs.Walk_FWL, new Vector2f(-1, 1));
        tree.addChild(AnimDefs.Walk_FWR, new Vector2f(1, 1));
        tree.addChild(AnimDefs.Walk_BWL, new Vector2f(-1, -1));
        tree.addChild(AnimDefs.Walk_BWR, new Vector2f(1, -1));
        // Create the state from the blend tree
        AnimatorState walk = sm.createBlendTree("BlendTree-Walk", tree);

        sm.setDefaultState(walk);
    }

    private void setupChaseCamera() {
        // disable the default 1st-person flyCam!
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setLookAtOffset(new Vector3f(0, 1.5f, 0));
        chaseCam.setMinDistance(5);
        chaseCam.setMaxDistance(8);
        chaseCam.setRotationSpeed(2f);
        chaseCam.setMinVerticalRotation(-FastMath.QUARTER_PI);
        chaseCam.setMaxVerticalRotation(FastMath.QUARTER_PI);
        chaseCam.setDownRotateOnCloseViewOnly(false);

        chaseCam.setDefaultDistance(chaseCam.getMinDistance());
    }

    private interface AnimDefs {

        final String MODEL = "Models/Rifle/character.gltf";
        final String Walk_R = "Move_R";
        final String Walk_L = "Move_L";
        final String Walk_FWR = "Move_FR";
        final String Walk_FWL = "Move_FL";
        final String Walk_FW = "Move_FW";
        final String Walk_BWR = "Move_BR";
        final String Walk_BWL = "Move_BL";
        final String Walk_BW = "Move_BW";
        final String Idle = "Idle_SA";
        final String IdleAiming = "IdleAiming_SA";
        final String IdleCrouching = "IdleCrouching";
        final String IdleCrouchingAiming = "IdleCrouchingAiming";
        final String TPose = "TPose";

    }

    private class PlayerMovementControl extends AbstractControl implements ActionListener, AnalogListener {

        public float m_MoveSpeed = 1.8f;
        public float m_TurnSpeed = 2.5f;

        private Camera camera;
        private AnimatorController animator;
        private BetterCharacterControl bcc;
        private final Vector3f walkDirection = new Vector3f();
        private final Vector3f viewDirection = new Vector3f(0, 0, 1);
        private boolean _MoveForward, _MoveBackward, _StrafeLeft, _StrafeRight;

        /**
         * Constructor.
         * @param app
         */
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
            if (!enabled)
                return;

            if (name.equals(InputMapping.MOVE_FORWARD)) {
                _MoveForward = isPressed;
            } else if (name.equals(InputMapping.MOVE_BACKWARD)) {
                _MoveBackward = isPressed;
            } else if (name.equals(InputMapping.MOVE_LEFT)) {
                _StrafeLeft = isPressed;
            } else if (name.equals(InputMapping.MOVE_RIGHT)) {
                _StrafeRight = isPressed;
            }
        }

        @Override
        protected void controlUpdate(float tpf) {
            // update physics
            walkDirection.set(0, 0, 0);
            float hSpeed = 0;
            float vSpeed = 0;

            if (_MoveForward || _MoveBackward) {
                Vector3f dz = spatial.getWorldRotation().mult(Vector3f.UNIT_Z);
                vSpeed = _MoveForward ? 1 : -1;
                walkDirection.addLocal(dz.multLocal(vSpeed));
            }

            if (_StrafeLeft || _StrafeRight) {
                Vector3f dx = spatial.getWorldRotation().mult(Vector3f.UNIT_X);
                hSpeed = _StrafeLeft ? -1 : 1;
                walkDirection.addLocal(dx.multLocal(-hSpeed));
            }

            walkDirection.normalizeLocal();
            bcc.setWalkDirection(walkDirection.multLocal(m_MoveSpeed));

            // update view direction
            Vector3f lookDir = camera.getDirection();
            lookDir.y = 0;
            lookDir.normalizeLocal();
            Quaternion lookRotation = FRotator.lookRotation(lookDir);
            spatial.getLocalRotation().slerp(lookRotation, m_TurnSpeed * tpf);
            spatial.getLocalRotation().mult(Vector3f.UNIT_Z, viewDirection);
            bcc.setViewDirection(viewDirection);

            // update animation
            animator.setFloat("hSpeed", hSpeed);
            animator.setFloat("vSpeed", vSpeed);
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
    }

    private class PlayerInputAppState extends BaseAppState implements AnalogListener, ActionListener {

        private InputManager inputManager;
        private PlayerMovementControl playerControl;

        @Override
        protected void initialize(Application app) {
            this.inputManager = app.getInputManager();
            addInputMappings();
        }

        @Override
        protected void cleanup(Application app) {}

        @Override
        protected void onEnable() {}

        @Override
        protected void onDisable() {}

        private void addInputMappings() {
            addMapping(InputMapping.MOVE_FORWARD, 	new KeyTrigger(KeyInput.KEY_W));
            addMapping(InputMapping.MOVE_BACKWARD, 	new KeyTrigger(KeyInput.KEY_S));
            addMapping(InputMapping.MOVE_LEFT, 		new KeyTrigger(KeyInput.KEY_A));
            addMapping(InputMapping.MOVE_RIGHT, 	new KeyTrigger(KeyInput.KEY_D));
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