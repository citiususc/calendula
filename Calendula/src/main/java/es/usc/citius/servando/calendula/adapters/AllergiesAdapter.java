package es.usc.citius.servando.calendula.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.AllergenListeners;
import es.usc.citius.servando.calendula.activities.AllergiesActivity;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.util.IconUtils;

/**
 * Created by alvaro.brey.vilas on 7/11/16.
 */

public class AllergiesAdapter extends RecyclerView.Adapter<AllergiesAdapter.AllergenViewHolder> {


    private static final String TAG = "AllergiesAdapter";

    private AllergenListeners.DeleteAllergyActionListener listener;
    private AllergiesActivity.AllergiesStore store;
    private Context context;

    public AllergiesAdapter(AllergiesActivity.AllergiesStore store, AllergenListeners.DeleteAllergyActionListener listener, Context context) {
        super();
        this.store = store;
        this.listener = listener;
        this.context = context;
    }

    public PatientAllergen getItemByPosition(int i) {
        return store.getAllergies().get(i);
    }

    @Override
    public int getItemCount() {
        if (store.getAllergies() == null)
            return 0;
        return store.getAllergies().size();
    }

    @Override
    public void onBindViewHolder(final AllergenViewHolder allergenViewHolder, final int i) {
        PatientAllergen allergen = store.getAllergies().get(i);

        final String name = allergen.getName();
        allergenViewHolder.title.setText(name);

        String type = "";
        switch (allergen.getType()) {
            case ACTIVE_INGREDIENT:
                type = context.getString(R.string.active_ingredient);
                break;
            case EXCIPIENT:
                type = context.getString(R.string.excipient);
                break;
        }
        allergenViewHolder.subtitle.setText(type);

        allergenViewHolder.button.setImageDrawable(new IconicsDrawable(allergenViewHolder.button.getContext())
                .icon(CommunityMaterial.Icon.cmd_delete)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(10)
                .sizeDp(38));

        allergenViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteAction(getItemByPosition(allergenViewHolder.getAdapterPosition()));
            }
        });

    }

    @Override
    public AllergenViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.allergen_list_item, viewGroup, false);


        return new AllergenViewHolder(itemView);
    }

    public static class AllergenViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView subtitle;
        private ImageButton button;

        public AllergenViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.text1);
            subtitle = (TextView) itemView.findViewById(R.id.text2);
            button = (ImageButton) itemView.findViewById(R.id.delete_button);
        }

        public TextView getTitle() {
            return title;
        }

        public void setTitle(TextView title) {
            this.title = title;
        }

        public TextView getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(TextView subtitle) {
            this.subtitle = subtitle;
        }
    }

}
