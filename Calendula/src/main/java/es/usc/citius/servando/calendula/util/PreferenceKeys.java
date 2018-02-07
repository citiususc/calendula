/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util;

import android.support.annotation.StringRes;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;

/**
 * Keys for preferences.
 * <p>
 * To access the key string use {@link #key()}.
 * <p>
 * Created by alvaro.brey.vilas on 05/01/17.
 */
public enum PreferenceKeys {

    // Allergies
    ALLERGIES_WARNING_SHOWN(R.string.prefkey_allergies_warning_shown),
    // ConfirmActivity
    CONFIRM_CHECK_WINDOW_MARGIN(R.string.prefkey_confirm_check_window_margin),
    // Drug DB
    DRUGDB_CURRENT_DB(R.string.prefkey_drugdb_current_db),
    DRUGDB_ENABLE_DRUGDB(R.string.prefkey_drugdb_enable_drugdb),
    DRUGDB_LAST_VALID(R.string.prefkey_drugdb_last_valid),
    DRUGDB_VERSION(R.string.prefkey_drugdb_version),
    DRUGDB_DOWNLOAD_ID(R.string.prefkey_drugdb_download_id),
    DRUGDB_DOWNLOAD_DB(R.string.prefkey_drugdb_download_db),
    DRUGDB_DOWNLOAD_VERSION(R.string.prefkey_drugdb_download_version),
    DRUGDB_DOWNLOAD_TYPE(R.string.prefkey_drugdb_download_type),
    DRUGDB_DB_PROMPT(R.string.prefkey_drugdb_prompt),
    // Home
    HOME_INTRO_SHOWN(R.string.prefkey_home_intro_shown),
    HOME_DAILYAGENDA_EXPANDED(R.string.prefkey_home_dailyagenda_expanded),
    HOME_DISPLAY_NAME(R.string.prefkey_home_display_name),
    HOME_PROFILE_BACKGROUND_INDEX(R.string.prefkey_home_profile_background_index),
    HOME_LAST_MOOD(R.string.prefkey_home_last_mood),
    // Medicines
    MEDICINES_USE_PRESCRIPTIONS_SHOWN(R.string.prefkey_medicines_use_prescription_shown),
    MEDICINES_LEGACY_DB_ENABLED(R.string.prefkey_medicines_db_enabled),
    // Test data module
    TEST_DATA_GENERATED(R.string.prefkey_testdata_generated),
    // Patients
    PATIENTS_ACTIVE(R.string.prefkey_patients_active),
    // Settings
    SETTINGS_ALARM_INSISTENT(R.string.prefkey_settings_alarm_insistent),
    SETTINGS_ALARM_REMINDER_WINDOW(R.string.prefkey_settings_alarm_reminder_window),
    SETTINGS_ALARM_REPEAT_ENABLED(R.string.prefkey_settings_alarm_repeat_enabled),
    SETTINGS_ALARM_REPEAT_FREQUENCY(R.string.prefkey_settings_alarm_repeat_frequency),
    SETTINGS_ALARM_NOTIFICATIONS(R.string.prefkey_settings_alarm_notifications),
    SETTINGS_ALARM_PICKUP_NOTIFICATIONS(R.string.prefkey_settings_alarm_pickup_notifications),
    SETTINGS_DATABASE_UPDATE(R.string.prefkey_settings_database_update),
    SETTINGS_NOTIFICATION_TONE(R.string.prefkey_settings_notification_tone),
    SETTINGS_INSISTENT_NOTIFICATION_TONE(R.string.prefkey_settings_insistent_notification_tone),
    SETTINGS_STOCK_ALERT_DAYS(R.string.prefkey_settings_stock_alert_days),
    // Schedules
    SCHEDULES_HELP_SHOWN(R.string.prefkey_schedules_help_shown),
    // Unlock PIN
    PRIVACY(R.string.prefkey_privacy),
    UNLOCK_PIN(R.string.prefkey_pin_lock),
    UNLOCK_PIN_HASH(R.string.prefkey_unlock_pin_hash),
    UNLOCK_PIN_SALT(R.string.prefkey_unlock_pin_salt),
    FINGERPRINT_ENABLED(R.string.prefkey_fingerprint_enabled);

    @StringRes
    private final int stringId;

    PreferenceKeys(int stringId) {
        this.stringId = stringId;
    }


    public String key() {
        return toString();
    }

    @Override
    public String toString() {
        return CalendulaApp.getContext().getString(stringId);
    }
}
