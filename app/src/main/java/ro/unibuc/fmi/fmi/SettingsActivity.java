package ro.unibuc.fmi.fmi;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Created by alexandru on 12.04.2016
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        PreferenceScreen preferenceScreen = getPreferenceScreen();

        CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);

        checkBoxPreference.setChecked(true);
        checkBoxPreference.setTitle("Noutati");
        checkBoxPreference.setKey("notify_news");

        preferenceScreen.addPreference(checkBoxPreference);
        //PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
    }
}
