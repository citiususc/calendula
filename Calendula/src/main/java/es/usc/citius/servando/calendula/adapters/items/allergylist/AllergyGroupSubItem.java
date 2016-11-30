package es.usc.citius.servando.calendula.adapters.items.allergylist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergyGroupSubItem extends AbstractExpandableItem<AllergyGroupItem, AllergyGroupSubItem.ViewHolder, AllergyGroupSubItem> implements Comparable<AllergyGroupSubItem> {

    private PatientAllergen allergen;
    private String title;
    private String subtitle;
    private ViewHolder holder;
    private AllergyGroupItem parent;

    public AllergyGroupSubItem(PatientAllergen vo, Context context) {
        this.allergen = vo;
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
        holder.title.setText(title);
        holder.subtitle.setText(subtitle);
        this.holder = holder;
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
        holder.subtitle.setText(null);
    }

    @Override
    public int compareTo(@NonNull AllergyGroupSubItem o) {
        return this.title.compareTo(o.title);
    }

    public PatientAllergen getAllergen() {
        return allergen;
    }

    @Override
    public AllergyGroupItem getParent() {
        return parent;
    }

    public void setParent(AllergyGroupItem parent) {
        this.parent = parent;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergy_group_sub_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_search_group_sub_list_item;
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
