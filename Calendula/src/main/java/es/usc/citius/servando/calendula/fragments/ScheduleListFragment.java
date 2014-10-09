package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.store.ScheduleStore;
import es.usc.citius.servando.calendula.store.StoreListener;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class ScheduleListFragment extends Fragment implements StoreListener{

    private static final String TAG = ScheduleListFragment.class.getSimpleName();

    List<Schedule> mSchedules;
    OnScheduleSelectedListener mScheduleSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.schedule_list);

        ScheduleStore.instance().addListener(this);

        mSchedules = ScheduleStore.instance().getSchedules();
        adapter = new ScheduleListAdapter(getActivity(), R.layout.schedules_list_item, mSchedules);
        listview.setAdapter(adapter);

        rootView.findViewById(R.id.schedule_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScheduleSelectedCallback != null)
                    mScheduleSelectedCallback.onCreateSchedule();
            }
        });



        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ScheduleStore.instance().removeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        //onChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        //ScheduleStore.instance().removeListener(this);
    }

    public void notifyDataChange() {
        mSchedules = ScheduleStore.instance().getSchedules();
//        Log.d(getTag(), "Routines : " + mSchedules.size() + ", " + RoutineStore.instance().size());
//        adapter.clear();
//        for (Schedule r : mSchedules) {
//            adapter.add(r);
//        }
        adapter.notifyDataSetChanged();
    }

    private View createScheduleListItem(LayoutInflater inflater, final Schedule schedule) {

        View item = inflater.inflate(R.layout.schedules_list_item, null);


        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        icon.setImageDrawable(getResources().getDrawable(schedule.getMedicine().getPresentation().getDrawable()));

        ((TextView)item.findViewById(R.id.schedules_list_item_medname)).setText(schedule.getMedicine().getName());
        ((TextView)item.findViewById(R.id.schedules_list_item_times)).setText(ScheduleUtils.getTimesStr(schedule.items()));
        ((TextView) item.findViewById(R.id.schedules_list_item_days)).setText(ScheduleUtils.getDaysStr(schedule.getDays()));


        View overlay = item.findViewById(R.id.schedules_list_item_container);
        overlay.setTag(schedule);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Schedule s = (Schedule) view.getTag();
                if (mScheduleSelectedCallback != null && s != null) {
                    Log.d(getTag(), "Click at " + s.getMedicine().getName() + " schedule");
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
        builder.setMessage("Remove " + s.getMedicine().getName() + " schedule?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ScheduleStore.instance().removeSchedule(s);
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

    @Override
    public void onChange() {
        Log.d(TAG, "ScheduleStore changed!");
        mSchedules = ScheduleStore.instance().getSchedules();
        adapter.notifyDataSetChanged();
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