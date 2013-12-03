package es.usc.citius.servando.calendula.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.DummyDataGenerator;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.store.RoutineStore;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class RoutinesListFragment extends Fragment {


    List<Routine> routines;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routines_list, container, false);
        final ListView listview = (ListView) rootView.findViewById(R.id.routines_list);

        DummyDataGenerator.fillRoutineStore();
        routines = RoutineStore.getInstance().asList();

        ListAdapter adapter = new RoutinesListAdapter(getActivity(), R.layout.daily_view_hour, routines);
        listview.setAdapter(adapter);

        return rootView;
    }

    private class RoutinesListAdapter extends ArrayAdapter<Routine> {

        public RoutinesListAdapter(Context context, int layoutResourceId, List<Routine> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createRoutineListItem(layoutInflater, routines.get(position));
        }

    }


    private View createRoutineListItem(LayoutInflater inflater, Routine routine) {


        int hour = routine.getTime().getHourOfDay();
        int minute = routine.getTime().getMinuteOfHour();

        String strHour = String.valueOf(hour >= 10 ? hour : "0" + hour) + ":";
        String strMinute = String.valueOf(minute >= 10 ? minute : "0" + minute);

        View item = inflater.inflate(R.layout.routines_list_item, null);
        ((TextView) item.findViewById(R.id.routines_list_item_hour)).setText(strHour);
        ((TextView) item.findViewById(R.id.routines_list_item_minute)).setText(strMinute);
        ((TextView) item.findViewById(R.id.routines_list_item_name)).setText(routine.getName());

        return item;
    }


}