package org.jmonkeyengine.g_jaime_demo.gamelogic.physicsray;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * A <code>PhysicsRayResult</code> represents a single collision instance
 * between a {@link PhysicsRay} and a {@link PhysicsCollisionObject}.
 * A collision can result at the front of the collison object or at the back
 * of the collision object or both.
 *
 * @author iwgeric
 */
public class PhysicsRayResult implements Comparable<PhysicsRayResult> {

    private Spatial spatial;
    private Vector3f contactPoint;
    private Vector3f contactNormal;
    private float distance;
    private PhysicsCollisionObject collisionObject;

    /**
     * Stores a collision result along with the ray used.
     *
     * @param ray Ray used
     * @param testResult Ray test result from physics
     */
    public PhysicsRayResult(PhysicsRay ray, PhysicsRayTestResult testResult) {
        this.collisionObject = testResult.getCollisionObject();
        Object object = collisionObject.getUserObject();
        if (object instanceof Spatial) {
            this.spatial = (Spatial)object;
        } else {
            this.spatial = null;
        }

        Vector3f rayVector = ray.getEndLocation().subtract(ray.getStartLocation());
        float rayLength = rayVector.length();
        distance = rayLength * testResult.getHitFraction();
        Vector3f rayDirection = rayVector.normalize();

        contactPoint = rayDirection.clone().multLocal(distance).addLocal(ray.getStartLocation());
        contactNormal = testResult.getHitNormalLocal();

    }

    /**
     * Used to test the distance to this result compared to another result.
     * @param other Other RayTestResult
     * @return Returns the Float.compare result between the 2 distances.  0 = equal distance.
     *         >0 when this distance is greater than the other distance. <0 when this distance is
     *         less than the other distance.
     */
    public int compareTo(PhysicsRayResult other) {
        return Float.compare(distance, other.distance);
    }

    /**
     * Tests for equality between this result and another result.  Results are equal when the
     * distance is equal.
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PhysicsRayResult){
            return ((PhysicsRayResult)obj).compareTo(this) == 0;
        }
        return super.equals(obj);
    }

    /**
     * Retrieves the contact point for this ray test result
     * @return Contact Point of ray result
     */
    public Vector3f getContactPoint() {
        return contactPoint;
    }

    /**
     * Retrieves the contact normal of the ray result to the collision object
     * @return Contact normal of the collision object
     */
    public Vector3f getContactNormal() {
        return contactNormal;
    }

    /**
     * Retrieves the distance from the ray start to the collision object
     * @return Distance from the ray start to the collision object
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Spatial tied to the collision object
     * @return Spatial tied to the collision object
     */
    public Spatial getSpatial() {
        return spatial;
    }

    /**
     * Retieves the collision object tied to this ray test result
     * @return Collision object tied to this ray test result
     */
    public PhysicsCollisionObject getCollisionObject() {
        return collisionObject;
    }

}
