package es.usc.citius.servando.calendula.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ScheduleSummaryFragment extends Fragment {

    public static final String TAG = ScheduleSummaryFragment.class.getName();

    public ScheduleSummaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_summary, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // update summary info when this fragment becomes visible
            updateSummary();
        }
    }

    public void updateSummary() {

        Log.d(TAG, "updateSummary ScheduleSUmmaryFragment");
        View rootView = getView();

        final TextView medNameTv = (TextView) rootView.findViewById(R.id.sched_summary_medname);
        final TextView medDaysTv = (TextView) rootView.findViewById(R.id.sched_summary_medi_days);
        final TextView medDailyFreqTv = (TextView) rootView.findViewById(R.id.sched_summary_medi_dailyfreq);
        final ImageView medIconImage = (ImageView) rootView.findViewById(R.id.sched_summary_medicon);


        String medName = ScheduleCreationHelper.instance().getSelectedMed().name();
        int medIcon = ScheduleCreationHelper.instance().getSelectedMed().presentation().getDrawable();
        String freq = ScheduleUtils.getTimesStr(ScheduleCreationHelper.instance().getScheduleItems().size());
        String days[] = ScheduleCreationHelper.instance().getDays();
        String dayStr = ScheduleUtils.stringifyDays(days);

        medNameTv.setText(medName);
        medDailyFreqTv.setText(freq);
        medDaysTv.setText(dayStr);

        medIconImage.setImageDrawable(getResources().getDrawable(medIcon));
        Log.d(TAG, "Idx: " + ScheduleCreationHelper.instance().getSelectedScheduleIdx());
        Log.d(TAG, "Days: " + Arrays.toString(ScheduleCreationHelper.instance().getSelectedDays()));
        Log.d(TAG, "Schedule: " + ScheduleCreationHelper.instance().getTimesPerDay());

    }


}
