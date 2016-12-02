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

package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.util.Snack;


/**
 * Created by joseangel.pineiro
 */
public class RoutineCreateOrEditFragment extends DialogFragment implements RadialTimePickerDialog.OnTimeSetListener, TimePickerDialogFragment.TimePickerDialogHandler {

    OnRoutineEditListener mRoutineEditCallback;
    Routine mRoutine;

    Button timeButton;
    TextView mNameTextView;
    Button mConfirmButton;

    int hour;
    int minute;

    int pColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_routine, container, false);

        pColor = DB.patients().getActive(getActivity()).color();

        mNameTextView = (TextView) rootView.findViewById(R.id.routine_edit_name);
        timeButton = (Button) rootView.findViewById(R.id.button2);

        timeButton.setTextColor(pColor);

        long routineId = -1;

        if (getArguments() != null) {
            routineId = getArguments().getLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
        }

        if (routineId == -1 && savedInstanceState != null) {
            routineId = savedInstanceState.getLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
        }

        if (routineId != -1) {
            mRoutine = Routine.findById(routineId);
            setRoutine(mRoutine);
            hour = mRoutine.time().getHourOfDay();
            minute = mRoutine.time().getMinuteOfHour();
        } else {
            DateTime now = DateTime.now();
            hour = now.getHourOfDay();
            minute = now.getMinuteOfHour();
        }

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
                Log.d("RoutineCOEFragment", "Density: " + density);
                if (density >= DisplayMetrics.DENSITY_XHIGH) {
                    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                            .newInstance(RoutineCreateOrEditFragment.this, hour, minute, true);
                    timePickerDialog.show(getChildFragmentManager(), "111");
                } else {
                    TimePickerBuilder tpb = new TimePickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light);
                    tpb.addTimePickerDialogHandler(RoutineCreateOrEditFragment.this);
                    tpb.show();
                }

            }
        });

        updateTime();


        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRoutine != null)
            outState.putLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, mRoutine.getId());
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
                Log.d(getTag(), "Routine created");
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

        if (r.scheduleItems().size() > 0) {
            message = String.format(getString(R.string.remove_routine_message_long), r.name());
        } else {
            message = String.format(getString(R.string.remove_routine_message_short), r.name());
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

        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnRoutineEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnRoutineEditListener) {
            Log.d(getTag(), "Set onRoutineEditListener onAttach");
            mRoutineEditCallback = (OnRoutineEditListener) activity;
        }
    }

    // optionally set the listener manually
    public void setOnRoutineEditListener(OnRoutineEditListener l) {
        mRoutineEditCallback = l;
    }

    @Override
    public void onTimeSet(RadialTimePickerDialog dialog, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        updateTime();
    }

    @Override
    public void onDialogTimeSet(int ref, int hour, int minute) {
        onTimeSet(null, hour, minute);
    }

    void updateTime() {
        timeButton.setText(new LocalTime(hour, minute).toString("kk:mm"));
    }

    private void setRoutine(Routine r) {
        Log.d(getTag(), "Routine set: " + r.name());
        mRoutine = r;
        mNameTextView.setText(mRoutine.name());
        updateTime();
    }

    // Container Activity must implement this interface
    public interface OnRoutineEditListener {
        void onRoutineEdited(Routine r);

        void onRoutineCreated(Routine r);

        void onRoutineDeleted(Routine r);
    }

}