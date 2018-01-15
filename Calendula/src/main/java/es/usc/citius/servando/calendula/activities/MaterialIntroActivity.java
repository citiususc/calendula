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

public class MaterialIntroActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setFullscreen(true);
        super.onCreate(savedInstanceState);

        setSkipEnabled(true);

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_1)
                .background(R.color.intro_slide_1_light)
                .backgroundDark(R.color.intro_slide_1_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_3)
                .background(R.color.intro_slide_3_light)
                .backgroundDark(R.color.intro_slide_3_dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.intro_slide_2)
                .background(R.color.intro_slide_2_light)
                .backgroundDark(R.color.intro_slide_2_dark)
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
        PreferenceUtils.edit().putBoolean(PreferenceKeys.HOME_INTRO_SHOWN.key(), true).apply();
    }
}