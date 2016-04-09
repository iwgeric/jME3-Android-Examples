package org.jmonkeyengine.g_jaime_demo.gamelogic;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.event.TouchEvent;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;

import org.jmonkeyengine.g_jaime_demo.gamelogic.Input.TouchInputHandler;
import org.jmonkeyengine.g_jaime_demo.gamelogic.assetloader.AsyncAssetLoader;
import org.jmonkeyengine.g_jaime_demo.gamelogic.assetloader.SceneDataLoadTask;
import org.jmonkeyengine.g_jaime_demo.gamelogic.processors.CheapShadowRenderer;

import java.util.logging.Logger;

/**
 *
 */
public class Main extends SimpleApplication implements TouchListener {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private JmeAndroidInterface android = null;
    private UserSettings userSettings = null;
    private AsyncAssetLoader assetLoader = null;
    private SceneDataLoadTask sceneDataLoadTask = null;

    private Spatial world = null;


    /**
     * Saves a reference to the android class to serve as an interface between the jME code
     * and the Android classes
     * @param android  Reference to Android MainActivity instance
     */
    public void setAndroidInterface(JmeAndroidInterface android) {
        this.android = android;
    }

    /**
     * Retrieve the UserSettings instance.
     * @return
     */
    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void simpleInitApp() {
        // initialize Lemur for use with UIF objects
        GuiGlobals.initialize(this);

        // attach a BulletAppState to enable physics
        BulletAppState bullet = new BulletAppState();
        bullet.setDebugEnabled(false);
        getStateManager().attach(bullet);

        // attach app state to manage loading assets in a separate thread
        assetLoader = new AsyncAssetLoader();
        getStateManager().attach(assetLoader);
        sceneDataLoadTask = new SceneDataLoadTask();
        assetLoader.addLoadData(sceneDataLoadTask);

        userSettings = new UserSettings(this);
        setDisplayFps(userSettings.getShowFPS());
        setDisplayStatView(userSettings.getShowStats());

        inputManager.addMapping("touch", new TouchTrigger(TouchInput.ALL));
        inputManager.addListener(this, "touch");

    }

    @Override
    public void update() {
        super.update();


        // check to see if the assets are loaded.  If they are, add them to the scene
        if (assetLoader != null && assetLoader.isAllLoaded()) {

            world = sceneDataLoadTask.getLoadData().getWorld();
            getRootNode().attachChild(world);

            CheapShadowRenderer shadows = new CheapShadowRenderer(assetManager);
            getViewPort().addProcessor(shadows);
            rootNode.attachChild(shadows.getShadowNode());

            getStateManager().attach(new TouchInputHandler(sceneDataLoadTask.getLoadData().getMainCharacter()));

            getStateManager().detach(assetLoader);
            assetLoader = null;
        }
    }

    /**
     * Callback from jME input manager that a touch event happened
     * @param name
     * @param event
     * @param tpf
     */
    @Override
    public void onTouch(String name, TouchEvent event, float tpf) {
        if (event.getType().equals(TouchEvent.Type.TAP)) {
            if (android != null) {
                android.toggleMenus();
            }
        }
    }
}
