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
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

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
public class LeftDrawerMgr implements Drawer.OnDrawerItemClickListener, Drawer.OnDrawerItemLongClickListener, AccountHeader.OnAccountHeaderListener{

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

        for(Patient p : DB.patients().findAll()){
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
                                .withName("Pacientes")
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
                                .withName("Farmacias")
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_map_marker_multiple, R.color.black).alpha(38))
                                .withEnabled(false)
                                .withIdentifier(PHARMACIES),
                        new PrimaryDrawerItem()
                                .withName("Plan de viaje")
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_airplanemode_active, R.color.black).alpha(38))
                                .withEnabled(false)
                                .withIdentifier(TRAVELPLAN),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName("Recorrido")
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_pin_assistant,R.color.black).alpha(130))
                                .withIdentifier(HELP),
                        new PrimaryDrawerItem()
                                .withName("Ajustes")
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_settings, R.color.black).alpha(110))
                                .withIdentifier(SETTINGS),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem()
                                .withName("Open Source")
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_github_circle, R.color.black).alpha(110))
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
                headerResult.setActiveProfile(p.id().intValue(),false);
                updateHeaderBackground(p);

                onPharmacyModeChanged(isPharmaEnabled);

    }

    @Override
    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {

        int identifier = iDrawerItem.getIdentifier();

        switch (identifier){
            case HOME:
                home.showPagerItem(0,false);
                break;
            case ROUTINES:
                home.showPagerItem(1,false);
                break;
            case MEDICINES:
                home.showPagerItem(2,false);
                break;
            case SCHEDULES:
                home.showPagerItem(3,false);
                break;
            case HELP:
                home.showTutorial();
                drawer.setSelection(HOME, false);
                break;
            case PATIENTS:
                launchActivity(new Intent(home, PatientsActivity.class));
                drawer.setSelection(HOME, false);
                break;
            case SETTINGS:
                launchActivity(new Intent(home, SettingsActivity.class));
                drawer.setSelection(HOME,false);
                break;
            case ABOUT:
                showAbout();
                drawer.setSelection(HOME,false);
                break;
            default:
                return false;
        }
        drawer.closeDrawer();
        return true;
    }

    @Override
    public boolean onItemLongClick(View view, int i, IDrawerItem iDrawerItem) {
        int identifier = iDrawerItem.getIdentifier();
        if(identifier == PHARMACIES){
            home.enableOrDisablePharmacyMode();
            return true;
        }
        return false;
    }

    public void onPharmacyModeChanged(boolean enabled){
        PrimaryDrawerItem item = (PrimaryDrawerItem) drawer.getDrawerItem(PHARMACIES);
        BadgeStyle bs = new BadgeStyle();
        if(enabled){
            Drawable bg = new IconicsDrawable(home)
                    .icon(GoogleMaterial.Icon.gmd_check)
                    .color(home.getResources().getColor(R.color.dark_grey_text))
                    .sizeDp(18);
            bs.withBadgeBackground(bg);
        }else{
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
        switch (pagerPosition){
            case 0:
                drawer.setSelection(HOME, false);
                break;
            case 1:
                drawer.setSelection(ROUTINES,false);
                break;
            case 2:
                drawer.setSelection(MEDICINES,false);
                break;
            case 3:
                drawer.setSelection(SCHEDULES,false);
                break;
        }
    }

    @Override
    public boolean onProfileChanged(View view, IProfile profile, boolean current) {

        if(profile instanceof ProfileSettingDrawerItem){
            Intent intent = new Intent(home, PatientDetailActivity.class);
            launchActivity(intent);
            return true;
        }else{
            Long id = Long.valueOf(profile.getIdentifier());
            Patient p = DB.patients().findById(id);
            boolean isActive = DB.patients().isActive(p, home);
            if(isActive) {
                Intent intent = new Intent(home, PatientDetailActivity.class);
                intent.putExtra("patient_id", id);
                launchActivity(intent);
            }else{
                DB.patients().setActive(p, home);
                updateHeaderBackground(p);
            }
        }
        return false;
    }

    private void showAbout(){
        launchActivity(new Intent(home, AboutActivity.class));
    }

    public void updateHeaderBackground(Patient p){
        currentPatient = p;
        //int colors[] = AvatarMgr.colorsFor(home.getResources(), p.avatar());
        LayerDrawable layers = (LayerDrawable) headerResult.getHeaderBackgroundView().getDrawable();
        ColorDrawable color = (ColorDrawable) layers.findDrawableByLayerId(R.id.color_layer);
        color.setColor(ScreenUtils.equivalentNoAlpha(p.color(), 1f));
    }



    public Drawer drawer(){
        return drawer;
    }

    public AccountHeader header(){
        return headerResult;
    }

    public void onActivityResume(Patient p) {
        if(!p.equals(currentPatient) || header().getActiveProfile().getIcon().getIconRes() != AvatarMgr.res(p.avatar())){
            headerResult.setActiveProfile(p.id().intValue(), false);
            IProfile profile = headerResult.getActiveProfile();
            profile.withIcon(AvatarMgr.res(p.avatar()));
            headerResult.updateProfile(profile);
            updateHeaderBackground(p);
        }
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
