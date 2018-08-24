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

package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.codetroopers.betterpickers.timepicker.TimePickerBuilder;
import com.codetroopers.betterpickers.timepicker.TimePickerDialogFragment;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.Snack;


public class RoutineCreateOrEditFragment extends DialogFragment implements RadialTimePickerDialogFragment.OnTimeSetListener, TimePickerDialogFragment.TimePickerDialogHandler {

    private static final String TAG = "RoutineCoEFragment";

    OnRoutineEditListener mRoutineEditCallback;
    Routine mRoutine;


    @BindView(R.id.button2)
    Button timeButton;
    @BindView(R.id.routine_edit_name)
    TextView mNameTextView;

    Button mConfirmButton;

    Unbinder unbinder = null;

    int hour;
    int minute;

    int pColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_routine, container, false);
        ButterKnife.bind(this, rootView);

        pColor = DB.patients().getActive(getActivity()).getColor();

        timeButton.setTextColor(pColor);

        long routineId = -1;

        if (getArguments() != null) {
            routineId = getArguments().getLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
        }

        if (routineId == -1 && savedInstanceState != null) {
            routineId = savedInstanceState.getLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
        }

        setRoutine(routineId);

        if (getDialog() != null) {
            getDialog().setTitle(R.string.title_create_routine_activity);
            mConfirmButton = (Button) rootView.findViewById(R.id.done_button);
            mConfirmButton.setVisibility(View.VISIBLE);
            mConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEdit();
                }
            });
        }


        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float density = getResources().getDisplayMetrics().densityDpi;
                LogUtil.d(TAG, "Density: " + density);
                if (density >= DisplayMetrics.DENSITY_XHIGH || Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    RadialTimePickerDialogFragment timePickerFragment = new RadialTimePickerDialogFragment()
                            .setOnTimeSetListener(RoutineCreateOrEditFragment.this)
                            .setStartTime(hour, minute);
                    timePickerFragment.show(getActivity().getSupportFragmentManager(), "TIME_PICKER_FRAGMENT");
                } else {
                    TimePickerBuilder tpb = new TimePickerBuilder()
                            .setFragmentManager(getActivity().getSupportFragmentManager())
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light);
                    tpb.addTimePickerDialogHandler(RoutineCreateOrEditFragment.this);
                    tpb.show();
                }

            }
        });

        updateTime();


        return rootView;
    }

    public void setRoutine(long routineId) {
        if (routineId != -1) {
            mRoutine = Routine.findById(routineId);
            hour = mRoutine.getTime().getHourOfDay();
            minute = mRoutine.getTime().getMinuteOfHour();
            setRoutine(mRoutine);
        } else {
            DateTime now = DateTime.now();
            hour = now.getHourOfDay();
            minute = now.getMinuteOfHour();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRoutine != null)
            outState.putLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, mRoutine.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
    }

    public void onEdit() {

        String name = mNameTextView.getText().toString();

        if (name != null && name.length() > 0) {


            // if editing
            if (mRoutine != null) {
                mRoutine.setName(name);
                mRoutine.setTime(new LocalTime(hour, minute));
                DB.routines().saveAndFireEvent(mRoutine);
                //mRoutine.save();
                if (mRoutineEditCallback != null) {
                    mRoutineEditCallback.onRoutineEdited(mRoutine);
                }
            }
            // if creating
            else {
                mRoutine = new Routine(new LocalTime(hour, minute), name);
                mRoutine.setPatient(DB.patients().getActive(getContext()));
                LogUtil.d(TAG, "Routine created");
                DB.routines().saveAndFireEvent(mRoutine);
                if (mRoutineEditCallback != null) {
                    mRoutineEditCallback.onRoutineCreated(mRoutine);
                }
            }
        } else {
            Snack.show(R.string.medicine_no_name_error_message, getActivity());
        }
    }

    public void showDeleteConfirmationDialog(final Routine r) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String message;

        if (r.getScheduleItems().size() > 0) {
            message = String.format(getString(R.string.remove_routine_message_long), r.getName());
        } else {
            message = String.format(getString(R.string.remove_routine_message_short), r.getName());
        }

        builder.setMessage(message)
                .setCancelable(true)
                .setTitle(getString(R.string.remove_routine_dialog_title))
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mRoutineEditCallback != null) {
                            mRoutineEditCallback.onRoutineDeleted(mRoutine);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        LogUtil.d(TAG, "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnRoutineEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnRoutineEditListener) {
            LogUtil.d(TAG, "Set onRoutineEditListener onAttach");
            mRoutineEditCallback = (OnRoutineEditListener) activity;
        }
    }

    // optionally set the listener manually
    public void setOnRoutineEditListener(OnRoutineEditListener l) {
        mRoutineEditCallback = l;
    }

    @Override
    public void onDialogTimeSet(int ref, int hour, int minute) {
        onTimeSet(null, hour, minute);
    }

    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        updateTime();
    }

    void updateTime() {
        timeButton.setText(new LocalTime(hour, minute).toString("HH:mm"));
    }

    private void setRoutine(Routine r) {
        LogUtil.d(TAG, "Routine set: " + r.getName());
        mRoutine = r;
        mNameTextView.setText(mRoutine.getName());
        updateTime();
    }

    // Container Activity must implement this interface
    public interface OnRoutineEditListener {
        void onRoutineEdited(Routine r);

        void onRoutineCreated(Routine r);

        void onRoutineDeleted(Routine r);
    }

}