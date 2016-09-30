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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.fragments.dosePickers;


import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import com.filippudak.ProgressPieView.ProgressPieView;

import es.usc.citius.servando.calendula.R;


/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class PillDosePickerFragment extends DosePickerFragment {

    NumberPicker integerPicker;
    NumberPicker fractionPicker;
    ProgressPieView progress1;
    ProgressPieView progress2;

    String[] integers = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    int[] integersValues = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    String[] fractions = new String[]{"0", "1/8", "1/4", "1/2", "3/4"};
    float[] fractionValues = new float[]{0, 0.125f, 0.25f, 0.5f, 0.75f};

    @Override
    protected int getLayoutResource() {
        return R.layout.med_dose_picker;
    }

    @Override
    protected void setupRootView(View rootView) {

        integerPicker = (NumberPicker) rootView.findViewById(R.id.dosePickerInteger);
        fractionPicker = (NumberPicker) rootView.findViewById(R.id.dosePickerDecimal);

        progress1 = (ProgressPieView) rootView.findViewById(R.id.progressPieView);
        progress2 = (ProgressPieView) rootView.findViewById(R.id.progressPieView2);

        integerPicker.setMaxValue(integers.length - 1);
        integerPicker.setMinValue(0);
        integerPicker.setWrapSelectorWheel(false);
        integerPicker.setDisplayedValues(integers);
        integerPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        fractionPicker.setMaxValue(fractions.length - 1);
        fractionPicker.setMinValue(0);
        fractionPicker.setWrapSelectorWheel(false);
        fractionPicker.setDisplayedValues(fractions);
        fractionPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateProgress();
            }
        };

        integerPicker.setOnValueChangedListener(valueChangeListener);
        fractionPicker.setOnValueChangedListener(valueChangeListener);

    }

    @Override
    protected void setInitialValue(double initialDose) {
        int integerPart = (int) initialDose;
        double fraction = initialDose - integerPart;

        if (fraction == 0.125) {
            fractionPicker.setValue(1);
            progress2.setProgress(12);
            progress2.setText("1/8");
        } else if (fraction == 0.25) {
            fractionPicker.setValue(2);
            progress2.setProgress(25);
            progress2.setText("1/4");
        } else if (fraction == 0.5) {
            fractionPicker.setValue(3);
            progress2.setProgress(50);
            progress2.setText("1/2");
        } else if (fraction == 0.75) {
            fractionPicker.setValue(4);
            progress2.setProgress(75);
            progress2.setText("3/4");
        } else {
            fractionPicker.setValue(0);
            progress2.setProgress(0);
            progress2.setText("0");
        }

        integerPicker.setValue(integerPart);

        if (integerPart > 0) {
            progress1.setProgress(100);
            progress1.setText(integerPart + "");
        }
    }


    @Override
    protected double getSelectedDose() {
        double dose = integersValues[integerPicker.getValue()] + fractionValues[fractionPicker.getValue()];
        Log.d("VALUE ", integersValues[integerPicker.getValue()] + "." + fractionValues[fractionPicker.getValue()]);
        return dose;
    }

    void updateProgress() {
        double integerPart = integersValues[integerPicker.getValue()];
        double fraction = fractionValues[fractionPicker.getValue()];


        progress1.setProgress(100);

        if (fraction == 0.125) {
            progress2.setProgress(12);
            progress2.setText("1/8");

        } else if (fraction == 0.25) {
            progress2.setProgress(25);
            progress2.setText("1/4");
        } else if (fraction == 0.5) {
            progress2.setProgress(50);
            progress2.setText("1/2");
        } else if (fraction == 0.75) {
            progress2.setProgress(75);
            progress2.setText("3/4");
        } else {
            progress2.setProgress(0);
            progress2.setText("");
        }

        if (integerPart > 0) {
            progress1.setProgress(100);
            progress1.setText(((int) integerPart) + "");
        } else {
            progress1.setProgress(0);
            progress1.setText("");
        }


    }



}