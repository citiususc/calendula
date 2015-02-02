package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Persistence;
import es.usc.citius.servando.calendula.persistence.Schedule;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.schedule_list);
        mSchedules = Schedule.findAll();
        adapter = new ScheduleListAdapter(getActivity(), R.layout.schedules_list_item, mSchedules);
        listview.setAdapter(adapter);
        return rootView;
    }

    public void notifyDataChange() {
        Log.d(getTag(), "Schedules - Notify data change");
        new ReloadItemsTask().execute();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mSchedules = Schedule.findAll();

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

    private View createScheduleListItem(LayoutInflater inflater, final Schedule schedule) {

        View item = inflater.inflate(R.layout.schedules_list_item, null);
        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        icon.setImageDrawable(getResources().getDrawable(schedule.medicine().presentation().getDrawable()));
        ((TextView) item.findViewById(R.id.schedules_list_item_medname)).setText(schedule.medicine().name());
        ((TextView) item.findViewById(R.id.schedules_list_item_times)).setText(ScheduleUtils.getTimesStr(schedule.items().size(), getActivity()));
        ((TextView) item.findViewById(R.id.schedules_list_item_days)).setText(ScheduleUtils.stringifyDays(schedule.days(), getActivity()));


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
                if (view.getTag() != null)
                    showDeleteConfirmationDialog((Schedule) view.getTag());
                return true;
            }
        });
        return item;
    }

    void showDeleteConfirmationDialog(final Schedule s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Remove " + s.medicine().name() + " schedule?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Persistence.instance().deleteCascade(s);
                        notifyDataChange();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnScheduleSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnScheduleSelectedListener) {
            mScheduleSelectedCallback = (OnScheduleSelectedListener) activity;
        }
    }

    // Container Activity must implement this interface
    public interface OnScheduleSelectedListener {
        public void onScheduleSelected(Schedule r);

        public void onCreateSchedule();
    }
}