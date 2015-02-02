package es.usc.citius.servando.calendula.activities;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.RoutineCreateOrEditFragment;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;

public class RoutinesActivity extends ActionBarActivity implements RoutineCreateOrEditFragment.OnRoutineEditListener {

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
    MenuItem removeItem;

    long mRoutineId;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);
        processIntent();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.title_activity_routines));
        toolbar.setSubtitle(getString(mRoutineId != -1 ? R.string.title_edit_routine_activity : R.string.create_routine_button_text));
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_alarm_white_48dp), 15, 15, 15, 15));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    private void processIntent() {
        mRoutineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.telt
        getMenuInflater().inflate(R.menu.routines, menu);
        removeItem = menu.findItem(R.id.action_remove);
        removeItem.setVisible(mRoutineId != -1 ? true : false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                ((RoutineCreateOrEditFragment) getViewPagerFragment(0)).showDeleteConfirmationDialog(Routine.findById(mRoutineId));
                return true;
            case R.id.action_done:
                ((RoutineCreateOrEditFragment) getViewPagerFragment(0)).onEdit();
                return true;
            default:
                finish();
                return true;
        }
        //return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoutineEdited(Routine r) {
        AlarmScheduler.instance().onCreateOrUpdateRoutine(r, this);
        Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRoutineDeleted(Routine r) {
        AlarmScheduler.instance().onDeleteRoutine(r, this);
        r.deleteCascade();
        Toast.makeText(this, "Routine deleted!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRoutineCreated(Routine r) {
        AlarmScheduler.instance().onCreateOrUpdateRoutine(r, this);
        Toast.makeText(this, "Routine created!", Toast.LENGTH_SHORT).show();
        // send result to caller activity
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("Routines", "Create fragment: " + mRoutineId);
            Fragment f = new RoutineCreateOrEditFragment();
            Bundle args = new Bundle();
            args.putLong(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, mRoutineId);
            f.setArguments(args);
            return f;
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.home_menu_routines);

        }
    }


    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

}
