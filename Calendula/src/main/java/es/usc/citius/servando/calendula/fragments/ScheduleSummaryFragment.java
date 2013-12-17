package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.usc.citius.servando.calendula.R;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link es.usc.citius.servando.calendula.fragments.ScheduleSummaryFragment.ScheduleCreationListener} interface
 * to handle interaction events.
 */
public class ScheduleSummaryFragment extends Fragment {

    private ScheduleCreationListener mListener;

    public ScheduleSummaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_summary, container, false);

        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            if (activity instanceof ScheduleCreationListener) {
                mListener = (ScheduleCreationListener) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ScheduleCreationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ScheduleCreationListener {

        public void onScheduleCreated();
    }

}
