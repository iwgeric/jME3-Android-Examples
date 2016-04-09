package org.jmonkeyengine.g_jaime_demo.gamelogic.controls;


import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.ChaseCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

import org.jmonkeyengine.g_jaime_demo.gamelogic.physicsray.PhysicsRay;
import org.jmonkeyengine.g_jaime_demo.gamelogic.physicsray.PhysicsRayHelpers;
import org.jmonkeyengine.g_jaime_demo.gamelogic.physicsray.PhysicsRayResult;

import java.util.logging.Logger;

/**
 * CustomChaseCamera adds the functionality of automatically rotates/zooms the camera to try to keep
 * the target spatial in view.  This is done by creating 2 PhysicsRays between
 * the spatial and the camera.  If only 1 ray is blocked, the camera rotates.
 * When both rays are blocked, the camera zooms in until the rays aren't blocked
 * anymore.
 *
 *
 * @author iwgeric
 */
public class CustomChaseCamera extends ChaseCamera {
    private static final Logger logger = Logger.getLogger(CustomChaseCamera.class.getName());
    private float userZoom = 0f;
    private boolean autoZoomActive = false;
    private boolean autoRotateActive = false;
    private float autoRotateTarget = 0f;
    private float autoRotateCurrent = 0f;

    private Vector3f targetToCamera = new Vector3f();
    private Vector3f targetToCameraDirection = new Vector3f();
    private Vector3f leftLocation = new Vector3f();
    private Vector3f leftEnd = new Vector3f();
    private Vector3f targetToLeft = new Vector3f();
    private Vector3f rightLocation = new Vector3f();
    private Vector3f rightEnd = new Vector3f();
    private Vector3f targetToRight = new Vector3f();
    private PhysicsSpace physicsSpace = null;


    public CustomChaseCamera(Camera cam, final Spatial target) {
        super(cam, target);
        dragToRotate = true;
        canRotate = false;
        userZoom = targetDistance;
    }

    public CustomChaseCamera(Camera cam) {
        super(cam);
        dragToRotate = true;
        canRotate = false;
        userZoom = targetDistance;
    }

    public void setPhysicsSpace(PhysicsSpace physicsSpace) {
        this.physicsSpace = physicsSpace;
    }

    public void keepTargetVisible(float tpf) {
        float rayOffset = 0.75f;

        float leftDistToCollision = 9999f;
        float rightDistToCollision = 9999f;
        boolean okToResetZoom = true;

        targetLocation.set(target.getWorldTranslation()).addLocal(lookAtOffset);
        targetToCamera.set(cam.getLocation()).subtractLocal(targetLocation);
        targetToCameraDirection.set(targetToCamera).normalizeLocal();
        float distToCamera = (cam.getLocation().subtract(targetLocation)).length();

        // ray to left of camera
        leftLocation.set(cam.getLeft()).multLocal(rayOffset).addLocal(targetLocation);
        leftEnd.set(cam.getLeft()).multLocal(rayOffset).addLocal(cam.getLocation());
        targetToLeft.set(leftEnd).subtractLocal(targetLocation);
        float leftAngle = targetToCameraDirection.angleBetween(targetToLeft.normalize());

        PhysicsRay rayLeft = new PhysicsRay(leftLocation, leftEnd);
        PhysicsRayResult leftClosestResult =
                PhysicsRayHelpers.getClosestExcludedResult(physicsSpace, target, rayLeft);

        if (leftClosestResult != null) {
            okToResetZoom = false;
            leftDistToCollision = leftClosestResult.getDistance();
        }

        // ray to right of camera
        rightLocation.set(cam.getLeft()).negateLocal().multLocal(rayOffset).addLocal(targetLocation);
        rightEnd.set(cam.getLeft()).negateLocal().multLocal(rayOffset).addLocal(cam.getLocation());
        targetToRight.set(rightEnd).subtractLocal(targetLocation);
        float rightAngle = targetToCameraDirection.angleBetween(targetToRight.normalize());

        PhysicsRay rayRight = new PhysicsRay(rightLocation, rightEnd);
        PhysicsRayResult rightClosestResult =
                PhysicsRayHelpers.getClosestExcludedResult(physicsSpace, target, rayRight);

        if (rightClosestResult != null) {
            okToResetZoom = false;
            rightDistToCollision = rightClosestResult.getDistance();
        }


        if (leftDistToCollision < distToCamera && rightDistToCollision < distToCamera) {
            // do zoom
            okToResetZoom = false;
            autoZoom(Math.min(leftDistToCollision, rightDistToCollision) * tpf);
        } else if (leftDistToCollision < distToCamera) {
            // rotate right
//            logger.log(Level.INFO, "leftAngle: {0}", leftAngle);
            autoRotate(-1f * leftAngle / 2);
        } else if (rightDistToCollision < distToCamera) {
            // rotate left
//            logger.log(Level.INFO, "rightAngle: {0}", rightAngle);
            autoRotate(1f * rightAngle / 2);
        } else {
//            logger.log(Level.INFO, "CLEAR!!");
        }

        if (okToResetZoom) {
            resetAutoZoom();
        }
    }
    @Override
    public void setDefaultDistance(float distance) {
        super.setDefaultDistance(distance);
        userZoom = targetDistance;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (!autoZoomActive) {
//            logger.log(Level.INFO, "userZoom: {0}, targetDistance: {1}",
//                    new Object[]{userZoom, targetDistance});
            internalZoom((userZoom-targetDistance)*2f*tpf);
        }

        if (autoRotateActive) {
            if (Math.abs(autoRotateCurrent-autoRotateTarget) < 0.01) {
                autoRotateCurrent = autoRotateTarget = 0f;
                autoRotateActive = false;
            } else {
//                logger.log(Level.INFO, "updating autoRotateCurrent: {0}, autoRotateTarget: {1}",
//                        new Object[]{autoRotateCurrent, autoRotateTarget});
                float value = (autoRotateTarget-autoRotateCurrent)*10f*tpf;
                internalRotate(value);
                autoRotateCurrent += value;
            }
        }

        keepTargetVisible(tpf);
    }

    public void enableRotation(boolean enable) {
        canRotate = enable;
    }

    public boolean isRotationEnabled() {
        return canRotate;
    }

    public void hRotate(float value) {
        rotateCamera(value);
    }

    public void vRotate(float value) {
        vRotateCamera(value);
    }

    public void zoom(float value) {
        internalZoom(value);
        userZoom = targetDistance;
//        logger.log(Level.INFO, "zoom userZoom: {0}, targetDistance: {1}",
//                new Object[]{userZoom, targetDistance});
    }

    public void autoRotate(float value) {
//        logger.log(Level.INFO, "Setting ForceRotate: {0}", value);
        autoRotateActive = true;
        autoRotateCurrent = 0f;
        autoRotateTarget = value;
    }

    public void autoZoom(float value) {
//        boolean prevEnableRotation = activeCamera.isRotationEnabled();
//        activeCamera.enableRotation(true);
        autoZoomActive = true;
        internalZoom(-value);
//        activeCamera.enableRotation(prevEnableRotation);
    }

    public void resetAutoZoom() {
        autoZoomActive = false;
    }

    private void internalZoom(float value) {
        zoomCamera(value);
        if (value < 0) {
            if (zoomin == false) {
                distanceLerpFactor = 0;
            }
            zoomin = true;
        } else {
            if (zoomin == true) {
                distanceLerpFactor = 0;
            }
            zoomin = false;
        }
    }

    private void internalRotate(float value) {
        boolean prevEnableRotation = isRotationEnabled();
        enableRotation(true);
        hRotate(value);
        enableRotation(prevEnableRotation);
    }

    public Vector3f getCameraLeft() {
        return cam.getLeft();
    }

    public Vector3f getCameraLocation() {
        return cam.getLocation();
    }

    public Vector3f getCameraDirection() {
        return cam.getDirection();
    }

}
