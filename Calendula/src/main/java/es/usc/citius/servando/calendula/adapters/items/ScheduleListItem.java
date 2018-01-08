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
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by alvaro.brey.vilas on 22/11/16.
 */

public class ScheduleListItem extends AbstractItem<ScheduleListItem, ScheduleListItem.ScheduleViewHolder> {

    private static final String TAG = "ScheduleListItem";
    private final Schedule schedule;

    public ScheduleListItem(Schedule s) {
        this.schedule = s;
    }


    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_schedule_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.schedules_list_item;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void bindView(ScheduleViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        Context ctx = holder.itemView.getContext();
        String timeStr;
        List<ScheduleItem> items = schedule.items();

        if (schedule.type() != Schedule.SCHEDULE_TYPE_HOURLY) {
            timeStr = ScheduleUtils.getTimesStr(items != null ? items.size() : 0, ctx);
        } else {
            timeStr = ScheduleUtils.getTimesStr(24 / schedule.rule().getInterval(), ctx);
        }

        LogUtil.d(TAG, "Schedule " + schedule.medicine().getName() + " is scanned: " + schedule.scanned());
        String auto = schedule.scanned() ? " ↻" : "";

        holder.icon2.setImageDrawable(new IconicsDrawable(ctx)
                .icon(schedule.medicine().getPresentation().icon())
                .color(Color.WHITE)
                .paddingDp(8)
                .sizeDp(40));

        IIcon i = schedule.repeatsHourly() ? CommunityMaterial.Icon.cmd_history : CommunityMaterial.Icon.cmd_clock;

        holder.icon.setImageDrawable(new IconicsDrawable(ctx)
                .icon(i)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(8)
                .sizeDp(40));

        holder.medName.setText(schedule.medicine().getName() + auto);
        holder.itemTimes.setText(timeStr);
        holder.itemDays.setText(schedule.toReadableString(ctx));

    }

    @Override
    public void unbindView(ScheduleViewHolder holder) {
        super.unbindView(holder);
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageButton)
        ImageView icon;
        @BindView(R.id.imageView)
        ImageView icon2;
        @BindView(R.id.schedules_list_item_medname)
        TextView medName;
        @BindView(R.id.schedules_list_item_times)
        TextView itemTimes;
        @BindView(R.id.schedules_list_item_days)
        TextView itemDays;

        public ScheduleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
