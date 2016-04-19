package org.jmonkeyengine.g_jaime_demo.gamelogic;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to serve as the "go between" from the Android Settings activity and the jME game logic.
 */
public class UserSettings {
    private static final Logger logger = Logger.getLogger(UserSettings.class.getName());
    private static UserSettings instance = null;

    private Main app;
    private static boolean SHOW_FPS = true;
    private static boolean SHOW_STATS = true;
    private static float CHARACTER_MAX_SPEED = 2f;
    private static float CHARACTER_RUN_SPEED = 1f;

    public static UserSettings getInstance() {
        return instance;
    }

    public UserSettings (Main app) {
        this.app = app;
        if (app != null) {
            UserSettings.instance = this;
        } else {
            UserSettings.instance = null;
        }
    }

    public boolean getShowFPS() {
        return UserSettings.SHOW_FPS;
    }

    public boolean setShowFPS(boolean show) {
        if (app != null) {
            app.setDisplayFps(show);
            UserSettings.SHOW_FPS = show;
            return true;
        } else {
            logger.log(Level.INFO, "App not set. Not applying setting.");
            return false;
        }
    }

    public boolean getShowStats() {
        return UserSettings.SHOW_STATS;
    }

    public boolean setShowStats(boolean show) {
        if (app != null) {
            app.setDisplayStatView(show);
            UserSettings.SHOW_STATS = show;
            return true;
        } else {
            logger.log(Level.INFO, "App not set. Not applying setting.");
            return false;
        }
    }

    public float getCharacterMaxSpeed() {
        return UserSettings.CHARACTER_MAX_SPEED;
    }

    public boolean setCharacterMaxSpeed(float maxSpeed) {
        UserSettings.CHARACTER_MAX_SPEED = maxSpeed;
        return true;
    }

    public float getCharacterRunSpeed() {
        return UserSettings.CHARACTER_RUN_SPEED;
    }

    public boolean setCharacterRunSpeed(float runSpeed) {
        UserSettings.CHARACTER_RUN_SPEED = runSpeed;
        return true;
    }

}
