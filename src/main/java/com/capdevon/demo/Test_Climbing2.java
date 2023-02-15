package com.capdevon.demo;

import static com.jme3.anim.tween.Tweens.callMethod;
import static com.jme3.anim.tween.Tweens.parallel;
import static com.jme3.anim.tween.Tweens.sequence;

import com.capdevon.anim.MixamoBodyBones;
import com.capdevon.debug.DebugShapes;
import com.capdevon.demo.control.AdapterControl;
import com.capdevon.engine.FVector;
import com.capdevon.physx.Physics;
import com.capdevon.physx.RaycastHit;
import com.capdevon.physx.TogglePhysicsDebugState;
import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.AnimTrack;
import com.jme3.anim.Joint;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.TransformTrack;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.action.BaseAction;
import com.jme3.app.Application;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.debug.DebugTools;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;

import jme3utilities.MyAnimation;

/**
 * @author capdevon
 * @author AliRS
 */
public class Test_Climbing2 extends SimpleApplication {

    /**
     * @param args
     */
    public static void main(String[] args) {

        Test_Climbing2 app = new Test_Climbing2();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);

        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    private BulletAppState physics;
    private Node scene;
    private Node player;
    private final String CHARACTER_MODEL = "Models/YBot/YBot.j3o";
    private final String SCENE_MODEL = "Scenes/scene.gltf";
    
    private interface AnimDefs {
        final String Idle       = "Idle";
        final String Running    = "Running";
        final String Sprinting  = "Sprinting";
        final String Climbing   = "Climbing";
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        initPhysics();
//        setupSky();
        setupScene();
        setupPlayer();
        setupLights();
    }

    private void initPhysics() {
        physics = new BulletAppState();
        stateManager.attach(physics);
        physics.setDebugEnabled(true);

        // press 0 to toggle physics debug
        stateManager.attach(new TogglePhysicsDebugState());
    }

    /**
     * a sky as background
     */
    private void setupSky() {
        Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", SkyFactory.EnvMapType.CubeMap);
        sky.setShadowMode(RenderQueue.ShadowMode.Off);
        rootNode.attachChild(sky);
    }

    private void setupScene() {
        scene = (Node) assetManager.loadModel(SCENE_MODEL);
        rootNode.attachChild(scene);

        CollisionShape shape = CollisionShapeFactory.createMeshShape(scene);
        RigidBodyControl rgb = new RigidBodyControl(shape, 0f);
        scene.addControl(rgb);
        physics.getPhysicsSpace().add(rgb);
    }

    private void setupLights() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.clone());
        rootNode.addLight(ambient);
        ambient.setName("ambient");

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, 0.5f).normalizeLocal());
        sun.setColor(ColorRGBA.White.clone());
        rootNode.addLight(sun);
        sun.setName("sun");

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);

        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 2_048, 3);
        shadowFilter.setLight(sun);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);
        fpp.addFilter(shadowFilter);
    }

    private void setupPlayer() {
        DebugShapes debugShapes = new DebugShapes(assetManager);

        //
        player = new Node("Player");
        player.attachChild(debugShapes.drawAxis());
        player.setLocalTranslation(0, 1, -1);
        rootNode.attachChild(player);

        // vertical
        Node ledgeRayV = new Node("LedgeRayV");
        ledgeRayV.attachChild(debugShapes.drawWireCube(0.1f, ColorRGBA.Red));
        player.attachChild(ledgeRayV);
        ledgeRayV.setLocalTranslation(FVector.forward(player).multLocal(0.5f).addLocal(0, 3, 0));

        // horizontal
        Node ledgeRayH = new Node("LedgeRayH");
        ledgeRayH.attachChild(debugShapes.drawWireCube(0.1f, ColorRGBA.Blue));
        player.attachChild(ledgeRayH);
        ledgeRayH.setLocalTranslation(FVector.forward(player).multLocal(0.2f).addLocal(0, 1.5f, 0));

        // setup model
        Spatial model = assetManager.loadModel(CHARACTER_MODEL);
        model.setName("Character.Model");
        player.attachChild(model);

        // setup physics character
        BetterCharacterControl bcc = new BetterCharacterControl(.4f, 1.8f, 40f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);

        // Setup root motion physics control
        RigidBodyControl rbc = new RigidBodyControl(bcc.getRigidBody().getCollisionShape());
        // Kinematic mode must be enabled so character is not influenced by physics
        rbc.setKinematic(true);
        // Apply spatial transform to the collision shape
        rbc.setKinematicSpatial(true);
        // Adding it to the same node that root motion is applied to.
        model.addControl(rbc);
        // Disable it. Will be enabled when playing root motion animation.
        rbc.setEnabled(false);
        rbc.setPhysicsSpace(physics.getPhysicsSpace());

        // setup third person camera
        setupChaseCamera(model);

        Geometry rootBoneRef = debugShapes.drawWireCube(0.2f, ColorRGBA.White);
        rootNode.attachChild(rootBoneRef);

        // setup player control
        PlayerControl pControl = new PlayerControl(this);
        pControl.ledgeRayH = ledgeRayH;
        pControl.ledgeRayV = ledgeRayV;
        pControl.model = model;
        pControl.rootBoneRef = rootBoneRef;
        player.addControl(pControl);
    }

    private void setupChaseCamera(Spatial target) {
        // disable the default 1st-person flyCam!
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, target, inputManager);
        chaseCam.setUpVector(Vector3f.UNIT_Y.clone());
        chaseCam.setLookAtOffset(new Vector3f(0f, 1f, 0f));
        chaseCam.setMaxDistance(8f);
        chaseCam.setMinDistance(5f);
        chaseCam.setDefaultDistance(chaseCam.getMaxDistance());
        chaseCam.setMaxVerticalRotation(FastMath.QUARTER_PI);
        chaseCam.setMinVerticalRotation(-FastMath.QUARTER_PI);
        chaseCam.setRotationSpeed(2f);
        chaseCam.setRotationSensitivity(1.5f);
        chaseCam.setZoomSensitivity(4f);
        chaseCam.setDownRotateOnCloseViewOnly(false);
    }

    /**
     * ---------------------------------------------------------
     * @class PlayerControl
     * ---------------------------------------------------------
     */
    private class PlayerControl extends AdapterControl implements ActionListener {

        public Node ledgeRayV;
        public Node ledgeRayH;
        public Spatial model;
        public Geometry rootBoneRef;

        private Camera camera;
        private DebugTools debugTools;
        private InputManager inputManager;
        private AnimComposer animComposer;
        private BetterCharacterControl bcc;
        private RigidBodyControl rbc;

        private final Vector3f walkDirection = new Vector3f(0, 0, 0);
        private final Vector3f viewDirection = new Vector3f(0, 0, 1);
        private final Vector3f camDir = new Vector3f();
        private final Vector3f camLeft = new Vector3f();
        private final Quaternion lookRotation = new Quaternion();
        private final RaycastHit hitInfo = new RaycastHit();

        private float m_MoveSpeed = 4.5f;
        private float m_TurnSpeed = 10f;
        private boolean _MoveForward, _MoveBackward, _MoveLeft, _MoveRight;
        private boolean isClimbingMode, startClimb;
        private boolean isClimbingAnimDone = true;
        private TransformTrack hipsTrack;
        private Transform rootMotion = new Transform();

        /**
         * Constructor.
         *
         * @param app
         */
        public PlayerControl(Application app) {
            this.camera = app.getCamera();
            this.debugTools = new DebugTools(app.getAssetManager());
            registerWithInput(app.getInputManager());
        }

        @Override
        public void setSpatial(Spatial sp) {
            super.setSpatial(sp);
            if (spatial != null) {
                this.bcc = getComponent(BetterCharacterControl.class);
                this.rbc = getComponentInChildren(RigidBodyControl.class);
                this.animComposer = getComponentInChildren(AnimComposer.class);

                // setup animations
                animComposer.getAnimClipsNames().forEach(animName -> animComposer.action(animName));

                SkinningControl skeleton = getComponentInChildren(SkinningControl.class);
                skeleton.getArmature().applyBindPose();
                Joint hips = skeleton.getArmature().getJoint("mixamorig:" + MixamoBodyBones.Hips);
                Vector3f hipsOrigin = hips.getModelTransform().getTranslation().clone();
                AnimClip climbing = animComposer.getAnimClip(AnimDefs.Climbing);
                hipsTrack = MyAnimation.findJointTrack(climbing, hips.getId());

                AnimTrack[] tracks = climbing.getTracks();
                for (int i = 0; i < tracks.length; i++) {
                    if (tracks[i] == hipsTrack) {
                        // Convert it to an in-place animation by removing translations data
                        tracks[i] = new TransformTrack(hipsTrack.getTarget(), hipsTrack.getTimes(), null, hipsTrack.getRotations(), hipsTrack.getScales());
                    }
                }

                Vector3f[] translations = hipsTrack.getTranslations();
                for (Vector3f translation : translations) {
                    // Because hip origin(0.0, 0.99, 0.002) and model origin(0, 0, 0) is not coincide,
                    // we must translate it back to model origin
                    translation.subtractLocal(hipsOrigin);
                }
                // Create a root motion track for "model" node.
                // Note, applying it to "player" node will cause a strange side effect. Probably because BCC is also controlling it!
                TransformTrack climbingRootMotionTrack = new TransformTrack(model, hipsTrack.getTimes(), translations, null, null);

                animComposer.addAction(AnimDefs.Climbing, new BaseAction(
                        sequence(
                                // Activate root motion physics
                                callMethod(bcc, "setEnabled", false),
                                callMethod(rbc, "setEnabled", true),
                                parallel(
                                        animComposer.getAction(AnimDefs.Climbing),
                                        new RootMotion(climbingRootMotionTrack, true)
                                ),
                                // Deactivate root motion physics
                                callMethod(rbc, "setEnabled", false),
                                callMethod(bcc, "setEnabled", true),
                                // Make sure to remove our self now
                                callMethod(animComposer, "removeCurrentAction"),
                                callMethod(this, "onClimbingDone")
                        )));
            }
        }

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(InputMapping.MOVE_LEFT)) {
                _MoveLeft = isPressed;
            } else if (name.equals(InputMapping.MOVE_RIGHT)) {
                _MoveRight = isPressed;
            } else if (name.equals(InputMapping.MOVE_FORWARD)) {
                _MoveForward = isPressed;
            } else if (name.equals(InputMapping.MOVE_BACKWARD)) {
                _MoveBackward = isPressed;
            } else if (name.equals(InputMapping.ACTION) && isPressed && isClimbingAnimDone) {
                checkLedgeGrab();
            }
        }

        @Override
        protected void controlUpdate(float tpf) {
            if (!isClimbingMode) {
                // Player is in NORMAL state
                updateMovement(tpf);

            } else {
                // Player is in CLIMBING state
                if (startClimb && !isClimbingAnimDone) {
                    // align with wall
                    //spatial.getWorldRotation().slerp(helper.getRotation(), tpf * 5);

//                    hipsTrack.getDataAtTime(animComposer.getTime(), rootMotion);
//                    Vector3f vec = animComposer.getSpatial().localToWorld(rootMotion.getTranslation(), null);
//                    rootBoneRef.setLocalTranslation(vec);
//                    rootBoneRef.setLocalRotation(rootMotion.getRotation());

                } else if (isClimbingAnimDone) {
                    isClimbingMode = false;
                    startClimb = false;
                    //spatial.setLocalTranslation(goalPosition);

                    bcc.warp(goalPosition);
                }
            }
        }

        private float hDistAwayFromLedge = 0.1f;
        private float vDistAwayFromLedge = 0.1f;
        private Transform helper = new Transform();
        private Vector3f goalPosition = new Vector3f();

        private void checkLedgeGrab() {

            if (!isClimbingMode && bcc.isOnGround()) {

                Ray vRay = new Ray(ledgeRayV.getWorldTranslation(), Vector3f.UNIT_Y.negate());
                debugTools.setRedArrow(vRay.getOrigin(), vRay.getDirection());

                if (Physics.raycast(vRay, hitInfo, 2)) {

                    System.out.println(hitInfo);
                    Vector3f hRayPosition = ledgeRayH.getWorldTranslation().clone();
                    hRayPosition.setY(hitInfo.point.y - 0.01f);

                    Ray hRay = new Ray(hRayPosition, ledgeRayH.getWorldRotation().mult(Vector3f.UNIT_Z));
                    debugTools.setBlueArrow(hRay.getOrigin(), hRay.getDirection());

                    if (Physics.raycast(hRay, hitInfo, 2)) {
                        System.out.println(hitInfo);
                        debugTools.setPinkArrow(hitInfo.point, hitInfo.normal);

                        goalPosition.set(hitInfo.point.add(0, 0.01f, 0));

                        bcc.setViewDirection(hitInfo.normal.negate()); // align with wall
                        bcc.setWalkDirection(Vector3f.ZERO); // stop walking

                        //helper.setTranslation(hitInfo.normal.negate().multLocal(hDistAwayFromLedge).addLocal(spatial.getWorldTranslation()));
                        //helper.getTranslation().setY(hitInfo.point.y - vDistAwayFromLedge);
                        //helper.setRotation(FRotator.lookRotation(hitInfo.normal.negate()));
                        setAnimation(AnimDefs.Climbing);

                        isClimbingMode = true;
                        startClimb = true;
                        isClimbingAnimDone = false;
                        System.out.println("startClimbing");
                    }
                }
            } else {
                isClimbingMode = false;
            }
        }

        void onClimbingDone() {
            isClimbingAnimDone = true;
            System.out.println("climbingDone");
        }

        private void updateMovement(float tpf) {

            camera.getDirection(camDir).setY(0);
            camera.getLeft(camLeft).setY(0);
            walkDirection.set(0, 0, 0);

            if (_MoveForward) {
                walkDirection.addLocal(camDir);
            } else if (_MoveBackward) {
                walkDirection.addLocal(camDir.negateLocal());
            }

            if (_MoveLeft) {
                walkDirection.addLocal(camLeft);
            } else if (_MoveRight) {
                walkDirection.addLocal(camLeft.negateLocal());
            }

            walkDirection.normalizeLocal();
            boolean isMoving = walkDirection.lengthSquared() > 0;

            if (isMoving) {
                float angle = FastMath.atan2(walkDirection.x, walkDirection.z);
                lookRotation.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
                spatial.getWorldRotation().slerp(lookRotation, m_TurnSpeed * tpf);
                spatial.getWorldRotation().mult(Vector3f.UNIT_Z, viewDirection);
                bcc.setViewDirection(viewDirection);
            }

            bcc.setWalkDirection(walkDirection.multLocal(m_MoveSpeed));
            setAnimation(isMoving ? AnimDefs.Running : AnimDefs.Idle);
        }

        private void setAnimation(String animName) {
            if (animComposer.getCurrentAction() != animComposer.getAction(animName)) {
                animComposer.setCurrentAction(animName);
            }
        }

        private void stopMove() {
            _MoveForward   = false;
            _MoveBackward  = false;
            _MoveLeft      = false;
            _MoveRight     = false;
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
            if (debugTools != null) {
                debugTools.show(rm, vp);
            }
        }

        /**
         * Custom Keybinding: Map named actions to inputs.
         */
        private void registerWithInput(InputManager inputManager) {
            this.inputManager = inputManager;

            addMapping(InputMapping.MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_W));
            addMapping(InputMapping.MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
            addMapping(InputMapping.MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
            addMapping(InputMapping.MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
            addMapping(InputMapping.ACTION, new KeyTrigger(KeyInput.KEY_SPACE));
        }

        private void addMapping(String mapping, Trigger... triggers) {
            inputManager.addMapping(mapping, triggers);
            inputManager.addListener(this, mapping);
        }

    }

    private interface InputMapping {

        final String MOVE_LEFT = "MOVE_LEFT";
        final String MOVE_RIGHT = "MOVE_RIGHT";
        final String MOVE_FORWARD = "MOVE_FORWARD";
        final String MOVE_BACKWARD = "MOVE_BACKWARD";
        final String ACTION = "ACTION";
    }

    private static class RootMotion implements Tween {

        private final TransformTrack track;
        private final Spatial spatial;
        private final Transform transform = new Transform();

        private boolean resetLocation;
        private Vector3f startLoc;

        /**
         *
         * @param track
         * @param resetLocation if true, then at the end of the animation it
         * will reset the spatial location to where it was at the beginning.
         */
        public RootMotion(TransformTrack track, boolean resetLocation) {
            this.track = track;
            if (!(track.getTarget() instanceof Spatial)) {
                throw new IllegalArgumentException("Target of root motion track must be a spatial.");
            }

            this.spatial = (Spatial) track.getTarget();
            this.resetLocation = resetLocation;
        }

        @Override
        public double getLength() {
            return track.getLength();
        }

        @Override
        public boolean interpolate(double t) {
            if (t > getLength()) {
                if (resetLocation) {
                    spatial.setLocalTranslation(startLoc);
                }

                startLoc = null;
                return false;
            }

            if (startLoc == null) {
                startLoc = spatial.getLocalTranslation().clone();
            }

            track.getDataAtTime(t, transform);
            Vector3f newLocation = startLoc.add(transform.getTranslation());
            spatial.setLocalTranslation(newLocation);
            return true;
        }
    }

}