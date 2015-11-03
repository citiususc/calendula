package es.usc.citius.servando.calendula;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.activities.CalendarActivity;
import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.ReminderNotification;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;
import es.usc.citius.servando.calendula.util.AppTutorial;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Snack;


/**
 * Main activity holding fragments for agenda, routine, medicine, a schedule management
 */
public class HomeActivity extends CalendulaActivity
        implements ViewPager.OnPageChangeListener, View.OnClickListener,
        RoutinesListFragment.OnRoutineSelectedListener,
        MedicinesListFragment.OnMedicineSelectedListener,
        ScheduleListFragment.OnScheduleSelectedListener {

    private static final String TAG = "HomeActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    HomePageAdapter mSectionsPagerAdapter;

    int currentActionBarColor;
    int previousActionBarColor;

    FloatingActionsMenu addButton;
    FabMenuMgr fabMgr;
    String[] titles;

    PagerSlidingTabStrip tabs;
    View tabsShadow;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
//    DrawerListAdapter drawerListAdapter;

    private AppTutorial tutorial;
    private LeftDrawerMgr drawerMgr;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the content view layout
        setContentView(R.layout.activity_home);

        setupToolbar(null, Color.TRANSPARENT);
        initializeDrawer(savedInstanceState);
        setupStatusBar(Color.TRANSPARENT);
        subscribeToEvents();

        // initialize current and previous action bar colors
        currentActionBarColor = getResources().getColor(R.color.transparent);
        previousActionBarColor = getResources().getColor(R.color.transparent);
        // Create the adapter that will manage sections
        mSectionsPagerAdapter = new HomePageAdapter(getSupportFragmentManager(), this, this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsShadow = findViewById(R.id.tabs_shadow);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);

        titles = getResources().getStringArray(R.array.home_action_list);
        addButton = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        fab = (FloatingActionButton) findViewById(R.id.add_button);
        fabMgr = new FabMenuMgr(fab, addButton, drawerMgr, this);
        fabMgr.init();

        tabs.setOnPageChangeListener(this);
        tabs.setShouldExpand(true);
        tabs.setAllCaps(true);
        tabs.setTabPaddingLeftRight(30);
        tabs.setShouldExpand(true);
        tabs.setDividerColor(getResources().getColor(R.color.white_50));
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.tab_indicator_height));
        tabs.setScrollOffset(50);
        tabs.setIndicatorColor(getResources().getColor(R.color.white));
        tabs.setTextColor(getResources().getColor(R.color.white_80));
        tabs.setUnderlineColor(getResources().getColor(R.color.transparent));

        tabs.setBackgroundColor(getResources().getColor(R.color.transparent));
        tabs.setVisibility(View.GONE);
        tabsShadow.setVisibility(View.GONE);
        tabs.setViewPager(mViewPager);

        setTutorial(new AppTutorial());
        getTutorial().init(this);
        checkReminder(getIntent());
        startTutorialIfNeeded();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateAempsIfNeeded();
            }
        }, 1500);


    }

    private void initializeDrawer(Bundle savedInstanceState) {
        drawerMgr = new LeftDrawerMgr(this,toolbar);
        drawerMgr.init(savedInstanceState);
    }

    @Override
    public void onRoutineSelected(Routine r) {
        Intent i = new Intent(this, RoutinesActivity.class);
        i.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());
        launchActivity(i);
    }

    @Override
    public void onCreateRoutine() {
        //do nothing
    }

    @Override
    public void onMedicineSelected(Medicine m) {
        Intent i = new Intent(this, MedicinesActivity.class);
        i.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, m.getId());
        launchActivity(i);
    }

    @Override
    public void onCreateMedicine() {

        //do nothing
    }

    @Override
    public void onScheduleSelected(Schedule r) {
        Intent i = new Intent(this, ScheduleCreationActivity.class);
        i.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, r.getId());
        launchActivity(i);
    }

    @Override
    public void onCreateSchedule() {

    }

    public AppTutorial getTutorial() {
        return tutorial;
    }

    public void setTutorial(AppTutorial tutorial) {
        this.tutorial = tutorial;
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.add_button) {
            Intent i;
            switch (mViewPager.getCurrentItem()) {
                case 0: // agenda
                    launchActivity(new Intent(this, ScheduleCreationActivity.class));
                    break;
                case 1: // routines
                    launchActivity(new Intent(this, RoutinesActivity.class));
                    break;
                case 2: // medicines
                    i = new Intent(this, MedicinesActivity.class);
                    i.putExtra("create", true);
                    launchActivity(i);
                    break;
                case 3: // schedules
                    launchActivity(new Intent(this, ScheduleCreationActivity.class));
                    break;
            }
        }
    }

    private void launchActivity(Intent i) {
        startActivity(i);
        this.overridePendingTransition(0, 0);
    }

    public void showPagerItem(int position){
        showPagerItem(position,true);
    }


    public void showPagerItem(int position, boolean updateDrawer){
        if(position >= 0 && position < mViewPager.getChildCount()){
            mViewPager.setCurrentItem(position);
            if(updateDrawer) {
                drawerMgr.onPagerPositionChange(position);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkReminder(intent);
    }

    private void checkReminder(Intent intent) {

        Log.d(TAG, "CheckReminder: " + intent.getDataString());
        final long remindRoutineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1l);
        final long delayRoutineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID, -1l);
        final long remindScheduleId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1l);
        final long delayScheduleId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID, -1l);
        final String scheduleTime = intent.getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME);

        if (remindRoutineId != -1) {
            // TODO mDrawerLayout.closeDrawer(drawerView);
            showReminder(remindRoutineId);
            getIntent().removeExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID);
        } else if (delayRoutineId != -1) {
            Log.d(TAG, "isDelay! ");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showPagerItem(0);
                    // TODO mDrawerLayout.closeDrawer(drawerView);
                    final Routine r = Routine.findById(delayRoutineId);
                    ((DailyAgendaFragment) getViewPagerFragment(0)).showDelayDialog(r);
                    ReminderNotification.cancel(HomeActivity.this);
                    getIntent().removeExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID);
                }
            }, 1000);
        } else if (remindScheduleId != -1) {
            // TODO mDrawerLayout.closeDrawer(drawerView);
            showReminder(remindScheduleId,
                    LocalTime.parse(scheduleTime, DateTimeFormat.forPattern("kk:mm")));
            getIntent().removeExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID);
        } else if (delayScheduleId != -1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showPagerItem(0);
                    // TODO mDrawerLayout.closeDrawer(drawerView);
                    final Schedule s = Schedule.findById(delayScheduleId);
                    LocalTime t = LocalTime.parse(scheduleTime, DateTimeFormat.forPattern("kk:mm"));
                    ((DailyAgendaFragment) getViewPagerFragment(0)).showDelayDialog(s, t);

                    ReminderNotification.cancel(HomeActivity.this);
                    getIntent().removeExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID);
                }
            }, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        CalendulaApp.eventBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        int pageNum = mViewPager.getCurrentItem();
        if (pageNum == 0) {
            boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();
            menu.findItem(R.id.action_expand).setVisible(true);
            menu.findItem(R.id.action_expand)
                    .setIcon(getResources().getDrawable(expanded ? R.drawable.ic_unfold_less_white_48dp
                            : R.drawable.ic_unfold_more_white_48dp));
        } else {
            menu.findItem(R.id.action_expand).setVisible(false);
        }

        if (pageNum == 2 && CalendulaApp.isPharmaModeEnabled(this)) {
            menu.findItem(R.id.action_calendar).setVisible(true);
        } else {
            menu.findItem(R.id.action_calendar).setVisible(false);
        }


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_calendar:
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            case R.id.action_expand:
                Log.d("Home", "ToogleExpand");
                ((DailyAgendaFragment) getViewPagerFragment(0)).toggleViewMode();
                boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();
                item.setIcon(getResources().getDrawable(
                        expanded ? R.drawable.ic_unfold_less_white_48dp
                                : R.drawable.ic_unfold_more_white_48dp));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        boolean backProcesed = false;

        if (tutorial.isOpen()) {
            tutorial.hide();
            return;
        }

        Fragment current = getViewPagerFragment(mViewPager.getCurrentItem());
        if (current instanceof OnBackPressedListener) {
            backProcesed = ((OnBackPressedListener) current).doBack();
        }

        if (mViewPager.getCurrentItem() != 0) {
            showPagerItem(0);
        } else if (!backProcesed) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0) {
            hideTabs();
        } else {
            showTabs();
        }
    }

    void showReminder(final Long routineId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Routine r = Routine.findById(routineId);
                showPagerItem(0);
                ((DailyAgendaFragment) getViewPagerFragment(0)).showReminder(r);
            }
        }, 1000);
    }

    void showReminder(final Long scheduleId, final LocalTime scheduleTime) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Schedule r = Schedule.findById(scheduleId);
                showPagerItem(0);
                ((DailyAgendaFragment) getViewPagerFragment(0)).showReminder(r, scheduleTime);
            }
        }, 1000);
    }

    @Override
    public void onPageSelected(int page) {
        invalidateOptionsMenu();
        fabMgr.onViewPagerItemChange(page);
        updateTitle(page);
        drawerMgr.onPagerPositionChange(page);
        showTutorialStage(page);
        if (page == 0) {
            hideTabs();
        } else if (toolbar.getVisibility() != View.VISIBLE) {
            toolbar.setVisibility(View.VISIBLE);
            showTabs();
        }
    }

    private void updateTitle(int page) {
        String title;

        switch (page) {
            case 1:
                title = getString(R.string.title_activity_routines);
                break;
            case 2:
                title = getString(R.string.title_activity_medicines);
                break;
            case 3:
                title = getString(R.string.title_activity_schedules);
                break;
            default:
                title = "";
                break;
        }
        toolbar.setTitle(title);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    public void hideToolbar() {
        toolbar.setVisibility(View.GONE);
    }

    public void showToolbar() {
        toolbar.setVisibility(View.VISIBLE);
    }

    public void showTabs() {
        if (!(tabs.getVisibility() == View.VISIBLE)) {
            tabs.setVisibility(View.VISIBLE);
            tabsShadow.setVisibility(View.VISIBLE);
        }
    }

    public void hideTabs() {
        if (!(tabs.getVisibility() == View.GONE)) {
            tabs.setVisibility(View.GONE);
            tabsShadow.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
            }
        }
    }

    // Method called from the event bus
    @SuppressWarnings("unused")
    public void onEvent(Object evt) {
        if(evt instanceof  PersistenceEvents.ModelCreateOrUpdateEvent){
            PersistenceEvents.ModelCreateOrUpdateEvent event = (PersistenceEvents.ModelCreateOrUpdateEvent)evt;
            Log.d(TAG, "onEvent: " + event.clazz.getName());
            ((DailyAgendaFragment) getViewPagerFragment(0)).notifyDataChange();
            ((RoutinesListFragment) getViewPagerFragment(1)).notifyDataChange();
            ((MedicinesListFragment) getViewPagerFragment(2)).notifyDataChange();
            ((ScheduleListFragment) getViewPagerFragment(3)).notifyDataChange();
        }else if(evt instanceof PersistenceEvents.ActiveUserChangeEvent){
            // TODO
        }


    }

    private void startTutorialIfNeeded() {
        getTutorial().show(AppTutorial.WELCOME, AppTutorial.HOME_INFO, this);
    }

    private void showTutorialStage(int step) {
        switch (step) {
            case 0:
                //Home
                getTutorial().show(AppTutorial.HOME_INFO, this);
                break;
            case 1:
                // routines
                getTutorial().show(AppTutorial.ROUTINES_INFO, this);
                break;
            case 2:
                // meds
                getTutorial().show(AppTutorial.MEDICINES_INFO, this);
                break;
            case 3:
                // schedules
                getTutorial().show(AppTutorial.SCHEDULES_INFO, this);
                break;
            default:
                break;
        }
    }

    public void showTutorial() {
        showPagerItem(0);
        getTutorial().reset(this);
        getTutorial().show(AppTutorial.WELCOME, AppTutorial.HOME_INFO, this);
    }

    public interface OnBackPressedListener {
        boolean doBack();
    }


    public void enableOrDisablePharmacyMode(){
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(CalendulaApp.isPharmaModeEnabled(getApplicationContext())){
            prefs.edit().putBoolean(CalendulaApp.PHARMACY_MODE_ENABLED, false).commit();
            Snack.show("Acabas de deshabilitar el modo farmacia!", HomeActivity.this);
            fabMgr.onPharmacyModeChanged(false);
        }else {
            prefs.edit().putBoolean(CalendulaApp.PHARMACY_MODE_ENABLED, true)
                    .putBoolean("enable_prescriptions_db", true)
                    .commit();
            try {
                DB.prescriptions().executeRaw("DELETE FROM Prescriptions;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            new PopulatePrescriptionDatabaseTask().execute("");
        }
    }


    public class PopulatePrescriptionDatabaseTask extends AsyncTask<String, String, Void> {


        ProgressDialog dialog;
        int msgResource = R.string.enable_prescriptions_progress_messgae;


        public  PopulatePrescriptionDatabaseTask(){}

        public PopulatePrescriptionDatabaseTask(int msgResource){
            this.msgResource = msgResource;
        }

        @Override
        protected Void doInBackground(String... params) {
            new PopulatePrescriptionDBService().updateIfNeeded(HomeActivity.this);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(HomeActivity.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(msgResource));
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Snack.show("Acabas de habilitar el modo farmacia!", HomeActivity.this);
            fabMgr.onPharmacyModeChanged(true);
        }
    }

    void updateAempsIfNeeded(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int msgRes = R.string.updating_prescriptions_db_msg;
        boolean dbEnabled = prefs.getBoolean("enable_prescriptions_db", false);
        if(dbEnabled){
            new PopulatePrescriptionDatabaseTask(msgRes).execute("");
        }
    }


}
