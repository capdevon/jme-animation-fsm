package com.capdevon.demo;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.capdevon.control.PlayerBaseControl;
import com.capdevon.physx.Physics;
import com.capdevon.physx.PhysxQuery;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.environment.util.BoundingSphereDebug;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

/**
 *
 * @author capdevon
 */
public class Test_OverlappingSphere extends SimpleApplication implements ActionListener {
    
    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Test_OverlappingSphere app = new Test_OverlappingSphere();
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
    private GhostControl ghostControl;
    
    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        
        initPhysics();
        createFloor();
        createCharacters(5);
        createBoxItems();
        setupPlayer();
        setupChaseCamera();
        setupKeys();
    }
    
    /**
     * Initialize the physics simulation
     *
     */
    public void initPhysics() {
        physics = new BulletAppState();
        //physics.setThreadingType(ThreadingType.SEQUENTIAL);
        stateManager.attach(physics);

        physics.getPhysicsSpace().setAccuracy(0.01f); // 10-msec timestep
        physics.getPhysicsSpace().getSolverInfo().setNumIterations(15);
        physics.setDebugAxisLength(1);
        physics.setDebugEnabled(true);
    }
    
    private Material getUnshadedMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }
    
    private void createFloor() {
        Box box = new Box(20, 0.2f, 20);
        Geometry floorGeo = new Geometry("Floor.GeoMesh", box);
        floorGeo.setMaterial(getUnshadedMaterial(ColorRGBA.DarkGray));
        rootNode.attachChild(floorGeo);

        CollisionShape collShape = CollisionShapeFactory.createMeshShape(floorGeo);
        RigidBodyControl rBody = new RigidBodyControl(collShape, 0f);
        floorGeo.addControl(rBody);
        physics.getPhysicsSpace().add(rBody);
    }
    
    private void createCharacters(float radius) {
        for (int i = 1; i <= 8; i++) {
            float x = FastMath.sin(FastMath.QUARTER_PI * i) * radius;
            float z = FastMath.cos(FastMath.QUARTER_PI * i) * radius;

            Node node = new Node("Character." + i);
            node.attachChild(createLabel("Ch" + i, ColorRGBA.Green));
            node.setLocalTranslation(x, 1f, z);
            rootNode.attachChild(node);

            BetterCharacterControl bcc = new BetterCharacterControl(.5f, 2f, 40f);
            node.addControl(bcc);
            physics.getPhysicsSpace().add(bcc);
            //bcc.getRigidBody().setDebugMaterial(getUnshadedMaterial(ColorRGBA.Red));
            bcc.getRigidBody().setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        }
    }

    private void createBoxItems() {

        int nFragments = 15;
        float halfExtent = 0.4f;

        // Common fields
        Material mat = getUnshadedMaterial(ColorRGBA.randomColor());
        Box mesh = new Box(halfExtent, halfExtent, halfExtent);
        CollisionShape collShape = new BoxCollisionShape(halfExtent);

        for (int i = 0; i < nFragments; i++) {
            Node node = new Node("Box." + i);
            Geometry geo = new Geometry("Box.GeoMesh." + i, mesh);
            geo.setMaterial(mat);
            node.attachChild(geo);
            node.attachChild(createLabel("Bx" + i, ColorRGBA.Red));
            node.setLocalTranslation(getRandomPoint(10).setY(4));
            rootNode.attachChild(node);

            RigidBodyControl rBody = new RigidBodyControl(collShape, 15f);
            node.addControl(rBody);
            physics.getPhysicsSpace().add(rBody);
            rBody.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            rBody.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        }
    }
    
    private Vector3f getRandomPoint(int radius) {
        int dx = FastMath.nextRandomInt(-radius, radius);
        int dz = FastMath.nextRandomInt(-radius, radius);
        return new Vector3f(dx, 0, dz);
    }

    private Spatial createLabel(String text, ColorRGBA color) {
        BitmapText bmp = new BitmapText(guiFont);
        bmp.setText(text);
        bmp.setColor(color);
        bmp.setSize(1);
        bmp.setBox(new Rectangle((-bmp.getLineWidth() / 2) * bmp.getSize(), 0f, bmp.getLineWidth() * bmp.getSize(), bmp.getLineHeight()));
        bmp.setQueueBucket(RenderQueue.Bucket.Transparent);
        bmp.setAlignment(BitmapFont.Align.Center);
        bmp.addControl(new BillboardControl());

        Node label = new Node("Label");
        label.attachChild(bmp);
        label.setLocalTranslation(0, 2, 0);
        label.scale(0.5f);
        
        return label;
    }
    
    private void setupPlayer() {
        Geometry body = new Geometry("CollisionBox", new WireBox(radius, radius, radius));
        body.setMaterial(getUnshadedMaterial(ColorRGBA.Black));
        
        Geometry sphere = BoundingSphereDebug.createDebugSphere(assetManager);
        sphere.setLocalScale(radius);
        
        player = new Node("Player");
        player.attachChild(body);
        player.attachChild(sphere);
        rootNode.attachChild(player);
        
        BetterCharacterControl bcc = new BetterCharacterControl(.5f, 2f, 40f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        
//        ghostControl = new GhostControl(new SphereCollisionShape(radius));
//        player.addControl(ghostControl);
//        physics.getPhysicsSpace().add(ghostControl);
//        ghostControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_04);
//        ghostControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
        
        PlayerBaseControl baseControl = new PlayerBaseControl(this);
        player.addControl(baseControl);
    }

    private void setupChaseCamera() {
        // disable the default 1st-person flyCam!
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        //Uncomment this to look 2 world units above the target
        chaseCam.setLookAtOffset(Vector3f.UNIT_Y.mult(2));
        chaseCam.setMaxDistance(15);
        chaseCam.setMinDistance(6);
        chaseCam.setRotationSpeed(4f);
    }
    
    private void setupKeys() {
        addMapping("checkSphere", new KeyTrigger(KeyInput.KEY_SPACE));
        addMapping("overlapSphere", new KeyTrigger(KeyInput.KEY_RETURN));
        addMapping("TogglePhysxDebug", new KeyTrigger(KeyInput.KEY_0));
    }
    
    private void addMapping(String mappingName, Trigger... triggers) {
        inputManager.addMapping(mappingName, triggers);
        inputManager.addListener(this, mappingName);
    }
    
    private float radius = 2.5f;
    private final int LAYER_1 = PhysicsCollisionObject.COLLISION_GROUP_01;
    private final int LAYER_2 = PhysicsCollisionObject.COLLISION_GROUP_02;
    private final Predicate<PhysicsRigidBody> dynamicObjects = (x) -> x.getMass() > 0;
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        //To change body of generated methods, choose Tools | Templates.
        
        if (name.equals("TogglePhysxDebug") && isPressed) {
            boolean debugEnabled = physics.isDebugEnabled();
            physics.setDebugEnabled(!debugEnabled);
            
        } else if (name.equals("overlapSphere") && isPressed) {

            System.out.println("\n--OverlapSphere with ContactTest:");
            Set<PhysicsCollisionObject> set = Physics.overlapSphere(player.getWorldTranslation(), radius, LAYER_2);
            for (PhysicsCollisionObject pco : set) {
                printDetails(pco);
            }

        } else if (name.equals("checkSphere") && isPressed) {

            System.out.println("\n--CheckSphere with Math:");
            List<PhysicsRigidBody> lst = PhysxQuery.checkSphere(player.getWorldTranslation(), radius); // LAYER_1, dynamicObjects));
            for (PhysicsRigidBody pco : lst) {
                printDetails(pco);
            }
        }
    }
    
    private void printDetails(PhysicsCollisionObject pco) {
        System.out.printf("Class: %s, UserObj: %s, CollisionGroup: %d %n",
                pco.getClass().getSimpleName(),
                pco.getUserObject().toString(),
                pco.getCollisionGroup());
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //To change body of generated methods, choose Tools | Templates.
    }
    
}
