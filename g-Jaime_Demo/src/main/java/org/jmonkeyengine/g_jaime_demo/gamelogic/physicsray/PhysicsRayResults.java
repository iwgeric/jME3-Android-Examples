package org.jmonkeyengine.g_jaime_demo.gamelogic.physicsray;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.collision.CollisionResults;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * <code>PhysicsRayResults</code> is a collection returned as a result of a
 * physics ray test operation done by {@link PhysicsSpace}.  This is a copy of
 * {@link CollisionResults}.
 *
 * @author iwgeric
 */
public class PhysicsRayResults implements Iterable<PhysicsRayResult> {

    private final ArrayList<PhysicsRayResult> results = new ArrayList<PhysicsRayResult>();
    private boolean sorted = true;

    /**
     * Clears all collision results added to this list
     */
    public void clear(){
        results.clear();
    }

    /**
     * Iterator for iterating over the collision results.
     *
     * @return the iterator
     */
    public Iterator<PhysicsRayResult> iterator() {
        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.iterator();
    }

    /**
     * Add a result to the list
     * @param result
     */
    public void addResult(PhysicsRayResult result){
        results.add(result);
        sorted = false;
    }

    /**
     * Returns the size of the list of results
     * @return
     */
    public int size(){
        return results.size();
    }

    /**
     * Returns the result that has the smallest distance
     * @return
     */
    public PhysicsRayResult getClosestResult(){
        if (size() == 0)
            return null;

        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(0);
    }

    /**
     * Returns the result with the largest distance
     * @return
     */
    public PhysicsRayResult getFarthestResult(){
        if (size() == 0)
            return null;

        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(size()-1);
    }

    /**
     * Returns the result at the given index into the list
     * @param index
     * @return
     */
    public PhysicsRayResult getResult(int index){
        if (!sorted){
            Collections.sort(results);
            sorted = true;
        }

        return results.get(index);
    }

    /**
     * Internal use only.
     * @param index
     * @return
     */
    public PhysicsRayResult getResultDirect(int index){
        return results.get(index);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PhysicsRayResults[");
        for (PhysicsRayResult result : results){
            sb.append(result).append(", ");
        }
        if (results.size() > 0)
            sb.setLength(sb.length()-2);

        sb.append("]");
        return sb.toString();
    }

}