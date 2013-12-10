package es.usc.citius.servando.calendula;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.PointTarget;

import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.store.MedicineStore;
import es.usc.citius.servando.calendula.store.RoutineStore;
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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    ShowcaseView sv;

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

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.home_action_list,
                android.R.layout.simple_spinner_dropdown_item);

        mActionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
        mViewPager.setOnPageChangeListener(this);

        if (MedicineStore.getInstance().size() == 0) {
            DummyDataGenerator.fillMedicineStore();
        }
        if (RoutineStore.getInstance().size() == 0) {
            DummyDataGenerator.fillRoutineStore();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sv == null) {
            ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.hideOnClickOutside = true;
            co.centerText = true;
            co.shotType = ShowcaseView.TYPE_ONE_SHOT; // display only first time
            // ViewTarget target = new ViewTarget(R.id.pager, this);
            //sv = ShowcaseView.insertShowcaseView(target, this, "Daily agenda", "Swipe left to se the full agenda", co);

            Log.d("TAG", mViewPager.getLeft() + ", " + (Screen.getDpSize(this).y / 2));

            PointTarget pt = new PointTarget((int) Screen.getDpSize(this).x * 2, (int) Screen.getDpSize(this).y);
            sv = ShowcaseView.insertShowcaseView(pt, this, "Daily agenda", "Swipe left to se the full agenda", co);
            sv.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                @Override
                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                    Log.d("SV", "Hide");
                }

                @Override
                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                    Log.d("SV", "DIVHide");
                }

                @Override
                public void onShowcaseViewShow(ShowcaseView showcaseView) {
                    Log.d("SV", "Show");
                }
            });
            sv.show();
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
        }
        return super.onOptionsItemSelected(item);
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
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mActionBar.setSelectedNavigationItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
