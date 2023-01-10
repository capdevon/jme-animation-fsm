package com.capdevon.demo.util;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

/**
 * 
 * @author capdevon
 */
public class MeshBuilder {
	
    private static AssetManager assetManager;

    private MeshBuilder() {
        //private constructor.
    }

    public static void init(AssetManager assetManager) {
        MeshBuilder.assetManager = assetManager;
    }
	
    /**
     * Get default axes.
     *
     * @param id
     * @return 
     */
    public static Node createAxes(String id) {
        Node node = new Node(id);
        node.attachChild(createArrow("X", Vector3f.UNIT_X, ColorRGBA.Red));
        node.attachChild(createArrow("Y", Vector3f.UNIT_Y, ColorRGBA.Green));
        node.attachChild(createArrow("Z", Vector3f.UNIT_Z, ColorRGBA.Blue));
        return node;
    }

    /**
     * Get an arrow with name, dir and color.
     *
     * @param name
     * @param dir
     * @param color
     * @return
     */
    public static Geometry createArrow(String name, Vector3f dir, ColorRGBA color) {
        Arrow arrow = new Arrow(dir);
        Geometry geo = new Geometry(name, arrow);
        Material mat = new Material(assetManager, Materials.UNSHADED);
        mat.setColor("Color", color);
        geo.setMaterial(mat);
        return geo;
    }

    /**
     * Get a solid cube with color and size.
     *
     * @param color
     * @param size
     * @return
     */
    public static Geometry createCube(ColorRGBA color, Vector3f size) {
        Material mat = new Material(assetManager, Materials.UNSHADED);
        mat.setColor("Color", color);
        return createCube(mat, size);
    }
    
    public static Geometry createCube(Material mat, Vector3f size) {
        Box box = new Box(size.x, size.y, size.z);
        Geometry geo = new Geometry("Box.GeoMesh", box);
        geo.setMaterial(mat);
        return geo;
    }

    /**
     * Get a solid sphere with color and radius.
     *
     * @param color
     * @param radius
     * @return
     */
    public static Geometry createSphere(ColorRGBA color, float radius) {
        Material mat = new Material(assetManager, Materials.UNSHADED);
        mat.setColor("Color", color);
        return createSphere(mat, radius);
    }
    
    public static Geometry createSphere(Material mat, float radius) {
        Sphere sphere = new Sphere(6, 6, radius);
        Geometry geo = new Geometry("Sphere.GeoMesh", sphere);
        geo.setMaterial(mat);
        return geo;
    }
    
    public static Node createCapsule(ColorRGBA color, float radius, float height) {
        Node capsule = new Node("Capsule");
        capsule.setLocalTranslation(0, height/2, 0);
        buildCapsule(capsule, radius, height);
        Material mat = new Material(assetManager, Materials.UNSHADED);
        mat.setColor("Color", color);
        capsule.setMaterial(mat);
        return capsule;
    }

    private static void buildCapsule(Node node, float r, float h) {
        Geometry cylinder = new Geometry("Cylinder", new Cylinder(16, 16, r, h));
        Geometry top = new Geometry("Top.Sphere", new Sphere(16, 16, r));
        Geometry bottom = new Geometry("Bottom.Sphere", new Sphere(16, 16, r));
        cylinder.rotate(FastMath.HALF_PI, 0, 0);
        bottom.setLocalTranslation(0, -h / 2, 0);
        top.setLocalTranslation(0, h / 2, 0);
        node.attachChild(cylinder);
        node.attachChild(bottom);
        node.attachChild(top);
    }

}
