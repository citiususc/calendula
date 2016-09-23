package es.usc.citius.servando.calendula.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import es.usc.citius.servando.calendula.R;


public class SchedulesHelpActivity extends IntroActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){

        setFullscreen(true);
        super.onCreate(savedInstanceState);

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.schedules_help_1)
                .background(R.color.schedule_help_background)
                .backgroundDark(R.color.schedule_help_background_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.schedules_help_2)
                .background(R.color.schedule_help_background)
                .backgroundDark(R.color.schedule_help_background_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.schedules_help_3)
                .background(R.color.schedule_help_background)
                .backgroundDark(R.color.schedule_help_background_dark)
                .build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("PREFERENCE_SCHEDULE_HELP_SHOWN", true).commit();
    }

}