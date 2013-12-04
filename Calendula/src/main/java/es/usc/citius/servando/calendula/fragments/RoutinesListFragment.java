package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import es.usc.citius.servando.calendula.DummyDataGenerator;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.store.RoutineStore;

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

        if (RoutineStore.getInstance().size() == 0) {
            DummyDataGenerator.fillRoutineStore();
        }
        mRoutines = RoutineStore.getInstance().asList();
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
        mRoutines = RoutineStore.getInstance().asList();
        Log.d(getTag(), "Routines : " + mRoutines.size() + ", " + RoutineStore.getInstance().size());
        adapter.clear();
        for (Routine r : mRoutines) {
            adapter.add(r);
        }
        adapter.notifyDataSetChanged();
        adapter.notifyDataSetInvalidated();

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

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRoutineSelectedCallback != null) {
                    Log.d(getTag(), "Click at " + routine.getName());
                    mRoutineSelectedCallback.onRoutineSelected(routine);
                } else {
                    Log.d(getTag(), "No callback set");
                }

            }
        };

        overlay.setOnClickListener(clickListener);
        overlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getActivity(), "Delete is not supported yet :(", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        return item;
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