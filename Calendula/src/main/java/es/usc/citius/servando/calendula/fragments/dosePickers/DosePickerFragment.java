package es.usc.citius.servando.calendula.fragments.dosePickers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 10/7/15.
 */
public abstract class DosePickerFragment extends DialogFragment {

    OnDoseSelectedListener mDoseSelectedListener;

    private double initialDose = 1.0f;


    protected abstract int getLayoutResource();

    protected abstract void setupRootView(View root);

    protected abstract void setInitialValue(double initialDose);

    protected abstract double getSelectedDose();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        initialDose = args.getDouble("dose", 1d);
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(getLayoutResource(), null);

        setupRootView(rootView);
        setInitialValue(initialDose);

        if (getDialog() != null) {
            getDialog().setTitle(R.string.title_select_dose_dialog);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .setTitle(R.string.title_select_dose_dialog)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDone();
                        if (mDoseSelectedListener != null)
                            mDoseSelectedListener.onDoseSelected(getSelectedDose());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onCancel();
                    }
                }).create();
    }

    protected void onDone() {
        // do nothing by default
    }

    protected void onCancel(){
        // do nothing by default
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }


}
