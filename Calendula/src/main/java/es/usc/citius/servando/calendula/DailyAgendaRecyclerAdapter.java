package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.fragments.HomeProfileMgr;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.view.CustomDigitalClock;

/**
 * Created by joseangel.pineiro on 11/6/15.
 */
public class DailyAgendaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean expanded = false;


    public interface AgendaItemClickListener{
        void onClick(View v, DailyAgendaItemStub item, int position);
    }

    private final int SPACER = 1;
    private final int EMPTY = 2;
    private final int NORMAL = 3;

    private int lastPosition = -1;

    List<DailyAgendaItemStub> items;
    private AgendaItemClickListener listener;

    public DailyAgendaRecyclerAdapter(List<DailyAgendaItemStub> items) {
        this.items = items;
    }

    public void setListener(AgendaItemClickListener listener) {
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_view_empty_placeholder, parent, false);
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
        }else if (item.isEmptyPlaceholder){
            return SPACER;
        }

        Log.d("Recycle", "Item type at position " + position + ": " + type + ", " + item.toString());

        return type;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final DailyAgendaItemStub item = items.get(position);
        final int type = holder.getItemViewType();

        Log.d("Recycler", "onBindViewHolder at position " + position + ": " + type + ", " + item.toString());

        if(holder instanceof SpacerItemViewHolder) {
            Log.d("Recycler", "onBindViewHolder1");
            onBindViewSpacerItemViewHolder((SpacerItemViewHolder) holder, item, position);
        }
        else if (holder instanceof NormalItemViewHolder) {
            Log.d("Recycler", "onBindViewHolder2");
            onBindNormalItemViewHolder((NormalItemViewHolder) holder, item, position);
        }
        else{
            Log.d("Recycler", "onBindViewHolder3");
            onBindEmptyItemViewHolder((EmptyItemViewHolder) holder, item, position);
        }

        holder.itemView.setTag(String.valueOf(item.hour));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SpacerItemViewHolder extends RecyclerView.ViewHolder {

        SpacerItemViewHolder(View itemView) {
            super(itemView);
        }

    }

    public static class EmptyItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        RelativeLayout container;
        TextView hourText;
        DailyAgendaItemStub stub;
        FrameLayout clcokContainer;
        View currentHourLine;

        EmptyItemViewHolder(View itemView) {
            super(itemView);
            hourText = (TextView) itemView.findViewById(R.id.hour_text);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
            clcokContainer = (FrameLayout) itemView.findViewById(R.id.clock_container);
            currentHourLine = itemView.findViewById(R.id.current_hour_line);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            Log.d("Recycler", "OnRcycleViewItemClick" + stub.toString());
        }
    }

    public class NormalItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

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
        //View takenNowHint;

        View actionsView;
        ImageButton checkAll;

        RotateAnimation rotateAnimUp;
        RotateAnimation rotateAnimDown;

        public NormalItemViewHolder(View itemView) {
            super(itemView);

            this.context = itemView.getContext();
            this.inflater = LayoutInflater.from(itemView.getContext());
            this.medList = (LinearLayout) itemView.findViewById(R.id.med_item_list);
            this.itemTypeIcon = (ImageView) itemView.findViewById(R.id.imageButton2);
            this.avatarIcon =  (ImageView) itemView.findViewById(R.id.patient_avatar);
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
                            .colorRes(R.color.agenda_item_title)
                            .icon(CommunityMaterial.Icon.cmd_check_all) //cmd_arrow_right_bold
                            .paddingDp(0)
                            .sizeDp(28)
            );

            top.setOnClickListener(this);
            arrow.setOnClickListener(this);
            itemView.setOnClickListener(this);
            checkAll.setOnClickListener(this);

            rotateAnimUp = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimUp.setInterpolator(new DecelerateInterpolator());
            rotateAnimUp.setDuration(300);
            rotateAnimUp.setFillAfter(true);

            rotateAnimDown = new RotateAnimation(0, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimDown.setInterpolator(new DecelerateInterpolator());
            rotateAnimDown.setDuration(300);
            rotateAnimDown.setFillAfter(true);
        }

        @Override
        public void onClick(View view) {
            Log.d("Recycler", "Click row, listener is null? " + (listener == null));

            if(view.getId() == R.id.check_all_button){

                List<DailyScheduleItem> sitems = new ArrayList<>();
                if(stub.isRoutine){
                    List<ScheduleItem> rsi = ScheduleUtils.getRoutineScheduleItems(Routine.findById(stub.id), true);
                    for(ScheduleItem si : rsi){
                        sitems.add(DailyScheduleItem.findByScheduleItem(si));
                    }
                }else{

                    Schedule s = Schedule.findById(stub.id);
                    sitems.add(DB.dailyScheduleItems().findByScheduleAndTime(s,stub.time));
                }

                for (DailyScheduleItem item : sitems) {
                    item.setTakenToday(true);
                    item.save();
                }
                updateItem(getAdapterPosition(), context);

            }else if(listener != null){
                listener.onClick(view, stub, getAdapterPosition());
            }
        }
    }

    public void onBindViewSpacerItemViewHolder(SpacerItemViewHolder holder, DailyAgendaItemStub item, int position) {
        // do nothing
        View background = holder.itemView.findViewById(R.id.top);
        background.setBackgroundColor(HomeProfileMgr.colorForCurrent(holder.itemView.getContext()));
    }

    public void onBindEmptyItemViewHolder(EmptyItemViewHolder viewHolder, DailyAgendaItemStub item, int position) {

        Resources r = viewHolder.container.getResources();
        viewHolder.stub = item;

        DateTime hourStart = viewHolder.stub.time.toDateTimeToday().withMinuteOfHour(0);
        DateTime hourEnd = hourStart.plusHours(1);
        Interval hour = new Interval(hourStart, hourEnd);
        boolean isCurrentHour = hour.contains(DateTime.now());

        if(isCurrentHour && expanded) {
            Context ctx = viewHolder.container.getContext();
            int layout = R.layout.empty_intake_current_time;
            CustomDigitalClock c = (CustomDigitalClock) LayoutInflater.from(ctx).inflate(layout, null);
            c.setShowSeconds(true);
            viewHolder.clcokContainer.addView(c);
            viewHolder.hourText.setVisibility(View.GONE);
            viewHolder.currentHourLine.setVisibility(View.VISIBLE);
        } else if(expanded){
            viewHolder.clcokContainer.removeAllViews();
            viewHolder.hourText.setVisibility(View.VISIBLE);
            viewHolder.currentHourLine.setVisibility(View.GONE);
            viewHolder.hourText.setText(item.time != null ? item.time.toString("kk:mm") : "--");
        } else{
            viewHolder.clcokContainer.removeAllViews();
        }

        ViewGroup.LayoutParams params = viewHolder.container.getLayoutParams();
        params.height = expanded ? ScreenUtils.dpToPx(r, 45) : 0;
        viewHolder.container.setLayoutParams(params);
    }

    public void onBindNormalItemViewHolder(NormalItemViewHolder viewHolder, DailyAgendaItemStub item, int i) {

        Log.d("Recycler", "OnBindNormalItem" + i );
        viewHolder.stub = item;

        DateTime t = viewHolder.stub.time.toDateTimeToday();
        boolean available = AlarmScheduler.isWithinDefaultMargins(t,viewHolder.context);
        boolean displayable = available || expanded || t.isAfterNow();

        if(displayable) {

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
            viewHolder.hour.setText((item.hour > 9 ? item.hour : "0" + item.hour) + ":");
            viewHolder.minute.setText((item.minute > 9 ? item.minute : "0" + item.minute) + "");

            boolean allTaken = addMeds(viewHolder, item);

            if (allTaken) {
                viewHolder.takenOverlay.setVisibility(View.VISIBLE);
                viewHolder.actionsView.setVisibility(View.GONE);
            } else {
                viewHolder.takenOverlay.setVisibility(View.GONE);
                if (available) {
                    viewHolder.actionsView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.actionsView.setVisibility(View.GONE);
                }
            }
        }

        ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();
        params.height = displayable ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
        viewHolder.itemView.setLayoutParams(params);

    }

    private boolean addMeds(NormalItemViewHolder viewHolder, DailyAgendaItemStub item) {

        boolean allTaken = true;

        viewHolder.medList.removeAllViews();

        for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {
            View medNameView = viewHolder.inflater.inflate(R.layout.daily_view_intake_med, null);
            ((TextView) medNameView.findViewById(R.id.med_item_name)).setText(element.medName);
            if (element.taken) {
                medNameView.findViewById(R.id.ic_done).setVisibility(View.VISIBLE);
            } else {
                allTaken = false;
                medNameView.findViewById(R.id.ic_done).setVisibility(View.INVISIBLE);
            }
            ((TextView) medNameView.findViewById(R.id.med_item_dose)).setText(
                    element.displayDose + " " + (element.presentation.units(viewHolder.context.getResources()))
                            + (element.dose > 1 ? "s" : ""));

            Drawable icon = new IconicsDrawable(medNameView.getContext())
                    .icon(element.presentation.icon())
                    .colorRes(R.color.white)
                    .sizeDp(24)
                    .paddingDp(0);


                    ((ImageView) medNameView.findViewById(R.id.imageView)).setImageDrawable(icon);

            viewHolder.medList.addView(medNameView);
        }
        return allTaken;
    }


    public boolean isExpanded() {
        return expanded;
    }

    public void toggleCollapseMode(){
        Log.d("RVAdapter","toggleCollapseMode");
        expanded = !expanded;
        for(int i= 0; i< items.size();i++){
            notifyItemChanged(i);
        }
    }

    private void updateItem(int position, Context context){
        DailyAgendaItemStub stub = items.get(position);
        DailyAgendaItemStub item;

        if(!stub.isRoutine){
            LocalTime t = stub.time;
            Schedule s = DB.schedules().findById(stub.id);
            item = new DailyAgendaItemStub(t);
            item.meds = new ArrayList<>();
            item.hasEvents = true;
            int minute = t.getMinuteOfHour();
            Medicine med = s.medicine();
            DailyAgendaItemStub.DailyAgendaItemStubElement el = new DailyAgendaItemStub.DailyAgendaItemStubElement();
            el.medName = med.name();
            el.dose = s.dose();
            el.displayDose = s.displayDose();
            el.res = med.presentation().getDrawable();
            el.presentation = med.presentation();
            el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
            el.taken = DB.dailyScheduleItems()
                    .findByScheduleAndTime(s, t)
                    .takenToday();
            item.meds.add(el);
            item.id = s.getId();
            item.patient = s.patient();
            item.isRoutine = false;
            item.title = s.toReadableString(context);
            item.hour = t.getHourOfDay();
            item.minute = t.getMinuteOfHour();
            item.time = new LocalTime(item.hour, item.minute);
        }else{

            LocalDate today = LocalDate.now();
            Routine r = Routine.findById(stub.id);
            List<ScheduleItem> doses = r.scheduleItems();

            // create an ItemStub for the current hour
            item = new DailyAgendaItemStub(r.time());
            item.meds = new ArrayList<>();
            for (ScheduleItem scheduleItem : doses) {
                if (scheduleItem.schedule() != null && scheduleItem.schedule().enabledForDate(today)) {
                    item.hasEvents = true;
                    int minute = r.time().getMinuteOfHour();
                    Medicine med = scheduleItem.schedule().medicine();
                    DailyAgendaItemStub.DailyAgendaItemStubElement el = new DailyAgendaItemStub.DailyAgendaItemStubElement();
                    el.medName = med.name();
                    el.dose = scheduleItem.dose();
                    el.displayDose = scheduleItem.displayDose();
                    el.res = med.presentation().getDrawable();
                    el.presentation = med.presentation();
                    el.minute = minute < 10 ? "0" + minute : String.valueOf(minute);
                    el.taken = DailyScheduleItem.findByScheduleItem(scheduleItem).takenToday();
                    item.meds.add(el);
                }
            }
            Collections.sort(item.meds);

            if (!item.meds.isEmpty())
            {
                item.id = r.getId();
                item.patient = r.patient();
                item.title = r.name();
                item.hour = r.time().getHourOfDay();
                item.minute = r.time().getMinuteOfHour();
            }
        }
        items.remove(position);
        items.add(position, item);
        //items.get(position).copyFrom(item);
        notifyItemChanged(position);
    }

}
