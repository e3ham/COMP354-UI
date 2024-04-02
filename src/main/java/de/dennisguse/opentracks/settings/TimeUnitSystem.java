package de.dennisguse.opentracks.settings;

import de.dennisguse.opentracks.R;

public enum TimeUnitSystem {
    FIVE_SEC(R.string.stats_time_unit_five),

    TEN_SEC(R.string.stats_time_unit_ten),
    FIFTEEN_SEC(R.string.stats_time_unit_fifteen),

    CUSTOM(R.string.stats_time_unit_custom); // Nautical miles with feet

    private final int preference;

    TimeUnitSystem(int preference) {
        this.preference = preference;
    }

    public int getPreferenceId() {
        return preference;
    }

    @Deprecated //TODO used to initialize before loading from preferences; should be loaded first
    public static TimeUnitSystem defaultUnitSystem() {
        return FIVE_SEC;
    }
}
