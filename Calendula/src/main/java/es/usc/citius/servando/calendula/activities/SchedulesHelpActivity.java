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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import es.usc.citius.servando.calendula.R;


public class SchedulesHelpActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean("PREFERENCE_SCHEDULE_HELP_SHOWN", true).apply();
    }

}