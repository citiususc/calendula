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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.activities;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
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
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.scheduling.PickupReminderMgr;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PickupUtils;

public class CalendarActivity extends CalendulaActivity {

    public static final int ACTION_SHOW_REMINDERS = 1;
    private static final String TAG = "CalendarActivity";
    static PickupUtils pickupUtils;
    private static DateFormat dtf2 = new SimpleDateFormat("dd/MMM");
    DateTime from;
    DateTime to;
    String df;
    Patient patient;
    Patient selectedPatient;
    int selectedPatientIdx = 0;
    long selectedPatientId;
    List<Patient> pats;
    Date selectedDate = null;
    CharSequence bestDayText;
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
    private Pair<LocalDate, List<PickupInfo>> bestDay;

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
                //onBestDaySelected();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    public CharSequence addPickupList(CharSequence msg, List<PickupInfo> pks) {

        Paint textPaint = new Paint();
        //obviously, we have to set textSize into Paint object
        textPaint.setTextSize(getResources().getDimensionPixelOffset(R.dimen.medium_font_size));
        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();

        for (PickupInfo p : pks) {
            Patient patient = pickupUtils.getPatient(p);
            int color = patient.color();
            String str = "       " + p.medicine().name() + " (" + dtf2.format(p.from().toDate()) + " - " + dtf2.format(p.to().toDate()) + ")\n";
            Spannable text = new SpannableString(str);
            text.setSpan(new ForegroundColorSpan(color), 0, str.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Drawable d = getResources().getDrawable(AvatarMgr.res(patient.avatar()));
            d.setBounds(0, 0, fontMetrics.bottom, fontMetrics.bottom);
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            text.setSpan(span, 0, 5, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            msg = TextUtils.concat(msg, text);
        }
        return msg;
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

    public CharSequence getBestDayText() {

        final List<PickupInfo> urgent = pickupUtils.urgentMeds();
        Pair<LocalDate, List<PickupInfo>> best = pickupUtils.getBestDay();

        final List<PickupInfo> next = (best == null || best.first == null) ? new ArrayList<PickupInfo>() : best.second;

        CharSequence msg = new SpannableString("No hai medicinas que recoger");
        LocalDate today = LocalDate.now();

        // there are not urgent meds, but there are others to pickup
        if (urgent.isEmpty() && best != null) {

//            LogUtil.d(TAG, "Urgent: " + urgent.size());
//            LogUtil.d(TAG, "Next: " + next.size());

            LogUtil.d(TAG, "there are not urgent meds, but there are others to pickup");
            if (next.size() > 1) {
                msg = new SpannableString(getString(R.string.best_single_day_messge, best.first.toString(getString(R.string.best_date_format)), next.size()) + "\n\n");
            } else {
                msg = new SpannableString(getString(R.string.best_single_day_messge_one_med, best.first.toString(getString(R.string.best_date_format))) + "\n\n");
            }
            msg = addPickupList(msg, next);
        }

        // there are urgent meds
        LogUtil.d(TAG, "there are urgent meds");
        if (!urgent.isEmpty()) {
            // and others
            LogUtil.d(TAG, "and others");
            if (best != null) {

                String bestStr = best.equals(LocalDate.now().plusDays(1)) ? getString(R.string.calendar_date_tomorrow) : best.first.toString(getString(R.string.best_date_format));

                // and the others date is near
                LogUtil.d(TAG, "and the others date is near");
                if (today.plusDays(3).isAfter(best.first)) {
                    List<PickupInfo> all = new ArrayList<>();
                    all.addAll(urgent);
                    all.addAll(next);
                    msg = new SpannableString(getString(R.string.best_single_day_messge, bestStr, all.size()) + "\n\n");
                    msg = addPickupList(msg, all);
                }
                // and the others date is not near
                else {

                    LogUtil.d(TAG, "and the others date is not near");
                    msg = addPickupList(new SpannableString(getString(R.string.pending_meds_msg) + "\n\n"), urgent);

                    msg = TextUtils.concat(msg, new SpannableString("\n"));
                    if (next.size() > 1) {
                        LogUtil.d(TAG, " size > 1");
                        msg = TextUtils.concat(msg, getString(R.string.best_single_day_messge_after_pending, bestStr, next.size()) + "\n\n");
                    } else {
                        LogUtil.d(TAG, " size <= 1");
                        msg = TextUtils.concat(msg, getString(R.string.best_single_day_messge_after_pending_one_med, bestStr) + "\n\n");
                    }
                    msg = addPickupList(msg, next);
                }
            } else {
                LogUtil.d(TAG, " there are only urgent meds");
                // there are only urgent meds
                msg = addPickupList(getString(R.string.pending_meds_msg) + "\n\n", urgent);
            }
        }

        LogUtil.d(TAG, msg.toString());
        return msg;
    }

    void setupNewCalendar() {
        caldroidFragment = new CaldroidSampleCustomFragment();
        Bundle args = new Bundle();
        DateTime now = DateTime.now();
        args.putInt(CaldroidFragment.MONTH, now.getMonthOfYear());
        args.putInt(CaldroidFragment.YEAR, now.getYear());
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
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
                if (bestDay != null && bestDay.first != null && bestDay.first.equals(d)) {
                    //Toast.makeText(CalendarActivity.this, "Best day!", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                    builder.setTitle(R.string.best_date_recommendation_title)
                            .setPositiveButton(getString(R.string.driving_warning_gotit), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setMessage(bestDayText);
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
        this.bestDay = pickupUtils.getBestDay();
        this.bestDayText = getBestDayText();

        if (this.bestDay != null && this.bestDay.first != null) {
            //Toast.makeText(CalendarActivity.this, "Best day: " + bestDay.first.toString("dd/MM/YY"), Toast.LENGTH_SHORT).show();
            caldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(getResources().getColor(R.color.android_green_light)), this.bestDay.first.toDate());
        }

        caldroidFragment.refreshView();
    }

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

    @Override
    protected void onDestroy() {
        selectedDate = null;
        super.onDestroy();
    }

    private void setupPatientSpinner() {
        String[] names = new String[pats.size() + 1];

        names[0] = getString(R.string.calendar_patient_all);
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

    private void checkIntent() {

        int action = getIntent().getIntExtra("action", -1);
        if (action == ACTION_SHOW_REMINDERS) {
            //onBestDaySelected();
        }
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

    public static class CaldroidSampleCustomFragment extends CaldroidFragment {
        @Override
        public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
            return new CaldroidSampleCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData, pickupUtils);
        }
    }


    public class UpdatePickupsTask extends AsyncTask<Void, Void, Void> {


        ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... params) {
            pickupUtils = new PickupUtils(selectedPatientIdx == 0 ? DB.pickups().findAll() : DB.pickups().findByPatient(selectedPatient));
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
