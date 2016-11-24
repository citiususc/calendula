package es.usc.citius.servando.calendula.adapters.items.allergensearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.commons.items.AbstractExpandableItem;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenVO;

/**
 * Created by alvaro.brey.vilas on 23/11/16.
 */

public class AllergenGroupSubItem extends AbstractExpandableItem<AllergenGroupItem, AllergenGroupSubItem.ViewHolder, AllergenGroupSubItem> implements Comparable<AllergenGroupSubItem> {

    private AllergenVO vo;
    private String title;
    private String subtitle;
    private ViewHolder holder;
    private AllergenGroupItem parent;

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
    public int compareTo(AllergenGroupSubItem o) {
        return this.title.compareTo(o.title);
    }

    public AllergenVO getVo() {
        return vo;
    }

//    public void triggerBackgroundUpdate(boolean selected) {
//        if (holder != null && holder.itemView != null)
//            if (selected) {
//                holder.itemView.setBackgroundColor(Color.CYAN);
//            } else {
//                holder.itemView.setBackgroundResource(R.drawable.touchable_white);
//            }
//    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView subtitle;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.text1);
            subtitle = (TextView) itemView.findViewById(R.id.text2);
        }
    }

}
