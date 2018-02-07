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


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Presentation;


/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class DefaultDosePickerFragment extends DosePickerFragment {

    TextView unitsText;
    EditText text;
    Presentation presentation;

    @Override
    protected int getLayoutResource() {
        return R.layout.default_dose_picker;
    }

    @Override
    protected void setupRootView(View rootView) {
        unitsText = (TextView) rootView.findViewById(R.id.units_text);
        text = (EditText) rootView.findViewById(R.id.editText);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())) {
                    double value = Double.parseDouble(s.toString().trim());
                    unitsText.setText(presentation.units(getResources(), value));
                }
            }
        });
    }

    @Override
    protected void setInitialValue(double initialDose) {
        Bundle args = getArguments();
        Presentation p = (Presentation) args.getSerializable("presentation");
        p = p != null ? p : Presentation.UNKNOWN;
        presentation = p;
        unitsText.setText(p.units(getResources(), initialDose));
        text.setText(String.valueOf(initialDose));
    }


    @Override
    protected double getSelectedDose() {
        String t = text.getText().toString();
        return Double.valueOf(t);
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        closeKeyboard(getActivity(), text.getWindowToken());
    }

    @Override
    protected void onDone() {
        super.onDone();
        closeKeyboard(getActivity(), text.getWindowToken());
    }
}