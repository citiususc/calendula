package es.usc.citius.servando.calendula;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.PointTarget;

import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.fragments.EditUserProfileFragment;
import es.usc.citius.servando.calendula.fragments.HomeFragment;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.user.User;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Screen;

public class HomeActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener, ActionBar.OnNavigationListener {

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

    ImageView profileImageView;
    View profileImageContainer;
    TextView profileUsername;
    RelativeLayout profileContainer;





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

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new HomePageAdapter(getSupportFragmentManager(), this, this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mActionBar = getSupportActionBar();
        //mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        //mActionBar.hide();
        initializeDrawer();

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.home_action_list,
                android.R.layout.simple_spinner_dropdown_item);

        mActionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
        mViewPager.setOnPageChangeListener(this);
        profileContainer = (RelativeLayout) findViewById(R.id.profile_container);
        profileImageContainer = findViewById(R.id.profile_image_container);
        profileImageView = (ImageView) findViewById(R.id.profile_image);
        profileUsername = (TextView) findViewById(R.id.profile_username);
        updateProfileInfo();
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });


        boolean welcome = getIntent().getBooleanExtra("welcome",false);
        if(welcome) {
            Toast.makeText(getBaseContext(), "Welcome to calendula!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeDrawer() {

        String items[] = {"Action 1", "Action 2", "Action 3"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, items));
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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


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


        mDrawerLayout.closeDrawer(mDrawerList);
    }


    @Override
    protected void onResume() {
        super.onResume();
//       showShowCase();
    }


    void updateProfileInfo() {

        User u = Session.instance().getUser();
        profileUsername.setText(u.getName());
        Bitmap profileImage = Session.instance().getUserProfileImage(this);
        if (profileImage != null) {
            profileImageView.setImageBitmap(profileImage);
        }

    }

    void showEditProfileDialog() {
        FragmentManager fm = getSupportFragmentManager();
        final EditUserProfileFragment editUserProfileFragment = new EditUserProfileFragment();
        editUserProfileFragment.setOnProfileEditListener(new EditUserProfileFragment.OnProfileEditListener() {
            @Override
            public void onProfileEdited(User u) {
                editUserProfileFragment.dismiss();
                updateProfileInfo();
            }
        });
        editUserProfileFragment.show(fm, "fragment_edit_profile");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_exit:
                logout();
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
        HomeFragment fragment = (HomeFragment) getViewPagerFragment(0);
        fragment.onScroll(positionOffset, positionOffsetPixels);

    }

    @Override
    public void onPageSelected(int i) {
        mActionBar.setSelectedNavigationItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    private boolean profileShown = true;

    public void hideProfile() {
        if (profileShown) {
            profileShown = false;
            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_slide_up);
            slide.setFillAfter(true);
            profileContainer.startAnimation(slide);
        }
    }

    public void showProfile() {
        if (!profileShown) {
            profileShown = true;
            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_slide_up);
            slide.setFillAfter(true);
            profileContainer.startAnimation(slide);
        }
    }

}
