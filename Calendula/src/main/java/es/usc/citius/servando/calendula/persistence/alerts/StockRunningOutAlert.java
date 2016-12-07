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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.persistence.alerts;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.LocalDate;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.adapters.AlertViewRecyclerAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.medicine.StockUtils;

/**
 * Represents an stock alert for an specific medicine
 */
public class StockRunningOutAlert extends PatientAlert<StockRunningOutAlert, StockRunningOutAlert.StockAlertInfo> {

    // mandatory no-arg constructor
    public StockRunningOutAlert() {
        super();
    }

    /**
     * Create an instance of an stock alert
     *
     * @param m    Medicine whose stock is running out
     * @param date Date the alert is created
     */
    public StockRunningOutAlert(Medicine m, LocalDate date) {
        super();
        setPatient(m.patient());
        setMedicine(m);
        setType(StockRunningOutAlert.class.getCanonicalName());
        setLevel(Level.MEDIUM);
        setDetails(new StockAlertInfo(date));
    }

    @Override
    public Class<?> getDetailsType() {
        return StockAlertInfo.class;
    }

    @Override
    public Class<?> viewProviderType() {
        // the view of this type of alerts will be created by
        // an instance of StockAlertViewProvider class
        return StockRunningOutAlert.StockAlertViewProvider.class;
    }

    /**
     * Envelope for details of stock alert instances
     */
    public static class StockAlertInfo {

        /**
         * Date of the stock alert
         */
        private LocalDate date;

        public StockAlertInfo() {
        }

        public StockAlertInfo(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }


    /**
     * Handles the creation of views for stock alerts
     */
    public static class StockAlertViewProvider implements AlertViewRecyclerAdapter.AlertViewProvider {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_alert_list_item, parent, false);
            return new StockAlertViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, PatientAlert alert) {

            StockAlertViewHolder viewHolder = (StockAlertViewHolder) holder;
            viewHolder.alert = alert;
            final Medicine m = alert.getMedicine();
            final Context c = viewHolder.context;
            DB.medicines().refresh(m);
            int stock = m.stock().intValue();
            // setup ui
            viewHolder.alertIcon.setImageDrawable(IconUtils.alertLevelIcon(alert.getLevel(), c));
            viewHolder.title.setText(R.string.stock_running_out);
            final Context ctx = viewHolder.itemView.getContext();
            viewHolder.description.setText(ctx.getString(R.string.stock_remaining_msg, stock, m.presentation().units(c.getResources())));
            viewHolder.duration.setText(ctx.getString(R.string.stock_enough_for_days, StockUtils.getEstimatedStockDays(m)));
            viewHolder.manageStockBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(c, MedicinesActivity.class);
                    intent.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, m.getId());
                    intent.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, "add_stock");
                    c.startActivity(intent);
                }
            });
        }

        public static class StockAlertViewHolder extends RecyclerView.ViewHolder {

            Context context;
            PatientAlert alert;
            LayoutInflater inflater;
            ImageView alertIcon;
            TextView title;
            TextView description;
            TextView duration;
            Button manageStockBtn;

            public StockAlertViewHolder(View itemView) {
                super(itemView);
                this.context = itemView.getContext();
                this.inflater = LayoutInflater.from(itemView.getContext());
                this.title = (TextView) itemView.findViewById(R.id.alert_title);
                this.description = (TextView) itemView.findViewById(R.id.alert_description);
                this.duration = (TextView) itemView.findViewById(R.id.stock_duration);
                this.alertIcon = (ImageView) itemView.findViewById(R.id.alert_icon);
                this.manageStockBtn = (Button) itemView.findViewById(R.id.manage_stock_btn);
            }
        }

    }


}
