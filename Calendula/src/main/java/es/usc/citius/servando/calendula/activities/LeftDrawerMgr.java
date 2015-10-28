package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.AvatarMgr;

/**
 * Created by joseangel.pineiro on 10/28/15.
 */
public class LeftDrawerMgr implements Drawer.OnDrawerNavigationListener{

    public static final int HOME = 0;
    public static final int ROUTINES = 1;
    public static final int MEDICINES = 2;
    public static final int SCHEDULES = 3;

    private AccountHeader headerResult = null;
    private Drawer drawer = null;
    private Toolbar toolbar;
    private Activity activity;

    public LeftDrawerMgr(Activity activity, Toolbar toolbar) {
        this.toolbar = toolbar;
        this.activity = activity;
    }

    public void init(Bundle savedInstanceState) {

        ArrayList<IProfile> profiles = new ArrayList<>();

        for(Patient p : DB.patients().findAll()){
            profiles.add(new ProfileDrawerItem()
                    .withName(p.name())
                    .withEmail(p.name() + "@calendula.app")
                    .withIcon(AvatarMgr.res(p.avatar())));
        }

        headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.color.android_orange_darker)
                .withCompactStyle(false)
                .withProfiles(profiles)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        //sample usage of the onProfileChanged listener

                        //false if you have not consumed the event and it should close the drawer
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.title_home)
                                .withIcon(icon(GoogleMaterial.Icon.gmd_home, R.color.dark_grey_home))
                                .withIdentifier(HOME),
                        new SecondaryDrawerItem()
                                .withName("Pacientes")
                                .withIcon(icon(CommunityMaterial.Icon.cmd_account, R.color.dark_grey_home)),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_routines)
                                .withIcon(icon(GoogleMaterial.Icon.gmd_alarm, R.color.android_orange_dark))
                                .withIdentifier(ROUTINES),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_medicines)
                                .withIcon(icon(CommunityMaterial.Icon.cmd_pill, R.color.android_pink_dark))
                                .withIdentifier(MEDICINES),
                        new PrimaryDrawerItem()
                                .withName(R.string.title_activity_schedules)
                                .withIcon(icon(GoogleMaterial.Icon.gmd_event, R.color.android_green_dark))
                                .withIdentifier(SCHEDULES),
                        new SecondaryDrawerItem()
                                .withName("Farmacias")
                                .withIcon(icon(GoogleMaterial.Icon.gmd_location_on, R.color.light_grey))
                                .withEnabled(false),
                        new SecondaryDrawerItem()
                                .withName("Plan de viaje")
                                .withIcon(icon(GoogleMaterial.Icon.gmd_airplanemode_active, R.color.light_grey))
                                .withEnabled(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem()
                                .withName("Recorrido")
                                .withIcon(icon(GoogleMaterial.Icon.gmd_assistant, R.color.dark_grey_home)),
                        new SecondaryDrawerItem()
                                .withName("Ajustes")
                                .withIcon(icon(CommunityMaterial.Icon.cmd_settings, R.color.dark_grey_home))
                )
                .withOnDrawerNavigationListener(this)
                .withDelayOnDrawerClose(100)
                .withStickyFooterShadow(true)
                .withSavedInstance(savedInstanceState)
                .build();



    }


    @Override
    public boolean onNavigationClickListener(View view) {
        //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
        //if the back arrow is shown. close the activity

        //return true if we have consumed the event
        return true;
    }

    private IconicsDrawable icon(IIcon ic, int color){
        return new IconicsDrawable(activity, ic)
                .sizeDp(48)
                .paddingDp(0)
                .colorRes(color);
    }


}
