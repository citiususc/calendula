package es.usc.citius.servando.calendula.adapters.items.allergylist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import es.usc.citius.servando.calendula.R;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergyGroupItem extends AbstractExpandableItem<AllergyGroupItem, AllergyGroupItem.ViewHolder, AllergyGroupSubItem> implements Comparable<AllergyGroupItem> {


    private static final String TAG = "AllergyGroupItem";
    private final String title;
    private Context context;


    public AllergyGroupItem(String title, Context ctx) {
        this.title = title;
        this.context = ctx;
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
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean isAutoExpanding() {
        return false;
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
    }

    @Override
    public int compareTo(AllergyGroupItem o) {
        return this.title.compareTo(o.title);
    }

    public String getTitle() {
        return title;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView title;
        protected TextView subtitle;
        public ImageButton dropButton;
        public ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.text1);
            subtitle = (TextView) itemView.findViewById(R.id.text2);
            dropButton = (ImageButton) itemView.findViewById(R.id.group_button);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
        }
    }

}
