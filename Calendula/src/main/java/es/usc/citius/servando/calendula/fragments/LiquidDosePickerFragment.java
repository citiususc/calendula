package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import es.usc.citius.servando.calendula.R;


/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class LiquidDosePickerFragment extends DialogFragment {

    OnDoseSelectedListener mDoseSelectedListener;

    SeekBar seekBar;
    TextView seekBarText;



    double initialDose = 10.0f;
    double dose = initialDose;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        initialDose = args.getDouble("dose", 1d);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.liquid_dose_picker, null);
        seekBarText = (TextView) rootView.findViewById(R.id.seek_bar_text);

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setMax(30);
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

        setInitialValue();

        if (getDialog() != null) {
            getDialog().setTitle(R.string.title_select_dose_dialog);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .setTitle(R.string.title_select_dose_dialog)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDoseSelectedListener != null)
                            mDoseSelectedListener.onDoseSelected(getDose());
                    }
                })
                .setNegativeButton(R.string.cancel, null).create();
    }

    private void setInitialValue() {
        seekBar.setProgress((int)initialDose);
        seekBarText.setText(((int) initialDose) + " ML");
    }

    private double getDose() {
        return dose;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnDoseSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnDoseSelectedListener) {
            mDoseSelectedListener = (OnDoseSelectedListener) activity;
        }
    }

    public void setOnDoseSelectedListener(OnDoseSelectedListener l) {
        mDoseSelectedListener = l;
    }

    // Container Activity must implement this interface
    public interface OnDoseSelectedListener {
        void onDoseSelected(double dose);
    }

}