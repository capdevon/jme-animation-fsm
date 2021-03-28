package com.capdevon.demo;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author capdevon
 */
public class Test_CollisionGroups extends SimpleApplication implements ActionListener {

    /**
     * @param args 
     */
    public static void main(String[] args) {
        Test_CollisionGroups app = new Test_CollisionGroups();
        app.start();
    }
    
    private BulletAppState physics;
    private GhostControl ghostControl;
    
    @Override
    public void simpleInitApp() {
        cam.setLocation(Vector3f.UNIT_XYZ.mult(15f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(20f);
        
        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));
        
        initPhysics();
        setupScene();
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

        //physics.getPhysicsSpace().setAccuracy(1 / 60f);
        physics.getPhysicsSpace().setAccuracy(0.01f); // 10-msec timestep
        physics.getPhysicsSpace().getSolverInfo().setNumIterations(15);
        physics.setDebugAxisLength(1);
        physics.setDebugEnabled(true);
    }
    
    private void createFloor() {
        Box box = new Box(20, 0.2f, 20);
        box.scaleTextureCoordinates(new Vector2f(10, 10));
        Geometry floorGeo = new Geometry("Floor.GeoMesh", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.DarkGray);
        floorGeo.setMaterial(mat);
        rootNode.attachChild(floorGeo);

        CollisionShape collShape = CollisionShapeFactory.createMeshShape(floorGeo);
        RigidBodyControl rBody = new RigidBodyControl(collShape, 0f);
        floorGeo.addControl(rBody);
        physics.getPhysicsSpace().add(rBody);
    }

    private void setupScene() {
        createFloor();
        
        float radius = 5f;
        createCharacters(radius);
        createBoxItems(radius);
        createPlayer(radius);
    }
    
    private void createCharacters(float radius) {
        for (int i = 1; i <= 8; i++) {
            float x = FastMath.sin(FastMath.QUARTER_PI * i) * radius;
            float z = FastMath.cos(FastMath.QUARTER_PI * i) * radius;
            
            Node node = new Node("Character." + i);
            node.setLocalTranslation(x, 1f, z);
            rootNode.attachChild(node);
            
            BetterCharacterControl bcc = new BetterCharacterControl(.5f, 2f, 40f);
            node.addControl(bcc);
            physics.getPhysicsSpace().add(bcc);
            bcc.getRigidBody().setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        }
    }
    
    private void createBoxItems(float radius) {
        float halfExtent = 0.4f;
        Box mesh = new Box(halfExtent, halfExtent, halfExtent);
        CollisionShape collShape = new BoxCollisionShape(halfExtent);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.randomColor());
        
        for (int i = 1; i <= 8; i++) {
            float x = FastMath.sin(FastMath.QUARTER_PI * i) * radius/2;
            float z = FastMath.cos(FastMath.QUARTER_PI * i) * radius/2;

            Node cube = new Node("Box." + i);
            Geometry geo = new Geometry("Box.GeoMesh." + i, mesh);
            geo.setMaterial(mat);
            cube.attachChild(geo);
            cube.setLocalTranslation(x, 1f, z);
            rootNode.attachChild(cube);

            RigidBodyControl rBody = new RigidBodyControl(collShape, 25f);
            cube.addControl(rBody);
            physics.getPhysicsSpace().add(rBody);
            rBody.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_04);
        }
    }
    
    private void createPlayer(float radius) {
        Node player = new Node("Player");
        rootNode.attachChild(player);
        
        BetterCharacterControl bcc = new BetterCharacterControl(.5f, 2f, 40f);
        player.addControl(bcc);
        physics.getPhysicsSpace().add(bcc);
        
        ghostControl = new GhostControl(new SphereCollisionShape(radius));
        player.addControl(ghostControl);
        physics.getPhysicsSpace().add(ghostControl);
        ghostControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_06);
//        ghostControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
    }
        
    private void setupKeys() {
        addMapping("OverlapSphere", new KeyTrigger(KeyInput.KEY_RETURN));
        addMapping("TogglePhysxDebug", new KeyTrigger(KeyInput.KEY_0));
    }
    
    private void addMapping(String mappingName, Trigger... triggers) {
        inputManager.addMapping(mappingName, triggers);
        inputManager.addListener(this, mappingName);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        //To change body of generated methods, choose Tools | Templates.
        
        if (name.equals("TogglePhysxDebug") && isPressed) {
            boolean debugEnabled = physics.isDebugEnabled();
            physics.setDebugEnabled(!debugEnabled);
            
        } else if (name.equals("OverlapSphere") && isPressed) {
        	
            System.out.println("--getOverlappingObjects");
            for (PhysicsCollisionObject pco : ghostControl.getOverlappingObjects()) {
                String userObj = pco.getUserObject().toString();
                System.out.println("\t" + userObj);
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
