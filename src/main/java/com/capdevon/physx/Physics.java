package com.capdevon.physx;

import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;

public class Physics {
    
    public static void addObject(Spatial sp) {
        PhysicsSpace.getPhysicsSpace().add(sp);
    }

    public static void addControl(PhysicsControl control) {
        PhysicsSpace.getPhysicsSpace().add(control);
    }

    public static void addCollisionListener(PhysicsCollisionListener listener) {
        PhysicsSpace.getPhysicsSpace().addCollisionListener(listener);
    }

    public static void addTickListener(PhysicsTickListener listener) {
        PhysicsSpace.getPhysicsSpace().addTickListener(listener);
    }
 
    /**
     * 
     * @param spatial
     * @param radius
     * @param height
     * @param mass 
     */
    public static void addCapsuleCollider(Spatial spatial, float radius, float height, float mass) {
        BetterCharacterControl bcc = new BetterCharacterControl(radius, height, mass);
        spatial.addControl(bcc);
        PhysicsSpace.getPhysicsSpace().add(bcc);
    }

    public static void addCapsuleCollider(Spatial spatial) {
        BoundingBox bb = (BoundingBox) spatial.getWorldBound();
        float radius = Math.min(bb.getXExtent(), bb.getZExtent());
        float height = Math.max(bb.getYExtent(), radius * 2.5f);
        float mass = 50f;
        addCapsuleCollider(spatial, radius, height, mass);
    }

    public static void addBoxCollider(Spatial sp, float mass, boolean isKinematic) {
        BoundingBox bb = (BoundingBox) sp.getWorldBound();
        BoxCollisionShape box = new BoxCollisionShape(bb.getExtent(null));
        addRigidBody(box, sp, mass, isKinematic);
    }

    public static void addSphereCollider(Spatial sp, float mass, boolean isKinematic) {
        BoundingSphere bs = (BoundingSphere) sp.getWorldBound();
        SphereCollisionShape sphere = new SphereCollisionShape(bs.getRadius());
        addRigidBody(sphere, sp, mass, isKinematic);
    }

    public static void addMeshCollider(Spatial sp, float mass, boolean isKinematic) {
        CollisionShape shape = CollisionShapeFactory.createMeshShape(sp);
        addRigidBody(shape, sp, mass, isKinematic);
    }

    public static void addDynamicMeshCollider(Spatial sp, float mass, boolean isKinematic) {
        CollisionShape shape = CollisionShapeFactory.createDynamicMeshShape(sp);
        addRigidBody(shape, sp, mass, isKinematic);
    }

    public static void addRigidBody(CollisionShape shape, Spatial sp, float mass, boolean isKinematic) {
        RigidBodyControl rgb = new RigidBodyControl(shape, mass);
        sp.addControl(rgb);
        rgb.setKinematic(isKinematic);
        PhysicsSpace.getPhysicsSpace().add(rgb);
    }
    
    /**
     * 
     * @param origin
     * @param direction
     * @param hitInfo
     * @param distance
     * @return 
     */
    public static boolean doRaycast(Vector3f origin, Vector3f direction, RaycastHit hitInfo, float distance) {

    	boolean collision = false;
        float hf = distance;

        TempVars t = TempVars.get();
        Vector3f beginVec = t.vect1.set(origin);
        Vector3f finalVec = t.vect2.set(direction).multLocal(distance).addLocal(origin);

        List<PhysicsRayTestResult> results = PhysicsSpace.getPhysicsSpace().rayTest(beginVec, finalVec);
        for (PhysicsRayTestResult ray : results) {
        	
        	PhysicsCollisionObject pco = ray.getCollisionObject();
        	System.out.println(pco);
        	
            if (pco instanceof GhostControl) {
                continue;
            }
            
            if (ray.getHitFraction() < hf) {
            	collision = true;
                hf = ray.getHitFraction();
                
                hitInfo.rigidbody 	= pco;
                hitInfo.collider 	= pco.getCollisionShape();
                hitInfo.userObject 	= pco.getUserObject();
                hitInfo.distance 	= finalVec.subtract(beginVec).length() * hf;
                hitInfo.normal.set(ray.getHitNormalLocal());
                hitInfo.point.interpolateLocal(beginVec, finalVec, hf);
//                FastMath.interpolateLinear(hf, beginVec, finalVec, hitInfo.point);
            }
        }

        if (!collision) {
        	// hitInfo.clear(); ???
        }

        t.release();
        return collision;
    }
    
    /**
     * 
     * @param beginVec
     * @param finalVec
     * @param hitInfo
     * @return 
     */
    public static boolean doRaycast(Vector3f beginVec, Vector3f finalVec, RaycastHit hitInfo) {

    	boolean collision = false;
        float hf = Float.MAX_VALUE;

        List<PhysicsRayTestResult> results = PhysicsSpace.getPhysicsSpace().rayTest(beginVec, finalVec);
        for (PhysicsRayTestResult ray : results) {
            
            PhysicsCollisionObject pco = ray.getCollisionObject();
            System.out.println(pco);
            
            if (pco instanceof GhostControl) {
                continue;
            }
            
            if (ray.getHitFraction() < hf) {
            	collision = true;
                hf = ray.getHitFraction();
                
                hitInfo.rigidbody 	= pco;
                hitInfo.collider 	= pco.getCollisionShape();
                hitInfo.userObject 	= pco.getUserObject();
                hitInfo.point 		= FastMath.interpolateLinear(hf, beginVec, finalVec);
                hitInfo.distance 	= finalVec.subtract(beginVec).length() * hf;
                hitInfo.normal		= ray.getHitNormalLocal();
            }
        }

        return collision;
    }
}
