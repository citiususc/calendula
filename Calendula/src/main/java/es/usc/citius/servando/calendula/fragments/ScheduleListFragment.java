package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class ScheduleListFragment extends Fragment {

    private static final String TAG = ScheduleListFragment.class.getSimpleName();

    List<Schedule> mSchedules;
    OnScheduleSelectedListener mScheduleSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.schedule_list);
        mSchedules = DB.schedules().findAllForActivePatient(getContext());
        adapter = new ScheduleListAdapter(getActivity(), R.layout.schedules_list_item, mSchedules);
        listview.setAdapter(adapter);
        return rootView;
    }

    public void notifyDataChange() {
        Log.d(getTag(), "Schedules - Notify data change");
        new ReloadItemsTask().execute();
    }

    private View createScheduleListItem(LayoutInflater inflater, final Schedule schedule) {

        View item = inflater.inflate(R.layout.schedules_list_item, null);
        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        ImageView icon2 = (ImageView) item.findViewById(R.id.imageView);

        String timeStr = "";
        List<ScheduleItem> items = schedule.items();

        if (schedule.type() != Schedule.SCHEDULE_TYPE_HOURLY) {
            timeStr = ScheduleUtils.getTimesStr(items != null ? items.size() : 0, getActivity());
        } else {
            timeStr = ScheduleUtils.getTimesStr(24 / schedule.rule().interval(), getActivity());
        }


        icon2.setImageDrawable(new IconicsDrawable(getContext())
                .icon(schedule.medicine().presentation().icon())
                .color(Color.WHITE)
                .paddingDp(8)
                .sizeDp(40));

        IIcon i = schedule.repeatsHourly() ? CommunityMaterial.Icon.cmd_history : CommunityMaterial.Icon.cmd_clock;

        icon.setImageDrawable(new IconicsDrawable(getContext())
                    .icon(i)
                    .colorRes(R.color.agenda_item_title)
                    .paddingDp(8)
                    .sizeDp(40));

        ((TextView) item.findViewById(R.id.schedules_list_item_medname)).setText(
                schedule.medicine().name());
        ((TextView) item.findViewById(R.id.schedules_list_item_times)).setText(timeStr);
        ((TextView) item.findViewById(R.id.schedules_list_item_days)).setText(
                schedule.toReadableString(getActivity()));

        View overlay = item.findViewById(R.id.schedules_list_item_container);
        overlay.setTag(schedule);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Schedule s = (Schedule) view.getTag();
                if (mScheduleSelectedCallback != null && s != null) {
                    Log.d(getTag(), "Click at " + s.medicine().name() + " schedule");
                    mScheduleSelectedCallback.onScheduleSelected(s);
                } else {
                    Log.d(getTag(), "No callback set");
                }
            }
        };
        overlay.setOnClickListener(clickListener);
        overlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view.getTag() != null) showDeleteConfirmationDialog((Schedule) view.getTag());
                return true;
            }
        });
        return item;
    }

    void showDeleteConfirmationDialog(final Schedule s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(
                String.format(getString(R.string.remove_medicine_message_short), s.medicine().name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DB.schedules().deleteCascade(s, true);
                                notifyDataChange();
                            }
                        })
                .setNegativeButton(getString(R.string.dialog_no_option),
                        new DialogInterface.OnClickListener() {
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

        Log.d(getTag(), "Activity "
                + activity.getClass().getName()
                + ", "
                + (activity instanceof OnScheduleSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnScheduleSelectedListener) {
            mScheduleSelectedCallback = (OnScheduleSelectedListener) activity;
        }
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

            Log.d(TAG, "Schedules after reload: " + mSchedules.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.clear();
            for (Schedule s : mSchedules) {
                adapter.add(s);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class ScheduleListAdapter extends ArrayAdapter<Schedule> {

        public ScheduleListAdapter(Context context, int layoutResourceId, List<Schedule> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createScheduleListItem(layoutInflater, mSchedules.get(position));
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
    public void onEvent(Object evt) {
        if(evt instanceof PersistenceEvents.ActiveUserChangeEvent){
            notifyDataChange();
        }
    }
}