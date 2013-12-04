package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.store.RoutineStore;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class RoutineCreateOrEditFragment extends Fragment {

    OnRoutineEditListener mRoutineEditCallback;
    Routine mRoutine;

    TimePicker mTimePicker;
    TextView mNameTextView;
    Button mConfirmButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_routine, container, false);
        mTimePicker = (TimePicker) rootView.findViewById(R.id.routine_time_picker);
        mNameTextView = (TextView) rootView.findViewById(R.id.routine_edit_name);
        mConfirmButton = (Button) rootView.findViewById(R.id.routine_button_ok);

        mTimePicker.setIs24HourView(true);
        mConfirmButton.setText(getString(R.string.create_routine_button_text));

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEdit();
            }
        });

        Log.d(getTag(), "Root view created. Routine is null?  " + (mRoutine == null));

        return rootView;
    }


    public void setRoutine(Routine r) {
        Log.d(getTag(), "Routine set: " + r.getName());
        mRoutine = r;
        mNameTextView.setText(mRoutine.getName());
        mTimePicker.setCurrentHour(mRoutine.getTime().getHourOfDay());
        mTimePicker.setCurrentMinute(mRoutine.getTime().getMinuteOfHour());
        mConfirmButton.setText(getString(R.string.edit_routine_button_text));

    }

    public void clear() {
        mRoutine = null;
        mNameTextView.setText("");
        mTimePicker.setCurrentHour(12);
        mTimePicker.setCurrentMinute(00);
        mConfirmButton.setText(getString(R.string.create_routine_button_text));

    }


    private void onEdit() {

        String name = mNameTextView.getText().toString();
        int hour = mTimePicker.getCurrentHour();
        int minute = mTimePicker.getCurrentMinute();

        // if editing
        if (mRoutine != null) {
            mRoutine.setName(name);
            mRoutine.setTime(new LocalTime(hour, minute));
            if (mRoutineEditCallback != null) {
                mRoutineEditCallback.onRoutineEdited(mRoutine);
            }
        }
        // if creating
        else {
            Routine r = new Routine(new LocalTime(hour, minute), name);
            RoutineStore.getInstance().addRoutine(r);
            if (mRoutineEditCallback != null) {
                mRoutineEditCallback.onRoutineCreated(mRoutine);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnRoutineEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnRoutineEditListener) {
            mRoutineEditCallback = (OnRoutineEditListener) activity;
        }
    }

    // Container Activity must implement this interface
    public interface OnRoutineEditListener {
        public void onRoutineEdited(Routine r);

        public void onRoutineCreated(Routine r);
    }

}