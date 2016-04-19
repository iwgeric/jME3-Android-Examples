package org.jmonkeyengine.g_jaime_demo;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;


/**
 *
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //get a handle on preferences that require validation
        Preference prefMaxCharSpeed = getPreferenceScreen().findPreference("pref_key_max_character_speed");

        //Validate numbers only
        prefMaxCharSpeed.setOnPreferenceChangeListener(numberCheckListener);
    }



    /* Checks that a preference is a valid numerical value
    */
    Preference.OnPreferenceChangeListener numberCheckListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //Check that the string is an integer.
            return numberCheck(newValue);
        }
    };

    private boolean numberCheck(Object newValue) {
        if( !newValue.toString().equals("")  &&  newValue.toString().matches("\\d*") ) {
            return true;
        }
        else {
            Toast.makeText(SettingsActivity.this,
                    newValue + " " +
                    getResources().getString(R.string.is_an_invalid_number),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }


}
