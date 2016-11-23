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

package es.usc.citius.servando.calendula.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import org.joda.time.LocalDate;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.medicine.StockUtils;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;


/**
 * Created by joseangel.pineiro
 */
public class MedInfoFragment extends Fragment{

    ImageView icProspect;
    ImageView icMedName;
    ImageView icScheduleInfo;
    ImageView icMedStock;

    TextView medName;
    TextView medDesc;
    TextView scheduleInfo;
    TextView stockInfo;
    TextView stockInfoEnd;
    Button showProspectBtn;

    Medicine m;
    PrescriptionDBMgr dbMgr;

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
        if(getArguments() != null){
            m = DB.medicines().findById(getArguments().getLong("medicine_id",-1));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_med_info, container, false);
        icMedName = (ImageView) rootView.findViewById(R.id.ic_med_large_name);
        icProspect = (ImageView) rootView.findViewById(R.id.ic_prospect);
        icScheduleInfo = (ImageView) rootView.findViewById(R.id.ic_schedule_info);
        icMedStock = (ImageView) rootView.findViewById(R.id.ic_med_stock);
        medName = (TextView) rootView.findViewById(R.id.med_large_name);
        medDesc = (TextView) rootView.findViewById(R.id.med_large_name_description);
        scheduleInfo = (TextView) rootView.findViewById(R.id.schedule_info);
        stockInfo = (TextView) rootView.findViewById(R.id.med_stock);
        stockInfoEnd = (TextView) rootView.findViewById(R.id.med_stock_readable);
        showProspectBtn = (Button) rootView.findViewById(R.id.med_leaflet_butn);
        setupView();

        if(m.isBoundToPrescription()) {
            showProspectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProspectUtils.openProspect(DB.drugDB().prescriptions().findByCn(m.cn()), getActivity(), true);
                }
            });
        }else{
            showProspectBtn.setText("Prospecto no disponible");
        }

        return rootView;
    }

    private void setupView() {
        Context c = getActivity();
        int color = R.color.black;
        icMedName.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_information,color , 24, 4));
        icProspect.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_file_document, color, 24, 4));
        icScheduleInfo.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_calendar, color, 24, 4));
        icMedStock.setImageDrawable(IconUtils.icon(c, CommunityMaterial.Icon.cmd_basket, color, 24, 4));

        String name = "";
        String desc = "";
        if( m != null ){
            if(m.isBoundToPrescription()){
                Prescription p = DB.drugDB().prescriptions().findByCn(m.cn());
                if(p != null){
                    name += getNameWhyNot(p) + "\n";
                    desc += "CN - " + p.getCode() + "\n";
                    desc += "" + p.getContent() + "\n";
                    desc += "" + p.getDose();
                }
            }else{
                medDesc.setVisibility(View.GONE);
                name += m.name();
            }
        }

        medName.setText(name);
        medDesc.setText(desc);

        int scheduleCount = DB.schedules().findByMedicine(m).size();

        if(scheduleCount == 0){
            scheduleInfo.setText("Sin pautas activas");
        }else if(scheduleCount == 1){
            scheduleInfo.setText(scheduleCount + " pauta activa");
        }else{
            scheduleInfo.setText(scheduleCount + " pautas activas");
        }

        if(m.stockManagementEnabled()){
            stockInfo.setText(m.stock() + " " + m.presentation().units(getResources()));
            LocalDate d = StockUtils.getEstimatedStockEnd(m);
            String msg = StockUtils.getReadableStockDuration(d);
            stockInfoEnd.setText(msg);
        }else{
            stockInfo.setText("Sin datos");
            stockInfoEnd.setText("No se ha indicado información de stock para este medicamento");
        }

    }


    private String getNameWhyNot(Prescription p){

        String dose = p.getDose();
        String originalName = p.getName();
        String doseFirstPart = dose.contains(" ") ? dose.split(" ")[0] : dose;


        if(doseFirstPart != null && originalName.contains(doseFirstPart)){
            int index = originalName.indexOf(doseFirstPart);
            return originalName.substring(0, index);
        }
        return originalName;
    }
}