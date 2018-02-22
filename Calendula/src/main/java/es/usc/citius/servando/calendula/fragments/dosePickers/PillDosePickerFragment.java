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

package es.usc.citius.servando.calendula.fragments.dosePickers;


import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.filippudak.ProgressPieView.ProgressPieView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.LogUtil;


public class PillDosePickerFragment extends DosePickerFragment {

    public static final int MAX_DISPLAY_PILLS = 3;
    private static final String TAG = "PillDosePickerFragm";
    NumberPicker leftPicker;
    NumberPicker rightPicker;
    LinearLayout graphicsLayout;
    String[] leftLabels = new String[]{"0", "1/8", "1/4", "1/2", "3/4", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    double[] leftValues = new double[]{0d, 0.125d, 0.25d, 0.5d, 0.75d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d};
    String[] rightLabels = new String[]{"0", "1/8", "1/4", "1/2", "3/4"};
    double[] rightValues = new double[]{0d, 0.125d, 0.25d, 0.5d, 0.75d};
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
        double value = leftValues[leftPicker.getValue()] + rightValues[rightPicker.getValue()];
        int integerPart = (int) value;
        double fraction = value - (double) integerPart;

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

        leftPicker = (NumberPicker) rootView.findViewById(R.id.dosePickerInteger);
        rightPicker = (NumberPicker) rootView.findViewById(R.id.dosePickerDecimal);
        graphicsLayout = (LinearLayout) rootView.findViewById(R.id.graphics_layout);


        leftPicker.setMaxValue(leftLabels.length - 1);
        leftPicker.setMinValue(0);
        leftPicker.setWrapSelectorWheel(false);
        leftPicker.setDisplayedValues(leftLabels);
        leftPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        rightPicker.setMaxValue(rightLabels.length - 1);
        rightPicker.setMinValue(0);
        rightPicker.setWrapSelectorWheel(false);
        rightPicker.setDisplayedValues(rightLabels);
        rightPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        NumberPicker.OnValueChangeListener valueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateProgress();
            }
        };

        leftPicker.setOnValueChangedListener(valueChangeListener);
        rightPicker.setOnValueChangedListener(valueChangeListener);

        pills = new ArrayList<>();

    }

    @Override
    protected void setInitialValue(double initialDose) {
        int integerPart = (int) initialDose;
        double fraction = initialDose - integerPart;

        leftPicker.setValue(integerPart);
        for (int i = 0; i < rightValues.length; i++) {
            if (rightValues[i] == fraction) {
                rightPicker.setValue(i);
                break;
            }
        }
        updateProgress();
    }

    @Override
    protected double getSelectedDose() {
        double dose = leftValues[leftPicker.getValue()] + rightValues[rightPicker.getValue()];
        LogUtil.d(TAG, leftValues[leftPicker.getValue()] + "." + rightValues[rightPicker.getValue()]);
        return dose;
    }


}