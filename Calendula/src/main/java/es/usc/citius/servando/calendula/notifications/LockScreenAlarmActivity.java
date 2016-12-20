package es.usc.citius.servando.calendula.notifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Alarm activity that plays a ringtone over the lock screen when
 * the user has enabled insistent notifications
 *
 * @author angelpinheiro
 */
public class LockScreenAlarmActivity extends AppCompatActivity {

    private static final String TAG = "LockScreenAlarmActivity";

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

    @Override
    public void onBackPressed() {
        stopPlayingAlarm();
        animateAndFinish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupForVisibilityOverLockScreen();
        setContentView(R.layout.activity_lock_screen_alarm);
        anim = (ImageView) findViewById(R.id.anim_image);

        if(getIntent() != null){
            target = getIntent().getParcelableExtra("target");
            Log.d(TAG, "Target " + (target != null));
        }

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            // stop sound and vibration if the user
            // press volume down button
            stopPlayingAlarm();
        }else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            // restart sound and vibration if the user
            // press volume down up
            startPlayingAlarm();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        stopPlayingAlarm();
        super.onDestroy();
    }

    /**
     * Sets an animated drawable as background
     */
    private void startAlarmAnimation() {
        try {
            anim.setImageDrawable(new GifDrawable(getResources(), R.drawable.animated_clock));
        } catch (Exception e) {
            Log.e(TAG, "An error occurred loading animation", e);
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
            mediaPlayer.setVolume(1,1);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
        } catch (Exception e) {
            Log.e(TAG, "An error occurred while preparing media player", e);
            finish();
        }
    }

    /**
     * Start vibrating and playing sound
     */
    private void startPlayingAlarm() {
        Log.d(TAG, "startPlayingAlarm() called");
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
        Log.d(TAG, "stopPlayingAlarm() called");
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
        SharedPreferences prefs = PreferenceUtils.instance().preferences();
        String ringtonePref = prefs.getString("pref_notification_tone", null);
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
                Log.d(TAG, "Target " + (target != null));
                if(target != null){
                    startActivity(target);
                }
                finish();
            }
        }, 600);
    }
}
