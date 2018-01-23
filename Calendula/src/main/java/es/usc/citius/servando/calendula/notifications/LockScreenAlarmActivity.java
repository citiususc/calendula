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

package es.usc.citius.servando.calendula.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Alarm activity that plays a ringtone over the lock screen when
 * the user has enabled insistent notifications
 *
 * @author angelpinheiro
 */
public class LockScreenAlarmActivity extends AppCompatActivity {

    private static final String TAG = "LockScreenAlarmAct";

    public static final String STOP_SIGNAL = "finish_lock_screen_alarm_activity";

    // vibration pattern
    public static long[] VIB_PATTERN = new long[]{
            1000, // 1s delay
            200,  // vibration
            100,  // pause
            300,  // vibration
            400,  // pause
            200,  // vibration
            100,  // pause
            300,  // vibration
            400,  // pause
            200,  // vibration
            100,  // pause
            300,  // vibration
            2000  // pause
    };

    private ImageView anim;
    private Handler h = new Handler();
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private ImageButton confirmButton;
    private View confirmButtonBackground;
    private Intent target = null;

    private BroadcastReceiver finishReceiver;

    @Override
    public void onBackPressed() {
        stopPlayingAlarm();
        animateAndFinish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            // stop sound and vibration if the user
            // press volume down button
            stopPlayingAlarm();
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            // restart sound and vibration if the user
            // press volume down up
            startPlayingAlarm();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupForVisibilityOverLockScreen();
        setContentView(R.layout.activity_lock_screen_alarm);
        anim = (ImageView) findViewById(R.id.anim_image);

        if (getIntent() != null) {
            target = getIntent().getParcelableExtra("target");
            LogUtil.d(TAG, "Target " + (target != null));
        }

        registerFinishReceiver();

        confirmButtonBackground = findViewById(R.id.confirm_button_bg);
        confirmButton = (ImageButton) findViewById(R.id.confirm_btn);
        confirmButton.setImageDrawable(IconUtils.icon(this, CommunityMaterial.Icon.cmd_alarm_off, R.color.white, 70, 10));
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateAndFinish();
            }
        });
        startPlayingAlarm();
        startAlarmAnimation();
    }

    @Override
    protected void onDestroy() {
        stopPlayingAlarm();
        unregisterFinishReceiver();
        super.onDestroy();
    }

    /**
     * Registers a broadcast receiver that will finish the activity on STOP_SIGNAL. The objective
     * is to stop the alarm after the user has confirmed or cancelled an intake, for example by
     * clicking a notification action.
     */
    private void registerFinishReceiver() {
        finishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals(STOP_SIGNAL)) {
                    finish();
                }
            }
        };
        registerReceiver(finishReceiver, new IntentFilter(STOP_SIGNAL));
    }

    private void unregisterFinishReceiver() {
        if (finishReceiver != null) {
            unregisterReceiver(finishReceiver);
        }
    }

    /**
     * Sets an animated drawable as background
     */
    private void startAlarmAnimation() {
        try {
            anim.setImageDrawable(new GifDrawable(getResources(), R.drawable.animated_clock));
        } catch (Exception e) {
            LogUtil.e(TAG, "An error occurred loading animation", e);
        }
    }

    /**
     * Configure media player and vibration service
     */
    private void setupMediaPlayer() {
        Uri uri = getAlarmUri();
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
        } catch (Exception e) {
            LogUtil.e(TAG, "An error occurred while preparing media player", e);
            finish();
        }
    }

    /**
     * Start vibrating and playing sound
     */
    private void startPlayingAlarm() {
        LogUtil.d(TAG, "startPlayingAlarm() called");
        setupMediaPlayer();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            vibrator.vibrate(VIB_PATTERN, 0);
        }
    }

    /**
     * Stop the media player and cancel vibration
     */
    private void stopPlayingAlarm() {
        LogUtil.d(TAG, "stopPlayingAlarm() called");
        vibrator.cancel();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Get the user notification ringtone from preferences, or the system default if undefined
     *
     * @return A ringtone for intake insistent alarms
     */
    private Uri getAlarmUri() {
        String ringtonePref = PreferenceUtils.getString(PreferenceKeys.SETTINGS_NOTIFICATION_TONE, null);
        return ringtonePref != null ? Uri.parse(ringtonePref) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    }

    /**
     * Enable activity display over the lock screen
     */
    private void setupForVisibilityOverLockScreen() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        // change status bar color if it is possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.alarm_activity_bg_color));
        }
    }

    /**
     * Play some animations and finish activity
     */
    private void animateAndFinish() {
        stopPlayingAlarm();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // hide confirm button
                confirmButton.animate().scaleXBy(-1f);
                confirmButton.animate().scaleYBy(-1f);
                // translate the animated image out of the screen
                anim.animate().alpha(0).translationYBy(-anim.getHeight() * 3);
            }
        }, 100);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // expand confirm button background
                confirmButtonBackground.animate().alpha(0).scaleXBy(10);
                confirmButtonBackground.animate().alpha(0).scaleYBy(10);
            }
        }, 300);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    finishAndRemoveTask();
//                } else {
//                    finish();
//            }
                LogUtil.d(TAG, "Target " + (target != null));
                if (target != null) {
                    startActivity(target);
                }
                finish();
            }
        }, 600);
    }
}
