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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ReminderNotification;
import es.usc.citius.servando.calendula.adapters.items.ScheduleListItem;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class ScheduleListFragment extends Fragment {

    private static final String TAG = "ScheduleListFragment";

    List<Schedule> mSchedules;
    OnScheduleSelectedListener mScheduleSelectedCallback;

    FastItemAdapter<ScheduleListItem> adapter;

    @BindView(R.id.schedule_list)
    RecyclerView recyclerView;
    @BindView(android.R.id.empty)
    View empty;

    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        ButterKnife.bind(this, rootView);

        mSchedules = DB.schedules().findAllForActivePatient(getContext());
        if (mSchedules.size() > 0)
            empty.setVisibility(View.GONE);
        setupRecyclerView();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    public void notifyDataChange() {
        LogUtil.d(TAG, "Schedules - Notify data change");
        new ReloadItemsTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        LogUtil.d(TAG, "Activity "
                + activity.getClass().getName()
                + ", "
                + (activity instanceof OnScheduleSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnScheduleSelectedListener) {
            mScheduleSelectedCallback = (OnScheduleSelectedListener) activity;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        CalendulaApp.eventBus().register(this);
    }

    @Override
    public void onStop() {
        CalendulaApp.eventBus().unregister(this);
        super.onStop();
    }

    // Method called from the event bus
    @SuppressWarnings("unused")
    @Subscribe
    public void handleActiveUserChange(final PersistenceEvents.ActiveUserChangeEvent event) {
        notifyDataChange();
    }

    void showDeleteConfirmationDialog(final Schedule s) {
        new MaterialStyledDialog.Builder(getActivity())
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(getActivity(), CommunityMaterial.Icon.cmd_calendar, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setTitle(getString(R.string.remove_schedule_dialog_title))
                .setDescription(String.format(getString(R.string.remove_schedule_message), s.medicine().getName()))
                .setCancelable(true)
                .setNeutralText(getString(R.string.dialog_no_option))
                .setPositiveText(getString(R.string.dialog_yes_option))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DB.schedules().deleteCascade(s, true);
                        ReminderNotification.cancel(getContext(), ReminderNotification.scheduleNotificationId(s.getId().intValue()));
                        notifyDataChange();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void setupRecyclerView() {
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        adapter = new FastItemAdapter<>();
        adapter.withSelectable(false);
        adapter.withPositionBasedStateManagement(false);
        for (Schedule schedule : mSchedules) {
            adapter.add(new ScheduleListItem(schedule));
        }
        adapter.withOnClickListener(new FastAdapter.OnClickListener<ScheduleListItem>() {
            @Override
            public boolean onClick(View v, IAdapter<ScheduleListItem> adapter, ScheduleListItem item, int position) {
                Schedule s = item.getSchedule();
                if (mScheduleSelectedCallback != null && s != null) {
                    LogUtil.d(TAG, "Click at " + s.medicine().getName() + " schedule");
                    mScheduleSelectedCallback.onScheduleSelected(s);
                } else {
                    LogUtil.d(TAG, "No callback set");
                }
                return true;
            }
        });
        adapter.withOnLongClickListener(new FastAdapter.OnLongClickListener<ScheduleListItem>() {
            @Override
            public boolean onLongClick(View v, IAdapter<ScheduleListItem> adapter, ScheduleListItem item, int position) {
                showDeleteConfirmationDialog(item.getSchedule());
                return true;
            }
        });
        recyclerView.setAdapter(adapter);
    }

    // Container Activity must implement this interface
    public interface OnScheduleSelectedListener {
        void onScheduleSelected(Schedule r);

        void onCreateSchedule();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mSchedules = DB.schedules().findAllForActivePatient(getContext());

            LogUtil.d(TAG, "Schedules after reload: " + mSchedules.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mSchedules.size() > 0) {
                empty.setVisibility(View.GONE);
            } else {
                empty.setVisibility(View.VISIBLE);
            }
            adapter.clear();
            for (Schedule s : mSchedules) {
                adapter.add(new ScheduleListItem(s));
            }
            adapter.notifyAdapterDataSetChanged();
        }
    }

}