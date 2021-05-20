package com.capdevon.demo;

import java.util.LinkedList;

import com.capdevon.anim.AnimUtils;
import com.capdevon.anim.fsm.MyBlendAction;
import com.capdevon.anim.fsm.MyBlendSpace;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.action.BlendableAction;
import com.jme3.app.ChaseCameraAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.custom.ArmatureDebugAppState;
import com.jme3.system.AppSettings;

/**
 * 
 * @author capdevon
 */
public class Test_BlendActionLoop extends SimpleApplication {

    private ArmatureDebugAppState debugAppState;
    private AnimComposer animComposer;
    private final LinkedList<String> anims = new LinkedList<>();
    private boolean playAnim = false;
    private MyBlendAction action;
    private float blendValue = 1f;
    private float minThreshold = 1;
    private float maxThreshold = 4;

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_BlendActionLoop app = new Test_BlendActionLoop();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        settings.setFrameRate(60);
        settings.setSamples(4);
        settings.setBitsPerPixel(32);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 100f);
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
        rootNode.addLight(new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal()));
        rootNode.addLight(new AmbientLight(ColorRGBA.DarkGray));

        Spatial model = assetManager.loadModel("Models/Rifle/rifle.glb");
        //AnimMigrationUtils.migrate(model);
        rootNode.attachChild(model);

        debugAppState = new ArmatureDebugAppState();
        stateManager.attach(debugAppState);

        setupModel(model);

        flyCam.setEnabled(false);

        Node target = new Node("CamTarget");
        //target.setLocalTransform(model.getLocalTransform());
        target.move(0, 1, 0);
        ChaseCameraAppState chaseCam = new ChaseCameraAppState();
        chaseCam.setTarget(target);
        getStateManager().attach(chaseCam);
        chaseCam.setInvertHorizontalAxis(true);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setZoomSpeed(0.5f);
        chaseCam.setMinVerticalRotation(-FastMath.HALF_PI);
        chaseCam.setRotationSpeed(3);
        chaseCam.setDefaultDistance(3);
        chaseCam.setMinDistance(0.01f);
        chaseCam.setZoomSpeed(0.01f);
        chaseCam.setDefaultVerticalRotation(0.3f);

        initInputs();
    }

    private void initInputs() {

        inputManager.addMapping("toggleAnim", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    playAnim = !playAnim;
                    if (playAnim) {
                        String anim = anims.poll();
                        anims.add(anim);
                        animComposer.setCurrentAction(anim);
                        System.err.println(anim);
                    } else {
                        animComposer.reset();
                    }
                }
            }
        }, "toggleAnim");

        inputManager.addMapping("nextAnim", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed && animComposer != null) {
                    String anim = anims.poll();
                    anims.add(anim);
                    animComposer.setCurrentAction(anim);
                    System.err.println(anim);
                }
            }
        }, "nextAnim");

        inputManager.addMapping("toggleArmature", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(new ActionListener() {
            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                if (isPressed) {
                    debugAppState.setEnabled(!debugAppState.isEnabled());
                }
            }
        }, "toggleArmature");

        inputManager.addMapping("blendUp", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("blendDown", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addListener(new AnalogListener() {

            @Override
            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("blendUp")) {
                    blendValue += value;
                    blendValue = FastMath.clamp(blendValue, minThreshold, maxThreshold);
                    action.getBlendSpace().setValue(blendValue);
                    //action.setSpeed(blendValue);
                }
                if (name.equals("blendDown")) {
                    blendValue -= value;
                    blendValue = FastMath.clamp(blendValue, minThreshold, maxThreshold);
                    action.getBlendSpace().setValue(blendValue);
                    //action.setSpeed(blendValue);
                }
                System.err.println(blendValue);
            }
        }, "blendUp", "blendDown");
    }

    private void setupModel(Spatial model) {
        animComposer = AnimUtils.getAnimControl(model);
        for (String name: animComposer.getAnimClipsNames()) {
            anims.add(name);
        }

        MyBlendSpace blendSpace = new MyBlendSpace(minThreshold, maxThreshold);
        String[] clips = {
            AnimDefs.RifleWalk,
            AnimDefs.RifleRun
        };
        BlendableAction[] acts = new BlendableAction[clips.length];
        for (int i = 0; i < acts.length; i++) {
            BlendableAction ba = (BlendableAction) animComposer.makeAction(clips[i]);
            acts[i] = ba;
        }

        action = new MyBlendAction(blendSpace, acts);
        action.setTransitionLength(0);
        animComposer.addAction("Blend", action);
        action.getBlendSpace().setValue(minThreshold);

//        action = composer.actionBlended("Blend", new LinearBlendSpace(1, 4), "Walk", "Run");
//        action.setTransitionLength(0);
//        action.getBlendSpace().setValue(1);

        anims.addFirst("Blend");

        if (playAnim) {
            String anim = anims.poll();
            anims.add(anim);
            animComposer.setCurrentAction(anim);
            System.err.println(anim + " , lenght=" + animComposer.getAction(anim).getLength());
        }

        SkinningControl sc = AnimUtils.getSkeletonControl(model);
        debugAppState.addArmatureFrom(sc);
    }

    private interface AnimDefs {

        final String MODEL = "Models/Rifle/rifle.glb";
        final String RifleIdle = "RifleIdle";
        final String RifleWalk = "RifleWalk";
        final String RifleRun = "RifleRun";
        final String WalkWithRifle = "WalkWithRifle";
        final String ThrowGrenade = "ThrowGrenade";
        final String Reloading = "Reloading";
        final String RifleAimingIdle = "RifleAimingIdle";
        final String FiringRifleSingle = "FiringRifleSingle";
        final String FiringRifleAuto = "FiringRifleAuto";
        final String DeathFromRight = "DeathFromRight";
        final String DeathFromHeadshot = "DeathFromHeadshot";
        final String TPose = "TPose";

    }

}