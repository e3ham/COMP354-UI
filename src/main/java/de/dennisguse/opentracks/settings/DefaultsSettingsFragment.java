package de.dennisguse.opentracks.settings;

import static androidx.preference.PreferenceDialogFragmentCompat.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.ActivityType;
import de.dennisguse.opentracks.fragments.ChooseActivityTypeDialogFragment;
import de.dennisguse.opentracks.settings.bluetooth.BluetoothLeSensorPreference;

public class DefaultsSettingsFragment extends PreferenceFragmentCompat implements ChooseActivityTypeDialogFragment.ChooseActivityTypeCaller {

    // Used to forward update from ChooseActivityTypeDialogFragment; TODO Could be replaced with LiveData.
    private ActivityTypePreference.ActivityPreferenceDialog activityPreferenceDialog;

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> {
        if (PreferencesUtils.isKey(R.string.stats_units_key, key)) {
            getActivity().runOnUiThread(this::updateUnits);
        }
        if (PreferencesUtils.isKey(R.string.stats_time_units_key, key)) {
            getActivity().runOnUiThread(this::updateTimeUnits);
        }
    };


    private String custom_time; //use for CUSTOM

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_defaults);

        ListPreference statsTimeUnitsPreference = findPreference(getString(R.string.stats_time_units_key));

        // Set up the listener
        statsTimeUnitsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                if (newValue.equals("CUSTOM")) {
                    displayCustomInputDialog();
                    // Preventthe default behavior of the ListPreference
                    return false;
                } else {
                    //Allow the ListPreference to handle the change
                    return true;
                }
            }
        });
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
        SharedPreferences preferences = getContext().getSharedPreferences("default_time_unit", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(getString(R.string.stats_time_units_key), time.getPreferenceId());
        editor.apply();

        ListPreference statsTimePreferences = findPreference((getString(R.string.stats_time_units_key)));

        int entriesID = switch (time) {
            case FIVE_SEC, TEN_SEC, TWENTY_SEC, CUSTOM -> R.array.stats_time_units_options;
        };

        String[] entries = getResources().getStringArray(entriesID);
        statsTimePreferences.setEntries(entries);

        HackUtils.invalidatePreference(statsTimePreferences);
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




    //Custom Dialog
    protected void displayCustomInputDialog() {
        // Show dialog box for custom input
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Custom Time Unit");

        // Set the edit text box for user to enter
        final EditText cus_Input = new EditText(requireContext());
        //cus_Input.setInputType(InputType.TYPE_CLASS_NUMBER); //make sure it's a number

        builder.setView(cus_Input);

        //Buttons cancel or just say ok
        builder.setPositiveButton("OK", (dialog, which) -> {
            custom_time = cus_Input.getText().toString();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        //show final result
        builder.show();
    }
}
