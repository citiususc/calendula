package es.usc.citius.servando.calendula.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.activities.AllergiesActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientAllergenDao;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 7/11/16.
 */

public class AllergiesAdapter extends RecyclerView.Adapter<AllergiesAdapter.AllergenViewHolder> {


    private static final String TAG = "AllergiesAdapter";

    private AllergiesOnLongClickListener listener;

    private AllergiesActivity.AllergiesStore store;

    public interface AllergiesOnLongClickListener {
        public void onLongClick(PatientAllergen allergen);
    }

    public AllergiesAdapter(AllergiesActivity.AllergiesStore store, AllergiesOnLongClickListener listener) {
        super();
        this.store=store;
        this.listener = listener;
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
        allergenViewHolder.textView.setText(name);
        allergenViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onLongClick(getItemByPosition(allergenViewHolder.getAdapterPosition()));
                }
                return true;
            }
        });
    }

    @Override
    public AllergenViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(android.R.layout.simple_dropdown_item_1line, viewGroup, false);


        return new AllergenViewHolder(itemView);
    }

    static class AllergenViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public AllergenViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

}
