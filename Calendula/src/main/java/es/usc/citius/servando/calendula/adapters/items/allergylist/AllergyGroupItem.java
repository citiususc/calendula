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

package es.usc.citius.servando.calendula.adapters.items.allergylist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergyGroupItem extends AbstractExpandableItem<AllergyGroupItem, AllergyGroupItem.ViewHolder, AllergyGroupSubItem> implements Comparable<AllergyGroupItem> {


    @SuppressWarnings("unused")
    private static final String TAG = "AllergyGroupItem";
    private final String title;
    private Context context;


    public AllergyGroupItem(String title, Context ctx) {
        this.title = title;
        this.context = ctx;
    }

    @Override
    public void bindView(final ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.title.setText(this.title);
        holder.subtitle.setText(context.getString(R.string.allergies_group_elements_number, getSubItems().size()));
//        UIUtils.setBackground(holder.itemView, FastAdapterUIUtils.getSelectableBackground(holder.itemView.getContext(), Color.CYAN, true));
        holder.dropButton.setImageDrawable(new IconicsDrawable(holder.dropButton.getContext())
                .icon(GoogleMaterial.Icon.gmd_chevron_down)
                //.color(0xFF222222)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));
        holder.deleteButton.setImageDrawable(new IconicsDrawable(holder.deleteButton.getContext())
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
        holder.subtitle.setText(null);
        holder.dropButton.setRotation(0);
    }

    @Override
    public int compareTo(@NonNull AllergyGroupItem o) {
        return this.title.compareTo(o.title);
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergy_group_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_group_list_item;
    }

    @Override
    public boolean isAutoExpanding() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.group_button)
        public ImageButton dropButton;
        @BindView(R.id.delete_button)
        public ImageButton deleteButton;
        @BindView(R.id.text1)
        TextView title;
        @BindView(R.id.text2)
        TextView subtitle;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
