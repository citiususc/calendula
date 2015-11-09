package es.usc.citius.servando.calendula;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;

/**
 * Created by joseangel.pineiro on 11/6/15.
 */
public class DailyAgendaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface AgendaItemClickListener{
        void onClick(View v, DailyAgendaItemStub item);
    }

    private final int SPACER = 1;
    private final int EMPTY = 2;
    private final int NORMAL = 3;

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

        TextView hourText;
        DailyAgendaItemStub stub;

        EmptyItemViewHolder(View itemView) {
            super(itemView);
            hourText = (TextView) itemView.findViewById(R.id.hour_text);
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

        TextView title;
        TextView hour;
        TextView minute;

        View arrow;
        View top;
        View bottom;

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

            top.setOnClickListener(this);
            arrow.setOnClickListener(this);
            itemView.setOnClickListener(this);

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
            if (view.getId() == arrow.getId()){
                Log.d("Recycler", "Click bottom arrow");

                if(stub.isExpanded) {
                    collapse();
                }else {
                    expand();
                }

                stub.isExpanded = !stub.isExpanded;
                notifyItemChanged(getAdapterPosition());
            } else {
                Log.d("Recycler", "Click row, listener is null? " + (listener == null));
                if(listener != null){
                    listener.onClick(view, stub);
                }
            }
        }


        private void expand(){
            arrow.startAnimation(rotateAnimUp);
        }

        private void collapse(){
            arrow.startAnimation(rotateAnimDown);
        }
    }

    public void onBindViewSpacerItemViewHolder(SpacerItemViewHolder viewHolder, DailyAgendaItemStub item, int i) {
        // do nothing
    }

    public void onBindEmptyItemViewHolder(EmptyItemViewHolder viewHolder, DailyAgendaItemStub item, int i) {
        viewHolder.hourText.setText(item.time != null ? item.time.toString("kk:mm") : "--");
        viewHolder.stub = item;
    }

    public void onBindNormalItemViewHolder(NormalItemViewHolder viewHolder, DailyAgendaItemStub item, int i) {

        Log.d("Recycler", "OnBindNormalItem" + i );

        viewHolder.stub = item;

        if (!item.isRoutine) {
            viewHolder.itemTypeIcon.setImageResource(R.drawable.ic_history_black_48dp);
        }else{
            viewHolder.itemTypeIcon.setImageResource(R.drawable.ic_alarm_black_48dp);
        }

        if (item.patient != null) {
            viewHolder.avatarIcon.setImageResource(AvatarMgr.res(item.patient.avatar()));
        }

        viewHolder.title.setText(item.title);
        viewHolder.hour.setText((item.hour > 9 ? item.hour : "0" + item.hour) + ":");
        viewHolder.minute.setText((item.minute > 9 ? item.minute : "0" + item.minute) + "");
        addMeds(viewHolder, item);

        if(item.isExpanded){
            viewHolder.bottom.setVisibility(View.VISIBLE);
        }else{
            viewHolder.bottom.setVisibility(View.GONE);
        }

    }

    private void addMeds(NormalItemViewHolder viewHolder, DailyAgendaItemStub item) {

        viewHolder.medList.removeAllViews();

        for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {
            View medNameView = viewHolder.inflater.inflate(R.layout.daily_view_intake_med, null);
            ((TextView) medNameView.findViewById(R.id.med_item_name)).setText(element.medName);
            if (element.taken) {
                medNameView.findViewById(R.id.ic_done).setVisibility(View.VISIBLE);
            } else {
                medNameView.findViewById(R.id.ic_done).setVisibility(View.INVISIBLE);
            }
            ((TextView) medNameView.findViewById(R.id.med_item_dose)).setText(
                    element.displayDose + " " + (element.presentation.units(viewHolder.context.getResources()))
                            + (element.dose > 1 ? "s" : ""));
            ((ImageView) medNameView.findViewById(R.id.imageView)).setImageResource(
                    element.res);
            viewHolder.medList.addView(medNameView);
        }
    }


}
