package es.usc.citius.servando.calendula.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.util.Screen;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class HomePageAdapter extends FragmentPagerAdapter {

    private float dpWidth;
    String[] titles;

    public HomePageAdapter(FragmentManager fm, Context ctx, Activity activity) {
        super(fm);
        // obtain the window width in dp to use later
        dpWidth = Screen.getDpSize(activity).x;

        titles = new String[]{
                ctx.getString(R.string.title_home),
                ctx.getString(R.string.title_activity_routines),
                ctx.getString(R.string.title_activity_medicines),
                ctx.getString(R.string.title_activity_schedules)
        };
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DailyAgendaFragment();
            case 1:
                return new RoutinesListFragment();
            case 2:
                return new MedicinesListFragment();
            case 3:
                return new ScheduleListFragment();
        }
        return null;

        //return position == 1 ? new DailyAgendaFragment() : new HomeFragment();
    }

    @Override
    public float getPageWidth(int position) {

        // show 80dp of the agenda view
        int dipsToShow = 80;
        // get the percentage of the screen width that represents that 80dp
        // according to the current device
        float percent = 1.0f - (dipsToShow / dpWidth);
        // page width will be only reduced for home page to allow showing a
        // fragment of the agenda view
        return 1.0f;
        //return position == 0 ? percent : 1.0f;
    }

    @Override
    public int getCount() {
        // Show 2 total pages. Home and agenda
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
