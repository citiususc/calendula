package es.usc.citius.servando.calendula.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.DailyAgendaRecyclerAdapter;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub.DailyAgendaItemStubElement;

/**
 * Daily agenda fragment
 */
public class DailyAgendaFragment extends Fragment{

    final String TAG = "DailyAgendaFragment";

    View emptyView;

    LinearLayoutManager llm;

    RecyclerView rv;
    DailyAgendaRecyclerAdapter rvAdapter;
    DailyAgendaRecyclerAdapter.EventListener rvListener;

    List<DailyAgendaItemStub> items = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_daily_agenda, container, false);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        emptyView = rootView.findViewById(R.id.empty_view_placeholder);

        setupRecyclerView();
        setupEmptyView();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notifyDataChange();

    }

    private void setupRecyclerView() {
        llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rvAdapter = new DailyAgendaRecyclerAdapter(items, rv, llm, getActivity());
        rv.setAdapter(rvAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());

        rvListener = new DailyAgendaRecyclerAdapter.EventListener() {

            @Override
            public void onItemClick(View v, DailyAgendaItemStub item, int position) {
                showConfirmActivity(v, item, position);
            }

            @Override
            public void onAfterToggleCollapse(boolean expanded, boolean somethingVisible) {
                if(expanded) {
                    showOrHideEmptyView(false);
                } else if(!expanded && somethingVisible){
                    showOrHideEmptyView(false);
                }else{
                    showOrHideEmptyView(true);
                }
            }
        };

        rvAdapter.setListener(rvListener);
    }


    private void setupEmptyView() {
        Drawable icon = new IconicsDrawable(getContext())
                .icon(GoogleMaterial.Icon.gmd_alarm_check)
                .colorRes(R.color.agenda_item_title)
                .sizeDp(90)
                .paddingDp(0);
        ((ImageView) emptyView.findViewById(R.id.imageView_ok)).setImageDrawable(icon);
    }


    private void showConfirmActivity(View view, DailyAgendaItemStub item, int position) {

        Intent i = new Intent(getContext(), ConfirmActivity.class);
        i.putExtra("position", position);
        i.putExtra("date", item.date.toString("dd/MM/YYYY"));

        if(item.isRoutine) {
            i.putExtra("routine_id", item.id);
        }else{
            i.putExtra("schedule_id", item.id);
            i.putExtra("schedule_time", item.time.toString("kk:mm"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View v1 = view.findViewById(R.id.patient_avatar);
            View v2 = view.findViewById(R.id.linearLayout);
            View v3 = view.findViewById(R.id.routines_list_item_name);

            if(v1!=null && v2!=null && v3!=null) {
                ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        new Pair<>(v1, "avatar_transition"),
                        new Pair<>(v2, "time"),
                        new Pair<>(v3, "title")
                );
                ActivityCompat.startActivity(getActivity(), i, activityOptions.toBundle());
            }else{
                startActivity(i);
            }
        } else {
            startActivity(i);
        }
    }

    public List<DailyAgendaItemStub> buildItems() {

        List<DailyAgendaItemStub> stubs = new ArrayList<>();

        final List<DailyScheduleItem> daily = DB.dailyScheduleItems().findAll();

        DateTime max = DateTime.now().withTimeAtStartOfDay();

        // create stubs for hourly schedule items
        for(DailyScheduleItem dailyScheduleItem : daily){
            if(dailyScheduleItem.boundToSchedule())
            {
                Schedule schedule = dailyScheduleItem.schedule();
                Medicine medicine = schedule.medicine();
                LocalTime time = dailyScheduleItem.time();
                LocalDate date = dailyScheduleItem.date();

                // create a stub for this item
                DailyAgendaItemStub stub = new DailyAgendaItemStub(date,time);
                stub.isRoutine = false;
                stub.meds = new ArrayList<>();
                stub.hasEvents = true;
                stub.id = schedule.getId();
                stub.patient = schedule.patient();
                stub.isRoutine = false;
                stub.title = schedule.toReadableString(getContext());
                stub.time = time;

                // create a element for the schedule item
                DailyAgendaItemStubElement el = new DailyAgendaItemStubElement();
                el.medName = medicine.name();
                el.dose = schedule.dose();
                el.displayDose = schedule.displayDose();
                el.res = medicine.presentation().getDrawable();
                el.presentation = medicine.presentation();
                el.minute = time.toString("mm");
                el.taken = dailyScheduleItem.takenToday();
                stub.meds.add(el);
                stubs.add(stub);

                DateTime candidate = stub.date.toDateTime(stub.time);
                if(candidate.isAfter(max)){
                    max = candidate;
                }
            }
        }

        final Map<LocalDate, Map<Routine, DailyAgendaItemStub>> dateStubs = new HashMap<>();
        // create stubs for routine items
        for (Routine  routine: DB.routines().findAll()) {
            for (ScheduleItem  scheduleItem : routine.scheduleItems()) {
                // get item from daily agenda if exists
                List<DailyScheduleItem> dailyScheduleItems = DB.dailyScheduleItems().findAllByScheduleItem(scheduleItem);

                for (DailyScheduleItem dailyScheduleItem : dailyScheduleItems) {
                    // break if not, this means is not enabled for today
                    if (dailyScheduleItem == null) {
                        break;
                    }

                    LocalDate date = dailyScheduleItem.date();

                    if (!dateStubs.containsKey(date)) {
                        dateStubs.put(date, new HashMap<Routine, DailyAgendaItemStub>());
                    }

                    Map<Routine, DailyAgendaItemStub> routineStubs = dateStubs.get(date);

                    DailyAgendaItemStub stub;

                    LocalTime time = routine.time();

                    if (!routineStubs.containsKey(routine)) {
                        // create a new stub and add it to the list
                        stub = new DailyAgendaItemStub(date, routine.time());
                        stub.isRoutine = true;
                        stub.id = routine.getId();
                        stub.patient = routine.patient();
                        stub.title = routine.name();
                        stub.time = time;
                        stub.meds = new ArrayList<>();
                        stub.hasEvents = true;
                        routineStubs.put(routine, stub);

                        DateTime candidate = stub.date.toDateTime(stub.time);
                        if (candidate.isAfter(max)) {
                            max = candidate;
                        }

                    } else {
                        stub = routineStubs.get(routine);
                    }

                    Schedule schedule = scheduleItem.schedule();
                    Medicine medicine = schedule.medicine();

                    DailyAgendaItemStubElement el = new DailyAgendaItemStubElement();
                    // add element properties
                    el.medName = medicine.name();
                    el.dose = scheduleItem.dose();
                    el.scheduleItemId = scheduleItem.getId();
                    el.displayDose = scheduleItem.displayDose();
                    el.res = medicine.presentation().getDrawable();
                    el.presentation = medicine.presentation();
                    el.minute = time.toString("mm");
                    el.taken = dailyScheduleItem.takenToday();
                    stub.meds.add(el);
                }
            }
        }

        for(LocalDate date : dateStubs.keySet()){
            Map<Routine, DailyAgendaItemStub> routineStubs = dateStubs.get(date);
            for(Routine r : routineStubs.keySet()){
                stubs.add(routineStubs.get(r));
            }
        }

        addEmptyHours(stubs, DateTime.now(), max);
        Collections.sort(stubs, DailyAgendaItemStubComparator.instance);

        return stubs;
    }


    public void addEmptyHours(List<DailyAgendaItemStub> stubs, DateTime min, DateTime max){

        min = min.withTimeAtStartOfDay();
        max = max.withTimeAtStartOfDay().plusDays(1); // end of day

        // add empty hours if there is not an item with the same hour
        for(DateTime start = min; start.isBefore(max); start = start.plusHours(1)){

            boolean exact = false;
            for (DailyAgendaItemStub item : items) {
                if(start.equals(item.dateTime())){
                    exact = true; break;
                }
            }

            Interval hour = new Interval(start, start.plusHours(1));
            if(!exact || hour.contains(DateTime.now())) {
                stubs.add(new DailyAgendaItemStub(start.toLocalDate(), start.toLocalTime()));
            }

            if(start.getHourOfDay() == 0){
                DailyAgendaItemStub spacer = new DailyAgendaItemStub(start.toLocalDate(), start.toLocalTime());
                spacer.isSpacer = true;
                stubs.add(spacer);
            }
        }
    }

    public void showOrHideEmptyView(boolean show) {
        if(show) {
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.GONE);
        }
    }

    public void toggleViewMode() {
        rvAdapter.toggleCollapseMode();
    }


    public void refresh() {
        rvAdapter.notifyDataSetChanged();
    }

    public void refreshPosition(int position) {

        if(position == -1){
            notifyDataChange();
            return;
        }else if(position >= 0 && position < items.size()) {
            rvAdapter.updatePosition(position);
        }
    }


    public void scrollToNow(){
        scrollTo(LocalTime.now());
    }

    public void scrollTo(LocalTime time){
        int position = 0;
        for(DailyAgendaItemStub stub : items){
            if(stub.time != null && stub.time.isAfter(time)){
                break;
            }
            position++;
        }

        if(position > 0)
            llm.scrollToPositionWithOffset(position-1, 50);
    }


    public boolean isExpanded() {
        return rvAdapter.isExpanded();
    }

    public void notifyDataChange() {
        try {
            Log.d(TAG, "AgendaView NotifyDataChange");
            items.clear();
            items.addAll(buildItems());
            Log.d(TAG, "Items after rebuild " + items.size());
            rvAdapter.notifyDataSetChanged();
            // show empty list view if there are no items
            rv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showOrHideEmptyView(!rvAdapter.isShowingSomething());
                }
            }, 100);
        }catch (Exception e){
            Log.e(TAG, "Error onPostExecute", e);
        }
    }

    public void onUserUpdate() {
        notifyDataChange();
    }

    private static class DailyAgendaItemStubComparator implements Comparator<DailyAgendaItemStub> {

        static final DailyAgendaItemStubComparator instance = new DailyAgendaItemStubComparator();

        private DailyAgendaItemStubComparator(){};

        @Override
        public int compare(DailyAgendaItemStub a, DailyAgendaItemStub b) {

            DateTime aT = a.date.toDateTime(a.time);
            DateTime bT = b.date.toDateTime(b.time);

            if(aT.compareTo(bT) == 0 && a.isSpacer) {
                return -1;
            } else if(aT.compareTo(bT) == 0 && b.isSpacer){
                return 1;
            }else if(aT.compareTo(bT) == 0){
                return a.hasEvents ? -1 : 1;
            }
            return aT.compareTo(bT);
        }
    }
}