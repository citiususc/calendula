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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 10/7/15.
 */
public abstract class DosePickerFragment extends DialogFragment {

    private static final String TAG = "DosePickerFragment";
    OnDoseSelectedListener mDoseSelectedListener;

    private double initialDose = 1.0f;

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        initialDose = args.getDouble("dose", 1d);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtil.d(TAG, "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnDoseSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnDoseSelectedListener) {
            mDoseSelectedListener = (OnDoseSelectedListener) activity;
        }
    }

    public void setOnDoseSelectedListener(OnDoseSelectedListener l) {
        mDoseSelectedListener = l;
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

    protected abstract int getLayoutResource();

    protected abstract void setupRootView(View root);

    protected abstract void setInitialValue(double initialDose);

    protected abstract double getSelectedDose();

    protected void onDone() {
        // do nothing by default
    }

    protected void onCancel() {
        // do nothing by default
    }

    // Container Activity must implement this interface
    public interface OnDoseSelectedListener {
        void onDoseSelected(double dose);
    }


}
