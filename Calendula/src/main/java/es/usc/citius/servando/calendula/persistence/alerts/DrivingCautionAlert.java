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

package es.usc.citius.servando.calendula.persistence.alerts;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.AlertViewRecyclerAdapter;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

/**
 * Represents an stock alert for an specific medicine
 */
public class DrivingCautionAlert extends PatientAlert<DrivingCautionAlert, DrivingCautionAlert> {

    // mandatory no-arg constructor
    public DrivingCautionAlert() {
        super();
    }

    /**
     * Creates an driving caution alert
     * @param m The medicine
     */
    public DrivingCautionAlert(Medicine m){
        super();
        setPatient(m.patient());
        setMedicine(m);
        setType(DrivingCautionAlert.class.getCanonicalName());
        setLevel(Level.LOW);
    }


    @Override
    public Class<?> getDetailsType(){
        // we don't want to store more info for this alert
        return null;
    }

    @Override
    public Class<?> viewProviderType() {
        // the view of this alert type will be created by an
        // instance of DrivingAlertViewProvider class
        return DrivingCautionAlert.DrivingAlertViewProvider.class;
    }


    /**
     * Provides a mechanism for creating driving alert views
     */
    public static class DrivingAlertViewProvider implements AlertViewRecyclerAdapter.AlertViewProvider {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medicine_alert_list_item, parent, false);
            return new DrivingAlertViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, PatientAlert alert) {
            DrivingAlertViewHolder viewHolder = (DrivingAlertViewHolder) holder;
            viewHolder.alert = alert;
            Drawable icon = new IconicsDrawable(viewHolder.context)
                    .icon(CommunityMaterial.Icon.cmd_car)
                    .colorRes(R.color.android_blue_dark)
                    .sizeDp(24)
                    .paddingDp(4);

            viewHolder.alertIcon.setImageDrawable(icon);
            viewHolder.title.setText("Sea prudente al volante");
            viewHolder.description.setText("Es posible que este medicamente afecte a su capacidad para conducir");
        }

        public static class DrivingAlertViewHolder extends RecyclerView.ViewHolder{

            Context context;
            PatientAlert alert;
            LayoutInflater inflater;
            ImageView alertIcon;
            TextView title;
            TextView description;

            public DrivingAlertViewHolder(View itemView) {
                super(itemView);
                this.context = itemView.getContext();
                this.inflater = LayoutInflater.from(itemView.getContext());
                this.title = (TextView) itemView.findViewById(R.id.alert_title);
                this.description = (TextView) itemView.findViewById(R.id.alert_description);
                this.alertIcon = (ImageView) itemView.findViewById(R.id.alert_icon);
            }
        }

    }



}
