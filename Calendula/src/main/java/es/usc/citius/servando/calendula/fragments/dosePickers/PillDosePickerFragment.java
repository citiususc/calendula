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


import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.filippudak.ProgressPieView.ProgressPieView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.usc.citius.servando.calendula.R;


/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class PillDosePickerFragment extends DosePickerFragment {

    public static final int MAX_DISPLAY_PILLS = 3;
    NumberPicker integerPicker;
    NumberPicker fractionPicker;
    LinearLayout graphicsLayout;
    String[] integers = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    int[] integersValues = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    String[] fractions = new String[]{"0", "1/8", "1/4", "1/2", "3/4"};
    float[] fractionValues = new float[]{0, 0.125f, 0.25f, 0.5f, 0.75f};
    List<ProgressPieView> pills;
    ProgressPieView fractionPill = null;
    private int lastInt = 0;

    void addWholePills(final int number) {
        for (int i = 0; i < number; i++) {
            ProgressPieView p = new ProgressPieView(getContext());
            p.setProgress(100);
            graphicsLayout.addView(p, fractionPill != null ? graphicsLayout.getChildCount() - 1 : graphicsLayout.getChildCount());
            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) p.getLayoutParams();
            layoutParams.setMargins(0, 0, 10, 0);
            p.setLayoutParams(layoutParams);
            pills.add(p);
        }
    }

    void updateProgress() {
        int integerPart = integersValues[integerPicker.getValue()];
        float fraction = fractionValues[fractionPicker.getValue()];

        // whole pills
        if (integerPart > MAX_DISPLAY_PILLS && lastInt > MAX_DISPLAY_PILLS) {
            pills.get(0).setText(String.format(Locale.getDefault(), "%1$d", integerPart));
        } else {
            if (integerPart < lastInt) {
                // remove pills...
                if (lastInt > MAX_DISPLAY_PILLS) {
                    graphicsLayout.removeView(pills.get(0));
                    pills.remove(0);
                    addWholePills(integerPart);
                } else {
                    final int diff = lastInt - integerPart;
                    for (int i = 0; i < diff; i++) {
                        graphicsLayout.removeView(pills.get(pills.size() - 1));
                        pills.remove(pills.size() - 1);
                    }
                }
            } else if (integerPart > 0 && integerPart > lastInt) {
                if (integerPart > MAX_DISPLAY_PILLS) {
                    while (!pills.isEmpty()) {
                        graphicsLayout.removeView(pills.get(pills.size() - 1));
                        pills.remove(pills.size() - 1);
                    }
                    ProgressPieView p = new ProgressPieView(getContext());
                    p.setProgress(100);
                    p.setText(String.format(Locale.getDefault(), "%1$d", integerPart));
                    graphicsLayout.addView(p, fractionPill != null ? graphicsLayout.getChildCount() - 1 : graphicsLayout.getChildCount());
                    final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) p.getLayoutParams();
                    layoutParams.setMargins(0, 0, 10, 0);
                    p.setLayoutParams(layoutParams);
                    pills.add(p);
                } else {
                    final int diff = integerPart - lastInt;
                    addWholePills(diff);
                }
            }
        }

        // pill fraction
        if (fraction > 0) {
            final int progress = (int) (100 * fraction);
            if (fractionPill != null) {
                fractionPill.animateProgressFill(progress);
            } else {
                ProgressPieView p = new ProgressPieView(getContext());
                graphicsLayout.addView(p);
                p.animateProgressFill(progress);
                fractionPill = p;
            }
        } else {
            if (fractionPill != null) {
                graphicsLayout.removeView(fractionPill);
                fractionPill = null;
            }
        }

        lastInt = integerPart;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.med_dose_picker;
    }

    @Override
    protected void setupRootView(View rootView) {

        integerPicker = (NumberPicker) rootView.findViewById(R.id.dosePickerInteger);
        fractionPicker = (NumberPicker) rootView.findViewById(R.id.dosePickerDecimal);
        graphicsLayout = (LinearLayout) rootView.findViewById(R.id.graphics_layout);


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

        pills = new ArrayList<>();

    }

    @Override
    protected void setInitialValue(double initialDose) {
        int integerPart = (int) initialDose;
        double fraction = initialDose - integerPart;

        integerPicker.setValue(integerPart);
        for (int i = 0; i < fractionValues.length; i++) {
            if (fractionValues[i] == fraction) {
                fractionPicker.setValue(i);
                break;
            }
        }
        updateProgress();
    }

    @Override
    protected double getSelectedDose() {
        double dose = integersValues[integerPicker.getValue()] + fractionValues[fractionPicker.getValue()];
        Log.d("VALUE ", integersValues[integerPicker.getValue()] + "." + fractionValues[fractionPicker.getValue()]);
        return dose;
    }


}