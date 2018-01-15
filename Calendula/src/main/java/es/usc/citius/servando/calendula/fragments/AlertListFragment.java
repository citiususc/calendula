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

package es.usc.citius.servando.calendula.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.AlertViewRecyclerAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.DrivingCautionAlert;
import es.usc.citius.servando.calendula.persistence.alerts.StockRunningOutAlert;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Alert Lis Fragment
 */
public class AlertListFragment extends Fragment {

    private final static Class<?>[] alertViewProviders = new Class[]{
            StockRunningOutAlert.StockAlertViewProvider.class,
            DrivingCautionAlert.DrivingAlertViewProvider.class,
            AllergyPatientAlert.AllergyAlertViewProvider.class
    };
    private static final String TAG = "AlertListFragment";
    View emptyView;
    LinearLayoutManager llm;
    RecyclerView rv;
    AlertViewRecyclerAdapter rvAdapter;
    AlertViewRecyclerAdapter.EventListener rvListener;
    List<PatientAlert> items = new ArrayList<>();
    Medicine m;

    public static AlertListFragment newInstance(Medicine m) {
        AlertListFragment fragment = new AlertListFragment();
        Bundle args = new Bundle();
        args.putSerializable("medicine_id", m.getId());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            m = DB.medicines().findById(getArguments().getLong("medicine_id", -1));
        }
        items = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_daily_agenda, container, false);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        emptyView = rootView.findViewById(R.id.empty_view_placeholder);
        setupRecyclerView();
        setupEmptyView();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notifyDataChange();
    }

    public List<PatientAlert> buildItems() {
        List<PatientAlert> typed = new ArrayList<>();
        List<PatientAlert> original = DB.alerts().findByMedicineSortByLevel(m);
        for (PatientAlert a : original) {
            typed.add(a.map());
        }
        LogUtil.d(TAG, "buildItems: Alerts: " + typed.size());
        return typed;

    }

    public void refresh() {
        rvAdapter.notifyDataSetChanged();
    }

    public void notifyDataChange() {
        try {
            items.clear();
            items.addAll(buildItems());
            rvAdapter.notifyDataSetChanged();
            // show empty list view if there are no items
            rv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showOrHideEmptyView(items.isEmpty());
                }
            }, 100);
        } catch (Exception e) {
            LogUtil.e(TAG, "Error onPostExecute", e);
        }
    }

    public void showOrHideEmptyView(boolean show) {
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rvAdapter = new AlertViewRecyclerAdapter(items, rv, llm, getActivity());
        // register alert view providers on the adapter
        for (Class<?> vp : alertViewProviders) {
            try {
                rvAdapter.registerViewProvider((AlertViewRecyclerAdapter.AlertViewProvider) vp.newInstance(), vp);
            } catch (Exception e) {
                LogUtil.e(TAG, "setupRecyclerView: ", e);
                throw new RuntimeException(e);
            }
        }
        rv.setAdapter(rvAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
        rvListener = new AlertViewRecyclerAdapter.EventListener() {
            @Override
            public void onItemClick(View v, DailyAgendaItemStub item, int position) {

            }
        };
        rvAdapter.setListener(rvListener);
    }

    private void setupEmptyView() {
        Drawable icon = new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_emoticon_cool)
                .colorRes(R.color.agenda_item_title)
                .sizeDp(90)
                .paddingDp(0);
        ((ImageView) emptyView.findViewById(R.id.imageView_ok)).setImageDrawable(icon);
        ((TextView) emptyView.findViewById(R.id.textView3)).setText(R.string.no_alerts_detected);
    }

}