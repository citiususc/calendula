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

package es.usc.citius.servando.calendula.adapters.items.allergensearch;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IExpandable;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;
import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergenGroupItem extends AbstractExpandableItem<AllergenGroupItem, AllergenGroupItem.ViewHolder, AllergenGroupSubItem> implements Comparable<AllergenGroupItem> {


    @SuppressWarnings("unused")
    private static final String TAG = "AllergyGroupItem";
    private String title;
    private SpannableStringBuilder titleSpannable;
    private String subtitle;


    public AllergenGroupItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.title.setText(titleSpannable != null ? titleSpannable : title);
        holder.subtitle.setText(this.subtitle);
        final float rotation = isExpanded() ? 180 : 0;
        holder.imageButton.setRotation(rotation);
        final int selectedColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.med_presentation_circle_bg);
        UIUtils.setBackground(holder.itemView, FastAdapterUIUtils.getSelectableBackground(holder.itemView.getContext(), selectedColor, true));
        holder.imageButton.setImageDrawable(new IconicsDrawable(holder.imageButton.getContext())
                .icon(GoogleMaterial.Icon.gmd_chevron_down)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
        holder.subtitle.setText(null);
        holder.imageButton.setRotation(0);
    }

    @Override
    public int compareTo(@NonNull AllergenGroupItem o) {
        return this.title.compareTo(o.title);
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergen_group_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_search_group_list_item;
    }

    @Override
    public boolean isAutoExpanding() {
        return false;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTitleSpannable(SpannableStringBuilder titleSpannable) {
        this.titleSpannable = titleSpannable;
    }

    public static class GroupExpandClickEvent extends ClickEventHook<AbstractItem> {
        @Override
        public void onClick(View view, int i, FastAdapter<AbstractItem> fastAdapter, AbstractItem item) {
            IExpandable it = (IExpandable) item;
            if (it.isExpanded()) {
                fastAdapter.collapse(i);
                ViewCompat.animate(view).rotation(0);
            } else {
                fastAdapter.expand(i);
                ViewCompat.animate(view).rotation(180);
            }
        }

        @Override
        public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder instanceof ViewHolder) {
                return ((ViewHolder) viewHolder).imageButton;
            }
            return null;
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text1)
        TextView title;
        @BindView(R.id.text2)
        TextView subtitle;
        @BindView(R.id.group_button)
        ImageButton imageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
