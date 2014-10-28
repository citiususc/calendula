package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Routine;

//import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
//import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class RoutineCreateOrEditFragment extends DialogFragment {
//        implements RadialTimePickerDialog.OnTimeSetListener {

    OnRoutineEditListener mRoutineEditCallback;
    Routine mRoutine;

    //    timepicker mtimepicker;
    Button timeButton;
    TextView mNameTextView;
    Button mConfirmButton;

    int hour;
    int minute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_routine, container, false);
//        mTimePicker = (TimePicker) rootView.findViewById(R.id.routine_time_picker);
        mNameTextView = (TextView) rootView.findViewById(R.id.routine_edit_name);
        mConfirmButton = (Button) rootView.findViewById(R.id.routine_button_ok);
        timeButton = (Button) rootView.findViewById(R.id.button2);
//        mTimePicker.setIs24HourView(true);
        mConfirmButton.setText(getString(mRoutine == null ? R.string.create_routine_button_text : R.string.edit_routine_button_text));
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEdit();
            }
        });


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
            mConfirmButton.setText(getString(R.string.edit_routine_button_text));
        } else {
            DateTime now = DateTime.now();
            hour = now.getHourOfDay();
            minute = now.getMinuteOfHour();
        }

        if (getDialog() != null) {
            getDialog().setTitle(R.string.title_create_routine_activity);
        }


        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TimePickerBuilder tpb = new TimePickerBuilder()
//                        .setFragmentManager(getChildFragmentManager())
//                        .setStyleResId(R.style.BetterPickersDialogFragment_Light)
//                        .setTargetFragment(RoutineCreateOrEditFragment.this);
//                tpb.show();


//                DateTime now = DateTime.now();
//                RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
//                        .newInstance(RoutineCreateOrEditFragment.this, hour, minute, true);
//                timePickerDialog.show(getChildFragmentManager(), "111");
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

    private void setRoutine(Routine r) {
        Log.d(getTag(), "Routine set: " + r.name());
        mRoutine = r;
        mNameTextView.setText(mRoutine.name());
        updateTime();
        mConfirmButton.setText(getString(R.string.edit_routine_button_text));

    }

//    prievate void clear() {
//        mRoutine = null;
//        mNameTextView.setText("");
//        mTimePicker.setCurrentHour(12);
//        mTimePicker.setCurrentMinute(00);
//        mConfirmButton.setText(getString(R.string.create_routine_button_text));
//    }


    void updateTime() {
        timeButton.setText(new LocalTime(hour, minute).toString("kk:mm") + "h");
    }

    private void onEdit() {

        String name = mNameTextView.getText().toString();

        if (name != null && name.length() > 0) {


            // if editing
            if (mRoutine != null) {
                mRoutine.setName(name);
                mRoutine.setTime(new LocalTime(hour, minute));
                mRoutine.save();
                if (mRoutineEditCallback != null) {
                    mRoutineEditCallback.onRoutineEdited(mRoutine);
                }
            }
            // if creating
            else {
                mRoutine = new Routine(new LocalTime(hour, minute), name);
                Log.d(getTag(), "Routine created");
                mRoutine.save();
                if (mRoutineEditCallback != null) {
                    mRoutineEditCallback.onRoutineCreated(mRoutine);
                }
            }
        } else {
            mNameTextView.setError("Please, type a name");
            mNameTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mNameTextView.setError(null);
                    mNameTextView.removeTextChangedListener(this);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }


    public void showDeleteConfirmationDialog(final Routine r) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String message;

        if (r.scheduleItems().size() > 0) {
            message = "The routine " + r.name() + " has associated schedules that will be lost if you delete it. Do you want to remove it anyway?";
        } else {
            message = "Remove " + r.name() + " routine?";
        }

        builder.setMessage(message)
                .setCancelable(true)
                .setTitle("Remove routine")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mRoutineEditCallback != null) {
                            mRoutineEditCallback.onRoutineDeleted(mRoutine);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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

//    @Override
//    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
//        this.hour = hourOfDay;
//        this.minute = minute;
//        updateTime();
//    }

    // Container Activity must implement this interface
    public interface OnRoutineEditListener {
        public void onRoutineEdited(Routine r);

        public void onRoutineCreated(Routine r);

        public void onRoutineDeleted(Routine r);
    }

}