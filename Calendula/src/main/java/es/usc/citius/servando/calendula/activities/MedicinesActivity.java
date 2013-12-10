package es.usc.citius.servando.calendula.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.store.MedicineStore;
import es.usc.citius.servando.calendula.util.FragmentUtils;

public class MedicinesActivity extends ActionBarActivity implements MedicinesListFragment.OnMedicineSelectedListener, MedicineCreateOrEditFragment.OnMedicineEditListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
//    RoutinesListFragment listFragment;
//    RoutineCreateOrEditFragment editFragment;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.medicines, menu);
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
    public void onMedicineSelected(Medicine m) {
        mViewPager.setCurrentItem(1);
        ((MedicineCreateOrEditFragment) getViewPagerFragment(1)).setMedicne(m);
        setTitle(R.string.title_edit_medicine_activity);
    }

    @Override
    public void onCreateMedicine() {
        mViewPager.setCurrentItem(1);
        ((MedicineCreateOrEditFragment) getViewPagerFragment(1)).clear();
        setTitle(R.string.title_create_medicine_activity);
    }

    @Override
    public void onMedicineEdited(Medicine r) {
        Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(0);
        ((MedicinesListFragment) getViewPagerFragment(0)).notifyDataChange();
        setTitle(R.string.title_activity_medicines);
    }

    @Override
    public void onMedicineCreated(Medicine m) {
        MedicineStore.getInstance().addMedicine(m);
        Toast.makeText(this, "Medicine created!", Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(0);
        ((MedicinesListFragment) getViewPagerFragment(0)).notifyDataChange();
        setTitle(R.string.title_activity_medicines);
    }


    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return new MedicinesListFragment();
            } else {
                return new MedicineCreateOrEditFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0);
            setTitle(R.string.title_activity_medicines);
        } else {
            super.onBackPressed();
        }
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

}
