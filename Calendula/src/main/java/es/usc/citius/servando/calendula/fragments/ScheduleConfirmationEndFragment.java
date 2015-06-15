package es.usc.citius.servando.calendula.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;


/**
 * Created by joseangel.pineiro on 12/11/13.
 */
public class ScheduleConfirmationEndFragment extends Fragment {

    public static final String TAG = ScheduleConfirmationEndFragment.class.getName();

    List<ConfirmSchedulesActivity.PrescriptionWrapper> prescriptions;

    public static ScheduleConfirmationEndFragment newInstance() {
        ScheduleConfirmationEndFragment fragment = new ScheduleConfirmationEndFragment();
        Bundle args = new Bundle();
        //args.putSerializable(ARG_PRESCRIPTION, pw);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prescriptions = ((ConfirmSchedulesActivity) getActivity()).getPrescriptions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_confirmation_end, container, false);
        updateCounts(rootView);
        return rootView;
    }

    private void updateCounts(View rootView){

        int totalSchedules = prescriptions.size();
        int newSchedules = ((ConfirmSchedulesActivity)getActivity()).getNewCount();
        int updated = totalSchedules - newSchedules;


        StringBuffer sb = new StringBuffer();

        //.append(newMedsCount + " schedules will be updated")
        sb.append(newSchedules + " schedules will be created")
                .append("\n")
                .append(updated + " schedules will be updated")
                .append("\n")
                .append(updated + " meds will be added to your medical kit");

        ((TextView) rootView.findViewById(R.id.textView)).setText(sb.toString());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && getView() != null) {
            updateCounts(getView());
        }
    }
}