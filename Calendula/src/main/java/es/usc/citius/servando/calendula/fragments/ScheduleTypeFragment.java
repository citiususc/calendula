package es.usc.citius.servando.calendula.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.util.ScheduleHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleTypeFragment extends Fragment {

    public static final int TYPE_ROUTINES = 1;
    public static final int TYPE_HOURLY = 2;
    public static final int TYPE_PERIOD = 3;

    public static final String TAG = ScheduleTypeFragment.class.getName();

    public ScheduleTypeFragment()
    {
        // Required empty public constructor
    }

    View optionRoutines;
    View optionHourly;
    View optionPeriod;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_schedule_type, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);
        optionRoutines = view.findViewById(R.id.schedule_type_routines);
        optionHourly = view.findViewById(R.id.schedule_type_hourly);
        optionPeriod = view.findViewById(R.id.schedule_type_period);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override public void onClick(View v)
            {
                onClickitem(v.getId());
            }
        };

        optionRoutines.setOnClickListener(listener);
        optionPeriod.setOnClickListener(listener);
        optionHourly.setOnClickListener(listener);

        int type = ScheduleHelper.instance().getScheduleType();
        if (type == ScheduleTypeFragment.TYPE_HOURLY)
        {
            onClickitem(R.id.schedule_type_hourly);
        } else if (type == ScheduleTypeFragment.TYPE_ROUTINES)
        {
            onClickitem(R.id.schedule_type_routines);
        } else if (type == ScheduleTypeFragment.TYPE_PERIOD)
        {
            onClickitem(R.id.schedule_type_period);
        }
    }

    private void onClickitem(int id)
    {
        final int white = getResources().getColor(R.color.white);
        final int selected = Color.parseColor("#aaefefef");

        int type = 0;
        switch (id)
        {
            case R.id.schedule_type_routines:
                type = TYPE_ROUTINES;
                optionRoutines.setBackgroundColor(selected);
                optionHourly.setBackgroundColor(white);
                optionPeriod.setBackgroundColor(white);
                break;
            case R.id.schedule_type_hourly:
                type = TYPE_HOURLY;
                optionRoutines.setBackgroundColor(white);
                optionPeriod.setBackgroundColor(white);
                optionHourly.setBackgroundColor(selected);
                break;
            case R.id.schedule_type_period:
                type = TYPE_PERIOD;
                optionRoutines.setBackgroundColor(white);
                optionHourly.setBackgroundColor(white);
                optionPeriod.setBackgroundColor(selected);
                break;
            default:
                break;
        }
        ScheduleHelper.instance().setScheduleType(type);
        ((ScheduleCreationActivity) getActivity()).onScheduleTypeSelected();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
        {

        }
    }
}
