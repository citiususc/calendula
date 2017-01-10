/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.download.DownloadDatabaseHelper;
import es.usc.citius.servando.calendula.drugdb.download.InstallDatabaseService;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.StockModule;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.PermissionUtils;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.view.CustomListPreference;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int REQ_CODE_EXTERNAL_STORAGE_RINGTONE = 20;
    public static final int REQ_CODE_EXTERNAL_STORAGE_MED_DB = 21;

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static final String TAG = "SettingsActivity";
    private static final String KEY_PRESCRIPTIONS_DATABASE = "prescriptions_database";
    static Context ctx;
    static Activity activity;
    static Context appCtx;
    static String lastValidDatabase;
    static String NONE;
    static String SETTING_UP;
    static boolean settingUp = false;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(final Preference preference, final Object value) {
            final String stringValue = value.toString();

            if (preference instanceof es.usc.citius.servando.calendula.util.RingtonePreference) {
                String p = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                if (PermissionUtils.useRunTimePermissions() && !PermissionUtils.hasPermission(activity, p)) {
                    preference.setSummary("");
                } else {
                    Uri ringtoneUri = Uri.parse(stringValue);
                    Ringtone ringtone = RingtoneManager.getRingtone(ctx, ringtoneUri);
                    String name = ringtone != null ? ringtone.getTitle(ctx) : ctx.getString(R.string.pref_notification_tone_sum);
                    preference.setSummary(name);
                }


            } else if (preference instanceof ListPreference) {
                if (preference.getKey().equals(KEY_PRESCRIPTIONS_DATABASE)) {
                    //Toast.makeText(ctx, "Value: " + stringValue + ", settingUp:" + settingUp, Toast.LENGTH_SHORT).show();
                    if (!onUpdatePrescriptionsDatabasePreference((ListPreference) preference, stringValue)) {
                        //return false;
                    }
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);
                    if (stringValue.equals(SETTING_UP)) {
                        preference.setSummary(stringValue);
                        preference.setEnabled(false);
                    } else {
                        // Set the summary to reflect the new value.
                        preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : "Unknown");
                    }
                } else {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };
    BroadcastReceiver onDBSetupComplete;

    static boolean onUpdatePrescriptionsDatabasePreference(final ListPreference preference, final String stringValue) {
        Log.d(TAG, "New value: " + stringValue);
        if (!settingUp && !stringValue.equals(lastValidDatabase) && !NONE.equalsIgnoreCase(stringValue) && !SETTING_UP.equals(stringValue)) {
            DownloadDatabaseHelper.instance().showDownloadDialog(ctx, stringValue, new DownloadDatabaseHelper.DownloadDatabaseDialogCallback() {
                @Override
                public void onDownloadAcceptedOrCancelled(boolean accepted) {
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
                    SharedPreferences.Editor edit = settings.edit();
                    final String val = accepted ? SETTING_UP : lastValidDatabase;
                    edit.putString(KEY_PRESCRIPTIONS_DATABASE, val);
                    edit.apply();
                    if (accepted) {
                        settingUp = true;
                        preference.setEnabled(false);
                    }
                    preference.setValue(val);
                    bindPreferenceSummaryToValue(preference, true);
                }
            });
            return false;
        } else if (stringValue.equalsIgnoreCase(NONE)) {
            try {
                DBRegistry.instance().clear();
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
                SharedPreferences.Editor edit = settings.edit();
                edit.putString(KEY_PRESCRIPTIONS_DATABASE, settings.getString("last_valid_database", NONE));
                //edit.putString("last_valid_database", settings.getString("last_valid_database", NONE));
                edit.apply();
                lastValidDatabase = NONE;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference, boolean triggerListener) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        //if(triggerListener) {
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
        //}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("alarm_repeat_frequency".equals(key)) {
            Log.d("SettingsActivity", "Update " + key);
            AlarmScheduler.instance().updateAllAlarms(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_EXTERNAL_STORAGE_RINGTONE:
                PermissionUtils.markedPermissionAsAsked(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CheckBoxPreference ins = (CheckBoxPreference) findPreference("alarm_insistent");
                    ins.setChecked(true);
                }
                break;
            case REQ_CODE_EXTERNAL_STORAGE_MED_DB:
                PermissionUtils.markedPermissionAsAsked(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CheckBoxPreference ins = (CheckBoxPreference) findPreference("enable_prescriptions_db");
                    ins.setChecked(true);
                    showDatabaseDialog();
                    break;
                }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NONE = getString(R.string.database_none_id);
        SETTING_UP = getString(R.string.database_setting_up);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        lastValidDatabase = settings.getString("last_valid_database", NONE);

        settingUp = SETTING_UP.equals(settings.getString(KEY_PRESCRIPTIONS_DATABASE, null));

        if (settingUp && !DownloadDatabaseHelper.instance().isDBDownloadingOrInstalling(this)) {
            Log.d(TAG, "onCreate: " + "Something weird happened. It seems like InstallDatabaseService was killed while working!");
            settings.edit().putString(KEY_PRESCRIPTIONS_DATABASE, NONE).apply();
            settingUp = false;
        }

        Log.d(TAG, "sa onCreate: prescriptions_database: " + settings.getString(KEY_PRESCRIPTIONS_DATABASE, null));
        Log.d(TAG, "sa onCreate: SETTING_UP: " + SETTING_UP);
        Log.d(TAG, "sa onCreate: setting_up: " + settingUp);

        onDBSetupComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                Preference p = findPreference(KEY_PRESCRIPTIONS_DATABASE);
                p.setEnabled(true);
                bindPreferenceSummaryToValue(p, false);
                settingUp = false;
            }
        };

        IntentFilter downloadFinished = new IntentFilter();
        downloadFinished.addAction(InstallDatabaseService.ACTION_COMPLETE);
        downloadFinished.addAction(InstallDatabaseService.ACTION_ERROR);
        registerReceiver(onDBSetupComplete, downloadFinished);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ScreenUtils.setStatusBarColor(this, getResources().getColor(R.color.dark_grey_home));

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        toolbar.setBackgroundColor(getResources().getColor(R.color.dark_grey_home));
        toolbar.setNavigationIcon(getNavigationIcon());
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        root.addView(toolbar, 0); // insert at top

        activity = this;
        ctx = getBaseContext();
        ctx = SettingsActivity.this;
        appCtx = getApplicationContext();
        setupSimplePreferencesScreen();

        if (getIntent() != null && getIntent().getBooleanExtra("show_database_dialog", false)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CheckBoxPreference ins = (CheckBoxPreference) findPreference("enable_prescriptions_db");
                    if (ins.isChecked()) {
                        showDatabaseDialog();
                    } else {
                        boolean permitted = checkPreferenceAskForPermission(true, REQ_CODE_EXTERNAL_STORAGE_MED_DB);
                        if (permitted) {
                            ins.setChecked(true);
                            showDatabaseDialog();
                        }
                    }
                }
            }, 500);

        }

    }

    protected Drawable getNavigationIcon() {
        return new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(R.color.white);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(onDBSetupComplete);
        super.onDestroy();
    }

    private void showDatabaseDialog() {
        ((CustomListPreference) findPreference(KEY_PRESCRIPTIONS_DATABASE)).show();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader2 = new PreferenceCategory(this);
        fakeHeader2.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader2);
        addPreferencesFromResource(R.xml.pref_notification);

        if (ModuleManager.isEnabled(StockModule.ID)) {
            PreferenceCategory fakeHeader3 = new PreferenceCategory(this);
            fakeHeader3.setTitle(R.string.pref_header_stock);
            getPreferenceScreen().addPreference(fakeHeader3);
            addPreferencesFromResource(R.xml.pref_stock);
            bindPreferenceSummaryToValue(findPreference("stock_alert_days"), true);
        }

        // Add 'data and sync' preferences, and a corresponding header.
        //fakeHeader = new PreferenceCategory(this);
        //fakeHeader.setTitle(R.string.pref_header_data_sync);
        //getPreferenceScreen().addPreference(fakeHeader);
        //addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("display_name"), true);
        bindPreferenceSummaryToValue(findPreference("alarm_repeat_frequency"), true);
        bindPreferenceSummaryToValue(findPreference("alarm_reminder_window"), true);
        bindPreferenceSummaryToValue(findPreference("pref_notification_tone"), true);
        bindPreferenceSummaryToValue(findPreference(KEY_PRESCRIPTIONS_DATABASE), true);

        findPreference("alarm_insistent").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return checkPreferenceAskForPermission(o, REQ_CODE_EXTERNAL_STORAGE_RINGTONE);
            }
        });
        findPreference("enable_prescriptions_db").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ListPreference p = (ListPreference) findPreference(KEY_PRESCRIPTIONS_DATABASE);
                if (settingUp) {
                    // database cannot be disabled while it's setting up
                    return false;
                }
                final boolean val = (boolean) o;
                final boolean hasPermission = checkPreferenceAskForPermission(o, REQ_CODE_EXTERNAL_STORAGE_MED_DB);
                if (val && hasPermission) {
                    showDatabaseDialog();
                }
                return hasPermission;
            }
        });


        if (!CalendulaApp.isPharmaModeEnabled()) {
            Preference alarmPk = findPreference("alarm_pickup_notifications");
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(alarmPk);
        }
    }

    private boolean checkPreferenceAskForPermission(Object o, int reqCode) {
        boolean val = (boolean) o;
        String p = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (val && PermissionUtils.useRunTimePermissions() && !PermissionUtils.hasPermission(activity, p)) {
            if (PermissionUtils.shouldAskForPermission(activity, p))
                PermissionUtils.requestPermissions(activity, new String[]{p}, reqCode);
            else
                showStupidUserDialog();
            return false;
        }
        return true;
    }

    private void showStupidUserDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // "Remove " + m.name() + "?"
        builder.setMessage(getString(R.string.permission_dialog_go_to_settings))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.permission_dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PermissionUtils.goToAppSettings(activity);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
