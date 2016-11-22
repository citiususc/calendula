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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import es.usc.citius.servando.calendula.fragments.AlertListFragment;
import es.usc.citius.servando.calendula.fragments.MedInfoFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class MedInfoPageAdapter extends FragmentPagerAdapter {

    Medicine m;

    public MedInfoPageAdapter(FragmentManager fm, Medicine m) {
        super(fm);
        this.m = m;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MedInfoFragment.newInstance(m);
            case 1:
                return AlertListFragment.newInstance(m);
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages. info and alerts
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
