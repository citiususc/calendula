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

package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomePagerActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.ScreenUtils;

/**
 * Created by joseangel.pineiro on 10/28/15.
 */
public class LeftDrawerMgr implements Drawer.OnDrawerItemClickListener, Drawer.OnDrawerItemLongClickListener, AccountHeader.OnAccountHeaderListener {

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

    public static final int PATIENT_ADD = 11;
    public static final int CALENDAR = 12;

    public static final int ALLERGIES = 11;


    private AccountHeader headerResult = null;
    private Drawer drawer = null;
    private Toolbar toolbar;
    private HomePagerActivity home;
    private Patient currentPatient;

    public LeftDrawerMgr(HomePagerActivity activity, Toolbar toolbar) {
        this.toolbar = toolbar;
        this.home = activity;
    }

    public void init(Bundle savedInstanceState) {

        boolean isPharmaEnabled = CalendulaApp.isPharmaModeEnabled(home);

        ArrayList<IProfile> profiles = new ArrayList<>();

        profiles.add(new ProfileSettingDrawerItem()
                .withName("Añadir paciente")
                .withDescription("Gestionar la pautas de otra persona")
                .withIcon(new IconicsDrawable(home, GoogleMaterial.Icon.gmd_account_add)
                        .sizeDp(24)
                        .paddingDp(5)
                        .colorRes(R.color.dark_grey_home))
                .withIdentifier(PATIENT_ADD));

        for (Patient p : DB.patients().findAll()) {
            Log.d("LeftDrawer", "Adding patient to drawer: " + p.name());
            profiles.add(new ProfileDrawerItem()
                    .withIdentifier(p.id().intValue())
                    .withName(p.name())
                    .withEmail(p.name() + "@calendula")
                    .withIcon(AvatarMgr.res(p.avatar())));
        }

        headerResult = new AccountHeaderBuilder()
                .withActivity(home)
                .withHeaderBackground(R.drawable.drawer_header)
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withCompactStyle(false)
                .withProfiles(profiles)
                .withAlternativeProfileHeaderSwitching(true)
                .withThreeSmallProfileImages(true)
                .withOnAccountHeaderListener(this)
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        drawer = new DrawerBuilder()
                .withActivity(home)
                .withFullscreen(true)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.title_home)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_home, R.color.black).alpha(110))
                                .withIdentifier(HOME),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_patients)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_account_multiple, R.color.black).alpha(110))
                                .withIdentifier(PATIENTS),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_routines)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_alarm, R.color.black).alpha(110))
                                .withIdentifier(ROUTINES),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_medicines)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_pill, R.color.black).alpha(110))
                                .withIdentifier(MEDICINES),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_schedules)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_calendar, R.color.black).alpha(110))
                                .withIdentifier(SCHEDULES),
                        new PrimaryDrawerItem()
                                .withName(R.string.home_menu_allergies)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_exclamation, R.color.black).alpha(110))
                                .withIdentifier(ALLERGIES),
                        new PrimaryDrawerItem()
                                .withName(R.string.home_menu_pharmacies)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_map_marker_multiple, R.color.black).alpha(38))
                                .withEnabled(false)
                                .withIdentifier(PHARMACIES),
                        new PrimaryDrawerItem()
                                .withName(R.string.home_menu_plantrip)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_airplanemode_active, R.color.black).alpha(38))
                                .withEnabled(false)
                                .withIdentifier(TRAVELPLAN),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_help_option)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_pin_assistant, R.color.black).alpha(130))
                                .withIdentifier(HELP),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_settings_option)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_settings, R.color.black).alpha(110))
                                .withIdentifier(SETTINGS),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_about_option)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_information, R.color.black).alpha(110))
                                .withIdentifier(ABOUT)
                )
                .withOnDrawerItemClickListener(this)
                .withOnDrawerItemLongClickListener(this)
                .withDelayOnDrawerClose(70)
                .withStickyFooterShadow(true)
                .withScrollToTopAfterClick(true)
                .withSavedInstance(savedInstanceState)
                .build();

        Patient p = DB.patients().getActive(home);
        headerResult.setActiveProfile(p.id().intValue(), false);
        updateHeaderBackground(p);

        onPharmacyModeChanged(isPharmaEnabled);

    }

    @Override
    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {

        int identifier = iDrawerItem.getIdentifier();

        switch (identifier) {
            case HOME:
                home.showPagerItem(0, false);
                break;
            case ROUTINES:
                home.showPagerItem(1, false);
                break;
            case MEDICINES:
                home.showPagerItem(2, false);
                break;
            case SCHEDULES:
                home.showPagerItem(3, false);
                break;
            case CALENDAR:
                launchActivity(new Intent(home, CalendarActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case HELP:
                //home.showTutorial();
                launchActivity(new Intent(home, MaterialIntroActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case PATIENTS:
                launchActivity(new Intent(home, PatientsActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case SETTINGS:
                launchActivity(new Intent(home, SettingsActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case ABOUT:
                showAbout();
                drawer.setSelection(HOME, false);
                break;
            case ALLERGIES:
                launchActivity(new Intent(home, AllergiesActivity.class));
                drawer.setSelection(HOME, false);
                break;
            default:
                return false;
        }
        drawer.closeDrawer();
        return true;
    }

    private void addCalendarItem() {
        drawer.addItemAtPosition(new PrimaryDrawerItem()
                .withName("Dispensación")
                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_calendar_check, R.color.black).alpha(110))
                .withEnabled(true)
                .withIdentifier(CALENDAR), 7);
    }

    @Override
    public boolean onItemLongClick(View view, int i, IDrawerItem iDrawerItem) {
        int identifier = iDrawerItem.getIdentifier();
        if (identifier == PHARMACIES) {
            home.enableOrDisablePharmacyMode();
            return true;
        }
        return false;
    }

    public void onPharmacyModeChanged(boolean enabled) {
        PrimaryDrawerItem item = (PrimaryDrawerItem) drawer.getDrawerItem(PHARMACIES);
        BadgeStyle bs = new BadgeStyle();
        if (enabled) {
            addCalendarItem();
            Drawable bg = new IconicsDrawable(home)
                    .icon(GoogleMaterial.Icon.gmd_check)
                    .color(home.getResources().getColor(R.color.dark_grey_text))
                    .sizeDp(18);
            bs.withBadgeBackground(bg);
        } else {
            drawer.removeItem(CALENDAR);
            bs.withBadgeBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        item.withBadgeStyle(bs);
        item.withBadge(" ");
        drawer.updateItem(item);
    }

    private void launchActivity(Intent i) {
        home.startActivity(i);
        home.overridePendingTransition(0, 0);
    }

    public void onPagerPositionChange(int pagerPosition) {
        Log.d("LeftDrawer", "onPagerPositionChange: " + pagerPosition);
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

    @Override
    public boolean onProfileChanged(View view, IProfile profile, boolean current) {

        if (profile instanceof ProfileSettingDrawerItem) {
            Intent intent = new Intent(home, PatientDetailActivity.class);
            launchActivity(intent);
            return true;
        } else {
            Long id = Long.valueOf(profile.getIdentifier());
            Patient p = DB.patients().findById(id);
            boolean isActive = DB.patients().isActive(p, home);
            if (isActive) {
                Intent intent = new Intent(home, PatientDetailActivity.class);
                intent.putExtra("patient_id", id);
                launchActivity(intent);
            } else {
                DB.patients().setActive(p, home);
                updateHeaderBackground(p);
            }
        }
        return false;
    }

    private void showAbout() {
        launchActivity(new Intent(home, AboutActivity.class));
    }

    public void updateHeaderBackground(Patient p) {
        currentPatient = p;
        //int colors[] = AvatarMgr.colorsFor(home.getResources(), p.avatar());
        LayerDrawable layers = (LayerDrawable) headerResult.getHeaderBackgroundView().getDrawable();
        ColorDrawable color = (ColorDrawable) layers.findDrawableByLayerId(R.id.color_layer);
        color.setColor(ScreenUtils.equivalentNoAlpha(p.color(), 1f));
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
                Long id = Long.valueOf(pr.getIdentifier());
                boolean remove = true;
                for (Patient pat : patients) {
                    if (pat.id().equals(id)) {
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

        headerResult.setActiveProfile(p.id().intValue(), false);

        if (p != null && !p.equals(currentPatient) || header().getActiveProfile().getIcon().getIconRes() != AvatarMgr.res(p.avatar())) {
            headerResult.setActiveProfile(p.id().intValue(), false);
            IProfile profile = headerResult.getActiveProfile();
            profile.withIcon(AvatarMgr.res(p.avatar()));
            headerResult.updateProfile(profile);
        }
        updateHeaderBackground(p);
    }

    public void onPatientCreated(Patient p) {

        IProfile profile = new ProfileDrawerItem()
                .withIdentifier(p.id().intValue())
                .withName(p.name())
                .withEmail(p.name() + "@calendula")
                .withIcon(AvatarMgr.res(p.avatar()));

        headerResult.addProfiles(profile);
    }
}
