package es.usc.citius.servando.calendula.adapters.items.allergylist;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import java.util.List;

import es.usc.citius.servando.calendula.R;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergyGroupItem extends AbstractExpandableItem<AllergyGroupItem, AllergyGroupItem.ViewHolder, AllergyGroupSubItem> implements Comparable<AllergyGroupItem> {


    private static final String TAG = "AllergyGroupItem";
    private final String title;
    private Context context;
    private AllergyGroupListener listener;
    private ViewHolder holder;


    public AllergyGroupItem(String title, Context ctx, AllergyGroupListener listener) {
        this.title = title;
        this.context = ctx;
        this.listener = listener;
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
        this.holder = holder;
        holder.title.setText(this.title);
        holder.subtitle.setText(context.getString(R.string.allergies_group_elements_number, getSubItems().size()));
//        UIUtils.setBackground(holder.itemView, FastAdapterUIUtils.getSelectableBackground(holder.itemView.getContext(), Color.CYAN, true));
        final MaterialDesignIconic.Icon icon = MaterialDesignIconic.Icon.gmi_chevron_down;
        holder.dropButton.setImageDrawable(new IconicsDrawable(holder.dropButton.getContext())
                .icon(icon)
                //.color(0xFF222222)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));
        holder.dropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllergyGroupItem k = AllergyGroupItem.this;
                boolean expand = !k.isExpanded();
                listener.expandButton(k, expand);
                float rotate = expand ? 180 : 0;
                ViewCompat.animate(holder.dropButton).rotation(rotate);
            }
        });
        holder.deleteButton.setImageDrawable(new IconicsDrawable(holder.deleteButton.getContext())
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.deleteButton(AllergyGroupItem.this);
            }
        });
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.title.setText(null);
        holder.subtitle.setText(null);
        this.holder = null;
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

    public interface AllergyGroupListener {

        public void expandButton(AllergyGroupItem item, boolean expanded);

        public void deleteButton(AllergyGroupItem item);
    }


}
