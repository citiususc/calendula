package es.usc.citius.servando.calendula.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import es.usc.citius.servando.calendula.R;

public class MaterialIntroActivity extends IntroActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){

        setFullscreen(true);
        super.onCreate(savedInstanceState);

        setSkipEnabled(false);

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_1)
                .background(R.color.intro_slide_1_light)
                .backgroundDark(R.color.intro_slide_1_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_2)
                .background(R.color.intro_slide_2_light)
                .backgroundDark(R.color.intro_slide_2_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_3)
                .background(R.color.intro_slide_3_light)
                .backgroundDark(R.color.intro_slide_3_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_4)
                .background(R.color.intro_slide_4_light)
                .backgroundDark(R.color.intro_slide_4_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_5)
                .background(R.color.intro_slide_5_light)
                .backgroundDark(R.color.intro_slide_5_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_6)
                .background(R.color.intro_slide_6_light)
                .backgroundDark(R.color.intro_slide_6_dark)
                .build());

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("PREFERENCE_INTRO_SHOWN", true).commit();
    }
}