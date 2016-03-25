package org.jmonkeyengine.d_android_menus_immersive.gamelogic;

import com.jme3.app.SimpleApplication;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.event.TouchEvent;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Main extends SimpleApplication implements TouchListener {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private Material boxMaterial = null;
    private JmeAndroidInterface android = null;

    /**
     * Saves a reference to the android class to serve as an interface between the jME code
     * and the Android classes
     * @param android  Referece to Android class
     */
    public void setAndroidInterface(JmeAndroidInterface android) {
        this.android = android;
    }

    public void simpleInitApp() {

        inputManager.addMapping("touch", new TouchTrigger(TouchInput.ALL));
        inputManager.addListener(this, "touch");

        Box box = new Box(1, 1, 1);
        Geometry geom = new Geometry("box", box);
        boxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        // Set the box color and call back to the Android class to disable the Blue menu item
        setColor(ColorRGBA.Blue);

        // Like normal Android projects, place your texture/models/sounds/etc files in the project
        // "assets" folder (src/main/assets).  jME includes this directory automatically when
        // looking for game assets.
        Texture texture = assetManager.loadTexture("Textures/Monkey.png");
        boxMaterial.setTexture("ColorMap", texture);

        geom.setMaterial(boxMaterial);
        rootNode.attachChild(geom);

    }

    /**
     * Set the color of the box
     * @param color
     */
    public void setColor(ColorRGBA color) {
        if (boxMaterial != null) {
            boxMaterial.setColor("Color", color);
            if (android != null) {
                MatParam paramColor = boxMaterial.getParam("Color");
                if (paramColor != null) {
                    ColorRGBA currentColor = (ColorRGBA)boxMaterial.getParam("Color").getValue();
                    android.disableColorOption(color);
                }
            }
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
        logger.log(Level.INFO, "Touch Type: {0}", event.getType());
        if (event.getType().equals(TouchEvent.Type.TAP)) {
            if (android != null) {
                android.toggleMenus();
            }
        }
    }
}
