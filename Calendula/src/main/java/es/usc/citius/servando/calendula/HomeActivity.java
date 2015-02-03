package es.usc.citius.servando.calendula;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.melnykov.fab.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.activities.MedicinesActivity;
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
import es.usc.citius.servando.calendula.util.FragmentUtils;

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


    private static final String TAG = "HomeActivity";
    
    public static final int ANIM_ACTION_BAR_DURATION = 250;
    public static final int ANIM_TABS_DURATION = 250;

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
    ActionBarDrawerToggle mDrawerToggle;
    int currentActionBarColor;
    int previousActionBarColor;

    View addButton;
    boolean addButtonShown = true;
    //boolean profileShown = true;
    String[] titles;

    Toolbar toolbar;
    PagerSlidingTabStrip tabs;
    Handler mHandler;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    //    ShowcaseView sv;
    boolean showcaseShown = false;
    private boolean toolbarVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);


        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(this);
        tabs.setShouldExpand(true);
        tabs.setAllCaps(false);
        tabs.setTabPaddingLeftRight(30);
        tabs.setShouldExpand(true);
        tabs.setDividerColor(getResources().getColor(R.color.white_50));
        tabs.setDividerColor(getResources().getColor(R.color.transparent));
        tabs.setIndicatorHeight(10);
        tabs.setIndicatorColor(getResources().getColor(R.color.white));
        tabs.setTextColor(getResources().getColor(R.color.white_80));
        tabs.setUnderlineColor(getResources().getColor(R.color.android_blue_darker));
        tabs.setBackgroundColor(getResources().getColor(R.color.android_blue_darker));
        tabs.setVisibility(View.GONE);
        // set up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_launcher_white);
        // configure toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        // initialize left drawer
        initializeDrawer();

        titles = getResources().getStringArray(R.array.home_action_list);
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

        hideAddButton();

        boolean welcome = getIntent().getBooleanExtra("welcome", false);
        if (welcome) {
            showShowCase();
        }

        CalendulaApp.eventBus().register(this);

    }

    public int getActionDrawable(int index) {
        switch (index) {
            case 0:
                return R.drawable.ic_small_home_w;
            case 1:
                return R.drawable.ic_settings_white;
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
                return R.color.android_orange;
            case 8:
                return R.color.android_red;
            default:
                return R.color.dark_grey_home;

        }
    }


    private void initializeDrawer() {


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

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
                if (mViewPager.getCurrentItem() == 0 && !toolbarVisible)
                    setActionBarColor(getResources().getColor(R.color.transparent));
                updateTitle(mViewPager.getCurrentItem());
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (mViewPager.getCurrentItem() == 0 && !toolbarVisible)
                    setActionBarColor(getResources().getColor(R.color.toolbar_dark_background));
                toolbar.setTitle(R.string.toolbar_menu_title);
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

    class DrawerListAdapter extends ArrayAdapter<String> {

        public DrawerListAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position).toUpperCase();

            final LayoutInflater layoutInflater = getLayoutInflater();

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
                ((ImageView) v.findViewById(R.id.imageViewbg)).setImageResource(getActionColor(position));

                if (position == 8 || position == 9)
                    v.setEnabled(false);

                return v;
            }
        }
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        Log.d("Agenda", "Position :" + position);
        if (position == 0)
            mViewPager.setCurrentItem(0);
        else if (position == 1)
            launchActivity(new Intent(this, SettingsActivity.class));
        else if (position > 2 && position < 6)
            mViewPager.setCurrentItem(position - 2);
        else if (position > 6 && position < 9) {
            Toast.makeText(this, getString(R.string.work_in_progress), Toast.LENGTH_SHORT).show();
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        long remindRoutineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1l);
        if (remindRoutineId != -1) {
            showReminder(remindRoutineId);
            Log.d("Home", (mViewPager == null) + ", " + mViewPager.getCurrentItem() + ", " + mViewPager.getAdapter().getCount());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        long remindRoutineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1l);
        if (remindRoutineId != -1) {
            showReminder(remindRoutineId);
            Log.d("Home", (mViewPager == null) + ", " + mViewPager.getCurrentItem() + ", " + mViewPager.getAdapter().getCount());
        }
    }

    @Override
    protected void onDestroy() {
        CalendulaApp.eventBus().unregister(this);
        super.onDestroy();
    }

    private void showShowCase() {
//        if (!showcaseShown) {
//            ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//            co.hideOnClickOutside = true;
//            PointTarget pt = new PointTarget((int) Screen.getDpSize(this).x * 2, (int) Screen.getDpSize(this).y);
//            sv = ShowcaseView.insertShowcaseView(pt, HomeActivity.this, "Daily agenda", "Swipe left to se the full agenda", co);
//            sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//                @Override
//                public void onShowcaseViewHide(ShowcaseView showcaseView) {
//                    Log.d("SV", "Hide showcase at home");
//                    sv = null;
//                }
//
//                @Override
//                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
//                    Log.d("SV", "DidHide showcase at home");
//                }
//
//                @Override
//                public void onShowcaseViewShow(ShowcaseView showcaseView) {
//                    Log.d("SV", "Show showcase at home");
//                    showcaseShown = true;
//                }
//            });

//        ShowcaseView sv = new ShowcaseView.Builder(this)
//                    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.HOME))
//                    .setContentTitle("Welcome to Calendula!")
//                    .setContentText("See the menu to ...")
//                    .hideOnTouchOutside()
//                    .build();
//
//        sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
//            @Override
//            public void onShowcaseViewHide(ShowcaseView showcaseView) {
//                new ShowcaseView.Builder(HomeActivity.this)
//                        .setTarget(new PointTarget(
//                                (int) Screen.getDpSize(HomeActivity.this).x * 2,
//                                (int) Screen.getDpSize(HomeActivity.this).y))
//                        .setContentTitle("Discover")
//                        .doNotBlockTouches()
//                        .setContentText("Swipe left to see...")
//                        .hideOnTouchOutside()
//                        .build().show();
//            }
//
//            @Override
//            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
//
//            }
//
//            @Override
//            public void onShowcaseViewShow(ShowcaseView showcaseView) {
//
//            }
//        });
//
//        sv.show();


//        }
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

//    void logout() {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        builder.setTitle("Session will be closed, continue?")
//                .setPositiveButton("Yes, close", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int i) {
//                        dialog.dismiss();
//                        Session.instance().close(getApplicationContext());
//                        finish();
//                    }
//                })
//                .setNegativeButton("No, cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int i) {
//                        dialog.dismiss();
//                    }
//                }).show();
//
//    }

    @Override
    public void onBackPressed() {

        boolean backProcesed = false;

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
        //Log.d("Home", position + ", " + positionOffset + ", " + positionOffsetPixels);
    }

    void showReminder(final Long routineId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Routine r = Routine.findById(routineId);
                //Toast.makeText(HomeActivity.this, "Take your meds (" + r.name() + ")", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(0);
                ((DailyAgendaFragment) getViewPagerFragment(0)).showReminder(r);
            }
        }, 1000);

    }

    @Override
    public void onPageSelected(int page) {
        invalidateOptionsMenu();
        updateTitle(page);
        if (page == 0) {
            hideAddButton();
            hideTabs();
        } else {
            showAddButton();
            if (toolbar.getVisibility() != View.VISIBLE) {
                toolbar.setVisibility(View.VISIBLE);
            }
            showTabs();
            setActionBarColor(getResources().getColor(R.color.toolbar_dark_background));

        }

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


    public interface OnBackPressedListener {
        public boolean doBack();
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
        toolbar.setVisibility(View.VISIBLE);
    }


    public void showTabs() {
        if (!(tabs.getVisibility() == View.VISIBLE)) {
            final Animation slide = AnimationUtils.loadAnimation(this, R.anim.anim_show_tabs);
            slide.setDuration(ANIM_TABS_DURATION);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tabs.setVisibility(View.VISIBLE);
                    tabs.startAnimation(slide);
                }
            }, ANIM_ACTION_BAR_DURATION);

        }

    }

    public void hideTabs() {
        if (!(tabs.getVisibility() == View.GONE)) {

            if (Build.VERSION.SDK_INT >= 11) {

                Animation slide = AnimationUtils.loadAnimation(this, R.anim.anim_hide_tabs);
                slide.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        tabs.setVisibility(View.GONE);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                setActionBarColor(getResources().getColor(R.color.transparent));
                tabs.startAnimation(slide);
            } else {
                setActionBarColor(getResources().getColor(R.color.transparent));
                tabs.setVisibility(View.GONE);

            }
        }
    }


    private void setActionBarColor(final int color) {

        if (Build.VERSION.SDK_INT >= 11) {
            previousActionBarColor = currentActionBarColor;
            final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(
                    toolbar,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    currentActionBarColor,
                    color);
            backgroundColorAnimator.setDuration(ANIM_ACTION_BAR_DURATION);
            backgroundColorAnimator.start();
            backgroundColorAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    currentActionBarColor = color;
                }
            });
        } else {
            toolbar.setBackgroundColor(color);
        }

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

}
