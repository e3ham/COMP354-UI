package de.dennisguse.opentracks.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager; //Added for EdittextPreference


import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.ActivityType;
import de.dennisguse.opentracks.fragments.ChooseActivityTypeDialogFragment;

public class DefaultsSettingsFragment extends PreferenceFragmentCompat implements ChooseActivityTypeDialogFragment.ChooseActivityTypeCaller {

    // Used to forward update from ChooseActivityTypeDialogFragment; TODO Could be replaced with LiveData.
    private ActivityTypePreference.ActivityPreferenceDialog activityPreferenceDialog;

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> {
        if (PreferencesUtils.isKey(R.string.stats_units_key, key)) {
            getActivity().runOnUiThread(this::updateUnits);
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_defaults);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_defaults_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferencesUtils.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        updateTimeUnits(); //Make sure that time is kept
        updateUnits();
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferencesUtils.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ActivityTypePreference) {
            activityPreferenceDialog = ActivityTypePreference.ActivityPreferenceDialog.newInstance(preference.getKey());
            dialogFragment = activityPreferenceDialog;
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), getClass().getSimpleName());
            return;
        }

        super.onDisplayPreferenceDialog(preference);
    }

    //Modify the default time units for activities
    private void updateTimeUnits() {
        //Acquire time units from Preferences
        TimeUnitSystem time = PreferencesUtils.getTimeUnit();
        ListPreference statsTimePreferences = findPreference((getString(R.string.stats_time_units_key)));
        EditTextPreference statsTimeCustom = findPreference(getString(R.string.stats_time_units_custom_key));

        //See which option is chosen
        int entriesID = switch (time) {
            case FIVE_SEC, TEN_SEC, FIFTEEN_SEC -> R.array.stats_time_units_options;
            case CUSTOM ->
                R.array.stats_time_values;

        };

        String[] entries = getResources().getStringArray(entriesID);
        statsTimePreferences.setEntries(entries);

         // Set summary for custom time preference
         statsTimeCustom.setSummary(statsTimeCustom.getText());

        //Validates whether the time unit saving method will work
        HackUtils.invalidatePreference(statsTimePreferences);
        HackUtils.invalidatePreference((statsTimeCustom))
        ;
    }

    //TODO implement a method where the Settings retrieve the custom time value made by the user
    private String getCustomTimeValue() {
        // Get the shared preferences instance
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        //return custom time value input
        return sharedPreferences.getString(getString(R.string.stats_time_units_custom_key),"");
    }

    private void updateUnits() {
        UnitSystem unitSystem = PreferencesUtils.getUnitSystem();

        ListPreference statsRatePreferences = findPreference(getString(R.string.stats_rate_key));

        int entriesId = switch (unitSystem) {
            case METRIC -> R.array.stats_rate_metric_options;
            case IMPERIAL_FEET, IMPERIAL_METER ->
                    R.array.stats_rate_imperial_options;
            case NAUTICAL_IMPERIAL ->
                R.array.stats_rate_nautical_options;
        };

        String[] entries = getResources().getStringArray(entriesId);
        statsRatePreferences.setEntries(entries);

        HackUtils.invalidatePreference(statsRatePreferences);
    }

    @Override
    public void onChooseActivityTypeDone(ActivityType activityType) {
        if (activityPreferenceDialog != null) {
            activityPreferenceDialog.updateUI(activityType);
        }
    }
}
