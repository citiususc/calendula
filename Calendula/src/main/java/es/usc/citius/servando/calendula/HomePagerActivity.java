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

package es.usc.citius.servando.calendula;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.activities.CalendarActivity;
import es.usc.citius.servando.calendula.activities.ConfirmActivity;
import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MaterialIntroActivity;
import es.usc.citius.servando.calendula.activities.MedicineInfoActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SchedulesHelpActivity;
import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.adapters.HomePages;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.events.StockRunningOutEvent;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.HomeProfileMgr;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.medicine.StockUtils;
import es.usc.citius.servando.calendula.util.view.DisableableAppBarLayoutBehavior;

public class HomePagerActivity extends CalendulaActivity implements
        RoutinesListFragment.OnRoutineSelectedListener,
        MedicinesListFragment.OnMedicineSelectedListener,
        ScheduleListFragment.OnScheduleSelectedListener {

    public static final int REQ_CODE_EXTERNAL_STORAGE = 10;
    private static final String TAG = "HomePagerActivity";

    @MenuRes
    private static final int[] MENU_ITEMS = {
            R.id.action_sort, R.id.action_expand, R.id.action_calendar, R.id.action_schedules_help
    };


    @BindView(R.id.appbar)
    public AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.add_button)
    FloatingActionButton fab;
    @BindView(R.id.user_info_fragment)
    View userInfoFragment;
    @BindView(R.id.main_content)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.fab_menu)
    FloatingActionsMenu addButton;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.container)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout tabLayout;

    private boolean appBarLayoutExpanded = true;
    private boolean active = false;
    private Drawable icAgendaMore;
    private Drawable icAgendaLess;
    private FabMenuMgr fabMgr;
    private HomeProfileMgr homeProfileMgr;
    private HomePageAdapter mSectionsPagerAdapter;
    private LeftDrawerMgr drawerMgr;
    private Patient activePatient;
    private int pendingRefresh = -2;
    private Queue<Object> pendingEvents = new LinkedList<>();
    private Handler handler;

    private SparseArray<MenuItem> menuItems;

    @ColorInt
    private int previousColor = -1;

    public void showPagerItem(int position) {
        showPagerItem(position, true);
    }

    public void showPagerItem(int position, boolean updateDrawer) {
        if (position >= 0 && position < mViewPager.getChildCount()) {
            mViewPager.setCurrentItem(position);
            if (updateDrawer) {
                drawerMgr.onPagerPositionChange(position);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        menuItems = new SparseArray<>(menu.size());
        for(int menuItem : MENU_ITEMS){
            menuItems.put(menuItem, menu.findItem(menuItem));
        }
        menuItems.get(R.id.action_sort).setIcon(IconUtils.icon(getApplicationContext(), CommunityMaterial.Icon.cmd_sort, R.color.white));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        final int pageNum = mViewPager.getCurrentItem();
        final HomePages page = HomePages.getPage(pageNum);

        // hide all items first
        for (int menuItem : MENU_ITEMS) {
            menuItems.get(menuItem).setVisible(false);
        }

        // show items relevant to the current page
        switch (page) {
            case HOME:
                final boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).isExpanded();
                menuItems.get(R.id.action_expand).setVisible(true);
                menuItems.get(R.id.action_expand).setIcon(!expanded ? icAgendaMore : icAgendaLess);
                break;
            case MEDICINES:
                menuItems.get(R.id.action_sort).setVisible(true);
                break;
            case SCHEDULES:
                menuItems.get(R.id.action_schedules_help).setVisible(true);
                break;
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

                final boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).isExpanded();
                appBarLayout.setExpanded(expanded);


                boolean delay = appBarLayoutExpanded && !expanded || !appBarLayoutExpanded && expanded;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).toggleViewMode();
                    }
                }, delay ? 500 : 0);

                item.setIcon(expanded ? icAgendaMore : icAgendaLess);
                return true;
            case R.id.action_schedules_help:
                launchActivity(new Intent(this, SchedulesHelpActivity.class));
                return true;
            case R.id.action_sort:
                ((MedicinesListFragment) getViewPagerFragment(HomePages.MEDICINES)).toggleSort();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void launchActivityDelayed(final Class<?> activityClazz, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(HomePagerActivity.this, activityClazz));
                overridePendingTransition(0, 0);
            }
        }, delay);

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
        Intent i = new Intent(this, MedicineInfoActivity.class);
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

    // Method called from the event bus
    @Subscribe
    public void handleEvent(final Object event) {
        if (active) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (event instanceof PersistenceEvents.ModelCreateOrUpdateEvent) {
                        PersistenceEvents.ModelCreateOrUpdateEvent modelCreateOrUpdateEvent = (PersistenceEvents.ModelCreateOrUpdateEvent) event;
                        LogUtil.d(TAG, "handleEvent: " + modelCreateOrUpdateEvent.clazz.getName());
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).notifyDataChange();
                        ((RoutinesListFragment) getViewPagerFragment(HomePages.ROUTINES)).notifyDataChange();
                        ((MedicinesListFragment) getViewPagerFragment(HomePages.MEDICINES)).notifyDataChange();
                        ((ScheduleListFragment) getViewPagerFragment(HomePages.SCHEDULES)).notifyDataChange();
                    } else if (event instanceof PersistenceEvents.IntakeConfirmedEvent) {
                        // dismiss "take all" button, update checkboxes
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).notifyDataChange();
                        // stock info may need to be updated
                        ((MedicinesListFragment) getViewPagerFragment(HomePages.MEDICINES)).notifyDataChange();
                    } else if (event instanceof PersistenceEvents.ActiveUserChangeEvent) {
                        activePatient = ((PersistenceEvents.ActiveUserChangeEvent) event).patient;
                        updateTitle(mViewPager.getCurrentItem());
                        toolbarLayout.setContentScrimColor(activePatient.color());
                        fabMgr.onPatientUpdate(activePatient);
                    } else if (event instanceof PersistenceEvents.UserUpdateEvent) {
                        Patient p = ((PersistenceEvents.UserUpdateEvent) event).patient;
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).onUserUpdate();
                        drawerMgr.onPatientUpdated(p);
                        if (DB.patients().isActive(p, HomePagerActivity.this)) {
                            activePatient = p;
                            updateTitle(mViewPager.getCurrentItem());
                            toolbarLayout.setContentScrimColor(activePatient.color());
                            fabMgr.onPatientUpdate(activePatient);
                        }
                    } else if (event instanceof PersistenceEvents.UserCreateEvent) {
                        Patient created = ((PersistenceEvents.UserCreateEvent) event).patient;
                        drawerMgr.onPatientCreated(created);
                    } else if (event instanceof HomeProfileMgr.BackgroundUpdatedEvent) {
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).refresh();
                    } else if (event instanceof ConfirmActivity.ConfirmStateChangeEvent) {
                        pendingRefresh = ((ConfirmActivity.ConfirmStateChangeEvent) event).position;
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).refreshPosition(pendingRefresh);
                    } else if (event instanceof DailyAgenda.AgendaUpdatedEvent) {
                        ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).notifyDataChange();
                        homeProfileMgr.updateDate();
                    } else if (event instanceof StockRunningOutEvent) {
                        final StockRunningOutEvent sro = (StockRunningOutEvent) event;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                StockUtils.showStockRunningOutDialog(HomePagerActivity.this, sro.m, sro.days);
                            }
                        }, 1000);
                    }
                }
            });
        } else {
            pendingEvents.add(event);
        }
    }

    public void showSnackbar(@StringRes final int text) {
        Snack.show(text, coordinatorLayout, Snackbar.LENGTH_SHORT);
    }

    Fragment getViewPagerFragment(HomePages page) {
        String tag = FragmentUtils.makeViewPagerFragmentName(R.id.container, page.ordinal());
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupToolbar(null, Color.TRANSPARENT);
        initializeDrawer(savedInstanceState);
        setupStatusBar(Color.TRANSPARENT);
        subscribeToEvents();
        handler = new Handler();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new HomePageAdapter(getSupportFragmentManager(), this, this);

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(getPageChangeListener());
        mViewPager.setOffscreenPageLimit(5);

        // Set up home profile
        homeProfileMgr = new HomeProfileMgr();
        homeProfileMgr.init(userInfoFragment, this);

        activePatient = DB.patients().getActive(this);
        updateScrim(0);

        // Setup fab
        fabMgr = new FabMenuMgr(fab, addButton, drawerMgr, this);
        fabMgr.init();

        fabMgr.onPatientUpdate(activePatient);


        // Setup the tabLayout
        setupTabLayout();

        AppBarLayout.OnOffsetChangedListener mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                //LogUtil.d(TAG, "Values: (" + toolbarLayout.getHeight()+ " + " +verticalOffset + ") < (2 * " + ViewCompat.getMinimumHeight(toolbarLayout) + ")");

                if ((toolbarLayout.getHeight() + verticalOffset) < (1.8 * ViewCompat.getMinimumHeight(toolbarLayout))) {
                    homeProfileMgr.onCollapse();
                    toolbarTitle.animate().alpha(1);
                    appBarLayoutExpanded = false;
                    LogUtil.d(TAG, "OnCollapse");
                } else {
                    appBarLayoutExpanded = true;
                    if (mViewPager.getCurrentItem() == 0) {
                        toolbarTitle.animate().alpha(0);
                    }
                    homeProfileMgr.onExpand();
                    LogUtil.d(TAG, "OnExpand");
                }


            }
        };
        appBarLayout.addOnOffsetChangedListener(mListener);

        icAgendaLess = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_unfold_less)
                .color(Color.WHITE)
                .sizeDp(24);

        icAgendaMore = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_unfold_more)
                .color(Color.WHITE)
                .sizeDp(24);

        if (!PreferenceUtils.getBoolean(PreferenceKeys.HOME_INTRO_SHOWN, false)) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchActivity(new Intent(HomePagerActivity.this, MaterialIntroActivity.class));
                }
            }, 500);
        }

        if (getIntent() != null && getIntent().getBooleanExtra("invalid_notification_error", false)) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showInvalidNotificationError();
                }
            }, 500);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Patient p = DB.patients().getActive(this);
        drawerMgr.onActivityResume(p);
        active = true;

        // process pending events
        while (!pendingEvents.isEmpty()) {
            LogUtil.d(TAG, "Processing pending event...");
            handleEvent(pendingEvents.poll());
        }
    }


    // Interface implementations

    @Override
    protected void onPause() {
        active = false;
        super.onPause();
    }

    private void showInvalidNotificationError() {

        final boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).isExpanded();

        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_error_title)
                .setMessage(R.string.notification_error_msg)
                .setCancelable(true)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_bug, R.color.black))
                .setPositiveButton(R.string.tutorial_understood, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (!expanded) {
                            appBarLayout.setExpanded(expanded);
                            menuItems.get(R.id.action_expand).setIcon(expanded ? icAgendaMore : icAgendaLess);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).toggleViewMode();
                                }
                            }, 200);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).scrollTo(DateTime.now());
                                }
                            }, 600);
                        }
                    }
                }).create().show();
    }

    private void setupTabLayout() {

        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {

            Drawable icon = new IconicsDrawable(this)
                    .icon(HomePages.values()[i].icon)
                    .alpha(80)
                    .paddingDp(2)
                    .color(Color.WHITE)
                    .sizeDp(24);

            tabLayout.getTabAt(i).setIcon(icon);
        }
    }

    private ViewPager.OnPageChangeListener getPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTitle(position);
                updateScrim(position);
                fabMgr.onViewPagerItemChange(position);
                if (position == HomePages.HOME.ordinal() && !((DailyAgendaFragment) getViewPagerFragment(HomePages.HOME)).isExpanded()) {
                    appBarLayout.setExpanded(true);
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                    ((DisableableAppBarLayoutBehavior) layoutParams.getBehavior()).setEnabled(true);

                } else {
                    appBarLayout.setExpanded(false);
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                    ((DisableableAppBarLayoutBehavior) layoutParams.getBehavior()).setEnabled(false);
                }

                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

    private void updateScrim(int position) {
        @ColorInt final int color = (position == HomePages.HOME.ordinal()) ? ContextCompat.getColor(this,R.color.transparent_black) : activePatient.color();

        if (previousColor != -1) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), previousColor, color);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    toolbarLayout.setContentScrimColor((int) animator.getAnimatedValue());
                }

            });
            colorAnimation.start();
        } else {
            toolbarLayout.setContentScrimColor(color);
        }
        previousColor = color;
    }

    private void updateTitle(int page) {
        String title;
        if (page == HomePages.HOME.ordinal()) {
            title = getString(R.string.app_name);
        } else {
            title = getString(R.string.relation_user_possession_thing, activePatient.name(), getString(HomePages.values()[page].title));
        }

        toolbarTitle.setText(title);
    }

    private void initializeDrawer(Bundle savedInstanceState) {
        drawerMgr = new LeftDrawerMgr(this, toolbar);
        drawerMgr.init(savedInstanceState);
    }

    private void launchActivity(Intent i) {
        startActivity(i);
        this.overridePendingTransition(0, 0);
    }

    /*
    public void askForWEEPermissionsIfNeeded() {
        String p = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if(!PermissionUtils.hasAskedForPermission(this, p) && PermissionUtils.shouldAskForPermission(this, p)){
            PermissionUtils.requestPermissions(this, new String[]{p}, REQ_CODE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_EXTERNAL_STORAGE: {
                PermissionUtils.markedPermissionAsAsked(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomePagerActivity.this, "Granted   !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomePagerActivity.this, "Refused   !", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

    }
    */

}
