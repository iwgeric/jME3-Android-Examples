package org.jmonkeyengine.g_jaime_demo;

import com.jme3.app.AndroidHarnessFragment;

/**
 * A placeholder fragment containing a the jME GLSurfaceView.
 */
public class JmeFragment extends AndroidHarnessFragment {

    public JmeFragment() {
        // Set main project class (fully qualified path)
        appClass = "org.jmonkeyengine.g_jaime_demo.gamelogic.Main";

        // Set the desired EGL configuration
        eglBitsPerPixel = 24;
        eglAlphaBits = 0;
        eglDepthBits = 16;
        eglSamples = 0;
        eglStencilBits = 0;

        // Set the maximum framerate
        // (default = -1 for unlimited)
        frameRate = -1;

        // Set the maximum resolution dimension
        // (the smaller side, height or width, is set automatically
        // to maintain the original device screen aspect ratio)
        // (default = -1 to match device screen resolution)
        maxResolutionDimension = -1;

        // Set input configuration settings
        joystickEventsEnabled = false;
        keyEventsEnabled = true;
        mouseEventsEnabled = true;

        // Set application exit settings
        finishOnAppStop = true;
        handleExitHook = true;
        exitDialogTitle = "Do you want to exit?";
        exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";

        // Set splash screen resource id, if used
        // (default = 0, no splash screen)
        // For example, if the image file name is "splash"...
        //     splashPicID = R.drawable.splash;
//        splashPicID = 0;
        splashPicID = R.drawable.android_splash;
    }

}
