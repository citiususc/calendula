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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenVO;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergenGroupSubItem extends AbstractExpandableItem<AllergenGroupItem, AllergenGroupSubItem.ViewHolder, AllergenGroupSubItem> implements Comparable<AllergenGroupSubItem> {

    private AllergenVO vo;
    private String title;
    private String subtitle;
    private AllergenGroupItem parent;
    private SpannableStringBuilder titleSpannable;

    public AllergenGroupSubItem(AllergenVO vo, Context context) {
        this.vo = vo;
        this.title = vo.getName();
        switch (vo.getType()) {
            case ACTIVE_INGREDIENT:
                subtitle = context.getString(R.string.active_ingredient);
                break;
            case EXCIPIENT:
                subtitle = context.getString(R.string.excipient);
                break;
        }
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.title.setText(titleSpannable != null ? titleSpannable : title);
        holder.subtitle.setText(subtitle);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
        holder.subtitle.setText(null);
    }

    @Override
    public int compareTo(@NonNull AllergenGroupSubItem o) {
        return this.title.compareTo(o.title);
    }

    @Override
    public AllergenGroupItem getParent() {
        return parent;
    }

    public void setParent(AllergenGroupItem parent) {
        this.parent = parent;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergen_group_sub_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_search_group_sub_list_item;
    }

    public AllergenVO getVo() {
        return vo;
    }

    public void setTitleSpannable(SpannableStringBuilder titleSpannable) {
        this.titleSpannable = titleSpannable;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
