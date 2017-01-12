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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 22/11/16.
 */

public class AllergyItem extends AbstractItem<AllergyItem, AllergyItem.ViewHolder> implements Comparable<AllergyItem> {

    private String title;
    private String subtitle;
    private PatientAllergen allergen;
    private ViewHolder holder;

    public AllergyItem(PatientAllergen vo, Context context) {
        this.title = vo.getName();
        switch (vo.getType()) {
            case ACTIVE_INGREDIENT:
                subtitle = context.getString(R.string.active_ingredient);
                break;
            case EXCIPIENT:
                subtitle = context.getString(R.string.excipient);
                break;
            case ATC_CODE:
                subtitle = vo.getIdentifier();
                break;
        }
        this.allergen = vo;
    }

    @Override
    public int compareTo(@NonNull AllergyItem o) {
        return this.title.compareTo(o.title);
    }

    public PatientAllergen getAllergen() {
        return allergen;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergy_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_list_item;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
        this.holder = viewHolder;
        viewHolder.title.setText(title);
        if (TextUtils.isEmpty(subtitle)) {
            viewHolder.subtitle.setVisibility(View.GONE);
        } else {
            viewHolder.subtitle.setText(subtitle);
            viewHolder.subtitle.setVisibility(View.VISIBLE);
        }
        viewHolder.deleteButton.setImageDrawable(new IconicsDrawable(viewHolder.deleteButton.getContext())
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));
    }

    //reset the view here (this is an optional method, but recommended)
    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.delete_button)
        public ImageButton deleteButton;
        @BindView(R.id.text1)
        TextView title;
        @BindView(R.id.text2)
        TextView subtitle;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
