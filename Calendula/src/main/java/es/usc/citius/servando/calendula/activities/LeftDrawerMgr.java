package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import es.usc.citius.servando.calendula.HomeActivity;
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


    private AccountHeader headerResult = null;
    private Drawer drawer = null;
    private Toolbar toolbar;
    private HomeActivity home;

    public LeftDrawerMgr(HomeActivity activity, Toolbar toolbar) {
        this.toolbar = toolbar;
        this.home = activity;
    }

    public void init(Bundle savedInstanceState) {

        ArrayList<IProfile> profiles = new ArrayList<>();

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
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_home, R.color.dark_grey_home))
                                .withIdentifier(HOME),
                        new PrimaryDrawerItem()
                                .withName("Pacientes")
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_account, R.color.dark_grey_home))
                                .withIdentifier(PATIENTS),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_routines)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_alarm, R.color.android_orange_dark))
                                .withIdentifier(ROUTINES),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_medicines)
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_pill, R.color.android_pink_dark))
                                .withIdentifier(MEDICINES),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_schedules)
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_event, R.color.android_green_dark))
                                .withIdentifier(SCHEDULES),
                        new SecondaryDrawerItem()
                                .withName("Farmacias")
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_location_on, R.color.light_grey))
                                .withEnabled(false)
                                .withIdentifier(PHARMACIES),
                        new PrimaryDrawerItem()
                                .withName("Plan de viaje")
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_airplanemode_active, R.color.light_grey))
                                .withEnabled(false)
                                .withIdentifier(TRAVELPLAN),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName("Recorrido")
                                .withIcon(IconUtils.icon(home, GoogleMaterial.Icon.gmd_assistant, R.color.dark_grey_home))
                                .withIdentifier(HELP),
                        new PrimaryDrawerItem()
                                .withName("Ajustes")
                                .withIcon(IconUtils.icon(home, CommunityMaterial.Icon.cmd_settings, R.color.dark_grey_home))
                                .withIdentifier(SETTINGS)
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
        return false;
    }

    public void updateHeaderBackground(Patient p){
        int colors[] = AvatarMgr.colorsFor(home.getResources(), p.avatar());
        LayerDrawable layers = (LayerDrawable) headerResult.getHeaderBackgroundView().getDrawable();
        ColorDrawable color = (ColorDrawable) layers.findDrawableByLayerId(R.id.color_layer);
        color.setColor(ScreenUtils.equivalentNoAlpha(colors[0],0.7f));
    }

    public Drawer drawer(){
        return drawer;
    }
}
