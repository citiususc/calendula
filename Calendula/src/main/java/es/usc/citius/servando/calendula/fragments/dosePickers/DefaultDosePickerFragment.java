package es.usc.citius.servando.calendula.fragments.dosePickers;


import android.os.Bundle;
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

    @Override
    protected int getLayoutResource() {
        return R.layout.default_dose_picker;
    }

    @Override
    protected void setupRootView(View rootView) {
        unitsText = (TextView) rootView.findViewById(R.id.units_text);
        text = (EditText) rootView.findViewById(R.id.editText);
    }

    @Override
    protected void setInitialValue(double initialDose) {
        Bundle args = getArguments();
        Presentation p = (Presentation) args.getSerializable("presentation");
        p = p!=null ? p : Presentation.UNKNOWN;
        unitsText.setText(p.units(getResources()));
        text.setText(""+initialDose);
    }

    @Override
    protected double getSelectedDose() {
        String t = text.getText().toString();
        return Double.valueOf(t);
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        closeKeyboard(getActivity(),text.getWindowToken());
    }

    @Override
    protected void onDone() {
        super.onDone();
        closeKeyboard(getActivity(), text.getWindowToken());
    }
}