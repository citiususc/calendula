package es.usc.citius.servando.calendula.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.google.gson.Gson;
import com.melnykov.fab.FloatingActionButton;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.ScheduleConfirmationFragment;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Persistence;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.medicine.Prescription;

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
        
        String qrData = getIntent().getStringExtra("qr_data");
        
        if(qrData!=null){
           prescriptionList = filterValidPrescriptions(parseQRData(qrData));
           scheduleCount = prescriptionList.size();
           if(scheduleCount>0){
               updatePageTitle(0);               
           }else{
               Toast.makeText(this,"No prescriptions found!",Toast.LENGTH_SHORT).show();
           }
        }else{
            finish();            
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = mViewPager.getCurrentItem();
                
                if(current + 1 < scheduleCount){
                    mViewPager.setCurrentItem(current+1);
                }

            }
        });
        
        toolbar.setTitle("");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), scheduleCount);        
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(20);
        

    }

    private List<PrescriptionWrapper> filterValidPrescriptions(PrescriptionListWrapper prescriptionListWrapper) {        
        List<PrescriptionWrapper> p = new ArrayList<PrescriptionWrapper>();
        
        for(PrescriptionWrapper pw : prescriptionListWrapper.prescriptions){
            if(pw.cn!=null) {
                boolean prescriptionExists = Prescription.findByCn(pw.cn) != null;
                boolean medExists = Medicine.findByCn(pw.cn) != null;                
                
                if(prescriptionExists){
                    pw.exists = medExists;
                    p.add(pw);
                }
            }
        }
        return p;
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

        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < scheduleCount; i++) {
                
                Log.d("PRESCRIPTION", "Item " + i);
                
                Fragment f = getViewPagerFragment(i);
                
                if (f instanceof ScheduleConfirmationFragment) {

                    Log.d("PRESCRIPTION", "Fragment " + i);
                    
                    ScheduleConfirmationFragment c = (ScheduleConfirmationFragment) f;

                    if (c.validate()) {

                        Log.d("PRESCRIPTION", "Validate!");
                        String cn = prescriptionList.get(i).cn;
                        Medicine m = Medicine.findByCn(cn);                        
                        if (m == null) {
                            Log.d("PRESCRIPTION", "Saving medicine!");
                            m = Medicine.fromPrescription(Prescription.findByCn(cn));
                            m.save();
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
                        
                    }else{
                        mViewPager.setCurrentItem(i);
                        Snack.show("Hmmmmmm....",this);
                    }
                }            
            }
            CalendulaApp.eventBus().post(PersistenceEvents.SCHEDULE_EVENT);
            AlarmScheduler.instance().updateAllAlarms(this);
            ActiveAndroid.setTransactionSuccessful();            
            Toast.makeText(this, scheduleCount + " schedules saved!", Toast.LENGTH_SHORT).show();
            
        }catch (Exception e){
            Log.e("ConfirmSchedulesActivity", "Error saving prescriptions", e);            
        }finally {
            if(ActiveAndroid.inTransaction())
                ActiveAndroid.endTransaction();
        }
    }

    private void updatePageTitle(int i) {
        PrescriptionWrapper pw = prescriptionList.get(i);

        title.setText(getResources().getString(R.string.confirm_prescription_x_of_y, i+1, scheduleCount));

        if(pw.cn!=null){
            if(pw.p == null){
                pw.p = Prescription.findByCn(pw.cn);
            }
            medName.setText(Strings.toProperCase(pw.p.name));
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        
    }

    @Override
    public void onPageSelected(int i) {        
        updatePageTitle(i);
    }    

    @Override
    public void onPageScrollStateChanged(int i) {

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
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            PrescriptionWrapper pw = prescriptionList.get(position);
            return ScheduleConfirmationFragment.newInstance(pw);
        }

        @Override
        public int getCount() {            
            return scheduleCount;
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
        public List<PrescriptionWrapper> prescriptions;
    }

    public static class PrescriptionWrapper implements Serializable{
        public String cn;
        public boolean exists;
        public String sk;        
        public ScheduleWrapper sched;
        public Prescription p;
    }

    public static class ScheduleWrapper implements Serializable{
        public float dose = -1;
        public int interval = -1;
        public int period = -1;
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

}
