package org.jmonkeyengine.g_jaime_demo.gamelogic.assetloader;


import com.jme3.app.Application;

/**
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
