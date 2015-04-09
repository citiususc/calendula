package es.usc.citius.servando.calendula.activities;

import android.app.ProgressDialog;
import android.graphics.drawable.InsetDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.j256.ormlite.misc.TransactionManager;
import com.melnykov.fab.FloatingActionButton;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.ScheduleConfirmationEndFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleConfirmationFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleConfirmationStartFragment;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;

public class ConfirmSchedulesActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener{

    Toolbar toolbar;
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

    DateTimeFormatter df = DateTimeFormat.forPattern("yyMMdd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_schedules);

        title = (TextView) findViewById(R.id.textView);
        medName = (TextView) findViewById(R.id.textView2);
        fab = (FloatingActionButton) findViewById(R.id.add_button);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);        
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 15, 15, 15, 15));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }

        toolbar.setTitle("");
        
        String qrData = getIntent().getStringExtra("qr_data");


        new ProcessQRTask().execute(qrData);
        

    }

    private List<PrescriptionWrapper> filterValidPrescriptions(PrescriptionListWrapper prescriptionListWrapper) {        
        List<PrescriptionWrapper> p = new ArrayList<PrescriptionWrapper>();

        for (PrescriptionWrapper pw : prescriptionListWrapper.p) {
            if(pw.cn!=null) {
                Prescription pr = Prescription.findByCn(pw.cn);
                boolean prescriptionExists = pr != null;
                boolean medExists = DB.medicines().findOneBy(Prescription.COLUMN_CN, pw.cn) != null;
                
                if(prescriptionExists){
                    pw.exists = medExists;
                    pw.prescription = pr;
                    p.add(pw);
                }
            } else if (pw.g != null) {

                HomogeneousGroup group = findGroup(pw.g);
                if (group != null) {
                    Log.d("ConfirmSchedulesAct", "Found group: " + group.name);
                    pw.exists = true;
                    pw.isGroup = true;
                    pw.group = group;
                    p.add(pw);
                }
            }

            if (pw.pk != null) {
                for (PickupWrapper pkw : pw.pk) {
                    Log.d("ConfirmSchedulesAct", "Pickup : " + df.parseDateTime(pkw.f).toString("dd/MM/yyyy"));
                    Log.d("ConfirmSchedulesAct", "Pickup : " + df.parseDateTime(pkw.t).toString("dd/MM/yyyy"));
                    Log.d("ConfirmSchedulesAct", "Pickup : " + pkw.tk);
                }
            }



        }
        return p;
    }

    private HomogeneousGroup findGroup(String g) {
        return DB.groups().findOneBy(HomogeneousGroup.COLUMN_GROUP, g);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirm_schedules, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_confirm_all) {            
            saveSchedules();  
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveSchedules() {


        try {

            TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    for (int i = 0; i < scheduleCount; i++) {

                        Log.d("PRESCRIPTION", "Item " + i);

                        Fragment f = getViewPagerFragment(i + 1);

                        if (f instanceof ScheduleConfirmationFragment) {

                            Log.d("PRESCRIPTION", "Fragment " + i);

                            ScheduleConfirmationFragment c = (ScheduleConfirmationFragment) f;

                            if (c.validate()) {
                                PrescriptionWrapper w = prescriptionList.get(i);
                                Log.d("PRESCRIPTION", "Validate!");
                                String cn = w.cn;

                                Medicine m = null;

                                if (cn != null) {
                                    if (DB.medicines().findOneBy(Prescription.COLUMN_CN, cn) == null) {
                                        Log.d("PRESCRIPTION", "Saving medicine!");
                                        m = Medicine.fromPrescription(Prescription.findByCn(cn));
                                        m.save();
                                    }
                                } else if (w.isGroup) {
                                    m = new Medicine(Strings.firstPart(w.group.name));
                                    Presentation pres = Presentation.expected(w.group.name, w.group.name);
                                    m.setPresentation(pres != null ? pres : Presentation.PILLS);
                                    m.save();
                                } else {
                                    throw new RuntimeException(" Prescription must have a cn or group reference");
                                }

                                Schedule s = c.getSchedule();
                                List<ScheduleItem> items = c.getScheduleItems();
                                s.setMedicine(m);
                                s.save();

                                for (ScheduleItem item : items) {
                                    item.setSchedule(s);
                                    item.save();
                                    // add to daily schedule
                                    new DailyScheduleItem(item).save();
                                }
                                // save and fire event
                                Log.d("PRESCRIPTION", "Saving schedule!");
                                s.save();

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
            Toast.makeText(this, scheduleCount + " schedules saved!", Toast.LENGTH_SHORT).show();

        } catch (
                Exception e
                )

        {
            Log.e("ConfirmSchedulesAct", "Error saving prescriptions", e);
        }
    }

    private void updatePageTitle(int i) {

        if (i == 0) {
            title.setText(scheduleCount + " prescriptions");
            medName.setText("Review");
        } else if (i == scheduleCount + 1) {
            title.setText(scheduleCount + " prescriptions");
            medName.setText("Confirm");
        } else {

            PrescriptionWrapper pw = prescriptionList.get(i - 1);
            String name = "_";

            if (pw.cn != null) {
                if (pw.prescription == null) {
                    pw.prescription = Prescription.findByCn(pw.cn);
                }
                medName.setText(Strings.toProperCase(pw.prescription.name));
                name = pw.prescription.shortName();

            } else if (pw.isGroup) {
                medName.setText(pw.group.name);
                name = Strings.firstPart(pw.group.name);
            }

            boolean isNew = Medicine.findByName(name) == null;

            title.setText(getResources().getString(R.string.confirm_prescription_x_of_y, i, scheduleCount) + (isNew ? " (new)" : ""));
            
            
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        
    }

    @Override
    public void onPageSelected(int i) {        
        updatePageTitle(i);

        Log.d("ConfirmSchedulesAct", " Page Selected: " + i);

        if (i == 0) {
            fab.setVisibility(View.INVISIBLE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }    

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public List<PrescriptionWrapper> getPrescriptions() {
        return prescriptionList;
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

            if (position == 0) {
                return ScheduleConfirmationStartFragment.newInstance();
            } else if (position == scheduleCount + 1) {
                return ScheduleConfirmationEndFragment.newInstance();
            } else {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).            
                PrescriptionWrapper pw = prescriptionList.get(position - 1);
                return ScheduleConfirmationFragment.newInstance(pw);
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


    public PrescriptionListWrapper parseQRData(String data){
        return new Gson().fromJson(data,PrescriptionListWrapper.class);
    }

    public static class PrescriptionListWrapper{
        public List<PrescriptionWrapper> p;
    }

    public static class PrescriptionWrapper implements Serializable{
        public String cn;
        public String g;
        public String sk;
        public List<PickupWrapper> pk;
        public ScheduleWrapper s;


        public Prescription prescription;
        public HomogeneousGroup group;
        
        public boolean exists;

        public boolean isGroup = false;
    }

    public static class ScheduleWrapper implements Serializable{
        public float d = -1;
        public int i = -1;
        public int p = -1;
    }

    public static class PickupWrapper implements Serializable {
        public String t;
        public String f;
        public int tk = 0;
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
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
                prescriptionList = filterValidPrescriptions(parseQRData(qrData));
                Long sCount = (long) prescriptionList.size();
                if (sCount > 0) {
                    return sCount;
                } else {
                    return 0l;
                }
            } else {
                return -1l;
            }
        }


        protected void onPostExecute(Long schedules) {

            scheduleCount = schedules.intValue();

            if (schedules == -1) {
                finish();
            } else if (schedules == 0) {
                Toast.makeText(ConfirmSchedulesActivity.this, "No prescriptions found!", Toast.LENGTH_SHORT).show();
            } else {
                updatePageTitle(0);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int current = mViewPager.getCurrentItem();
                        if (current + 1 < scheduleCount) {
                            mViewPager.setCurrentItem(current + 1);
                        }
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
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


}
