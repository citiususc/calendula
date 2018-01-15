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

package es.usc.citius.servando.calendula.activities;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;


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
        PreferenceUtils.edit().putBoolean(PreferenceKeys.SCHEDULES_HELP_SHOWN.key(), true).apply();
    }

}