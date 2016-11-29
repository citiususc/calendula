package es.usc.citius.servando.calendula.adapters.items.allergylist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 22/11/16.
 */

public class AllergyItem extends AbstractItem<AllergyItem, AllergyItem.ViewHolder> implements Comparable<AllergyItem> {

    private String allergenType;
    private PatientAllergen allergen;
    private ViewHolder holder;

    protected String title;

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public AllergyItem(PatientAllergen vo, Context context) {
        this.title = vo.getName();
        switch (vo.getType()) {
            case ACTIVE_INGREDIENT:
                allergenType = context.getString(R.string.active_ingredient);
                break;
            case EXCIPIENT:
                allergenType = context.getString(R.string.excipient);
                break;
        }
        this.allergen = vo;
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
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
        this.holder = viewHolder;
        viewHolder.title.setText(title);
        viewHolder.subtitle.setText(allergenType);
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

    @Override
    public boolean isSelectable() {
        return false;
    }

    public PatientAllergen getAllergen() {
        return allergen;
    }

    @Override
    public int compareTo(AllergyItem o) {
        return this.title.compareTo(o.title);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected final TextView subtitle;
        protected TextView title;
        public ImageButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.text1);
            this.subtitle = (TextView) view.findViewById(R.id.text2);
            this.deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        }
    }
}
