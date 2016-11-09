package es.usc.citius.servando.calendula.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.AllergenListeners;
import es.usc.citius.servando.calendula.activities.AllergiesActivity;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.remote.AllergenRemoteFacade;

/**
 * Created by alvaro.brey.vilas on 7/11/16.
 */
public class AllergiesAutocompleteAdapter extends RecyclerView.Adapter<AllergiesAdapter.AllergenViewHolder> implements Filterable {

    private final static String TAG = "AllergiesAutoAdapter";
    private AllergenRemoteFacade remote;
    private AllergiesActivity.AllergiesStore store;
    private AllergenListeners.AddAllergyActionListener listener;
    private Context context;
    private List<PatientAllergen> search = new ArrayList<>();

    public AllergiesAutocompleteAdapter(AllergiesActivity.AllergiesStore store, Context context, AllergenListeners.AddAllergyActionListener listener) {
        super();
        this.store = store;
        this.context = context;
        this.listener = listener;
        remote = new AllergenRemoteFacade(context);
    }


    private Filter mFilter = new Filter() {

        @Override
        public String convertResultToString(Object resultValue) {
            return ((PatientAllergen) resultValue).getName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            try {
                if (constraint != null) {
                    List<PatientAllergen> list = remote.findAllergensByName(constraint.toString());
                    Collections.sort(list, new Comparator<PatientAllergen>() {
                        @Override
                        public int compare(PatientAllergen o1, PatientAllergen o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
//                    if (store != null) {
//                        list.removeAll(store.getAllergies());
//                    }
                    results.values = list;
                    results.count = list.size();
                }
            } catch (IOException | IllegalStateException e) {
                Log.e(TAG, "performFiltering: could not recover allergens", e);
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            search.clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                search.addAll((List<PatientAllergen>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public void clear() {
        search.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void remove(PatientAllergen allergen) {
        int index = search.indexOf(allergen);
        search.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public void onBindViewHolder(final AllergiesAdapter.AllergenViewHolder allergenViewHolder, final int i) {
        PatientAllergen allergen = search.get(i);

        final String name = allergen.getName();
        allergenViewHolder.getTitle().setText(name);

        if (store.getAllergies().contains(allergen)) {
            allergenViewHolder.itemView.setVisibility(View.GONE);
            ViewGroup.LayoutParams layoutParams = allergenViewHolder.itemView.getLayoutParams();
            layoutParams.height = 0;
            allergenViewHolder.itemView.setLayoutParams(layoutParams);
        } else {
            String type = "";
            switch (allergen.getType()) {
                case ACTIVE_INGREDIENT:
                    type = context.getString(R.string.active_ingredient);
                    break;
                case EXCIPIENT:
                    type = context.getString(R.string.excipient);
                    break;
            }
            allergenViewHolder.getSubtitle().setText(type);

            allergenViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        allergenViewHolder.itemView.setOnClickListener(null);
                        listener.onAddAction(getItemByPosition(allergenViewHolder.getAdapterPosition()));
                    }
                }
            });
        }

    }

    private PatientAllergen getItemByPosition(int position) {
        return search.get(position);
    }

    @Override
    public AllergiesAdapter.AllergenViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.allergen_search_list_item, viewGroup, false);


        return new AllergiesAdapter.AllergenViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return search.size();
    }
}
