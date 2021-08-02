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

package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.HomePagerActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.HomePages;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.AllergiesModule;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.settings.CalendulaSettingsActivity;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ScreenUtils;

public class LeftDrawerMgr implements Drawer.OnDrawerItemClickListener, AccountHeader.OnAccountHeaderListener {

    public static final int HOME = 0;
    public static final int ROUTINES = 1;
    public static final int MEDICINES = 2;
    public static final int SCHEDULES = 3;

    public static final int PATIENTS = 4;
    public static final int HELP = 5;
    public static final int SETTINGS = 6;
    public static final int TRAVELPLAN = 8;
    public static final int PHARMACIES = 9;
    public static final int ABOUT = 10;

    public static final int PATIENT_ADD_PROFILE_ID = 15;
    public static final int CALENDAR = 12;

    public static final int ALLERGIES = 11;

    private static final String TAG = "LeftDrawerMgr";


    private AccountHeader headerResult = null;
    private Drawer drawer = null;
    private Toolbar toolbar;
    private HomePagerActivity homeActivity;
    private Patient currentPatient;

    public LeftDrawerMgr(HomePagerActivity activity, Toolbar toolbar) {
        this.toolbar = toolbar;
        this.homeActivity = activity;
    }

    public void init(Bundle savedInstanceState) {


        headerResult = new AccountHeaderBuilder()
                .withActivity(homeActivity)
                .withHeaderBackground(R.drawable.drawer_header)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withCompactStyle(false)
                .withProfiles(genProfiles())
                .withAlternativeProfileHeaderSwitching(true)
                .withThreeSmallProfileImages(true)
                .withOnAccountHeaderListener(this)
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        DrawerBuilder b = new DrawerBuilder()
                .withActivity(homeActivity)
                .withFullscreen(true)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withOnDrawerItemClickListener(this)
                .withDelayOnDrawerClose(70)
                .withStickyFooterShadow(true)
                .withScrollToTopAfterClick(true)
                .withSavedInstance(savedInstanceState);

        b.addDrawerItems(
                new PrimaryDrawerItem()
                        .withName(R.string.title_home)
                        .withIcon(IconUtils.icon(homeActivity, GoogleMaterial.Icon.gmd_home, R.color.black).alpha(110))
                        .withIdentifier(HOME),
                new PrimaryDrawerItem()
                        .withName(R.string.title_activity_patients)
                        .withIcon(IconUtils.icon(homeActivity, CommunityMaterial.Icon.cmd_account_multiple, R.color.black).alpha(110))
                        .withIdentifier(PATIENTS),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withName(R.string.title_activity_medicines)
                        .withIcon(IconUtils.icon(homeActivity, CommunityMaterial.Icon.cmd_pill, R.color.black).alpha(110))
                        .withIdentifier(MEDICINES),
                new PrimaryDrawerItem()
                        .withName(R.string.title_activity_routines)
                        .withIcon(IconUtils.icon(homeActivity, GoogleMaterial.Icon.gmd_alarm, R.color.black).alpha(110))
                        .withIdentifier(ROUTINES),
                new PrimaryDrawerItem()
                        .withName(R.string.title_activity_schedules)
                        .withIcon(IconUtils.icon(homeActivity, GoogleMaterial.Icon.gmd_calendar, R.color.black).alpha(110))
                        .withIdentifier(SCHEDULES));
        if (ModuleManager.isEnabled(AllergiesModule.ID)) {
            b.addDrawerItems(new PrimaryDrawerItem()
                    .withName(R.string.home_menu_allergies)
                    .withIcon(IconUtils.icon(homeActivity, CommunityMaterial.Icon.cmd_alert, R.color.black).alpha(110))
                    .withIdentifier(ALLERGIES));
        }

        b.addDrawerItems(
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withName(R.string.drawer_help_option)
                        .withIcon(IconUtils.icon(homeActivity, GoogleMaterial.Icon.gmd_pin_assistant, R.color.black).alpha(130))
                        .withIdentifier(HELP),
                new PrimaryDrawerItem()
                        .withName(R.string.drawer_settings_option)
                        .withIcon(IconUtils.icon(homeActivity, CommunityMaterial.Icon.cmd_settings, R.color.black).alpha(110))
                        .withIdentifier(SETTINGS),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withName(R.string.drawer_about_option)
                        .withIcon(IconUtils.icon(homeActivity, CommunityMaterial.Icon.cmd_information, R.color.black).alpha(110))
                        .withIdentifier(ABOUT)
        );

        drawer = b.build();

        Patient p = DB.patients().getActive(homeActivity);
        headerResult.setActiveProfile(p.getId().intValue(), false);
        updateHeaderBackground(p);
    }

    @Override
    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {

        int identifier = iDrawerItem.getIdentifier();

        switch (identifier) {
            case HOME:
                homeActivity.showPagerItem(HomePages.HOME.ordinal(), false);
                break;
            case ROUTINES:
                homeActivity.showPagerItem(HomePages.ROUTINES.ordinal(), false);
                break;
            case MEDICINES:
                homeActivity.showPagerItem(HomePages.MEDICINES.ordinal(), false);
                break;
            case SCHEDULES:
                homeActivity.showPagerItem(HomePages.SCHEDULES.ordinal(), false);
                break;
            case CALENDAR:
                launchActivity(new Intent(homeActivity, CalendarActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case HELP:
                //homeActivity.showTutorial();
                launchActivity(new Intent(homeActivity, MaterialIntroActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case PATIENTS:
                launchActivity(new Intent(homeActivity, PatientsActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case SETTINGS:
                launchActivity(new Intent(homeActivity, CalendulaSettingsActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case ABOUT:
                showAbout();
                drawer.setSelection(HOME, false);
                break;
            case ALLERGIES:
                launchActivity(new Intent(homeActivity, AllergiesActivity.class));
                drawer.setSelection(HOME, false);
                break;
            default:
                return false;
        }
        drawer.closeDrawer();
        return true;
    }

    public void onPagerPositionChange(int pagerPosition) {
        LogUtil.d(TAG, "onPagerPositionChange: " + pagerPosition);
        switch (pagerPosition) {
            case 0:
                drawer.setSelection(HOME, false);
                break;
            case 1:
                drawer.setSelection(ROUTINES, false);
                break;
            case 2:
                drawer.setSelection(MEDICINES, false);
                break;
            case 3:
                drawer.setSelection(SCHEDULES, false);
                break;
        }
    }

    public void changePageSelected(int pagerPosition) {
        switch (pagerPosition) {
            case 0:
                drawer.setSelection(HOME, false);
                break;
            case 1:
                drawer.setSelection(MEDICINES, false);
                break;
            case 2:
                drawer.setSelection(ROUTINES, false);
                break;
            case 3:
                drawer.setSelection(SCHEDULES, false);
                break;
        }
    }

    @Override
    public boolean onProfileChanged(View view, IProfile profile, boolean current) {

        if (profile.getIdentifier() == PATIENT_ADD_PROFILE_ID) {
            Intent intent = new Intent(homeActivity, PatientDetailActivity.class);
            launchActivity(intent);
            return true;
        } else {
            Long id = (long) profile.getIdentifier();
            Patient p = DB.patients().findById(id);
            boolean isActive = DB.patients().isActive(p, homeActivity);
            if (isActive) {
                Intent intent = new Intent(homeActivity, PatientDetailActivity.class);
                intent.putExtra("patient_id", id);
                launchActivity(intent);
            } else {
                DB.patients().setActive(p);
                updateHeaderBackground(p);
            }
        }
        return false;
    }

    public void updateHeaderBackground(Patient p) {
        currentPatient = p;
        //int colors[] = AvatarMgr.colorsFor(homeActivity.getResources(), p.avatar());
        LayerDrawable layers = (LayerDrawable) headerResult.getHeaderBackgroundView().getDrawable();
        ColorDrawable color = (ColorDrawable) layers.findDrawableByLayerId(R.id.color_layer);
        color.setColor(ScreenUtils.equivalentNoAlpha(p.getColor(), 1f));
    }

    public Drawer drawer() {
        return drawer;
    }

    public AccountHeader header() {
        return headerResult;
    }

    public void onActivityResume(Patient p) {

        currentPatient = p;

        List<Patient> patients = DB.patients().findAll();
        ArrayList<IProfile> profiles = headerResult.getProfiles();
        ArrayList<IProfile> toRemove = new ArrayList<>();
        if (patients.size() != profiles.size()) {
            for (IProfile pr : profiles) {
                Long id = (long) pr.getIdentifier();
                boolean remove = true;
                for (Patient pat : patients) {
                    if (pat.getId().equals(id) || id == PATIENT_ADD_PROFILE_ID) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    toRemove.add(pr);
                }
            }
            for (IProfile pr : toRemove) {
                headerResult.removeProfile(pr);
            }
        }

        headerResult.setActiveProfile(p.getId().intValue(), false);

        if (p != null && !p.equals(currentPatient) || header().getActiveProfile().getIcon().getIconRes() != AvatarMgr.res(p.getAvatar())) {
            headerResult.setActiveProfile(p.getId().intValue(), false);
            IProfile profile = headerResult.getActiveProfile();
            profile.withIcon(AvatarMgr.res(p.getAvatar()));
            headerResult.updateProfile(profile);
        }
        updateHeaderBackground(p);
    }

    public void onPatientCreated(Patient p) {
        headerResult.setProfiles(genProfiles());
    }

    public void onPatientUpdated(Patient p) {
        headerResult.setProfiles(genProfiles());
    }

    private void addCalendarItem() {
        drawer.addItemAtPosition(new PrimaryDrawerItem()
                .withName(R.string.title_activity_pickup_calendar_short)
                .withIcon(IconUtils.icon(homeActivity, CommunityMaterial.Icon.cmd_calendar_check, R.color.black).alpha(110))
                .withEnabled(true)
                .withIdentifier(CALENDAR), 7);
    }

    private void launchActivity(Intent i) {
        homeActivity.startActivity(i);
        homeActivity.overridePendingTransition(0, 0);
    }

    private void showAbout() {
        launchActivity(new Intent(homeActivity, AboutActivity.class));
    }

    private ArrayList<IProfile> genProfiles() {
        ArrayList<IProfile> profiles = new ArrayList<>();

        for (Patient p : DB.patients().findAll()) {
            LogUtil.d(TAG, "Adding patient to drawer: " + p.getName());
            profiles.add(genProfile(p));
        }

        profiles.add(new ProfileSettingDrawerItem()
                .withName(homeActivity.getString(R.string.patient_add))
                .withIcon(new IconicsDrawable(homeActivity, GoogleMaterial.Icon.gmd_account_add)
                        .sizeDp(24)
                        .paddingDp(1)
                        .colorRes(R.color.dark_grey_home))
                .withIdentifier(PATIENT_ADD_PROFILE_ID));
        return profiles;
    }

    private IProfile genProfile(Patient p) {
        final int schedules = DB.schedules().findAll(p).size();
        String fakeMail;
        if (schedules > 0) {
            fakeMail = homeActivity.getString(R.string.active_schedules_number, schedules);
        } else {
            fakeMail = homeActivity.getString(R.string.active_schedules_none);
        }

        return new ProfileDrawerItem()
                .withIdentifier(p.getId().intValue())
                .withName(p.getName())
                .withEmail(fakeMail)
                .withIcon(AvatarMgr.res(p.getAvatar()))
                .withNameShown(true);
    }

}
