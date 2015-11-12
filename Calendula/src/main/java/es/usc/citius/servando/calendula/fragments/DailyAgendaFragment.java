package es.usc.citius.servando.calendula.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.DailyAgendaRecyclerAdapter;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.AgendaZoomHelper;
import es.usc.citius.servando.calendula.activities.ConfirmActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaFragment extends Fragment implements HomeActivity.OnBackPressedListener {

    private static final String TAG = DailyAgendaFragment.class.getName();
    DailyAgendaItemStubComparator dailyAgendaItemStubComparator =
            new DailyAgendaItemStubComparator();
    List<DailyAgendaItemStub> items = new ArrayList<>();



    boolean expanded = true;

    View emptyView;
    View zoomContainer;
    AgendaZoomHelper zoomHelper;

    RecyclerView rv;
    DailyAgendaRecyclerAdapter rvAdapter;
    private DailyAgendaRecyclerAdapter.AgendaItemClickListener rvListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<>();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_daily_agenda, container, false);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        inflater.inflate(R.layout.fragment_edit_profile, container, false);

        emptyView = rootView.findViewById(R.id.empty_view_placeholder);
        ImageView face = (ImageView) emptyView.findViewById(R.id.imageView_ok);

        Drawable icon = new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_alarm_check)
                .colorRes(R.color.agenda_item_title)
                .sizeDp(90)
                .paddingDp(0);

        face.setImageDrawable(icon);

        zoomContainer = rootView.findViewById(R.id.zoom_container);
        zoomHelper = new AgendaZoomHelper(zoomContainer, getActivity(),
                new AgendaZoomHelper.ZoomHelperListener() {
                    @Override
                    public void onChange() {
                        items.clear();
                        items.addAll(buildItems()); // allow user to change day
                        rvAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onHide() {
                        //updateBackground(DateTime.now());
                    }

                    @Override
                    public void onShow(Routine r) {
                        // updateBackground(r.time().toDateTimeToday());
                    }
                });

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rvAdapter = new DailyAgendaRecyclerAdapter(items);
        rv.setAdapter(rvAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());

        rvListener = new DailyAgendaRecyclerAdapter.AgendaItemClickListener() {
            @Override
            public void onClick(View v, DailyAgendaItemStub item, int position) {
                showConfirmActivity(v,item, position);
            }
        };

        rvAdapter.setListener(rvListener);
        new LoadDailyAgendaTask().execute(null, null, null);
        return rootView;

    }

    private void showConfirmActivity(View view, DailyAgendaItemStub item, int position) {

        Intent i = new Intent(getContext(), ConfirmActivity.class);
        i.putExtra("position", position);
        if(item.isRoutine) {
            i.putExtra("routine_id", item.id);
        }else{
            i.putExtra("schedule_id", item.id);
            i.putExtra("schedule_time", new LocalTime(item.hour, item.minute).toString("kk:mm"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    new Pair<>(view.findViewById(R.id.patient_avatar), "avatar_transition"),
                    new Pair<>(view.findViewById(R.id.linearLayout), "time"),
            new Pair<>(view.findViewById(R.id.routines_list_item_name), "title")
            );
            ActivityCompat.startActivity(getActivity(), i, activityOptions.toBundle());
        } else {
            startActivity(i);
        }
    }

    public void showReminder(Routine r) {
        zoomHelper.remind(getActivity(), r);
    }

    public void showReminder(Schedule s, LocalTime t) {
        zoomHelper.remind(getActivity(), s, t);
    }

    public void showDelayDialog(Routine r) {
        zoomHelper.showDelayDialog(r);
    }

    public void showDelayDialog(Schedule s, LocalTime t) {
        zoomHelper.showDelayDialog(s, t);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d(getTag(), "Visible to user");
        }
    }

    public List<DailyAgendaItemStub> buildItems() {
        boolean showPlaceholder = false;
        int now = DateTime.now().getHourOfDay();
        //String nextRoutineTime = getNextRoutineHour();
        ArrayList<DailyAgendaItemStub> items = new ArrayList<DailyAgendaItemStub>();
        //addSpacerTop(items);

        ///////////////////////////////////////

        // get hourly schedules for today
        DateTime from = DateTime.now().withTimeAtStartOfDay();
        DateTime to = from.plusDays(1);

        final List<Schedule> hourly = DB.schedules().findHourly();
        Log.d(TAG, "Hourly schedules: " + hourly.size());
        for (Schedule s : hourly) {
            final List<DateTime> times = s.rule().occurrencesBetween(from, to, s.startDateTime());
            if (times.size() > 0) {
                for (DateTime t : times) {
                    if (t.plusHours(1).isAfterNow() || expanded) {
                        Log.d(TAG, "RFC dailyAgenda: " + t.toString("E dd MMM, kk:mm ZZZ"));
                        DailyAgendaItemStub item = new DailyAgendaItemStub(t.toLocalTime());
                        item.meds = new ArrayList<>();
                        item.hasEvents = true;
                        int minute = t.getMinuteOfHour();
                        Medicine med = s.medicine();
                        DailyAgendaItemStub.DailyAgendaItemStubElement el =
                                new DailyAgendaItemStub.DailyAgendaItemStubElement();
                        el.medName = med.name();
                        el.dose = s.dose();
                        el.displayDose = s.displayDose();
                        el.res = med.presentation().getDrawable();
                        el.presentation = med.presentation();
                        el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
                        el.taken = DB.dailyScheduleItems()
                                .findByScheduleAndTime(s, t.toLocalTime())
                                .takenToday();
                        item.meds.add(el);
                        item.id = s.getId();
                        item.patient = s.patient();
                        item.isRoutine = false;
                        item.title = s.toReadableString(getContext());
                        item.hour = t.getHourOfDay();
                        item.minute = t.getMinuteOfHour();
                        item.time = new LocalTime(item.hour, item.minute);
                        items.add(item);
                    }
                }
            }
        }


        for (int i = 0; i < 24; i++) {
            List<DailyAgendaItemStub> hourItems = DailyAgendaItemStub.fromHour(i);
            for (DailyAgendaItemStub item : hourItems) {
                if (item.hasEvents && i >= now || expanded) {
                    items.add(item);
                }
            }
        }

        Log.d(getTag(), "Items: " + items.size());


        Collections.sort(items, dailyAgendaItemStubComparator);

        for (DailyAgendaItemStub i : items) {
            if (i.hasEvents && i.time != null && i.time.toDateTimeToday().isAfterNow()) {
                Log.d(TAG, "isNext: " + i.time.toString("kk:mm"));
                i.isNext = true;
                break;
            }
        }

        return items;
    }

    public void showOrHideEmptyView(boolean show) {
        if(show)
            emptyView.setVisibility(View.VISIBLE);
        else
            emptyView.setVisibility(View.GONE);
    }

    private void addEmptyPlaceholder(ArrayList<DailyAgendaItemStub> items) {
        DailyAgendaItemStub spacer = new DailyAgendaItemStub(null);
        spacer.isEmptyPlaceholder = true;
        items.add(spacer);
    }


    public void toggleViewMode() {
        expanded = !expanded;
        new LoadDailyAgendaTask().execute(null, null, null);
    }

    public void expand() {
        if(!expanded) {
            expanded = true;
            new LoadDailyAgendaTask().execute(null, null, null);
        }
    }

    public void collapse() {
        if(expanded) {
            expanded = false;
            new LoadDailyAgendaTask().execute(null, null, null);
        }
    }


    public void refresh() {
        rvAdapter.notifyDataSetChanged();
    }

    public void refreshPosition(int position) {

        DailyAgendaItemStub stub = items.get(position);

        DailyAgendaItemStub item;
        if(!stub.isRoutine){
            LocalTime t = stub.time;
            Schedule s = DB.schedules().findById(stub.id);
            item = new DailyAgendaItemStub(t);
            item.meds = new ArrayList<>();
            item.hasEvents = true;
            int minute = t.getMinuteOfHour();
            Medicine med = s.medicine();
            DailyAgendaItemStub.DailyAgendaItemStubElement el = new DailyAgendaItemStub.DailyAgendaItemStubElement();
            el.medName = med.name();
            el.dose = s.dose();
            el.displayDose = s.displayDose();
            el.res = med.presentation().getDrawable();
            el.presentation = med.presentation();
            el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
            el.taken = DB.dailyScheduleItems()
                    .findByScheduleAndTime(s, t)
                    .takenToday();
            item.meds.add(el);
            item.id = s.getId();
            item.patient = s.patient();
            item.isRoutine = false;
            item.title = s.toReadableString(getContext());
            item.hour = t.getHourOfDay();
            item.minute = t.getMinuteOfHour();
            item.time = new LocalTime(item.hour, item.minute);
        }else{

            LocalDate today = LocalDate.now();
            Routine r = Routine.findById(stub.id);
            List<ScheduleItem> doses = r.scheduleItems();

            // create an ItemStub for the current hour
            item = new DailyAgendaItemStub(r.time());
            item.meds = new ArrayList<>();
            for (ScheduleItem scheduleItem : doses) {
                if (scheduleItem.schedule() != null && scheduleItem.schedule().enabledForDate(today)) {
                    item.hasEvents = true;
                    int minute = r.time().getMinuteOfHour();
                    Medicine med = scheduleItem.schedule().medicine();
                    DailyAgendaItemStub.DailyAgendaItemStubElement el = new DailyAgendaItemStub.DailyAgendaItemStubElement();
                    el.medName = med.name();
                    el.dose = scheduleItem.dose();
                    el.displayDose = scheduleItem.displayDose();
                    el.res = med.presentation().getDrawable();
                    el.presentation = med.presentation();
                    el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
                    el.taken = DailyScheduleItem.findByScheduleItem(scheduleItem).takenToday();
                    item.meds.add(el);
                }
            }
            Collections.sort(item.meds);

            if (!item.meds.isEmpty())
            {
                item.id = r.getId();
                item.patient = r.patient();
                item.title = r.name();
                item.hour = r.time().getHourOfDay();
                item.minute = r.time().getMinuteOfHour();
                items.add(item);
            }
        }
        items.remove(position);
        items.add(position, item);
        rvAdapter.notifyItemChanged(position);
    }

    void updatePrefs() {
        SharedPreferences settings =
                getActivity().getSharedPreferences(CalendulaApp.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePrefs();
    }

    @Override
    public boolean doBack() {
        if (zoomContainer.getVisibility() == View.VISIBLE) {
            zoomHelper.hide();
            return true;
        }
        return false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void notifyDataChange() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new LoadDailyAgendaTask().execute(null, null, null);
            }
        });

    }


    public class LoadDailyAgendaTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            items.clear();
            items.addAll(buildItems());
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            for(DailyAgendaItemStub i : items){
                Log.d("Recycler", i.toString());
            }

            boolean showPlaceholder = items.size() == 0;
            showOrHideEmptyView(showPlaceholder);

            rvAdapter.notifyDataSetChanged();
        }
    }

    private class DailyAgendaItemStubComparator implements Comparator<DailyAgendaItemStub> {

        @Override
        public int compare(DailyAgendaItemStub a, DailyAgendaItemStub b) {

            if (a.isSpacer || a.time == null) {
                return -1;
            } else if (b.isSpacer || b.time == null) {
                return 1;
            }
            return a.time.compareTo(b.time);
        }
    }
}