package ro.unibuc.fmi.fmi;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import ro.unibuc.fmi.fmi.data.FmiContract;

/**
 * Created by alexandru on 12.04.2016
 */
public class SettingsActivity extends PreferenceActivity implements LoaderManager.LoaderCallbacks<Cursor>, Preference.OnPreferenceChangeListener {

    private PreferenceScreen preferenceScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        preferenceScreen = getPreferenceScreen();
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_language_key)));

        getLoaderManager().initLoader(MainActivity.CATEGORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MainActivity.createCursor(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst())
        {
            do {
                CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);

                checkBoxPreference.setChecked(true);
                checkBoxPreference.setTitle(data.getString(data.getColumnIndex(
                        FmiContract.TranslationEntry.COLUMN_VALUE)));
                checkBoxPreference.setKey("notify_" + data.getString(data.getColumnIndex(
                        FmiContract.CategoryEntry.TABLE_NAME + "." + FmiContract.CategoryEntry._ID)));

                preferenceScreen.addPreference(checkBoxPreference);
            } while (data.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        preferenceScreen.removeAll();
        addPreferencesFromResource(R.xml.pref_general);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }

            getLoaderManager().restartLoader(MainActivity.CATEGORY_LOADER, null, this);
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }
}
