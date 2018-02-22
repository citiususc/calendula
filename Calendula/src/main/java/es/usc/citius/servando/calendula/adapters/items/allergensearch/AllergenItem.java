/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

package es.usc.citius.servando.calendula.adapters.items.allergensearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.utils.FastAdapterUIUtils;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenVO;


public class AllergenItem extends AbstractItem<AllergenItem, AllergenItem.ViewHolder> implements Comparable<AllergenItem> {

    private String allergenType;

    private AllergenVO vo;

    private String title;

    private SpannableStringBuilder titleSpannable;

    public AllergenItem(AllergenVO vo, Context context) {
        this.title = vo.getName();
        switch (vo.getType()) {
            case ACTIVE_INGREDIENT:
                allergenType = context.getString(R.string.active_ingredient);
                break;
            case EXCIPIENT:
                allergenType = context.getString(R.string.excipient);
                break;
        }
        this.vo = vo;
    }

    @Override
    public int compareTo(@NonNull AllergenItem o) {
        return this.title.compareTo(o.title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergen_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_search_list_item;
    }

    public AllergenVO getVo() {
        return vo;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
        final int selectedColor = ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.med_presentation_circle_bg);
        UIUtils.setBackground(viewHolder.itemView, FastAdapterUIUtils.getSelectableBackground(viewHolder.itemView.getContext(), selectedColor, true));
        viewHolder.title.setText(titleSpannable != null ? titleSpannable : title);
        viewHolder.subtitle.setText(allergenType);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
        holder.subtitle.setText(null);
    }

    public void setTitleSpannable(SpannableStringBuilder titleSpannable) {
        this.titleSpannable = titleSpannable;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

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
