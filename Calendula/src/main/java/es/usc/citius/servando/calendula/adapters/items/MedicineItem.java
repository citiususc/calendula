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

package es.usc.citius.servando.calendula.adapters.items;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.StockModule;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.util.IconUtils;

/**
 * Created by alvaro.brey.vilas on 22/11/16.
 */

public class MedicineItem extends AbstractItem<MedicineItem, MedicineItem.MedicineViewHolder> {

    private Medicine medicine;

    public MedicineItem(Medicine m) {
        this.medicine = m;
    }


    public Medicine getMedicine() {
        return medicine;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_medicine_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.medicines_list_item;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void bindView(MedicineViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        Context ctx = holder.itemView.getContext();

        holder.name.setText(medicine.name());
        holder.icon.setImageDrawable(new IconicsDrawable(ctx)
                .icon(medicine.presentation().icon())
                .colorRes(R.color.agenda_item_title)
                .paddingDp(8)
                .sizeDp(40));

        if (ModuleManager.isEnabled(StockModule.ID)) {
            String nextPickup = medicine.nextPickup();
            holder.stockInfo.setVisibility(View.VISIBLE);

            if (nextPickup != null) {
                holder.stockInfo.setText("Próxima e-Receta: " + nextPickup);
            }

            if (medicine.stock() >= 0) {
                holder.stockInfo.setText(ctx.getString(R.string.stock_remaining_msg, medicine.stock().intValue(), medicine.presentation().units(ctx.getResources())));
            }
        }

        List<PatientAlert> alerts = DB.alerts().findBy(PatientAlert.COLUMN_MEDICINE, medicine);
        boolean hasAlerts = !alerts.isEmpty();

        if (!hasAlerts) {
            holder.alertIcon.setVisibility(View.GONE);
        } else {
            int level = PatientAlert.Level.LOW;
            for (PatientAlert a : alerts) {
                if (a.getLevel() > level) {
                    level = a.getLevel();
                }
            }
            holder.alertIcon.setImageDrawable(IconUtils.alertLevelIcon(level, ctx));
        }

    }

    //reset the view here (this is an optional method, but recommended)
    @Override
    public void unbindView(MedicineViewHolder holder) {
        super.unbindView(holder);
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageButton)
        public ImageView icon;
        @BindView(R.id.medicines_list_item_name)
        public TextView name;
        @BindView(R.id.imageView)
        public ImageView alertIcon;
        @BindView(R.id.stock_info)
        public TextView stockInfo;


        public MedicineViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
