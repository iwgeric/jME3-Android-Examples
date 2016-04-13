package org.jmonkeyengine.g_jaime_demo.gamelogic.physicsray;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * Helper class to store data used to create a physics ray.
 *
 * @author iwgeric
 */
public class PhysicsRay {
    private Vector3f startLocation = new Vector3f();
    private Vector3f endLocation = new Vector3f();

    /**
     * Create a data object for a physics ray based on a camera and a 2D x and y coordinate.  In this case,
     * the z value is set to 0 (near clip plane) at the start location and 1 (far clip plane) at the end location.
     * @param cam Camera object to use to calculate the 3D locations
     * @param x 2D X coordinate
     * @param y 2D Y coordinate
     */
    public PhysicsRay(Camera cam, float x, float y) {
        Vector2f click2d = new Vector2f(x, y);
        this.startLocation.set(cam.getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 0f));
        this.endLocation.set(cam.getWorldCoordinates(
                new Vector2f(click2d.x, click2d.y), 1f));
    }

    /**
     * Create a data object for a physics ray based on a 3D start location, direction, and distance.
     * @param startLocation 3D start location
     * @param direction Ray direction
     * @param length Ray length
     */
    public PhysicsRay(Vector3f startLocation, Vector3f direction, float length) {
        this.startLocation.set(startLocation);
        if (!direction.isUnitVector()) {
            this.endLocation.set(direction.normalize()).multLocal(length).addLocal(startLocation);
        } else {
            this.endLocation.set(direction).multLocal(length).addLocal(startLocation);
        }
    }

    /**
     * Create a data object for a physics ray based on a 3D start location and 3D end location.
     * @param startLocation 3D start location
     * @param endLocation 3D end location
     */
    public PhysicsRay(Vector3f startLocation, Vector3f endLocation) {
        this.startLocation.set(startLocation);
        this.endLocation.set(endLocation);
    }

    /**
     * Retrieve the physics ray start location
     * @return physics ray start location
     */
    public Vector3f getStartLocation() {
        return startLocation;
    }

    /**
     * Retrieve the physics ray end location
     * @return physics ray end location
     */
    public Vector3f getEndLocation() {
        return endLocation;
    }

}
