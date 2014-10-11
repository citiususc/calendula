package es.usc.citius.servando.calendula.fragments;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.SchedulesActivity;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    ImageView shadowBg;
    RelativeLayout buttonsContainer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // set click listener for all the home menu buttons
        rootView.findViewById(R.id.home_button_routines).setOnClickListener(this);
        rootView.findViewById(R.id.home_button_medicines).setOnClickListener(this);
        rootView.findViewById(R.id.home_button_schedules).setOnClickListener(this);
        rootView.findViewById(R.id.home_button_pharmacies).setOnClickListener(this);
        rootView.findViewById(R.id.home_button_plantrip).setOnClickListener(this);
        buttonsContainer = (RelativeLayout) rootView.findViewById(R.id.buttons_container);
        shadowBg = (ImageView) rootView.findViewById(R.id.bg_blur);
        return rootView;
    }



    @Override
    public void onClick(View view) {

        final int viewId = view.getId();

        switch (viewId) {
            case R.id.home_button_routines:
                launchActivity(RoutinesActivity.class);
                break;
            case R.id.home_button_medicines:
                launchActivity(MedicinesActivity.class);
                break;
            case R.id.home_button_schedules:
                launchActivity(SchedulesActivity.class);
                break;
            case R.id.home_button_pharmacies:
                break;
            case R.id.home_button_plantrip:
                break;
        }
    }

    /**
     * Launch the activity related to the user selection on the home menu
     *
     * @param activityCls
     */
    private void launchActivity(Class activityCls) {
        Intent intent = new Intent(getActivity(), activityCls);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }


    public void onScroll(float offset, int positionOffsetPixels) {


        // offset: 0..1 --> alpha: 0..255
        int alpha = new Float(255 - offset * 255).intValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            shadowBg.setImageAlpha(alpha);
        } else {
            shadowBg.setAlpha(alpha);
        }

        //profileImageContainer.scrollTo(left-positionOffsetPixels,top);
        buttonsContainer.scrollTo(positionOffsetPixels * 2, 0);
    }



}