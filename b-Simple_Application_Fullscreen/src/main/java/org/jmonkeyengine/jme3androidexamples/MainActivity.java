package org.jmonkeyengine.jme3androidexamples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The "activity_main" layout includes the reference
        // to the fragment that contains the GLSurfaceView
        // that will be used to display the jME content.
        setContentView(R.layout.activity_main);

    }


    /*
    For jME games that do not use Android UI controls, it is probably best to use the
    IMMERSIVE_STICKY mode.  Simply set the system flags as shown below and the layout contents
    defined above will be shown "full screen".  Swiping down from the top of the screen will
    temporarily show the navigation and status bars.
     */

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

}
