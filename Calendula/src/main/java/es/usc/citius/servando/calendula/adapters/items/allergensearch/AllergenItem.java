package es.usc.citius.servando.calendula.adapters.items.allergensearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenVO;

/**
 * Created by alvaro.brey.vilas on 22/11/16.
 */

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
        return false;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
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
        TextView subtitle;
        @BindView(R.id.text2)
        TextView title;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
