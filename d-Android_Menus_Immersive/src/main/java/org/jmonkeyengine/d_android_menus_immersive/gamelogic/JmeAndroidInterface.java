package org.jmonkeyengine.d_android_menus_immersive.gamelogic;

import com.jme3.math.ColorRGBA;

/**
 * Interface between the jME Game Logic and the Android system.  Used by the
 * jME game logic to call methods on the Android classes.
 */
public interface JmeAndroidInterface {
    public void disableColorOption(ColorRGBA color);
    public void toggleMenus();
}
