package es.usc.citius.servando.calendula.activities;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.scheduling.PickupReminderMgr;
import es.usc.citius.servando.calendula.util.AvatarMgr;

public class CalendarActivity extends CalendulaActivity {

    public static final int ACTION_SHOW_REMINDERS = 1;

    private static PickupInfo.PickupComparator pickupComparator = new PickupInfo.PickupComparator();
    private static DateFormat dtf2 = new SimpleDateFormat("dd/MMM");
    static List<PickupInfo> pickupInfos = new ArrayList<>();
    List<LocalDate> bestDays;
    Pair<LocalDate, String> bestDay;

    DateTime from;
    DateTime to;
    String df;
    Patient patient;
    Patient selectedPatient;
    int selectedPatientIdx = 0;
    long selectedPatientId;
    List<Patient> pats;
    Date selectedDate = null;


    static Map<LocalDate, List<PickupInfo>> pickupsMap = new HashMap<>();


    View bottomSheet;
    TextView title;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout toolbarLayout;
    TextView subtitle;
    ImageView avatar;
    CaldroidFragment caldroidFragment;
    View topBg;
    NestedScrollView nestedScrollView;
    View titleCollapsedContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        pats = DB.patients().findAll();
        patient = DB.patients().getActive(this);

        setupStatusBar(Color.TRANSPARENT);
        setupToolbar("", Color.TRANSPARENT, Color.WHITE);
        toolbar.setTitleTextColor(Color.WHITE);

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        titleCollapsedContainer = findViewById(R.id.collapsed_title_container);

        topBg = findViewById(R.id.imageView5);
        subtitle = (TextView) findViewById(R.id.routine_name);
        title = (TextView) findViewById(R.id.routine_name_title);
        avatar = (ImageView) findViewById(R.id.patient_avatar_title);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        df = getString(R.string.pickup_date_format);
        from = DateTime.now().minusMonths(3);
        to = DateTime.now().plusMonths(3);
        bottomSheet = findViewById(R.id.pickup_list_container);
        bottomSheet.setVisibility(View.INVISIBLE);

        setupPatientSpinner();
        onPatientUpdate();
        findViewById(R.id.close_pickup_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBottomSheet();
            }
        });
        checkIntent();
    }

    private void setupPatientSpinner() {
        String[] names = new String[pats.size() + 1];

        names[0] = "Todos";
        for (int i = 0; i < pats.size(); i++) {
            names[i + 1] = pats.get(i).name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.toolbar_spinner_item, names);
        adapter.setDropDownViewResource(R.layout.toolbar_spinner_item);
        Spinner spinner = (Spinner) findViewById(R.id.toolbar_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedPatientIdx = i;
                onPatientUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void onPatientUpdate() {
        if (selectedPatientIdx == 0) {
            topBg.setBackgroundColor(getResources().getColor(R.color.dark_grey_home));
            avatar.setImageDrawable(
                    new IconicsDrawable(CalendarActivity.this)
                            .icon(CommunityMaterial.Icon.cmd_account_multiple)
                            .color(Color.WHITE)
                            .paddingDp(6)
                            .sizeDp(48));
        } else {
            int pIndex = selectedPatientIdx - 1;
            selectedPatient = pats.get(pIndex);
            topBg.setBackgroundColor(selectedPatient.color());

            selectedPatientId = selectedPatient.id();
            avatar.setImageResource(AvatarMgr.res(selectedPatient.avatar()));
        }

        new UpdatePickupsTask().execute();
    }

    void setupNewCalendar() {
        caldroidFragment = new CaldroidSampleCustomFragment();
        Bundle args = new Bundle();
        args.putInt(CaldroidFragment.MONTH, 7);
        args.putInt(CaldroidFragment.YEAR, 2016);
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidDefaultNoGrid);
        caldroidFragment.setArguments(args);
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                LocalDate d = LocalDate.fromDateFields(date);
                onDaySelected(d);
                if (bestDay.first != null && bestDay.first.equals(d)) {
                    //Toast.makeText(CalendarActivity.this, "Best day!", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                    builder.setTitle(R.string.best_date_recommendation_title)
                            .setPositiveButton(getString(R.string.driving_warning_gotit), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
//        if(allowReminder) {
//            builder.setNeutralButton(getString(R.string.best_date_reminder), new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    onActivateReminder(best);
//                }
//            });
//        }
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setMessage(bestDay.second);
                    alertDialog.show();
                }
            }

            @Override
            public void onChangeMonth(int month, int year) {
                DateTime date = DateTime.now().withYear(year).withMonthOfYear(month);
                subtitle.setText(date.toString("MMMM YYYY").toUpperCase());
            }

            @Override
            public void onLongClickDate(Date date, View view) {

            }

            @Override
            public void onCaldroidViewCreated() {
                caldroidFragment.getView().findViewById(R.id.calendar_title_view).setVisibility(View.GONE);
                caldroidFragment.getMonthTitleTextView().setVisibility(View.GONE);
            }

        };

        caldroidFragment.setCaldroidListener(listener);
        bestDay = onBestDaySelected();

        if (bestDay.first != null) {
            //Toast.makeText(CalendarActivity.this, "Best day: " + bestDay.first.toString("dd/MM/YY"), Toast.LENGTH_SHORT).show();
            caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.android_green_light)), bestDay.first.toDate());
        }

        caldroidFragment.refreshView();
    }


    @Override
    protected void onDestroy() {
        selectedDate = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar, menu);

        IconicsDrawable icon = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_information_outline)
                .sizeDp(48)
                .paddingDp(6)
                .color(Color.WHITE);

        menu.getItem(0).setIcon(icon);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_best_day:
                onBestDaySelected();
                return true;
            default:
                return false;
        }
    }

    private Pair<LocalDate, String> onBestDaySelected() {

        final List<PickupInfo> urgent = urgentMeds(pickupInfos);
        final LocalDate best = bestDays.isEmpty() ? null : bestDays.get(0);
        final List<PickupInfo> next = best == null ? new ArrayList<PickupInfo>() : medsCanTakeAt(best);

        String msg = "No hai medicinas que recoger";
        LocalDate today = LocalDate.now();

        Log.d("Calendar", "Urgent: " + urgent.size());
        Log.d("Calendar", "Next: " + next.size());

        boolean allowReminder = false;

        // there are not urgent meds, but there are others to pickup
        if (urgent.isEmpty() && best != null) {

            allowReminder = true;

            Log.d("Calendar", "there are not urgent meds, but there are others to pickup");
            if (next.size() > 1) {
                msg = getString(R.string.best_single_day_messge, best.toString(getString(R.string.best_date_format)), next.size()) + "\n\n";
            } else {
                msg = getString(R.string.best_single_day_messge_one_med, best.toString(getString(R.string.best_date_format))) + "\n\n";
            }
            msg = addPcikupList(msg, next);
        }

        // there are urgent meds
        Log.d("Calendar", "there are urgent meds");
        if (!urgent.isEmpty()) {
            // and others
            Log.d("Calendar", "and others");
            if (best != null) {

                String bestStr = best.equals(LocalDate.now().plusDays(1)) ? getString(R.string.calendar_date_tomorrow) : best.toString(getString(R.string.best_date_format));

                // and the others date is near
                Log.d("Calendar", "and the others date is near");
                if (today.plusDays(3).isAfter(best)) {
                    List<PickupInfo> all = new ArrayList<>();
                    all.addAll(urgent);
                    all.addAll(next);
                    msg = getString(R.string.best_single_day_messge, bestStr, all.size()) + "\n\n";
                    msg = addPcikupList(msg, all);
                }
                // and the others date is not near
                else {

                    allowReminder = true;

                    Log.d("Calendar", "and the others date is not near");
                    msg = addPcikupList(getString(R.string.pending_meds_msg) + "\n\n", urgent);
                    msg += "\n";
                    if (next.size() > 1) {
                        Log.d("Calendar", " size > 1");
                        msg += getString(R.string.best_single_day_messge_after_pending, bestStr, next.size()) + "\n\n";
                    } else {
                        Log.d("Calendar", " size <= 1");
                        msg += getString(R.string.best_single_day_messge_after_pending_one_med, bestStr) + "\n\n";
                    }
                    msg = addPcikupList(msg, next);
                }
            } else {
                Log.d("Calendar", " there are only urgent meds");
                // there are only urgent meds
                msg = addPcikupList(getString(R.string.pending_meds_msg) + "\n\n", urgent);
            }
        }

        Log.d("BEST_DAY", msg);

        return new Pair<>(best, msg);
    }

    private void onActivateReminder(final LocalDate best) {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle(R.string.best_date_reminder)
                .setSingleChoiceItems(
                        getResources().getStringArray(R.array.calendar_pickup_reminder_values),
                        -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PickupReminderMgr.instance().setCheckPickupsAlarm(CalendarActivity.this, best.minusDays(which + 1));
                                Toast.makeText(CalendarActivity.this, "Recordatorio activado!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        alertDialog.show();
    }

    public String addPcikupList(String msg, List<PickupInfo> pks) {
        for (PickupInfo p : pks) {
            msg += "    • " + p.medicine().name() + " (" + dtf2.format(p.from().toDate()) + " - " + dtf2.format(p.to().toDate()) + ")\n";
        }
        return msg;
    }

    private void checkIntent() {

        int action = getIntent().getIntExtra("action", -1);
        if (action == ACTION_SHOW_REMINDERS) {
            onBestDaySelected();
        }
    }

    private void updatePickups() {
        pickupInfos = selectedPatientIdx == 0 ? DB.pickups().findAll() : DB.pickups().findByPatient(selectedPatient);
        Collections.sort(pickupInfos, pickupComparator);
        bestDays = bestDays(pickupInfos);
        pickupsMap.clear();
        for (PickupInfo pk : pickupInfos) {
            if (!pickupsMap.containsKey(pk.from())) {
                pickupsMap.put(pk.from(), new ArrayList<PickupInfo>());
            }

            pickupsMap.get(pk.from()).add(pk);
        }
    }

    public List<PickupInfo> urgentMeds(List<PickupInfo> pickupList) {

        List<PickupInfo> urgent = new ArrayList<>();
        Collections.sort(pickupList, pickupComparator);
        LocalDate now = LocalDate.now();
        for (PickupInfo p : pickupList) {
            Log.d("IsUrgent", p.medicine().name() + ", " + p.from().toString(df) + ", " + p.to().toString(df) + ", " + p.taken());
            if (!p.taken() && p.from().plusDays(7).isBefore(now) && p.to().isAfter(now)) {
                Log.d("Urgent", p.medicine().name() + ", " + p.from().toString(df) + ", " + p.to().toString(df) + ", " + p.taken());
                urgent.add(p);
            }
        }
        return urgent;
    }


    public List<LocalDate> bestDays(List<PickupInfo> pickupList) {
        List<LocalDate> bestDays = new ArrayList<>();
        int bestCount = 0;
        if (pickupList.size() > 0) {
            Collections.sort(pickupList, pickupComparator);

            LocalDate today = LocalDate.now();
            LocalDate first = LocalDate.now(); // compute first
            LocalDate now = LocalDate.now().minusDays(10);

            if (now.getDayOfWeek() == DateTimeConstants.SUNDAY) {
                now = now.plusDays(1);
            }

            // look for the first date we can take meds within the 10 days margin
            for (PickupInfo p : pickupList) {
                if (p.from().isAfter(now) && !p.taken()) {
                    first = p.from();
                    break;
                }
            }

            Log.d("Calendar", "BestDayCandidate - First: " + first.toString("dd/MM/yy"));
            List<Long> medIds = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                LocalDate d = first.plusDays(i);
                int count = 0;
                if ((d.isAfter(today) && d.getDayOfWeek() < DateTimeConstants.SATURDAY) || (d.isBefore(today) && d.getDayOfWeek() < DateTimeConstants.FRIDAY)) {
                    for (PickupInfo p : pickupList) {
                        DateTime iStart = p.from().toDateTimeAtStartOfDay();
                        DateTime iEnd = p.from().plusDays(9).toDateTimeAtStartOfDay();
                        Interval interval = new Interval(iStart, iEnd);
                        if (!p.taken() && interval.contains(d.toDateTimeAtStartOfDay())) {
                            if (!medIds.contains(p.medicine().getId())) {
                                count++;
                                medIds.add(p.medicine().getId());
                            }
                        }
                    }
                }

                Log.d("Calendar", "BestDayCandidate: " + d.toString("dd/MM/yy") + ": " + count + " meds");

                if (count > bestCount) {
                    bestCount = count;
                    bestDays.clear();
                    bestDays.add(d.toDateTimeAtStartOfDay().isAfterNow() ? d : LocalDate.now().plusDays(1));
                } else if (count == bestCount) {
                    bestDays.add(d);
                }
                medIds.clear();
            }
        }
        return bestDays;
    }

    private List<PickupInfo> medsCanTakeAt(LocalDate d) {
        List<PickupInfo> all = pickupInfos;
        Collections.sort(all, pickupComparator);
        List<PickupInfo> canTake = new ArrayList<>();
        List<Long> medIds = new ArrayList<>();

        for (PickupInfo p : all) {
            DateTime iStart = p.from().toDateTimeAtStartOfDay();
            DateTime iEnd = p.from().plusDays(9).toDateTimeAtStartOfDay();
            Interval interval = new Interval(iStart, iEnd);
            if (!p.taken() && interval.contains(d.toDateTimeAtStartOfDay())) {
                if (!medIds.contains(p.medicine().getId())) {
                    canTake.add(p);
                    medIds.add(p.medicine().getId());
                }
            }
        }
        return canTake;
    }

    private boolean onDaySelected(LocalDate date) {
        selectedDate = date.toDateTimeAtStartOfDay().toDate();
        return showPickupsInfo(date);
    }

    private boolean showPickupsInfo(final LocalDate date) {
        final List<PickupInfo> from = DB.pickups().findByFrom(date, true);
        if (!from.isEmpty()) {

            TextView title = ((TextView) bottomSheet.findViewById(R.id.bottom_sheet_title));

            LayoutInflater i = getLayoutInflater();
            LinearLayout list = (LinearLayout) findViewById(R.id.pickup_list);
            list.removeAllViews();
            for (final PickupInfo p : from) {

                Medicine m = DB.medicines().findById(p.medicine().getId());
                Patient pat = DB.patients().findById(m.patient().id());

                if (selectedPatientIdx == 0 || pat.id() == selectedPatientId) {

                    View v = i.inflate(R.layout.calendar_pickup_list_item, null);
                    TextView tv1 = ((TextView) v.findViewById(R.id.textView));
                    TextView tv2 = ((TextView) v.findViewById(R.id.textView2));
                    ImageView avatar = ((ImageView) v.findViewById(R.id.avatar));
                    String interval = getResources().getString(R.string.pickup_interval, p.to().toString(df));

                    if (p.taken()) {
                        interval += " ✔";
                        tv1.setAlpha(0.5f);
                    } else {
                        tv1.setAlpha(1f);
                        tv2.setAlpha(1f);
                    }

                    tv1.setText(p.medicine().name());
                    tv2.setText(interval);
                    avatar.setImageResource(AvatarMgr.res(pat.avatar()));

                    tv1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            p.taken(!p.taken());
                            DB.pickups().save(p);
                            showPickupsInfo(date);
                        }
                    });
                    list.addView(v);
                }
            }
            nestedScrollView.scrollBy(0, bottomSheet.getHeight());
            showBottomSheet();
            int total = list.getChildCount();
            title.setText(total + " " + getResources().getString(R.string.title_pickups_bottom_sheet, date.toString(df)));
            appBarLayout.setExpanded(false, true);
            return true;
        }
        return false;
    }

    public void showBottomSheet() {
        bottomSheet.setVisibility(View.VISIBLE);
    }

    public void hideBottomSheet() {
        LinearLayout list = (LinearLayout) findViewById(R.id.pickup_list);
        list.removeAllViews();
        appBarLayout.setExpanded(true, true);
        bottomSheet.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheet.getVisibility() == View.VISIBLE) {
            hideBottomSheet();
        } else {
            super.onBackPressed();
        }
    }


    public static class CaldroidSampleCustomFragment extends CaldroidFragment {
        @Override
        public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
            return new CaldroidSampleCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData, pickupsMap);
        }
    }


    public class UpdatePickupsTask extends AsyncTask<Void, Void, Void> {


        ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... params) {
            updatePickups();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(CalendarActivity.this);
            dialog.setIndeterminate(true);
            dialog.setMessage("Actualizando calendario...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            setupNewCalendar();
        }
    }

}
