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

package es.usc.citius.servando.calendula.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by joseangel.pineiro on 11/6/15.
 */
public class AlertViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String TAG = "AlertViewAdapter";

    private final int DEFAULT_ALERT = 1;

    HashMap<Class<?>, Integer> alertTypeMap = new HashMap<>();
    HashMap<Integer, AlertViewProvider> providerMap = new HashMap<>();

    List<PatientAlert> items;
    private EventListener listener;

    public AlertViewRecyclerAdapter(List<PatientAlert> items, final RecyclerView rv, final LinearLayoutManager llm, Activity ctx) {
        this.items = items;
        LogUtil.d(TAG, "AlertViewRecyclerAdapter: items " + this.items.size());

    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        PatientAlert item = items.get(position);
        LogUtil.d(TAG, "getItemViewType() called with: " + "position = [" + position + "]: " + item.viewProviderType());
        if (item.viewProviderType() != null) {
            return getTypeForClass(item.viewProviderType());
        }
        return DEFAULT_ALERT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {

        Integer viewType = type;
        LogUtil.d(TAG, "onCreateViewHolder() called with: viewType = [" + viewType + "]");
        if (viewType == DEFAULT_ALERT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medicine_alert_list_item, parent, false);
            return new NormalItemViewHolder(v);
        } else {
            return providerMap.get(viewType).onCreateViewHolder(parent);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final PatientAlert item = items.get(position);
        LogUtil.d(TAG, "onBindViewHolder() called with: " + item.viewProviderType());
        if (holder instanceof NormalItemViewHolder) {
            onBindNormalItemViewHolder((NormalItemViewHolder) holder, item, position);
        } else if (item.viewProviderType() != null) {
            LogUtil.d(TAG, "onBindViewHolder() provider type found");
            getProviderForClass(item.viewProviderType()).onBindViewHolder(holder, item);
        } else {
            LogUtil.d(TAG, "onBindViewHolder: unknown view holder type");
        }
    }

    @Override
    public int getItemCount() {
        LogUtil.d(TAG, "getItemCount: " + items.size());
        return items.size();
    }

    public void onBindNormalItemViewHolder(NormalItemViewHolder viewHolder, PatientAlert item, int i) {
        viewHolder.alert = item;
        // setup ui
        PatientAlert alert = item.map();
        viewHolder.alertIcon.setImageDrawable(IconUtils.alertLevelIcon(alert.getLevel(), viewHolder.context));
        viewHolder.title.setText("Alert " + alert.getClass().getSimpleName());
        viewHolder.description.setText("Description gose here");
    }

    public void registerViewProvider(AlertViewProvider provider, Class<?> cls) {

        Integer type = cls.hashCode();
        this.alertTypeMap.put(provider.getClass(), type);
        this.providerMap.put(type, provider);
    }

    private AlertViewProvider getProviderForClass(Class<?> type) {
        return providerMap.get(getTypeForClass(type));
    }

    private int getTypeForClass(Class<?> type) {
        return alertTypeMap.get(type);
    }


    public interface EventListener {
        void onItemClick(View v, DailyAgendaItemStub item, int position);
    }

    public interface AlertViewProvider {
        RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);

        void onBindViewHolder(RecyclerView.ViewHolder holder, PatientAlert item);
    }

    public class NormalItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

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

}
