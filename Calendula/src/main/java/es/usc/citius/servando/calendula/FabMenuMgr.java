/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

package es.usc.citius.servando.calendula;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;
import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScanActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SchedulesHelpActivity;
import es.usc.citius.servando.calendula.adapters.HomePages;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.fragments.ScheduleTypeFragment;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.view.ExpandableFAB;

/**
 * Helper to manage the home screen floating action button behaviour
 */
public class FabMenuMgr implements View.OnClickListener {


    private LeftDrawerMgr drawerMgr;
    private ExpandableFAB fabMenu;
    private HomePagerActivity activity;
    private List<View> fabMenuSubViews;
    private List<FloatingActionButton> fabMenuButtons;

    private int currentPage = 0;


    public FabMenuMgr(ExpandableFAB fab, LeftDrawerMgr drawerMgr, HomePagerActivity a) {
        this.fabMenu = fab;
        this.activity = a;
        this.drawerMgr = drawerMgr;
    }

    public void init() {
        fabMenu.setOnClickListener(this);
        fabMenu.setImageDrawable(new IconicsDrawable(activity)
                .icon(GoogleMaterial.Icon.gmd_plus)
                .paddingDp(5)
                .sizeDp(24)
                .colorRes(R.color.fab_default_icon_color));
        fabMenu.setSubViews(getScheduleActions());
        onViewPagerItemChange(0);
    }

    public void onViewPagerItemChange(int currentPage) {

        this.currentPage = currentPage;
        HomePages page = HomePages.values()[currentPage];

        switch (page) {
            case HOME:
                fabMenu.hide();
                break;
            case ROUTINES:
            case MEDICINES:
            case SCHEDULES:
                fabMenu.collapse();
                fabMenu.show();
                break;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.add_button:
                onClickAdd();
                break;

            // schedules
            case R.id.fab_action_routines_button:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_ROUTINES);
                break;
            case R.id.fab_action_interval_button:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_HOURLY);
                break;
            case R.id.fab_action_period_button:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_PERIOD);
                break;
            case R.id.fab_action_qr_button:
                startScanActivity();
                break;

        }


    }

    public void onPatientUpdate(Patient p) {
        for (FloatingActionButton fabMenuButton : fabMenuButtons) {
            fabMenuButton.setBackgroundTintList(ColorStateList.valueOf(p.getColor()));
        }
    }

    public void onPharmacyModeChanged(boolean enabled) {
        fabMenu.setSubViews(getScheduleActions());
        onViewPagerItemChange(currentPage);
    }

    private List<View> getScheduleActions() {
        fabMenuSubViews = new ArrayList<>();
        fabMenuButtons = new ArrayList<>();

        if (CalendulaApp.isPharmaModeEnabled()) {
            final View fabActionQrView = activity.findViewById(R.id.fab_action_qr);
            fabMenuSubViews.add(fabActionQrView);
            final FloatingActionButton fabActionQr = (FloatingActionButton) activity.findViewById(R.id.fab_action_qr_button);
            fabActionQr.setOnClickListener(this);
            fabActionQr.setBackgroundTintList(ColorStateList.valueOf(DB.patients().getActive(activity).getColor()));
            fabMenuButtons.add(fabActionQr);
        }

        final View fabActionIntervalView = activity.findViewById(R.id.fab_action_interval);
        final View fabActionRoutinesView = activity.findViewById(R.id.fab_action_routines);
        final View fabActionPeriodView = activity.findViewById(R.id.fab_action_period);

        fabMenuSubViews.add(fabActionPeriodView);
        fabMenuSubViews.add(fabActionIntervalView);
        fabMenuSubViews.add(fabActionRoutinesView);

        final FloatingActionButton fabActionInterval = (FloatingActionButton) activity.findViewById(R.id.fab_action_interval_button);
        final FloatingActionButton fabActionRoutines = (FloatingActionButton) activity.findViewById(R.id.fab_action_routines_button);
        final FloatingActionButton fabActionPeriod = (FloatingActionButton) activity.findViewById(R.id.fab_action_period_button);

        fabActionInterval.setOnClickListener(this);
        fabActionRoutines.setOnClickListener(this);
        fabActionPeriod.setOnClickListener(this);

        fabActionInterval.setBackgroundTintList(ColorStateList.valueOf(DB.patients().getActive(activity).getColor()));
        fabActionRoutines.setBackgroundTintList(ColorStateList.valueOf(DB.patients().getActive(activity).getColor()));
        fabActionPeriod.setBackgroundTintList(ColorStateList.valueOf(DB.patients().getActive(activity).getColor()));

        fabMenuButtons.add(fabActionPeriod);
        fabMenuButtons.add(fabActionInterval);
        fabMenuButtons.add(fabActionRoutines);


        return fabMenuSubViews;
    }

    private void onClickAdd() {
        HomePages page = HomePages.values()[currentPage];
        switch (page) {
            case HOME:
                return;
            case ROUTINES:
                launchActivity(RoutinesActivity.class);
                break;
            case MEDICINES:
                launchActivity(MedicinesActivity.class);
                break;
            case SCHEDULES:
                if (!PreferenceUtils.getBoolean(PreferenceKeys.SCHEDULES_HELP_SHOWN, false)) {
                    activity.launchActivityDelayed(SchedulesHelpActivity.class, 600);
                }
                fabMenu.toggle();
                break;
        }
    }

    private void launchActivity(Class<?> type) {
        activity.startActivity(new Intent(activity, type));
        activity.overridePendingTransition(0, 0);
    }

    private void startSchedulesActivity(int scheduleType) {
        Intent i = new Intent(activity, ScheduleCreationActivity.class);
        i.putExtra("scheduleType", scheduleType);
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        fabMenu.collapse();
    }

    private void startScanActivity() {
        Intent i = new Intent(activity, ScanActivity.class);
        i.putExtra("after_scan_pkg", activity.getPackageName());
        i.putExtra("after_scan_cls", ConfirmSchedulesActivity.class.getName());
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        fabMenu.collapse();
    }

}
