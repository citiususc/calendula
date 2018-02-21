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


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.StockModule;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.stock.MedicineScheduleStockProvider;
import es.usc.citius.servando.calendula.util.stock.StockCalculator;
import es.usc.citius.servando.calendula.util.stock.StockDisplayUtils;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;


/**
 * Created by joseangel.pineiro
 */
public class MedInfoFragment extends Fragment {

    @BindView(R.id.ic_prospect)
    ImageView icProspect;
    @BindView(R.id.ic_med_large_name)
    ImageView icMedName;
    @BindView(R.id.ic_schedule_info)
    ImageView icScheduleInfo;
    @BindView(R.id.ic_med_stock)
    ImageView icMedStock;

    @BindView(R.id.med_large_name)
    TextView medName;
    @BindView(R.id.med_large_name_description)
    TextView medDesc;
    @BindView(R.id.schedule_info)
    TextView scheduleInfo;
    @BindView(R.id.med_stock)
    TextView stockInfo;
    @BindView(R.id.med_stock_readable)
    TextView stockInfoEnd;
    @BindView(R.id.med_leaflet_butn)
    ImageButton showProspectBtn;
    @BindView(R.id.ic_show_prospect)
    ImageView showProspectIcon;

    @BindView(R.id.bind_medicine)
    Button bindMedBtn;

    @BindView(R.id.stock_layout)
    RelativeLayout stockLayout;

    Medicine m;
    PrescriptionDBMgr dbMgr;

    Unbinder unbinder;

    public static MedInfoFragment newInstance(Medicine m) {
        MedInfoFragment fragment = new MedInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("medicine_id", m.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMgr = DBRegistry.instance().current();
        if (getArguments() != null) {
            m = DB.medicines().findById(getArguments().getLong("medicine_id", -1));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_med_info, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupView();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    public void notifyDataChange() {
        DB.medicines().refresh(m);
        setupView();
    }

    private void setupView() {
        Context c = getActivity();
        int color = R.color.black;
        icMedName.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_script, color, 24, 4));
        icProspect.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_information, color, 24, 4));
        icScheduleInfo.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_calendar, color, 24, 4));
        icMedStock.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_basket, color, 24, 4));

        Drawable ic = new IconicsDrawable(c, CommunityMaterial.Icon.cmd_file_document)
                .sizeDp(60)
                .paddingDp(0)
                .color(DB.patients().getActive(c).getColor());

        showProspectIcon.setImageDrawable(ic);


        String name = "";
        String desc = "";
        if (m != null) {
            if (m.isBoundToPrescription()) {
                Prescription p = DB.drugDB().prescriptions().findByCn(m.getCn());
                if (p != null) {
                    name += getNameWhyNot(p) + "\n";
                    desc += "CN - " + p.getCode() + "\n";
                    desc += "" + p.getContent() + "\n";
                    desc += "" + p.getDose();
                }
            } else {
                desc = getString(R.string.message_link_real_prescription);
                name += m.getName();
            }
        }

        medName.setText(name);
        medDesc.setText(desc);

        int scheduleCount = DB.schedules().findByMedicine(m).size();

        if (scheduleCount == 0) {
            scheduleInfo.setText(R.string.active_schedules_none);
        } else if (scheduleCount == 1) {
            scheduleInfo.setText(R.string.active_schedules_one);
        } else {
            scheduleInfo.setText(getString(R.string.active_schedules_number, scheduleCount));
        }

        if (m.isBoundToPrescription()) {
            showProspectBtn.setAlpha(1f);
            showProspectIcon.setAlpha(1f);
            bindMedBtn.setVisibility(View.GONE);
            showProspectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProspectUtils.openProspect(DB.drugDB().prescriptions().findByCn(m.getCn()), getActivity(), true);
                }
            });
        } else {
            showProspectBtn.setAlpha(0.8f);
            showProspectIcon.setAlpha(0.3f);
            bindMedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), MedicinesActivity.class);
                    intent.putExtra(MedicinesActivity.EXTRA_SEARCH_TEXT, m.getName());
                    intent.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, m.getId());
                    startActivity(intent);
                }
            });
        }

        if (ModuleManager.isEnabled(StockModule.ID)) {
            stockLayout.setVisibility(View.VISIBLE);
            if (m.stockManagementEnabled()) {
                final Float s = m.getStock();
                final String stock = s.intValue() == s ? String.valueOf(s.intValue()) : String.valueOf(s);
                stockInfo.setText(stock + " " + m.getPresentation().units(getResources(), s));
                final StockCalculator.StockEnd stockEnd = StockCalculator.calculateStockEnd(LocalDate.now(), new MedicineScheduleStockProvider(m), m.getStock());
                String msg = StockDisplayUtils.getReadableStockDuration(stockEnd, getContext());
                stockInfoEnd.setText(msg);
            } else {
                stockInfo.setText(R.string.stock_no_data);
                stockInfoEnd.setText(R.string.stock_no_stock_info);
            }
        }

    }

    private String getNameWhyNot(Prescription p) {

        String dose = p.getDose();
        String originalName = p.getName();
        String doseFirstPart = dose.contains(" ") ? dose.split(" ")[0] : dose;

        if (doseFirstPart != null && originalName.contains(doseFirstPart)) {
            int index = originalName.indexOf(doseFirstPart);
            return originalName.substring(0, index);
        }
        return originalName;
    }
}