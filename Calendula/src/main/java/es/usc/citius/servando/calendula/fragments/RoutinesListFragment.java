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
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.AlarmScheduler;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.store.ScheduleStore;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class RoutinesListFragment extends Fragment {


    List<Routine> mRoutines;
    OnRoutineSelectedListener mRoutineSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routines_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.routines_list);


        mRoutines = RoutineStore.instance().asList();
        adapter = new RoutinesListAdapter(getActivity(), R.layout.daily_view_hour, mRoutines);
        listview.setAdapter(adapter);

        rootView.findViewById(R.id.routine_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRoutineSelectedCallback != null)
                    mRoutineSelectedCallback.onCreateRoutine();
            }
        });
        return rootView;
    }

    public void notifyDataChange() {
        mRoutines = RoutineStore.instance().asList();
        Log.d(getTag(), "Routines List Fragment: " + mRoutines.size() + ", " + RoutineStore.instance().size());
        adapter.clear();
        for (Routine r : mRoutines) {
            adapter.add(r);
        }
        adapter.notifyDataSetChanged();
    }

    private View createRoutineListItem(LayoutInflater inflater, final Routine routine) {

        int hour = routine.getTime().getHourOfDay();
        int minute = routine.getTime().getMinuteOfHour();

        String strHour = String.valueOf(hour >= 10 ? hour : "0" + hour);
        String strMinute = ":" + String.valueOf(minute >= 10 ? minute : "0" + minute);

        View item = inflater.inflate(R.layout.routines_list_item, null);

        ((TextView) item.findViewById(R.id.routines_list_item_hour)).setText(strHour);
        ((TextView) item.findViewById(R.id.routines_list_item_minute)).setText(strMinute);
        ((TextView) item.findViewById(R.id.routines_list_item_name)).setText(routine.getName());
        View overlay = item.findViewById(R.id.routines_list_item_overlay);
        overlay.setTag(routine);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Routine r = (Routine) view.getTag();
                if (mRoutineSelectedCallback != null && r != null) {
                    Log.d(getTag(), "Click at " + r.getName());
                    mRoutineSelectedCallback.onRoutineSelected(r);
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
                    showDeleteConfirmationDialog((Routine) view.getTag());
                return true;
            }
        });
        return item;
    }


    void showDeleteConfirmationDialog(final Routine r) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String message;

        if (ScheduleUtils.hasSchedules(r)) {
            message = "The routine " + r.getName() + " has associated schedules that will be lost if you delete it. Do you want to remove it anyway?";
        } else {
            message = "Remove " + r.getName() + " routine?";
        }

        builder.setMessage(message)
                .setCancelable(true)
                .setTitle("Remove routine")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RoutineStore.instance().removeRoutine(r);
                        RoutineStore.instance().save(getActivity());
                        ScheduleStore.instance().save(getActivity());
                        notifyDataChange();
                        // cancel routine alarm
                        AlarmScheduler.instance().cancelAlarm(r, getActivity());
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

    private class RoutinesListAdapter extends ArrayAdapter<Routine> {

        public RoutinesListAdapter(Context context, int layoutResourceId, List<Routine> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createRoutineListItem(layoutInflater, mRoutines.get(position));
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnRoutineSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnRoutineSelectedListener) {
            mRoutineSelectedCallback = (OnRoutineSelectedListener) activity;
        }
    }

    // Container Activity must implement this interface
    public interface OnRoutineSelectedListener {
        public void onRoutineSelected(Routine r);

        public void onCreateRoutine();
    }
}