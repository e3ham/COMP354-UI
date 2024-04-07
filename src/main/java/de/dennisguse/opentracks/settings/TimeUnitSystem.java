package de.dennisguse.opentracks.settings;

import de.dennisguse.opentracks.R;

public enum TimeUnitSystem {
    FIVE_SEC(R.string.stats_time_unit_five),

    TEN_SEC(R.string.stats_time_unit_ten),
    TWENTY_SEC(R.string.stats_time_unit_twenty),

    CUSTOM(R.string.stats_time_unit_custom);

    private final int preference;

    TimeUnitSystem(int preference) {
        this.preference = preference;
    }

    public int getPreferenceId() {
        return preference;
    }

    @Deprecated //TODO used to initialize before loading from preferences; should be loaded first
    public static TimeUnitSystem defaultUnitSystem() {
        return TEN_SEC;
    }
}
