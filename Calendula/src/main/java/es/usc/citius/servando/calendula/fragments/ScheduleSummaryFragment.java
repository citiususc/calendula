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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.LocalDate;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.SummaryCalendarActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ScheduleHelper;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ScheduleSummaryFragment extends Fragment {

    private static final String TAG = "ScheduleSummaryFragm";

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

        int color = DB.patients().getActive(getActivity()).getColor();

        LogUtil.d(TAG, "updateSummary ScheduleSUmmaryFragment");
        View rootView = getView();

        Medicine med = ScheduleHelper.instance().getSelectedMed();
        Schedule s = ScheduleHelper.instance().getSchedule();
        List<ScheduleItem> items = ScheduleHelper.instance().getScheduleItems();

        final TextView summaryTitle = (TextView) rootView.findViewById(R.id.summaryTitle);
        final TextView medNameTv = (TextView) rootView.findViewById(R.id.sched_summary_medname);
        final TextView medDaysTv = (TextView) rootView.findViewById(R.id.sched_summary_medi_days);
        final TextView medDailyFreqTv = (TextView) rootView.findViewById(R.id.sched_summary_medi_dailyfreq);
        final ImageView medIconImage = (ImageView) rootView.findViewById(R.id.sched_summary_medicon);
        final Button showCalendarButton = (Button) rootView.findViewById(R.id.button_show_calendar);

        if (med != null) {
            medNameTv.setText(med.getName());
        }

        medDaysTv.setText(s.toReadableString(getActivity()));
        medIconImage.setImageDrawable(iconFor(med != null ? med.getPresentation() : Presentation.PILLS));

        if (s.type() != Schedule.SCHEDULE_TYPE_HOURLY) {
            String freq =
                    ScheduleUtils.getTimesStr(items != null ? items.size() : 0, getActivity());
            medDailyFreqTv.setText(freq);
        } else {
            String freq = ScheduleUtils.getTimesStr(24 / s.rule().getInterval(), getActivity());
            medDailyFreqTv.setText(freq);
        }

        showCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Schedule s = ScheduleHelper.instance().getSchedule();

                LocalDate start = s.start();

                Intent i = new Intent(getActivity(), SummaryCalendarActivity.class);

                if (start != null) {
                    i.putExtra("start", start.toString(SummaryCalendarActivity.START_DATE_FORMAT));
                }

                if (s.type() == Schedule.SCHEDULE_TYPE_CYCLE) {
                    i.putExtra("active_days", s.getCycleDays());
                    i.putExtra("rest_days", s.getCycleRest());
                } else {
                    i.putExtra("rule", s.rule().toIcal());
                }

                startActivity(i);
            }
        });

        summaryTitle.setTextColor(color);
        medNameTv.setTextColor(color);
        medDailyFreqTv.setTextColor(color);
        summaryTitle.setVisibility(View.VISIBLE);
        medNameTv.setVisibility(View.VISIBLE);
        medDailyFreqTv.setVisibility(View.VISIBLE);
        medIconImage.setVisibility(View.VISIBLE);


    }

    IconicsDrawable iconFor(Presentation p) {
        return new IconicsDrawable(getContext())
                .icon(Presentation.iconFor(p))
                .colorRes(R.color.agenda_item_title)
                .sizeDp(60);
    }


}
