package es.usc.citius.servando.calendula;

import android.content.Intent;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScanActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.fragments.ScheduleTypeFragment;
import es.usc.citius.servando.calendula.util.IconUtils;

/**
 * Helper to manage the home screen floating action button behaviour
 */
public class FabMenuMgr implements View.OnClickListener{


    LeftDrawerMgr drawerMgr;
    FloatingActionsMenu fabMenu;
    FloatingActionButton fab;
    HomeActivity activity;

    List<FloatingActionButton> homeActions;
    List<FloatingActionButton> scheduleActions;
    FloatingActionButton scanQrAction;
    FloatingActionButton all;

    private int currentPage = 0;


    public FabMenuMgr(FloatingActionButton fab, FloatingActionsMenu fabMenu, LeftDrawerMgr drawerMgr, HomeActivity activity) {
        this.fab = fab;
        this.fabMenu = fabMenu;
        this.activity = activity;
        this.drawerMgr = drawerMgr;
        this.homeActions = getHomeActions();
        this.scheduleActions = getScheduleActions();


    }

    private List<FloatingActionButton> getScheduleActions() {
        ArrayList<FloatingActionButton> actions = new ArrayList<>();

        FloatingActionButton actionA = (FloatingActionButton) fabMenu.findViewById(R.id.action_a);
        FloatingActionButton actionB = (FloatingActionButton) fabMenu.findViewById(R.id.action_b);
        FloatingActionButton actionC = (FloatingActionButton) fabMenu.findViewById(R.id.action_c);

        actions.add(actionA);
        actions.add(actionB);
        actions.add(actionC);

        scanQrAction = (FloatingActionButton) fabMenu.findViewById(R.id.action_d);

        if(CalendulaApp.isPharmaModeEnabled(activity)) {
            scanQrAction.setVisibility(View.VISIBLE);
            actions.add(scanQrAction);
        }else{
            scanQrAction.setVisibility(View.GONE);
        }
        return actions;
    }

    private List<FloatingActionButton> getHomeActions() {
        ArrayList<FloatingActionButton> actions = new ArrayList<>();
        all = (FloatingActionButton) fabMenu.findViewById(R.id.user_all);
        all.setIconDrawable(IconUtils.icon(activity, GoogleMaterial.Icon.gmd_group, R.color.white, 36));
        all.setTitle("Todos");
        updateHomeActions(actions);
        return actions;
    }

    private void updateHomeActions(List<FloatingActionButton> actions){
        int[] ids = new int[]{R.id.user1, R.id.user2, R.id.user3};
        ArrayList<IProfile> profiles = drawerMgr.header().getProfiles();

        for(int i = 0; i < Math.min(ids.length,profiles.size()); i++){

            FloatingActionButton userFab;

            if(i < actions.size()){
                userFab = actions.get(i);
                IProfile p = profiles.get(i);
                userFab.setIcon(p.getIcon().getIconRes());
                userFab.setTitle(p.getName().getText());
            }else{
                userFab = (FloatingActionButton) fabMenu.findViewById(ids[i]);
                IProfile p = profiles.get(i);
                userFab.setIcon(p.getIcon().getIconRes());
                userFab.setTitle(p.getName().getText());
                actions.add(userFab);
            }
        }
    }

    public void init() {

        for(FloatingActionButton f : homeActions){
            f.setOnClickListener(this);
        }
        for(FloatingActionButton f : scheduleActions){
            f.setOnClickListener(this);
        }

        fab.setOnClickListener(this);

        onViewPagerItemChange(0);
    }

    public void onViewPagerItemChange(int currentPage){
        this.currentPage = currentPage;

        fab.setColorNormalResId(getFabColor(currentPage));
        fab.setColorPressedResId(getFabPressedColor(currentPage));

        switch (currentPage){
            case 0:
                for(FloatingActionButton f : homeActions){
                    f.setVisibility(View.GONE);
                }
                for(FloatingActionButton f : scheduleActions){
                    f.setVisibility(View.GONE);
                }
                all.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                fabMenu.setVisibility(View.GONE);
                scanQrAction.setVisibility(View.GONE);
                break;

            case 1:
            case 2:
                for(FloatingActionButton f : homeActions){
                    f.setVisibility(View.GONE);
                }
                for(FloatingActionButton f : scheduleActions){
                    f.setVisibility(View.GONE);
                }
                all.setVisibility(View.GONE);
                fabMenu.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                fab.bringToFront();
                break;

            case 3:
                for(FloatingActionButton f : homeActions){
                    f.setVisibility(View.GONE);
                }
                for(FloatingActionButton f : scheduleActions){
                    f.setVisibility(View.VISIBLE);
                }
                all.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                fabMenu.setVisibility(View.VISIBLE);
                fabMenu.bringToFront();
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
            case R.id.action_a:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_ROUTINES);
                break;
            case R.id.action_b:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_HOURLY);
                break;
            case R.id.action_c:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_PERIOD);
                break;
            case R.id.action_d:
                startScanActivity();
                break;

        }


    }

    private void onClickAdd() {
        switch (currentPage){
            case 0:
                return;
            case 1:
                launchActivity(RoutinesActivity.class);
                break;
            case 2:
                launchActivity(MedicinesActivity.class);
                break;
            case 3:
                return;
        }
    }

    private void launchActivity(Class<?> type){
        activity.startActivity(new Intent(activity,type));
        activity.overridePendingTransition(0, 0);
    }

    private void startSchedulesActivity(int scheduleType){
        Intent i = new Intent(activity, ScheduleCreationActivity.class);
        i.putExtra("scheduleType", scheduleType);
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        fabMenu.collapse();
    }

    private void startScanActivity(){
        Intent i = new Intent(activity, ScanActivity.class);
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        fabMenu.collapse();
        return;
    }

    public int getFabColor(int page) {
        switch (page) {
            case 1:
                return R.color.android_orange;
            case 2:
                return R.color.android_pink_dark;
            case 3:
                return R.color.android_green;
            default:
                return R.color.android_blue_darker;
        }
    }

    public int getFabPressedColor(int page) {
        switch (page) {
            case 1:
                return R.color.android_orange_dark;
            case 2:
                return R.color.android_pink;
            case 3:
                return R.color.android_green_dark;
            default:
                return R.color.android_blue_dark;
        }
    }

    public void onPharmacyModeChanged(boolean enabled){
        if(enabled && !scheduleActions.contains(scanQrAction)){
            scheduleActions.add(scanQrAction);
            scanQrAction.setVisibility(View.VISIBLE);
        }else if(!enabled && scheduleActions.contains(scanQrAction)){
            scanQrAction.setVisibility(View.GONE);
            scheduleActions.remove(scanQrAction);
        }
        fabMenu.invalidate();
        onViewPagerItemChange(currentPage);
    }

}
