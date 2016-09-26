/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
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

    Drawable ic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routines_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.routines_list);

        View empty = rootView.findViewById(android.R.id.empty);
        listview.setEmptyView(empty);

        mRoutines = DB.routines().findAllForActivePatient(getContext());

        ic = new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_clock)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(8)
                .sizeDp(40);

        adapter = new RoutinesListAdapter(getActivity(), R.layout.routines_list_item, mRoutines);
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

    private View createRoutineListItem(LayoutInflater inflater, final Routine routine) {

        int hour = routine.time().getHourOfDay();
        int minute = routine.time().getMinuteOfHour();

        String strHour = String.valueOf(hour >= 10 ? hour : "0" + hour);
        String strMinute = ":" + String.valueOf(minute >= 10 ? minute : "0" + minute);

        View item = inflater.inflate(R.layout.routines_list_item, null);

        ((TextView) item.findViewById(R.id.routines_list_item_hour)).setText(strHour);
        ((TextView) item.findViewById(R.id.routines_list_item_minute)).setText(strMinute);
        ((TextView) item.findViewById(R.id.routines_list_item_name)).setText(routine.name());
        ((ImageButton) item.findViewById(R.id.imageButton2)).setImageDrawable(ic);

        int items = routine.scheduleItems().size();

        ((TextView) item.findViewById(R.id.routines_list_item_subtitle)).setText((items > 0 ? (""+items) : "Sin ") + " pautas asociadas");
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
                        DB.routines().deleteCascade(r, true);
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

    // Container Activity must implement this interface
    public interface OnRoutineSelectedListener {
        void onRoutineSelected(Routine r);
        void onCreateRoutine();
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

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mRoutines = DB.routines().findAllForActivePatient(getContext());

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