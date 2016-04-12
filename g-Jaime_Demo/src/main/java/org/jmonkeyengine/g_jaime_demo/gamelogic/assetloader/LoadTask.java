package org.jmonkeyengine.g_jaime_demo.gamelogic.assetloader;


import com.jme3.app.Application;

/**
 * Interface to define a loadable task.  Used to allow for objects to be loaded asynchronously
 * in a separate thread.  The LoadTask is added to the AsyncAssetLoader and is called by the loader
 * to load the object.  Once loading is complete, the onDoneLoading method is called to allow
 * for post load processing.  The onDoneLoading method is called from the main render thread so
 * the object can be pre-loaded and/or added to the scene graph.
 *
 * @author iwgeric
 */
public interface LoadTask {

    /**
     * load is called by the loader on a separate thread.  Implementations need
     * to return an object that contains the loaded assets.  This way they can
     * be returned back to the LoadTask on the render thread during onLoadComplete.
     *
     * @param app  Application object
     * @return  Return the loaded asset
     */
    public Object load(Application app);

    /**
     * onDoneLoaded is called by the loader on the main update thread once the
     * asynch loading is complete.
     *
     * @param app  Application object
     * @param object Loaded object created during the load method
     */
    public void onDoneLoading(Application app, Object object);

    /**
     * Returns the loading status of the task
     * @return True if the task has completed loading
     */
    public boolean isLoaded();

}
