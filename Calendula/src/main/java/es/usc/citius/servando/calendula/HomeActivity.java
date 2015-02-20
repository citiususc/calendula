package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.makeramen.RoundedImageView;
import com.melnykov.fab.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.ReminderNotification;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SettingsActivity;
import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.util.AppTutorial;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.view.ScrimInsetsFrameLayout;

//import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
//import com.github.amlcurran.showcaseview.ShowcaseView;
//import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
//import com.github.amlcurran.showcaseview.targets.PointTarget;

public class HomeActivity extends ActionBarActivity implements
        ViewPager.OnPageChangeListener,
        ActionBar.OnNavigationListener,
        View.OnClickListener,
        RoutinesListFragment.OnRoutineSelectedListener,
        MedicinesListFragment.OnMedicineSelectedListener,
        ScheduleListFragment.OnScheduleSelectedListener {


    public static final int ANIM_ACTION_BAR_DURATION = 100;
    public static final int ANIM_TABS_DURATION = 250;
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
    //    ActionBar mActionBar;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ScrimInsetsFrameLayout drawerView;
    ActionBarDrawerToggle mDrawerToggle;
    int currentActionBarColor;
    int previousActionBarColor;

    FloatingActionButton addButton;
    boolean addButtonShown = true;
    //boolean profileShown = true;
    String[] titles;

    Toolbar toolbar;
    PagerSlidingTabStrip tabs;
    Handler mHandler;
    View tabsShadow;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    //    ShowcaseView sv;
    boolean showcaseShown = false;
    private boolean toolbarVisible;

//    ShowcaseView sv;

    private AppTutorial tutorial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                getWindow().getDecorView().setSystemUiVisibility(
//                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
//        }


        // set the content view layout
        setContentView(R.layout.activity_home);
        mHandler = new Handler();
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


        tabs.setOnPageChangeListener(this);
        tabs.setShouldExpand(true);
        tabs.setAllCaps(true);
        tabs.setTabPaddingLeftRight(30);
        tabs.setShouldExpand(true);
        tabs.setDividerColor(getResources().getColor(R.color.white_50));
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.tab_indicator_height));

        tabs.setIndicatorColor(getResources().getColor(R.color.white));
        tabs.setTextColor(getResources().getColor(R.color.white_80));
        tabs.setUnderlineColor(getResources().getColor(R.color.transparent));

        tabs.setBackgroundColor(getResources().getColor(R.color.transparent));
        tabs.setVisibility(View.GONE);
        tabsShadow.setVisibility(View.GONE);
        tabs.setViewPager(mViewPager);
        // set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        toolbar.setNavigationIcon(R.drawable.ic_launcher_white);
        // configure toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // initialize left drawer
        initializeDrawer();

        titles = getResources().getStringArray(R.array.home_action_list);
        addButton = (FloatingActionButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        hideAddButton();
        CalendulaApp.eventBus().register(this);

        setTutorial(new AppTutorial());
        getTutorial().init(this, tabs);

        Log.d(TAG, "OnCreate  - Routine Id Extra: " + getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1l));
        checkReminder(getIntent());
        startTutorialIfNeeded();

    }

    public int getActionDrawable(int index) {
        switch (index) {

            case 1:
                return R.drawable.ic_small_home_w;
            case 3:
                return R.drawable.ic_alarm_white_48dp;
            case 4:
                return R.drawable.ic_pill;
            case 5:
                return R.drawable.ic_event_white_48dp;
            case 7:
                return R.drawable.ic_room_white_48dp;
            case 8:
                return R.drawable.ic_small_plane_w;
            default:
                return R.drawable.ic_small_home_w;

        }
    }

    public int getActionColor(int index) {
        switch (index) {
            case 3:
                return R.color.android_blue;
            case 4:
                return R.color.android_pink;
            case 5:
                return R.color.android_green;
            case 7:
                return R.color.android_orange_lighter;
            case 8:
                return R.color.android_red_lighter;
            default:
                return R.color.dark_grey_home;

        }
    }

    public int getAddButtonColor(int page) {
        switch (page) {
            case 1:
                return R.color.android_blue_dark;
            case 2:
                return R.color.android_pink_dark;
            case 3:
                return R.color.android_green;
            default:
                return R.color.android_blue_darker;

        }
    }


    private void initializeDrawer() {


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        drawerView = (ScrimInsetsFrameLayout) findViewById(R.id.left_drawer);

        List<String> items = Arrays.asList(getResources().getStringArray(R.array.home_drawer_actions));
        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerListAdapter(getApplication(), R.layout.drawer_list_item, items));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,//R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//                if (mViewPager.getCurrentItem() != 0) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        //getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
//                    }
//                } else {
////                    getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
//                }
//                updateTitle(mViewPager.getCurrentItem());
//                int pageNum = mViewPager.getCurrentItem();
//                MenuItem expand = toolbar.getMenu().findItem(R.id.action_expand);
//                if (pageNum == 0 && expand != null) {
//                    expand.setVisible(true);
//                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    //if (mViewPager.getCurrentItem() != 0)
//                        //getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
//                }
                //setActionBarColor(getResources().getColor(R.color.toolbar_dark_background));
//                toolbar.setTitle(R.string.toolbar_menu_title);
//                int pageNum = mViewPager.getCurrentItem();
//                MenuItem expand = toolbar.getMenu().findItem(R.id.action_expand);
//                if (pageNum == 0 && expand != null) {
//                    expand.setVisible(false);
//                }
            }

        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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

    /**
     * Swaps fragments in the main content view
     */
    public void selectItem(int position) {
        Log.d("Agenda", "Position :" + position);
        if (position == 1)
            mViewPager.setCurrentItem(0);
        else if (position > 2 && position < 6)
            mViewPager.setCurrentItem(position - 2);
        else if (position > 6 && position < 9) {
            Snack.show(R.string.work_in_progress, this);
        } else if (position == 9) {
            launchActivity(new Intent(this, SettingsActivity.class));
        } else if (position == 10) {
            mViewPager.setCurrentItem(0);
            getTutorial().reset(this);
            getTutorial().show(AppTutorial.WELCOME, AppTutorial.HOME_INFO, this);
        }

        mDrawerLayout.closeDrawer(drawerView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkReminder(intent);
    }

    private void checkReminder(Intent intent) {
        Log.d(TAG, "CheckReminder" + intent.getDataString());
        final long remindRoutineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1l);
        final long delayRoutineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID, -1l);

        if (remindRoutineId != -1) {
            showReminder(remindRoutineId);
            getIntent().removeExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID);
        } else if (delayRoutineId != -1) {
            Log.d(TAG, "isDelay! ");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(0);
                    final Routine r = Routine.findById(delayRoutineId);
                    ((DailyAgendaFragment) getViewPagerFragment(0)).showDelayDialog(r);
                    ReminderNotification.cancel(HomeActivity.this);
                    getIntent().removeExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID);
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
            menu.findItem(R.id.action_expand).setIcon(
                    getResources().getDrawable(expanded ? R.drawable.ic_unfold_less_white_48dp : R.drawable.ic_unfold_more_white_48dp)
            );


        } else {
            menu.findItem(R.id.action_expand).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_expand:
                Log.d("Home", "ToogleExpand");
                ((DailyAgendaFragment) getViewPagerFragment(0)).toggleViewMode();
                boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();
                item.setIcon(
                        getResources().getDrawable(expanded ? R.drawable.ic_unfold_less_white_48dp : R.drawable.ic_unfold_more_white_48dp)
                );

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

        if (mViewPager.getCurrentItem() != 0)
            mViewPager.setCurrentItem(0);
        else if (!backProcesed) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        mViewPager.setCurrentItem(i);
        return true;
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
                mViewPager.setCurrentItem(0);
                ((DailyAgendaFragment) getViewPagerFragment(0)).showReminder(r);
            }
        }, 1000);

    }

    @Override
    public void onPageSelected(int page) {
        invalidateOptionsMenu();
        updateTitle(page);
        showTutorialStage(page);
        updateAddButton(page);
        if (page == 0) {
            hideAddButton();
            hideTabs();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
//            }

        } else {

            showAddButton();
            if (toolbar.getVisibility() != View.VISIBLE) {
                toolbar.setVisibility(View.VISIBLE);
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
//            }
            showTabs();
            setActionBarColor(getResources().getColor(R.color.android_blue_darker));

        }
    }

    private void updateAddButton(int page) {

        addButton.setColorNormalResId(getAddButtonColor(page));

    }

    private void updateTitle(int page) {
        String title = "";

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

    public void hideAddButton() {
        ((FloatingActionButton) (addButton)).hide(true);
    }

    public void showAddButton() {
        ((FloatingActionButton) (addButton)).show(true);
    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    public void setCustomTitle(String title) {
        setTitle(title);
//        ((TextView) findViewById(R.id.action_bar_custom_title)).setText(title);
    }

    public void enableToolbarTransparency() {
//        if (toolbarVisible) {
//            toolbarVisible = false;
//            Log.d("Home", "HideToolbar");
        setActionBarColor(getResources().getColor(R.color.transparent));
//        }

    }

    public void hideToolbar() {
        toolbar.setVisibility(View.GONE);

    }

    public void showToolbar() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
//        }
        toolbar.setVisibility(View.VISIBLE);
    }

    public void showTabs() {
        if (!(tabs.getVisibility() == View.VISIBLE)) {
            tabs.setVisibility(View.VISIBLE);
            tabsShadow.setVisibility(View.VISIBLE);
//            final Animation slide = AnimationUtils.loadAnimation(this, R.anim.anim_show_tabs);
//            slide.setDuration(ANIM_TABS_DURATION);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {

//                    tabs.startAnimation(slide);
//                }
//            }, ANIM_ACTION_BAR_DURATION);

        }

    }

    public void hideTabs() {
        if (!(tabs.getVisibility() == View.GONE)) {
            tabs.setVisibility(View.GONE);
            tabsShadow.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
            }
            setActionBarColor(getResources().getColor(R.color.transparent));

//            if (Build.VERSION.SDK_INT >= 11) {
//
//                Animation slide = AnimationUtils.loadAnimation(this, R.anim.anim_hide_tabs);
//                slide.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        tabs.setVisibility(View.GONE);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));
//                        }
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
//                setActionBarColor(getResources().getColor(R.color.transparent));
//                tabs.startAnimation(slide);
//            } else {
//                setActionBarColor(getResources().getColor(R.color.transparent));
//                tabs.setVisibility(View.GONE);
//
//            }
        }
    }

    private void setActionBarColor(final int color) {

//        if (Build.VERSION.SDK_INT >= 11) {
//            previousActionBarColor = currentActionBarColor;
//            final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(
//                    toolbar,
//                    "backgroundColor",
//                    new ArgbEvaluator(),
//                    currentActionBarColor,
//                    color);
//            backgroundColorAnimator.setDuration(ANIM_ACTION_BAR_DURATION);
//            backgroundColorAnimator.start();
//            backgroundColorAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    currentActionBarColor = color;
//                }
//            });
//        } else {
//            toolbar.setBackgroundColor(color);
//        }

    }

    // Method called from the event bus
    public void onEvent(PersistenceEvents.ModelCreateOrUpdateEvent event) {
//        if (event.clazz.equals(Routine.class)) {
//            ((RoutinesListFragment) getViewPagerFragment(1)).notifyDataChange();
//        } else if (event.clazz.equals(Medicine.class)) {
//            ((MedicinesListFragment) getViewPagerFragment(2)).notifyDataChange();
//        } else if (event.clazz.equals(Schedule.class)) {
//            ((ScheduleListFragment) getViewPagerFragment(3)).notifyDataChange();
//        }
        Log.d(TAG, "onEvent: " + event.clazz.getName());
        ((DailyAgendaFragment) getViewPagerFragment(0)).notifyDataChange();
        ((RoutinesListFragment) getViewPagerFragment(1)).notifyDataChange();
        ((MedicinesListFragment) getViewPagerFragment(2)).notifyDataChange();
        ((ScheduleListFragment) getViewPagerFragment(3)).notifyDataChange();
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

    public interface OnBackPressedListener {
        public boolean doBack();
    }

    class DrawerListAdapter extends ArrayAdapter<String> {

        public DrawerListAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position);

            final LayoutInflater layoutInflater = getLayoutInflater();

            if (item.equalsIgnoreCase(getString(R.string.drawer_top_option))) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                String displayName = prefs.getString("display_name", "Calendula");

                View v = layoutInflater.inflate(R.layout.drawer_top, null);
                ((TextView) v.findViewById(R.id.text)).setText(displayName);

                return v;
            }
            if (item.equalsIgnoreCase(getString(R.string.drawer_bottom_option))) {
                View v = layoutInflater.inflate(R.layout.drawer_bottom, null);
                TextView help = ((TextView) v.findViewById(R.id.text_help));
                help.setText(getString(R.string.drawer_help_option));
                help.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(10);
                    }
                });
                TextView settings = ((TextView) v.findViewById(R.id.text_settings));
                settings.setText(getString(R.string.drawer_settings_option));
                settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(9);
                    }
                });
                return v;
            }
            if (item.equalsIgnoreCase(getString(R.string.drawer_menu_option)) ||
                    item.equalsIgnoreCase(getString(R.string.drawer_services_option))) {
                View v = layoutInflater.inflate(R.layout.drawer_list_item_spacer, null);
                ((TextView) v.findViewById(R.id.text)).setText(item);
                v.setEnabled(false);
                return v;
            } else {
                View v = layoutInflater.inflate(R.layout.drawer_list_item, null);
                ((TextView) v.findViewById(R.id.text)).setText(item);
                ((ImageView) v.findViewById(R.id.imageView)).setImageResource(getActionDrawable(position));
                ((RoundedImageView) v.findViewById(R.id.imageViewbg)).setImageResource(getActionColor(position));
                ((RoundedImageView) v.findViewById(R.id.imageViewbg)).mutateBackground(true);

                if (position == 7 || position == 8) {
                    ((TextView) v.findViewById(R.id.text)).setTextColor(getResources().getColor(R.color.drawer_item_disabled));
                }

                return v;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return (position != 0 && position != 2 && position != 6);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }


}
