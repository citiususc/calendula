package es.usc.citius.servando.calendula.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.usc.citius.servando.calendula.activities.AllergiesActivity;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.remote.AllergenRemoteFacade;

/**
 * Created by alvaro.brey.vilas on 7/11/16.
 */
public class AllergiesAutocompleteAdapter extends ArrayAdapter<PatientAllergen> {

    private final static String TAG = "AllergiesAutoAdapter";
    private AllergenRemoteFacade remote;
    private AllergiesActivity.AllergiesStore store;


    public AllergiesAutocompleteAdapter(Context context, int resource) {
        super(context, resource);
        remote = new AllergenRemoteFacade(context);
    }

    public AllergiesAutocompleteAdapter(Context context, int resource, AllergiesActivity.AllergiesStore store) {
        this(context, resource);
        this.store = store;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PatientAllergen a = getItem(position);
        View v = super.getView(position, convertView, parent);
        ((TextView) v).setText(a.getName() == null ? Integer.toString(a.getRealId()) : a.getName());
        return v;
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
                    if (store != null) {
                        list.removeAll(store.getAllergies());
                    }
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
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((List<PatientAllergen>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

}
