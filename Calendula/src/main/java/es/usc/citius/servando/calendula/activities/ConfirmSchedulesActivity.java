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

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.j256.ormlite.misc.TransactionManager;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.qrWrappers.PickupWrapper;
import es.usc.citius.servando.calendula.activities.qrWrappers.PrescriptionListWrapper;
import es.usc.citius.servando.calendula.activities.qrWrappers.PrescriptionWrapper;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.model.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.ScheduleConfirmationEndFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleConfirmationStartFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleImportFragment;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;

public class ConfirmSchedulesActivity extends CalendulaActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = "ConfirmSchedules.class";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    List<PrescriptionWrapper> prescriptionList;
    int scheduleCount;

    FloatingActionButton fab;

    TextView medName;
    TextView title;

    ImageButton routinesItem;
    ImageButton hourlyItem;
    ImageButton cycleItem;

    View scheduleTypeSelector;
    View readingQrBox;

    DateTimeFormatter df = DateTimeFormat.forPattern("yyMMdd");
    int color;
    private String qrData;
    private Patient patient;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirm_schedules, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                finish();
                return true;
        }
    }

    public void onClickScheduleTypeButton(int id) {
        Fragment f = getViewPagerFragment(mViewPager.getCurrentItem());

        routinesItem.setAlpha(0.3f);
        hourlyItem.setAlpha(0.3f);
        cycleItem.setAlpha(0.3f);

        if (f instanceof ScheduleImportFragment) {
            if (id == R.id.schedule_type_routines) {
                routinesItem.setAlpha(1f);
                ((ScheduleImportFragment) f).changeScheduleType(Schedule.SCHEDULE_TYPE_EVERYDAY);
            } else if (id == R.id.schedule_type_hourly) {
                hourlyItem.setAlpha(1f);
                ((ScheduleImportFragment) f).changeScheduleType(Schedule.SCHEDULE_TYPE_HOURLY);
            } else if (id == R.id.schedule_type_period) {
                cycleItem.setAlpha(1f);
                ((ScheduleImportFragment) f).changeScheduleType(Schedule.SCHEDULE_TYPE_CYCLE);
            }
        }
    }

    public Map<Schedule, PrescriptionWrapper> getScheduleInfo() {
        Map<Schedule, PrescriptionWrapper> schedules = new HashMap<>();
        for (int i = 0; i < scheduleCount; i++) {
            Fragment f = getViewPagerFragment(i + 1);
            if (f instanceof ScheduleImportFragment) {
                ScheduleImportFragment importFragment = (ScheduleImportFragment) f;
                schedules.put(importFragment.getSchedule(), importFragment.getPrescriptionWrapper());
            }
        }
        return schedules;
    }

    public List<Schedule> getSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        for (int i = 0; i < scheduleCount; i++) {
            Fragment f = getViewPagerFragment(i + 1);
            if (f instanceof ScheduleImportFragment) {
                schedules.add(((ScheduleImportFragment) f).getSchedule());
            }
        }
        return schedules;
    }

    public void createSchedule(final Schedule s, List<ScheduleItem> items, Medicine m) {

        // save schedule
        s.setMedicine(m);
        s.setPatient(patient);
        s.setScanned(true);
        s.save();
        LogUtil.d(TAG, "Saving schedule..." + s.toString());

        if (!s.repeatsHourly()) {
            for (ScheduleItem item : items) {
                item.setSchedule(s);
                item.save();
                LogUtil.d(TAG, "Saving item..." + item.getId());
                // add to daily schedule
                DailyAgenda.instance().addItem(patient, item, false);
            }
        } else {
            for (DateTime time : s.hourlyItemsToday()) {
                LocalTime timeToday = time.toLocalTime();
                DailyAgenda.instance().addItem(patient, s, timeToday);
            }
        }
        // save and fire event
        DB.schedules().saveAndFireEvent(s);
        AlarmScheduler.instance().onCreateOrUpdateSchedule(s, ConfirmSchedulesActivity.this);
    }

    public void updateSchedule(final Schedule s, final Schedule current, List<ScheduleItem> items) {

        s.setType(current.type());
        s.setDays(current.days());
        s.setDose(current.dose());
        s.setStart(current.start());
        s.setRepetition(current.rule());
        s.setCycle(current.getCycleDays(), current.getCycleRest());
        s.setStartTime(current.startTime());

        List<Long> routinesTaken = new ArrayList<>();

        if (!s.repeatsHourly()) {
            for (ScheduleItem item : s.items()) {
                DailyScheduleItem d = DailyScheduleItem.findByScheduleItem(item);
                // if taken today, add to the list
                if (d != null && d.takenToday()) {
                    routinesTaken.add(item.routine().getId());
                }
                item.deleteCascade();
            }

            // save new items
            for (ScheduleItem i : items) {
                ScheduleItem item = new ScheduleItem();
                item.setDose(i.dose());
                item.setRoutine(i.routine());
                item.setSchedule(s);
                item.save();
                // add to daily schedule
                DailyScheduleItem dsi = new DailyScheduleItem(item);
                dsi.setPatient(patient);
                if (routinesTaken.contains(item.routine().getId())) {
                    dsi.setTakenToday(true);
                }
                dsi.save();
            }
        } else {
            DB.dailyScheduleItems().removeAllFrom(s);
            for (DateTime time : s.hourlyItemsToday()) {
                LocalTime timeToday = time.toLocalTime();
                DailyScheduleItem dsi = new DailyScheduleItem(s, timeToday);
                dsi.setPatient(patient);
                dsi.save();
                LogUtil.d(TAG, "Saving daily schedule item..."
                        + dsi.getId()
                        + " timeToday: "
                        + timeToday.toString("kk:mm"));
            }
        }
        // save and fire event
        DB.schedules().saveAndFireEvent(s);
        AlarmScheduler.instance().onCreateOrUpdateSchedule(s, ConfirmSchedulesActivity.this);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        updatePageTitle(i);

        LogUtil.d(TAG, " Page Selected: " + i);

        if (i == 0) {
            hideScheduleTypeSelector();
            fab.setVisibility(View.INVISIBLE);
        } else if (i == scheduleCount + 1) {
            hideScheduleTypeSelector();
            fab.setVisibility(View.VISIBLE);
        } else {
            showScheduleTypeSelector();
            fab.setVisibility(View.INVISIBLE);
            updateScheduleTypeSelector(i);
        }
    }

    public void updateScheduleTypeSelector(int page) {
        Fragment f = getViewPagerFragment(page);

        routinesItem.setAlpha(0.3f);
        hourlyItem.setAlpha(0.3f);
        cycleItem.setAlpha(0.3f);

        int type = ((ScheduleImportFragment) f).getSchedule().type();

        LogUtil.d(TAG, "Type: " + type);

        if (f instanceof ScheduleImportFragment) {
            if (type == Schedule.SCHEDULE_TYPE_HOURLY) {
                hourlyItem.setAlpha(1f);
            } else if (type == Schedule.SCHEDULE_TYPE_CYCLE) {
                cycleItem.setAlpha(1f);
            } else {
                routinesItem.setAlpha(1f);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public List<PrescriptionWrapper> getPrescriptions() {
        return prescriptionList;
    }

    public PrescriptionListWrapper parseQRData(String data) {
        LogUtil.d(TAG, "QRDATA: " + data);
        return new Gson().fromJson(data, PrescriptionListWrapper.class);
    }

    public void next() {
        int next = mViewPager.getCurrentItem() + 1;
        int size = mSectionsPagerAdapter.getCount();
        if (next < size) {
            mViewPager.setCurrentItem(next);
        }
    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_schedules);
        patient = DB.patients().getActive(this);
        color = patient.color();
        int dark = ScreenUtils.equivalentNoAlpha(color, Color.BLACK, 0.85f);

        findViewById(R.id.activity_layout).setBackgroundColor(dark);
        setupToolbar(null, color);
        setupStatusBar(color);

        title = (TextView) findViewById(R.id.textView);
        medName = (TextView) findViewById(R.id.textView2);
        fab = (FloatingActionButton) findViewById(R.id.add_button);
        scheduleTypeSelector = findViewById(R.id.schedule_type_selector);
        readingQrBox = findViewById(R.id.reading_qr_box);

        medName.setBackgroundColor(color);
        scheduleTypeSelector.setBackgroundColor(dark);

        routinesItem = (ImageButton) findViewById(R.id.schedule_type_routines);
        hourlyItem = (ImageButton) findViewById(R.id.schedule_type_hourly);
        cycleItem = (ImageButton) findViewById(R.id.schedule_type_period);

        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickScheduleTypeButton(v.getId());
            }
        };

        routinesItem.setOnClickListener(l);
        hourlyItem.setOnClickListener(l);
        cycleItem.setOnClickListener(l);

        qrData = getIntent().getStringExtra("qr_data");
        try {
            new ProcessQRTask().execute(qrData);
        } catch (Exception e) {
            LogUtil.e(TAG, "Error processing QR", e);
            Toast.makeText(this, "Error inesperado actualizando!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private List<PrescriptionWrapper> filterValidPrescriptions(PrescriptionListWrapper prescriptionListWrapper) {
        List<PrescriptionWrapper> p = new ArrayList<>();

        for (PrescriptionWrapper pw : prescriptionListWrapper.p) {
            if (pw.cn != null) {
                Prescription pr = DB.drugDB().prescriptions().findByCn(pw.cn);
                boolean prescriptionExists = pr != null;
                boolean medExists = DB.medicines().findOneBy(Medicine.COLUMN_CN, pw.cn) != null;

                if (prescriptionExists) {
                    pw.exists = medExists;
                    pw.prescription = pr;
                    p.add(pw);
                }
            } else if (pw.g != null) {

                HomogeneousGroup group = findGroup(pw.g);
                if (group != null) {
                    LogUtil.d(TAG, "Found group: " + group.getName());
                    pw.exists = true;
                    pw.isGroup = true;
                    pw.group = group;
                    p.add(pw);
                }
            }

            if (pw.pk != null) {
                for (PickupWrapper pkw : pw.pk) {
                    LogUtil.d(TAG, "Pickup : " + df.parseDateTime(pkw.f).toString("dd/MM/yyyy"));
                    LogUtil.d(TAG, "Pickup : " + df.parseDateTime(pkw.t).toString("dd/MM/yyyy"));
                    LogUtil.d(TAG, "Pickup : " + pkw.tk);
                }
            }
        }
        return p;
    }

    private HomogeneousGroup findGroup(String g) {
        return DB.drugDB().homogeneousGroups().findOneBy(HomogeneousGroup.COLUMN_HOMOGENEOUS_GROUP_ID, g);
    }

    private void hideScheduleTypeSelector() {
        scheduleTypeSelector.setVisibility(View.INVISIBLE);
    }

    private void showScheduleTypeSelector() {
        if (scheduleTypeSelector.getVisibility() != View.VISIBLE) {
            scheduleTypeSelector.setVisibility(View.VISIBLE);
        }
    }

    private void saveSchedules() {

        final ProgressDialog progress = ProgressDialog.show(this, "Calendula", "Actualizando pautas...", true);

        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg0) {
                try {
                    TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {

                            for (int i = 0; i < scheduleCount; i++) {

                                LogUtil.d(TAG, "Item " + i);

                                Fragment f = getViewPagerFragment(i + 1);

                                if (f instanceof ScheduleImportFragment) {

                                    LogUtil.d(TAG, "Fragment " + i);

                                    ScheduleImportFragment c = (ScheduleImportFragment) f;

                                    if (c.validate()) {
                                        PrescriptionWrapper w = prescriptionList.get(i);
                                        LogUtil.d(TAG, "Validate!");
                                        String cn = w.cn;
                                        Medicine m = null;
                                        if (cn != null) {
                                            m = DB.medicines().findByCnAndPatient(cn, patient);
                                            if (m == null) {
                                                LogUtil.d(TAG, "Saving medicine!");
                                                m = Medicine.fromPrescription(DB.drugDB().prescriptions().findByCn(cn));
                                                m.setPatient(patient);
                                                m.save();
                                            }
                                        } else if (w.isGroup) {
                                            m = DB.medicines().findByGroupAndPatient(w.group.getId(), patient);
                                            if (m == null) {
                                                m = new Medicine(Strings.firstPart(w.group.getName()));
                                                m.setHomogeneousGroup(w.group.getHomogeneousGroupID());
                                                Presentation pres = DBRegistry.instance().current().expectedPresentation(w.group.getName(), w.group.getName());
                                                m.setPresentation(pres != null ? pres : Presentation.PILLS);
                                                m.setPatient(patient);
                                                m.setDatabase(DBRegistry.instance().current().id());
                                                m.save();
                                            }
                                        } else {
                                            throw new RuntimeException(" Prescription must have a cn or group reference");
                                        }

                                        Schedule s = c.getSchedule();
                                        Schedule prev = DB.schedules().findByMedicineAndPatient(m, patient);
                                        // TODO: find by med and patient
                                        if (prev != null) {
                                            LogUtil.d(TAG, "Found previous schedule for med " + m.getId());
                                            updateSchedule(prev, s, c.getScheduleItems());
                                        } else {
                                            LogUtil.d(TAG, "Not found previous schedule for med " + m.getId());
                                            createSchedule(s, c.getScheduleItems(), m);
                                        }

                                        if (m != null) {
                                            // remove old pickups before inserting the new ones
                                            DB.pickups().removeByMed(m);
                                        }

                                        if (m != null && w.pk != null && w.pk.size() > 0) {


                                            for (PickupWrapper pkw : w.pk) {
                                                PickupInfo pickupInfo = new PickupInfo();
                                                pickupInfo.setTo(df.parseLocalDate(pkw.t));//.plusMonths(19));
                                                pickupInfo.setFrom(df.parseLocalDate(pkw.f));//.plusMonths(19));
                                                pickupInfo.taken(pkw.tk == 1);
                                                pickupInfo.setMedicine(m);
                                                DB.pickups().save(pickupInfo);
                                            }
                                        }
                                    } else {
                                        mViewPager.setCurrentItem(i + 1);
                                        Snack.show("Hmmmmmm....", ConfirmSchedulesActivity.this);
                                    }
                                }
                            }
                            CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
                            AlarmScheduler.instance().updateAllAlarms(ConfirmSchedulesActivity.this);
                            return null;
                        }
                    });

                    return true;

                } catch (Exception e) {
                    LogUtil.e(TAG, "Error saving prescriptions", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (progress != null) {
                    progress.dismiss();
                }
                if (result) {
                    finish();
                }
            }
        };

        task.execute((Void[]) null);
    }

    private void updatePageTitle(int i) {
        if (i == 0) {
            title.setText(scheduleCount + " " + getString(R.string.scan_prescriptions));
            medName.setText(getString(R.string.scan_review_title));
        } else if (i == scheduleCount + 1) {
            title.setText(scheduleCount + " " + getString(R.string.scan_prescriptions));
            medName.setText(getString(R.string.confirm));
        } else {
            PrescriptionWrapper pw = prescriptionList.get(i - 1);
            if (pw.cn != null) {
                if (pw.prescription == null) {
                    pw.prescription = DB.drugDB().prescriptions().findByCn(pw.cn);
                }
                medName.setText(Strings.toProperCase(pw.prescription.getName()));

            } else if (pw.isGroup) {
                medName.setText(pw.group.getName());
            }
            title.setText(getResources().getString(R.string.confirm_prescription_x_of_y, i, scheduleCount));
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        int scheduleCount;

        public SectionsPagerAdapter(FragmentManager fm, int scheduleCount) {
            super(fm);
            this.scheduleCount = scheduleCount;
        }

        @Override
        public Fragment getItem(int position) {

            LogUtil.d(TAG, "Position: " + position);

            if (position == 0) {
                return ScheduleConfirmationStartFragment.newInstance();
            } else if (position == scheduleCount + 1) {
                return ScheduleConfirmationEndFragment.newInstance();
            } else {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                PrescriptionWrapper pw = prescriptionList.get(position - 1);
                return ScheduleImportFragment.newInstance(pw);
            }
        }

        @Override
        public int getCount() {
            return scheduleCount + 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    private class ProcessQRTask extends AsyncTask<String, Integer, Long> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ConfirmSchedulesActivity.this);
            dialog.setMessage("Processing QR info ...");
        }

        protected Long doInBackground(String... data) {
            String qrData = data[0];

            if (qrData != null) {
                try {
                    prescriptionList = filterValidPrescriptions(parseQRData(qrData));
                    Long sCount = (long) prescriptionList.size();
                    if (sCount > 0) {
                        return sCount;
                    } else {
                        return 0L;
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, "Error processing QR", e);
                    return -1L;
                }
            } else {
                return -1L;
            }
        }


        protected void onPostExecute(Long schedules) {

            scheduleCount = schedules.intValue();

            if (schedules == -1) {
                Toast.makeText(ConfirmSchedulesActivity.this, "Error inesperado actualizando!", Toast.LENGTH_LONG).show();
                finish();
            } else if (schedules == 0) {
                Toast.makeText(ConfirmSchedulesActivity.this, "No se encontraron prescripciones válidas!", Toast.LENGTH_SHORT).show();
            } else {
                updatePageTitle(0);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveSchedules();
                    }
                });
                // Create the adapter that will return a fragment for each of the three
                // primary sections of the activity.
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), scheduleCount);
                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mViewPager.setOnPageChangeListener(ConfirmSchedulesActivity.this);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mViewPager.setOffscreenPageLimit(20);
                hideScheduleTypeSelector();
                readingQrBox.setVisibility(View.GONE);
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


}
