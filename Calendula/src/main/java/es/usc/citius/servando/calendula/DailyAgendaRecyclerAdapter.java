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

package es.usc.citius.servando.calendula;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.fragments.HomeProfileMgr;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub.DailyAgendaItemStubElement;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.view.ParallaxImageView;

/**
 * Created by joseangel.pineiro on 11/6/15.
 */
public class DailyAgendaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DailyAgendaAdapter";

    private final long window;
    private final int SPACER = 1;
    private final int EMPTY = 2;
    private final int NORMAL = 3;
    List<DailyAgendaItemStub> items;
    private boolean expanded = false;
    private int parallaxHeight;
    private int emptyItemHeight;
    private boolean enableParallax = true;
    private EventListener listener;
    private Context ctx;

    public DailyAgendaRecyclerAdapter(List<DailyAgendaItemStub> items, final RecyclerView rv, final LinearLayoutManager llm, Activity ctx) {
        this.items = items;
        this.ctx = ctx.getApplicationContext();
        emptyItemHeight = ScreenUtils.dpToPx(ctx.getResources(), 45);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String delayMinutesStr = prefs.getString("alarm_reminder_window", "60");
        window = Long.parseLong(delayMinutesStr);

        Display display = ctx.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        parallaxHeight = size.y * 2;

        if (enableParallax) {
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    updateParallax(llm, recyclerView);
                }
            });
        }
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case NORMAL:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_view_intake, parent, false);
                return new NormalItemViewHolder(v);
            case SPACER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_view_empty_dayspacer, parent, false);
                return new SpacerItemViewHolder(v);
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_view_empty_hour, parent, false);
                return new EmptyItemViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        DailyAgendaItemStub item = items.get(position);
        int type = EMPTY;
        if (item.hasEvents) {
            type = NORMAL;
        } else if (item.isSpacer) {
            return SPACER;
        }
        return type;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final DailyAgendaItemStub item = items.get(position);

        if (holder instanceof SpacerItemViewHolder) {
            onBindViewSpacerItemViewHolder((SpacerItemViewHolder) holder, item, position);
        } else if (holder instanceof NormalItemViewHolder) {
            onBindNormalItemViewHolder((NormalItemViewHolder) holder, item, position);
        } else {
            onBindEmptyItemViewHolder((EmptyItemViewHolder) holder, item, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void onBindViewSpacerItemViewHolder(SpacerItemViewHolder holder, DailyAgendaItemStub item, int position) {

        if (expanded) {
            int color = HomeProfileMgr.colorForCurrent(ctx);

            String title;
            if (item.date.equals(LocalDate.now())) {
                title = ctx.getString(R.string.today);
            } else if (item.date.equals(LocalDate.now().minusDays(1))) {
                title = ctx.getString(R.string.yesterday);
            } else if (item.date.equals(LocalDate.now().plusDays(1))) {
                title = ctx.getString(R.string.tomorrow);
            } else {
                title = item.date.toString("EEEE dd");
            }

            holder.dayBg.setBackgroundColor(color);
            holder.day.setVisibility(View.VISIBLE);
            holder.day.setText(title);
            holder.parallax.updateParallax();
        }

        holder.itemView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        int newHeight = expanded ? ScreenUtils.dpToPx(holder.itemView.getResources(), 80) : 0;

        if (params.height != newHeight) {
            params.height = newHeight;
            holder.itemView.setLayoutParams(params);
        }
    }

    public void onBindEmptyItemViewHolder(EmptyItemViewHolder viewHolder, DailyAgendaItemStub item, int position) {
        viewHolder.stub = item;

        if (expanded) {
            LocalDate d = viewHolder.stub.date;
            if (d.equals(DateTime.now().toLocalDate())) {
                viewHolder.hourText.setText(item.time != null ? item.time.toString("kk:mm") : "--");
            } else {
                viewHolder.hourText.setText(item.dateTime().toString("kk:mm"));
            }
        }
        viewHolder.itemView.setVisibility(expanded ? View.VISIBLE : View.GONE);

        ViewGroup.LayoutParams params = viewHolder.container.getLayoutParams();
        int newHeight = expanded ? emptyItemHeight : 0;
        if (params.height != newHeight) {
            params.height = newHeight;
            viewHolder.container.setLayoutParams(params);
        }
    }

    public void onBindNormalItemViewHolder(NormalItemViewHolder viewHolder, DailyAgendaItemStub item, int i) {

        viewHolder.stub = item;
        item.displayable = isDisplayable(item);

        if (item.displayable) {

            if (!item.isRoutine) {
                viewHolder.itemTypeIcon.setImageResource(R.drawable.ic_history_black_48dp);
            } else {
                viewHolder.itemTypeIcon.setImageResource(R.drawable.ic_alarm_black_48dp);
            }

            if (item.patient != null) {
                viewHolder.avatarIcon.setImageResource(AvatarMgr.res(item.patient.avatar()));
                viewHolder.patientIndicatorBand.setBackgroundColor(item.patient.color());
            }

            viewHolder.title.setText(item.title);
            viewHolder.hour.setText(item.time.toString("kk") + ":");
            viewHolder.minute.setText(item.time.toString("mm"));

            boolean allTaken = addMeds(viewHolder, item);

            if (allTaken) {
                viewHolder.takenOverlay.setVisibility(View.VISIBLE);
                viewHolder.actionsView.setVisibility(View.GONE);
            } else {
                viewHolder.takenOverlay.setVisibility(View.GONE);
                if (isAvailable(item)) {
                    viewHolder.actionsView.animate().alpha(1).scaleX(1f).scaleY(1f).setStartDelay(500);
                    viewHolder.actionsView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.actionsView.setVisibility(View.GONE);
                }
            }
        }

        viewHolder.itemView.setVisibility(item.displayable ? View.VISIBLE : View.GONE);

        ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();
        int newHeight = item.displayable ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
        if (params.height != newHeight) {
            params.height = newHeight;
            viewHolder.itemView.setLayoutParams(params);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isShowingSomething() {

        Log.d(TAG, "isShowingSomething, expanded: " + expanded);

        if (expanded && items.size() > 0)
            return true;

        boolean result = false;
        for (DailyAgendaItemStub item : items) {
            if (isDisplayable(item)) {
                Log.d(TAG, "Item is displayable: " + item.toString());
                result = true;
            }
        }

        return result;
    }

    public void toggleCollapseMode() {
        Log.d("RVAdapter", "toggleCollapseMode");
        expanded = !expanded;

        boolean willSHowSomething = isShowingSomething();

        if (listener != null) {
            listener.onBeforeToggleCollapse(expanded, willSHowSomething);
        }

        for (int i = 0; i < items.size(); i++) {
            notifyItemChanged(i);
        }

        if (listener != null) {
            listener.onAfterToggleCollapse(expanded, willSHowSomething);
        }
    }

    public void updatePosition(int position) {
        updateItem(position);
    }

    boolean isAvailable(DailyAgendaItemStub stub) {
        return isAvailable(stub.dateTime());
    }

    boolean isDisplayable(DailyAgendaItemStub stub) {
        DateTime t = stub.dateTime();
        DateTime midnight = DateTime.now().withTimeAtStartOfDay().plusDays(1);
        return stub.hasEvents && (isAvailable(stub) || expanded || (t.isAfterNow() && t.isBefore(midnight)));
    }

    boolean isAvailable(DateTime time) {
        DateTime now = DateTime.now();
        return time.isBefore(now) && time.plusMillis((int) window * 60 * 1000).isAfter(now);
    }

    void updateParallax(LinearLayoutManager lm, RecyclerView rv) {

        if (!expanded) {
            return;
        }

        int start = lm.findFirstVisibleItemPosition();
        int end = lm.findLastVisibleItemPosition();

        for (int i = start; i < end; i++) {
            RecyclerView.ViewHolder h = rv.findViewHolderForAdapterPosition(i);
            if (h instanceof SpacerItemViewHolder) {
                ((SpacerItemViewHolder) h).parallax.updateParallax();
            }
        }
    }

    private boolean addMeds(NormalItemViewHolder viewHolder, DailyAgendaItemStub item) {

        boolean allTaken = true;

        viewHolder.medList.removeAllViews();

        for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {

            View intakeView = viewHolder.inflater.inflate(R.layout.daily_view_intake_med, null);
            TextView medName = (TextView) intakeView.findViewById(R.id.med_item_name);
            TextView medDose = (TextView) intakeView.findViewById(R.id.med_item_dose);
            ImageView image = (ImageView) intakeView.findViewById(R.id.imageView);

            String units = element.presentation.units(viewHolder.context.getResources());
            image.setImageDrawable(medIcon(element.presentation.icon(), intakeView.getContext()));
            medDose.setText(element.displayDose + " " + units + (element.dose > 1 ? "s" : ""));
            medName.setText(element.medName);

            if (element.taken) {
                intakeView.findViewById(R.id.ic_done).setVisibility(View.VISIBLE);
            } else {
                allTaken = false;
                intakeView.findViewById(R.id.ic_done).setVisibility(View.INVISIBLE);
            }
            viewHolder.medList.addView(intakeView);
        }

        return allTaken;
    }

    private Drawable medIcon(IIcon icon, Context ctx) {
        return new IconicsDrawable(ctx)
                .icon(icon)
                .colorRes(R.color.white)
                .sizeDp(24)
                .paddingDp(0);
    }

    private void updateStub(DailyAgendaItemStub stub) {
        if (!stub.isRoutine) {
            Schedule s = DB.schedules().findById(stub.id);
            DailyScheduleItem dsi = DB.dailyScheduleItems().findBy(s, stub.date, stub.time);
            stub.meds.get(0).taken = dsi.takenToday();
        } else {
            for (DailyAgendaItemStubElement el : stub.meds) {
                ScheduleItem si = DB.scheduleItems().findById(el.scheduleItemId);
                DailyScheduleItem dsi = DB.dailyScheduleItems().findByScheduleItemAndDate(si, stub.date);
                el.taken = dsi.takenToday();
            }
        }
    }

    private void updateItem(int position) {
        if (position < items.size() && position > -1) {
            DailyAgendaItemStub stub = items.get(position);
            updateStub(stub);
            notifyItemChanged(position);
        }
    }

    public interface EventListener {
        void onItemClick(View v, DailyAgendaItemStub item, int position);

        void onBeforeToggleCollapse(boolean expanded, boolean somethingVisible);

        void onAfterToggleCollapse(boolean expanded, boolean somethingVisible);
    }

    public static class EmptyItemViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout container;
        TextView hourText;
        DailyAgendaItemStub stub;

        EmptyItemViewHolder(View itemView) {
            super(itemView);
            hourText = (TextView) itemView.findViewById(R.id.hour_text);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
        }
    }

    public class SpacerItemViewHolder extends RecyclerView.ViewHolder {

        TextView day;
        ImageView dayBg;
        View container;
        ParallaxImageView parallax;

        SpacerItemViewHolder(View itemView) {
            super(itemView);
            day = (TextView) itemView.findViewById(R.id.day_text);
            dayBg = (ImageView) itemView.findViewById(R.id.day_bg);
            container = itemView.findViewById(R.id.container);
            parallax = (ParallaxImageView) itemView.findViewById(R.id.parallax_bg);

            ViewGroup.LayoutParams layoutParams = parallax.getLayoutParams();
            layoutParams.height = parallaxHeight;
            parallax.setLayoutParams(layoutParams);
        }
    }

    public class NormalItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        Context context;
        DailyAgendaItemStub stub;

        LayoutInflater inflater;
        LinearLayout medList;
        ImageView itemTypeIcon;
        ImageView avatarIcon;
        ImageView patientIndicatorBand;

        TextView title;
        TextView hour;
        TextView minute;

        View arrow;
        View top;
        View bottom;

        View takenOverlay;

        View actionsView;
        ImageButton checkAll;

        public NormalItemViewHolder(View itemView) {
            super(itemView);

            this.context = itemView.getContext();
            this.inflater = LayoutInflater.from(itemView.getContext());
            this.medList = (LinearLayout) itemView.findViewById(R.id.med_item_list);
            this.itemTypeIcon = (ImageView) itemView.findViewById(R.id.imageButton2);
            this.avatarIcon = (ImageView) itemView.findViewById(R.id.patient_avatar);
            this.title = (TextView) itemView.findViewById(R.id.routines_list_item_name);
            this.hour = (TextView) itemView.findViewById(R.id.routines_list_item_hour);
            this.minute = (TextView) itemView.findViewById(R.id.routines_list_item_minute);
            this.arrow = itemView.findViewById(R.id.count_container);
            this.top = itemView.findViewById(R.id.routine_list_item_container);
            this.bottom = itemView.findViewById(R.id.bottom);
            this.patientIndicatorBand = (ImageView) itemView.findViewById(R.id.patient_indicator_band);
            this.takenOverlay = itemView.findViewById(R.id.taken_overlay);

            this.actionsView = itemView.findViewById(R.id.action_container);
            this.checkAll = (ImageButton) itemView.findViewById(R.id.check_all_button);

            this.checkAll.setImageDrawable(new IconicsDrawable(context)
                    .colorRes(R.color.white) //agenda_item_title
                    .icon(CommunityMaterial.Icon.cmd_check_all) //cmd_arrow_right_bold
                    .paddingDp(0)
                    .sizeDp(28)
            );

            actionsView.setOnClickListener(this);
            top.setOnClickListener(this);
            arrow.setOnClickListener(this);
            itemView.setOnClickListener(this);
            checkAll.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d("Recycler", "Click row, listener is null? " + (listener == null));

            if (view.getId() == R.id.check_all_button || view.getId() == R.id.action_container) {

                List<DailyScheduleItem> dailyScheduleItems = new ArrayList<>();
                if (stub.isRoutine) {
                    List<ScheduleItem> rsi = Routine.findById(stub.id).scheduleItems();
                    for (ScheduleItem si : rsi) {
                        DailyScheduleItem dsi = DB.dailyScheduleItems().findByScheduleItemAndDate(si, stub.date);
                        if (dsi != null)
                            dailyScheduleItems.add(dsi);
                    }
                } else {
                    Schedule s = Schedule.findById(stub.id);
                    dailyScheduleItems.add(DB.dailyScheduleItems().findBy(s, stub.date, stub.time));
                }

                for (DailyScheduleItem item : dailyScheduleItems) {
                    item.setTakenToday(true);
                    DB.dailyScheduleItems().saveAndUpdateStock(item, false, context);
                }

                if (stub.isRoutine) {
                    AlarmScheduler.instance().onIntakeCompleted(DB.routines().findById(stub.id), stub.date, context);
                } else {
                    AlarmScheduler.instance().onIntakeCompleted(DB.schedules().findById(stub.id), stub.time, stub.date, context);
                }

                actionsView.animate().alpha(0).scaleX(0.5f).scaleY(0.5f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        updateItem(getAdapterPosition());
                    }
                });

            } else if (listener != null) {
                listener.onItemClick(view, stub, getAdapterPosition());
            }
        }
    }

}
