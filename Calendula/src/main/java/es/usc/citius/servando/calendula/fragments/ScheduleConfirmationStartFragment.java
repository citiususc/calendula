package es.usc.citius.servando.calendula.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;


/**
 * Created by joseangel.pineiro on 12/11/13.
 */
public class ScheduleConfirmationStartFragment extends Fragment {

    public static final String TAG = ScheduleConfirmationStartFragment.class.getName();


    public static ScheduleConfirmationStartFragment newInstance() {
        ScheduleConfirmationStartFragment fragment = new ScheduleConfirmationStartFragment();
        Bundle args = new Bundle();
        //args.putSerializable(ARG_PRESCRIPTION, pw);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_confirmation_start, container, false);
        rootView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ConfirmSchedulesActivity)getActivity()).next();
            }
        });
        return rootView;
    }

}