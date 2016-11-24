package es.usc.citius.servando.calendula.adapters.items.allergylist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import es.usc.citius.servando.calendula.R;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergyGroupItem extends AbstractExpandableItem<AllergyGroupItem, AllergyGroupItem.ViewHolder, AllergyGroupSubItem> implements Comparable<AllergyGroupItem> {


    private static final String TAG = "AllergyGroupItem";
    private final String title;
    private String subtitle;
    private ViewHolder holder;


    public AllergyGroupItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_allergy_group_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.allergen_search_group_list_item;
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
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        holder.title.setText(this.title);
        holder.subtitle.setText(this.subtitle);
//        UIUtils.setBackground(holder.itemView, FastAdapterUIUtils.getSelectableBackground(holder.itemView.getContext(), Color.CYAN, true));
        final CommunityMaterial.Icon icon = AllergyGroupItem.this.isExpanded() ? CommunityMaterial.Icon.cmd_minus : CommunityMaterial.Icon.cmd_plus;
        holder.imageButton.setImageDrawable(new IconicsDrawable(holder.imageButton.getContext())
                .icon(icon)
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

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView title;
        protected TextView subtitle;
        protected ImageButton imageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.text1);
            subtitle = (TextView) itemView.findViewById(R.id.text2);
            imageButton = (ImageButton) itemView.findViewById(R.id.group_button);
        }
    }

    public static class GroupExpandClickEvent extends ClickEventHook<AllergyGroupItem> {
        @Override
        public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder instanceof ViewHolder) {
                return ((ViewHolder) viewHolder).imageButton;
            }
            return null;
        }

        @Override
        public void onClick(View v, int position, FastAdapter<AllergyGroupItem> fastAdapter, AllergyGroupItem item) {
            if (item.isExpanded()) {
                fastAdapter.collapse(position);
                ((ImageButton) v).setImageDrawable(new IconicsDrawable(v.getContext())
                        .icon(CommunityMaterial.Icon.cmd_plus)
                        .colorRes(R.color.agenda_item_title)
                        .paddingDp(10)
                        .sizeDp(38));
            } else {
                fastAdapter.expand(position);
                ((ImageButton) v).setImageDrawable(new IconicsDrawable(v.getContext())
                        .icon(CommunityMaterial.Icon.cmd_minus)
                        .colorRes(R.color.agenda_item_title)
                        .paddingDp(10)
                        .sizeDp(38));
            }
        }
    }

}
