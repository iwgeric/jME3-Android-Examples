package org.jmonkeyengine.g_jaime_demo.gamelogic.assetloader;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * App State to load game assets asynchronously.  Create a <code>LoadTask</code> object and
 * add it to be loaded by calling the addLoadData(loadTask) method.  This will automatically
 * start loading the assets.
 *
 * The LoadTask is called by the system when the assets are loaded by calling the
 * onDoneLoading(Application, LoadResult) method in the LoadTask.  This allows the user
 * to perform additional tasks on the main render thread after the assets are loaded.  For
 * example, this is where the user can perform pre-loading of the assets (ie. send data to
 * gpu) by calling renderManager.preload(scene).
 *
 * @author iwgeric
 */
public class AsyncAssetLoader extends BaseAppState {
    private static final Logger logger = Logger.getLogger(AsyncAssetLoader.class.getName());

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private Future future = null;

    private final ConcurrentLinkedQueue<LoadTask> loadDataQueue = new ConcurrentLinkedQueue<LoadTask>();

    public AsyncAssetLoader() {}

    /**
     * Method to determine if all load tasks have been completed.
     * @return True when all load tasks are complete.
     */
    public boolean isAllLoaded() {
        return future == null && loadDataQueue.isEmpty();
    }

    /**
     * Add a load task to the queue.  isAllLoaded immediately goes false until the task
     * (and all other tasks) have completed.
     * @param loadTask
     */
    public void addLoadData(LoadTask loadTask) {
        logger.log(Level.INFO, "adding load task: {0}", loadTask);
        loadDataQueue.add(loadTask);
    }


    /**
     * Creates a Callable to get the next load task in the queue and calls the load method.
     * This is meant to be run in its own thread.
     */
    private void createFuture() {
        Callable<LoadTaskResult> loadTaskCallable = new Callable<LoadTaskResult>() {

            public LoadTaskResult call() throws Exception {
                logger.log(Level.INFO, "Creating Callable");

                LoadTask loadTask = loadDataQueue.poll();
                logger.log(Level.INFO, "loadTask: {0}", loadTask.getClass().getSimpleName());
                Object asset = loadTask.load(getApplication());
                if (asset != null) {
                    logger.log(Level.INFO, "Asset loaded: {0}", asset);
                } else {
                    logger.log(Level.INFO, "No Asset returned for: {0}", loadTask.getClass().getSimpleName());
                }

                return new LoadTaskResult(loadTask, asset);
            }
        };

        //start the callable on the executor
//        logger.log(Level.INFO, "Submitting Callable");
        future = executor.submit(loadTaskCallable);    //  Thread starts!
    }

    @Override
    protected void initialize(Application app) {
    }

    @Override
    protected void cleanup(Application app) {
        loadDataQueue.clear();
        future = null;
        executor.shutdownNow();
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        try{
            //If we have started a callable already, we check the status
            if (future == null && !loadDataQueue.isEmpty()) {
//                logger.log(Level.INFO, "creating new callable. loadDataQueue: {0}", loadDataQueue.peek());
                createFuture();
            }
            if (future != null) {
//                logger.log(Level.INFO, "checking future with tpf: {0}", tpf);
                if(future.isDone()){
                    try {
                        LoadTaskResult result = (LoadTaskResult)(future.get());
//                        logger.log(Level.INFO, "Asset Name: {0}, Type: {1}",
//                                new Object[]{
//                                    data.getAssetKey().getName(),
//                                    data.getAsset().getClass().getSimpleName()
//                                });

                        result.getLoadTask().onDoneLoading(
                                getApplication(), result.getLoadResult());
                    } catch (ExecutionException ex) {
                        ex.getCause().printStackTrace();
                    }

                    future = null;
                } else if(future.isCancelled()){
                    logger.log(Level.SEVERE, "Future is cancelled");
                    //Set future to null. Maybe we succeed next time...
                }
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Exception: {0}", e);
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        }

    }

    /**
     * Internal data storage class to store the load task and the loaded object.
     */
    protected class LoadTaskResult {
        private final LoadTask loadTask;
        private final Object loadResult;

        protected LoadTaskResult(final LoadTask loadTask, final Object loadResult) {
            this.loadTask = loadTask;
            this.loadResult = loadResult;
        }

        protected LoadTask getLoadTask() {
            return loadTask;
        }

        protected Object getLoadResult() {
            return loadResult;
        }
    }
}
