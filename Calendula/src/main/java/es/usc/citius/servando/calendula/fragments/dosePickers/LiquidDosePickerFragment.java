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