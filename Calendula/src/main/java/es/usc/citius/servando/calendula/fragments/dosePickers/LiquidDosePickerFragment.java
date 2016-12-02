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

package es.usc.citius.servando.calendula.fragments.dosePickers;


import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import es.usc.citius.servando.calendula.R;


/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class LiquidDosePickerFragment extends DosePickerFragment {

    private static final int MAX_DOSE = 50;

    SeekBar seekBar;
    TextView seekBarText;

    double initialDose = 10.0f;
    double dose = initialDose;

    @Override
    protected int getLayoutResource() {
        return R.layout.liquid_dose_picker;
    }

    @Override
    protected void setupRootView(View rootView) {

        seekBarText = (TextView) rootView.findViewById(R.id.seek_bar_text);

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setMax(MAX_DOSE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarText.setText(progress + " ML");
                dose = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void setInitialValue(double initialDose) {
        seekBar.setProgress((int) initialDose);
        seekBarText.setText(((int) initialDose) + " ML");
        dose = initialDose;
    }

    @Override
    protected double getSelectedDose() {
        return dose;
    }

}