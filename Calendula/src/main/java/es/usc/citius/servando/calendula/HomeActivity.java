package es.usc.citius.servando.calendula;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.PointTarget;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.activities.AlertFullScreenActivity;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Screen;


public class HomeActivity extends ActionBarActivity implements
        ViewPager.OnPageChangeListener,
        ActionBar.OnNavigationListener,
        View.OnClickListener,
        RoutinesListFragment.OnRoutineSelectedListener {

    public static final int ROUTINES_ACTIVITY_RQ = 1;
    public static final int SCHEDULES_ACTIVITY_RQ = 2;
    public static final int MEDICINES_ACTIVITY_RQ = 3;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    HomePageAdapter mSectionsPagerAdapter;
    ActionBar mActionBar;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
//    ImageView actionBarImage;

    View addButton;
    boolean addButtonShown = true;
    //boolean profileShown = true;
    String[] titles;



    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    ShowcaseView sv;
    boolean showcaseShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
        }
        // create our manager instance after the content view is set
//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
//        tintManager.setStatusBarTintEnabled(true);
//        tintManager.setTintColor(Color.parseColor("#0099CC"));
        // enable navigation bar tint
        //tintManager.setNavigationBarTintEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new HomePageAdapter(getSupportFragmentManager(), this, this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mActionBar = getSupportActionBar();
        //mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.action_bar);
        //mActionBar.hide();
        initializeDrawer();

        titles = getResources().getStringArray(R.array.home_action_list);

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.home_action_list,
                android.R.layout.simple_spinner_dropdown_item);

        mActionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
        mViewPager.setOnPageChangeListener(this);


//        actionBarImage=(ImageView)findViewById(R.id.action_bar_profile_image);
//        actionBarImage.setImageBitmap(Session.instance().getUserProfileImage(this));
//        actionBarImage.setVisibility(View.INVISIBLE);
//        actionBarImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((DailyAgendaFragment)getViewPagerFragment(0)).editProfile();
//            }
//        });
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

//        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
//        mViewPager.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(), config.getPixelInsetBottom());
//        mDrawerList.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(), config.getPixelInsetBottom());

        boolean welcome = getIntent().getBooleanExtra("welcome",false);
        if(welcome) {
            Toast.makeText(getBaseContext(), "Welcome to calendula!", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public int getActionDrawable(int index){
        switch (index){
            case 0:
                return R.drawable.ic_small_home_w;
            case 2:
                return R.drawable.ic_small_alarm;
            case 4:
                return R.drawable.ic_pill;
            case 3:
                return R.drawable.ic_small_calendar_w;
            case 6:
                return R.drawable.ic_small_pin_w;
            case 7:
                return R.drawable.ic_small_plane_w;
            default:
                return R.drawable.ic_small_home_w;

        }
    }

    public int getActionColor(int index){
        switch (index){
            case 2:
                return R.color.android_blue;
            case 3:
                return R.color.android_pink;
            case 4:
                return R.color.android_green;
            case 6:
                return R.color.android_orange;
            case 7:
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
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //   getActionBar().setTitle(t);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // getActionBar().setTitle(mDrawerTitle);
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


    class DrawerListAdapter extends ArrayAdapter<String> {

        public DrawerListAdapter(Context context, int resource, List<String> items) {
            super(context, resource,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position).toUpperCase();

            final LayoutInflater layoutInflater = getLayoutInflater();

            if (item.equalsIgnoreCase(getString(R.string.drawer_menu_option)) ||
                    item.equalsIgnoreCase(getString(R.string.drawer_services_option))) {
                View v = layoutInflater.inflate(R.layout.drawer_list_item_spacer, null);
                ((TextView) v.findViewById(R.id.text)).setText(item);
                return v;
            } else {
                View v = layoutInflater.inflate(R.layout.drawer_list_item, null);
                ((TextView) v.findViewById(R.id.text)).setText(item);
                ((ImageView) v.findViewById(R.id.imageView)).setImageResource(getActionDrawable(position));
                ((ImageView) v.findViewById(R.id.imageViewbg)).setImageResource(getActionColor(position));
                return v;
            }
        }
    }



    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.add_button) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Home", "onActivityResult: " + requestCode + ", " + resultCode + ", " + data.toString());
        switch (requestCode) {
            case ROUTINES_ACTIVITY_RQ:
                ((RoutinesListFragment) getViewPagerFragment(1)).notifyDataChange();
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
        else if (position > 1 && position < 5)
            mViewPager.setCurrentItem(position - 1);
        else
            launchActivity(new Intent(this, AlertFullScreenActivity.class));
        //Toast.makeText(this,"Working on it!",Toast.LENGTH_SHORT).show();

        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    protected void onResume() {
        super.onResume();


    }




    private void showShowCase() {
        if (!showcaseShown) {
            ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.hideOnClickOutside = true;
            PointTarget pt = new PointTarget((int) Screen.getDpSize(this).x * 2, (int) Screen.getDpSize(this).y);
            sv = ShowcaseView.insertShowcaseView(pt, HomeActivity.this, "Daily agenda", "Swipe left to se the full agenda", co);
            sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                @Override
                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    Log.d("SV", "Hide showcase at home");
                    sv = null;
                }

                @Override
                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    Log.d("SV", "DidHide showcase at home");
                }

                @Override
                public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    Log.d("SV", "Show showcase at home");
                    showcaseShown = true;
                }
            });
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
            menu.findItem(R.id.action_expand).setVisible(true);

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
                ((DailyAgendaFragment) getViewPagerFragment(0)).toggleViewMode();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    void logout(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Session will be closed, continue?")
                .setPositiveButton("Yes, close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        Session.instance().close(getApplicationContext());
                        finish();
                    }
                })
                .setNegativeButton("No, cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                }).show();

    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() != 0)
            mViewPager.setCurrentItem(0);
        else {
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
        Log.d("Home", position + ", " + positionOffset + ", " + positionOffsetPixels);


    }

    @Override
    public void onPageSelected(int i) {

        invalidateOptionsMenu();
        if (i == 0) {
            hideAddButton();
        } else {
            setCustomTitle(titles[i]);
        }

        if(i > 0){
            showAddButton();
//            actionBarImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public void hideAddButton(boolean showProfile) {
//        this.profileShown = showProfile;
        hideAddButton();
    }

    public void showAddButton(boolean showProfile) {
//        this.profileShown = showProfile;
        showAddButton();
    }

    public void hideAddButton() {
        if (addButtonShown) {
            addButtonShown = false;
            Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down_2);
            slideDown.setFillAfter(true);
            addButton.startAnimation(slideDown);

            slideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setCustomTitle(Session.instance().getUser().getName());
//                    actionBarImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


        }
    }

    public void showAddButton() {

        if (!addButtonShown) {
            addButtonShown = true;
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up_2);
            slideUp.setFillAfter(true);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    addButton.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            addButton.startAnimation(slideUp);
        }
    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    public void setCustomTitle(String title) {
        ((TextView) findViewById(R.id.action_bar_custom_title)).setText(title);
    }



}
