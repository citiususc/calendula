package es.usc.citius.servando.calendula;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.activities.CalendarActivity;
import es.usc.citius.servando.calendula.activities.ConfirmActivity;
import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.HomeProfileMgr;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;
import es.usc.citius.servando.calendula.util.AppTutorial;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Snack;

public class HomePagerActivity extends CalendulaActivity implements
        RoutinesListFragment.OnRoutineSelectedListener,
        MedicinesListFragment.OnMedicineSelectedListener,
        ScheduleListFragment.OnScheduleSelectedListener {

    private static final String TAG = "HomePagerActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private HomePageAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout toolbarLayout;

    HomeProfileMgr homeProfileMgr;
    View userInfoFragment;

    private AppTutorial tutorial;
    private LeftDrawerMgr drawerMgr;

    private FloatingActionButton fab;
    FloatingActionsMenu addButton;
    FabMenuMgr fabMgr;

    TextView toolbarTitle;


    private Patient activePatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar(null, Color.TRANSPARENT);
        initializeDrawer(savedInstanceState);
        setupStatusBar(Color.TRANSPARENT);
        subscribeToEvents();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new HomePageAdapter(getSupportFragmentManager(),this,this);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(getPageChangeListener());
        mViewPager.setOffscreenPageLimit(5);

        // Set up home profile
        homeProfileMgr = new HomeProfileMgr();
        userInfoFragment = findViewById(R.id.user_info_fragment);
        homeProfileMgr.init(userInfoFragment, this);

        activePatient = DB.patients().getActive(this);
        toolbarLayout.setContentScrimColor(activePatient.color());

        // Setup fab
        addButton = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        fab = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.add_button);
        fabMgr = new FabMenuMgr(fab, addButton, drawerMgr, this);
        fabMgr.init();

        // Setup the tabLayout
        setupTabLayout();

        AppBarLayout.OnOffsetChangedListener mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(toolbarLayout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(toolbarLayout)) {
                    homeProfileMgr.onCollapse();
                    toolbarTitle.animate().alpha(1);
                } else {
                    if(mViewPager.getCurrentItem()==0) {
                        toolbarTitle.animate().alpha(0);
                    }
                    homeProfileMgr.onExpand();
                }
            }
        };
        appBarLayout.addOnOffsetChangedListener(mListener);

        // configure tutorial
        this.tutorial = new AppTutorial();
        this.tutorial.init(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateAempsIfNeeded();
            }
        }, 1500);
   }


    private void setupTabLayout(){

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        IIcon[] icons = new IIcon[]{
                GoogleMaterial.Icon.gmd_home,
                GoogleMaterial.Icon.gmd_alarm,
                CommunityMaterial.Icon.cmd_pill,
                GoogleMaterial.Icon.gmd_calendar,
        } ;

        for (int i = 0; i < tabLayout.getTabCount(); i++) {

            Drawable icon = new IconicsDrawable(this)
                    .icon(icons[i])
                    .alpha(80)
                    .paddingDp(2)
                    .color(Color.WHITE)
                    .sizeDp(24);

            tabLayout.getTabAt(i).setIcon(icon);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Patient p = DB.patients().getActive(this);
        drawerMgr.onActivityResume(p);
    }


    private ViewPager.OnPageChangeListener getPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTitle(position);
                fabMgr.onViewPagerItemChange(position);
                if (position == 0) {
                    appBarLayout.setExpanded(true);
                } else {
                    appBarLayout.setExpanded(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }


    private void updateTitle(int page) {
        String title;

        switch (page) {
            case 1:
                title = getString(R.string.title_activity_routines) + " de " + activePatient.name();
                break;
            case 2:
                title = getString(R.string.title_activity_medicines) + " de " + activePatient.name();
                break;
            case 3:
                title = getString(R.string.title_activity_schedules) + " de " + activePatient.name();
                break;
            default:
                title = "Calendula";
                break;
        }
        toolbarTitle.setText(title);
    }


    void showMessage(String text){
        Snackbar.make(appBarLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }


    private void initializeDrawer(Bundle savedInstanceState) {
        drawerMgr = new LeftDrawerMgr(this,toolbar);
        drawerMgr.init(savedInstanceState);
    }


    public void showPagerItem(int position){
        showPagerItem(position, true);
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
                final boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();
                ((DailyAgendaFragment) getViewPagerFragment(0)).toggleViewMode();
                appBarLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        appBarLayout.setExpanded(expanded);
                    }
                },200);
                item.setIcon(getResources().getDrawable((!expanded) ? R.drawable.ic_unfold_less_white_48dp
                                : R.drawable.ic_unfold_more_white_48dp));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public AppTutorial getTutorial() {
        return tutorial;
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
            activePatient = ((PersistenceEvents.ActiveUserChangeEvent) evt).patient;
            updateTitle(mViewPager.getCurrentItem());
            toolbarLayout.setContentScrimColor(activePatient.color());
        } else if(evt instanceof PersistenceEvents.UserCreateEvent){
            Patient created = ((PersistenceEvents.UserCreateEvent) evt).patient;
            drawerMgr.onPatientCreated(created);
        } else if(evt instanceof HomeProfileMgr.BackgroundUpdatedEvent){
            ((DailyAgendaFragment) getViewPagerFragment(0)).refresh();
        } else if (evt instanceof ConfirmActivity.ConfirmStateCHangeEvent) {
            int pos = ((ConfirmActivity.ConfirmStateCHangeEvent)evt).position;
            //Toast.makeText(this, "Position: " + pos, Toast.LENGTH_SHORT).show();
            ((DailyAgendaFragment) getViewPagerFragment(0)).refreshPosition(pos);
        }
    }


    public void showTutorial() {
        showPagerItem(0);
        getTutorial().reset(this);
        getTutorial().show(AppTutorial.WELCOME, AppTutorial.HOME_INFO, this);
    }


    public void enableOrDisablePharmacyMode(){
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(CalendulaApp.isPharmaModeEnabled(getApplicationContext())){
            prefs.edit().putBoolean(CalendulaApp.PHARMACY_MODE_ENABLED, false).commit();
            Snack.show("Acabas de deshabilitar el modo farmacia!", HomePagerActivity.this);
            fabMgr.onPharmacyModeChanged(false);
            drawerMgr.onPharmacyModeChanged(false);
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
            new PopulatePrescriptionDBService().updateIfNeeded(HomePagerActivity.this);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(HomePagerActivity.this);
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
            Snack.show("Acabas de habilitar el modo farmacia!", HomePagerActivity.this);
            fabMgr.onPharmacyModeChanged(true);
            drawerMgr.onPharmacyModeChanged(true);
        }
    }

    void updateAempsIfNeeded(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int msgRes = R.string.updating_prescriptions_db_msg;
        boolean dbEnabled = prefs.getBoolean("enable_prescriptions_db", false);
        if(dbEnabled && PopulatePrescriptionDBService.isDbOutdated(this)){
            new PopulatePrescriptionDatabaseTask(msgRes).execute("");
        }
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                FragmentUtils.makeViewPagerFragmentName(R.id.container, position));
    }


    private void launchActivity(Intent i) {
        startActivity(i);
        this.overridePendingTransition(0, 0);
    }


    // Interface implementations

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

}
