package org.jmonkeyengine.c_android_menus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jme3.math.ColorRGBA;

import org.jmonkeyengine.c_android_menus.gamelogic.JmeAndroidInterface;
import org.jmonkeyengine.c_android_menus.gamelogic.Main;

public class MainActivity extends AppCompatActivity implements JmeAndroidInterface {
    private Main jmeApp = null;
    private ColorRGBA currentColor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The "activity_main" layout includes the reference
        // to the fragment that contains the GLSurfaceView
        // that will be used to display the jME content.
        setContentView(R.layout.activity_main);

        // Save a reference to the jME application for later use when a menu item is selected.
        JmeFragment jmeFragment = (JmeFragment) getFragmentManager().findFragmentById(R.id.jMEFragment);
        jmeApp = (Main)jmeFragment.getJmeApplication();

        // Set the androidInterface object to this class.  This enables the game logic to
        // callback to this class to modify the Android based menu items.
        jmeApp.setAndroidInterface(this);
    }


    /*
    For jME games, it is probably best to use the IMMERSIVE_STICKY mode.  Simply set the system
    flags as shown below and the layout contents defined above will be shown "full screen".
    Swiping down from the top of the screen will temporarily show the navigation and status
    bars.
     */

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    /*
    Inflate the xml file that defines the menu items
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        boolean matchesCurrentColor;

        if (currentColor != null) {
            matchesCurrentColor = currentColor.equals(ColorRGBA.Red);
            menu.findItem(R.id.action_red).setEnabled(!matchesCurrentColor);
            Log.d("MainActivity", "Red Enabled: " + !matchesCurrentColor);

            matchesCurrentColor = currentColor.equals(ColorRGBA.Green);
            menu.findItem(R.id.action_green).setEnabled(!matchesCurrentColor);
            Log.d("MainActivity", "Green Enabled: " + !matchesCurrentColor);

            matchesCurrentColor = currentColor.equals(ColorRGBA.Blue);
            menu.findItem(R.id.action_blue).setEnabled(!matchesCurrentColor);
            Log.d("MainActivity", "Blue Enabled: " + !matchesCurrentColor);
        } else {
            Log.d("MainActivity", "currentColor is null, enabling all menu items");
            menu.findItem(R.id.action_red).setEnabled(true);
            menu.findItem(R.id.action_green).setEnabled(true);
            menu.findItem(R.id.action_blue).setEnabled(true);
        }

        return true;
    }

    /*
    When a menu item is selected, call a method in the game logic to perform
    an action based which menu item is selected.
    This uses the reference to the game logic instance obtained in onCreate
    from the jME fragment.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_red was selected
            case R.id.action_red:
                if (jmeApp == null) {
                    Toast.makeText(this, "No jME Application Found!!!", Toast.LENGTH_LONG)
                            .show();
                } else {
                    jmeApp.setColor(ColorRGBA.Red);
                }
                break;
            // action with ID action_green was selected
            case R.id.action_green:
                if (jmeApp == null) {
                    Toast.makeText(this, "No jME Application Found!!!", Toast.LENGTH_LONG)
                            .show();
                } else {
                    jmeApp.setColor(ColorRGBA.Green);
                }
                break;
            // action with ID action_blue was selected
            case R.id.action_blue:
                if (jmeApp == null) {
                    Toast.makeText(this, "No jME Application Found!!!", Toast.LENGTH_LONG)
                            .show();
                } else {
                    jmeApp.setColor(ColorRGBA.Blue);
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void disableColorOption(ColorRGBA color) {
        currentColor = color;
        invalidateOptionsMenu();
    }

}
