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
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;

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
        mRoutines = DB.Routines.findAll();
        adapter = new RoutinesListAdapter(getActivity(), R.layout.daily_view_hour, mRoutines);
        listview.setAdapter(adapter);
        return rootView;
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

    private View createRoutineListItem(LayoutInflater inflater, final Routine routine) {

        int hour = routine.time().getHourOfDay();
        int minute = routine.time().getMinuteOfHour();

        String strHour = String.valueOf(hour >= 10 ? hour : "0" + hour);
        String strMinute = ":" + String.valueOf(minute >= 10 ? minute : "0" + minute);

        View item = inflater.inflate(R.layout.routines_list_item, null);

        ((TextView) item.findViewById(R.id.routines_list_item_hour)).setText(strHour);
        ((TextView) item.findViewById(R.id.routines_list_item_minute)).setText(strMinute);
        ((TextView) item.findViewById(R.id.routines_list_item_name)).setText(routine.name());
        View overlay = item.findViewById(R.id.routine_list_item_container);
        overlay.setTag(routine);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Routine r = (Routine) view.getTag();
                if (mRoutineSelectedCallback != null && r != null) {
                    Log.d(getTag(), "Click at " + r.name());
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


        if (r.scheduleItems().size() > 0) {
            message = String.format(getString(R.string.remove_routine_message_long), r.name());
            //message = "The routine " + r.name() + " has associated schedules that will be lost if you delete it. Do you want to remove it anyway?";
        } else {
            //message = "Remove " + r.name() + " routine?";
            message = String.format(getString(R.string.remove_routine_message_short), r.name());
        }

        builder.setMessage(message)
                .setCancelable(true)
                .setTitle(getString(R.string.remove_routine_dialog_title))
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel routine alarm and delete it
                        AlarmScheduler.instance().onDeleteRoutine(r, getActivity());
                        r.deleteCascade();
                        notifyDataChange();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void notifyDataChange() {
        Log.d(getTag(), "Routines - Notify data change");
        new ReloadItemsTask().execute();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mRoutines = DB.Routines.findAll();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.clear();
            for (Routine r : mRoutines) {
                adapter.add(r);
            }
            adapter.notifyDataSetChanged();
        }
    }


    // Container Activity must implement this interface
    public interface OnRoutineSelectedListener {
        public void onRoutineSelected(Routine r);

        public void onCreateRoutine();
    }
}