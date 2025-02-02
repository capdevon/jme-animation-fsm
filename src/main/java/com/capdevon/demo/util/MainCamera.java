package com.capdevon.demo.util;

import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author capdevon
 */
public class MainCamera {
	
    /**
     * Z value for the far clipping plane (in screen coordinates)
     */
    private static final float farZ = 1f;
    /**
     * Z value for the near clipping plane (in screen coordinates)
     */
    private static final float nearZ = 0f;
    
    private Camera cam;
    private float fieldOfView;
    private float near;
    private float far;
    
    /**
     * Creates a camera state that will initialize the application camera to a
     * 45 degree fov, 0.1 near plane, and 1000 far plane.
     * 
     * @param cam
     */
    public MainCamera(Camera cam) {
        this(cam, 45, 0.1f, 1000); // 45 is the default JME fov
    }

    /**
     * Creates a camera state that will initialize the specified camera to the
     * specified parameters. If the specified camera is null then the
     * application's main camera will be used.
     * 
     * @param cam
     * @param fov
     * @param near
     * @param far
     */
    public MainCamera(Camera cam, float fov, float near, float far) {
        this.cam = cam;
        this.fieldOfView = fov;
        this.near = near;
        this.far = far;
        resetCamera();
    }
    
    public void setFieldOfView(float f) {
        if (this.fieldOfView == f) {
            return;
        }
        this.fieldOfView = f;
        resetCamera();
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public void setNear(float f) {
        if (this.near == f) {
            return;
        }
        this.near = f;
        resetCamera();
    }

    public float getNear() {
        return near;
    }

    public void setFar(float f) {
        if (this.far == f) {
            return;
        }
        this.far = f;
        resetCamera();
    }

    public float getFar() {
        return far;
    }
    
    private void resetCamera() {
        float aspect = (float) cam.getWidth() / (float) cam.getHeight();
        cam.setFrustumPerspective(fieldOfView, aspect, near, far);
    }
    
    /**
     * Returns a ray going from camera through a screen point.
     * usage is:
     * <pre>
     *     Ray ray = MainCamera.screenPointToRay(cam, inputManager.getCursorPosition());
     * </pre>
     */
    public static Ray screenPointToRay(Camera cam, Vector2f screenXY) {
        // Convert screen click to 3D position
        Vector3f nearPos = cam.getWorldCoordinates(screenXY, nearZ);
        Vector3f farPos = cam.getWorldCoordinates(screenXY, farZ);
        Vector3f dir = farPos.subtract(nearPos).normalizeLocal();
        // Aim the ray from the clicked spot forwards.
        Ray ray = new Ray(nearPos, dir);
        return ray;
    }

    /**
     * Transforms position from world space to local space.
     * 
     * @param cam
     * @param position
     * @return
     */
    public static Vector3f inverseTransformPoint(Camera cam, Vector3f position) {
        Transform tr = new Transform(cam.getLocation(), cam.getRotation(), Vector3f.UNIT_XYZ);
        Vector3f camRelative = tr.transformInverseVector(position, null);
        return camRelative;
    }

    /**
     * Transforms position from local space to world space.
     * 
     * @param cam
     * @param position
     * @return
     */
    public static Vector3f transformPoint(Camera cam, Vector3f position) {
        Transform tr = new Transform(cam.getLocation(), cam.getRotation(), Vector3f.UNIT_XYZ);
        Vector3f camRelative = tr.transformVector(position, null);
        return camRelative;
    }
    
}
