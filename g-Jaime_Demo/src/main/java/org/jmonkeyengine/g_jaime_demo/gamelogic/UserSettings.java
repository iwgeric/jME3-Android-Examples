package org.jmonkeyengine.g_jaime_demo.gamelogic;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to serve as the "go between" from the Android Settings activity and the jME game logic.
 */
public class UserSettings {
    private static final Logger logger = Logger.getLogger(UserSettings.class.getName());
    private Main app;
    private static boolean SHOW_FPS = true;
    private static boolean SHOW_STATS = true;

    public UserSettings(Main app) {
        this.app = app;
    }

    public boolean getShowFPS() {
//        if (app != null) {
//            StatsAppState statsState = app.getStateManager().getState(StatsAppState.class);
//            if (statsState != null) {
//                return statsState.getFpsText().getCullHint().equals(Spatial.CullHint.Never);
//            }
//        }
//        return false;

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
//        if (app != null) {
//            StatsAppState statsState = app.getStateManager().getState(StatsAppState.class);
//            if (statsState != null) {
//                return statsState.getStatsView().getCullHint().equals(Spatial.CullHint.Never);
//            }
//        }
//        return false;

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

}
