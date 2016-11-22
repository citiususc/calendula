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

package es.usc.citius.servando.calendula.adapters;

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

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub.DailyAgendaItemStubElement;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.view.ParallaxImageView;

/**
 * Created by joseangel.pineiro on 11/6/15.
 */
public class AlertViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DailyAgendaAdapter";

    private final int ALERT = 1;

    List<PatientAlert> items;
    private EventListener listener;

    public AlertViewRecyclerAdapter(List<PatientAlert> items, final RecyclerView rv, final LinearLayoutManager llm, Activity ctx) {
        this.items = items;

    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case ALERT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medicine_alert_list_item, parent, false);
                return new NormalItemViewHolder(v);
            default:
                throw new RuntimeException("Unsupported view type: " + viewType);
        }
    }

    @Override
    public int getItemViewType(int position) {
        PatientAlert item = items.get(position);
        int type = ALERT;
        // may be useful later
        return type;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final PatientAlert item = items.get(position);

        if (holder instanceof NormalItemViewHolder) {
            onBindNormalItemViewHolder((NormalItemViewHolder) holder, item, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }



    public class NormalItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

        Context context;
        PatientAlert alert;

        LayoutInflater inflater;

        ImageView alertIcon;
        TextView title;
        TextView description;

        public NormalItemViewHolder(View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            this.inflater = LayoutInflater.from(itemView.getContext());
            this.title = (TextView) itemView.findViewById(R.id.alert_title);
            this.description = (TextView) itemView.findViewById(R.id.alert_description);
            this.alertIcon = (ImageView) itemView.findViewById(R.id.alert_icon);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void onBindNormalItemViewHolder(NormalItemViewHolder viewHolder, PatientAlert item, int i) {
        viewHolder.alert = item;
        // setup ui
        PatientAlert alert = item.map();
        viewHolder.alertIcon.setImageDrawable(levelIcon(alert.getLevel(), viewHolder.context));
        viewHolder.title.setText("Alert " + alert.getClass().getSimpleName());
        viewHolder.description.setText("Description gose here");
    }


    private Drawable levelIcon(int level, Context context){

        IIcon ic;
        int color;

        switch (level){
            case PatientAlert.Level.HIGH:
                ic = CommunityMaterial.Icon.cmd_alert_circle;
                color = R.color.android_red_dark;
                break;
            case PatientAlert.Level.MEDIUM:
                ic = CommunityMaterial.Icon.cmd_alert_circle;
                color = R.color.android_orange_darker;
                break;
            default:
                ic = CommunityMaterial.Icon.cmd_alert_circle;
                color = R.color.android_orange;
                break;
        }

        return new IconicsDrawable(context)
                .icon(ic)
                .colorRes(color)
                .sizeDp(24)
                .paddingDp(4);
    }

    public interface EventListener {
        void onItemClick(View v, DailyAgendaItemStub item, int position);
    }

}
