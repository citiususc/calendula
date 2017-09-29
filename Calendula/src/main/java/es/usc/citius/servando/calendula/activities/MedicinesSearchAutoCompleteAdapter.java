package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;

/**
 * Created by alvaro.brey.vilas on 9/29/17.
 * <p>
 * Adapter for autocompleting medicine search.
 */
public class MedicinesSearchAutoCompleteAdapter extends ArrayAdapter<MedicinesSearchAutoCompleteAdapter.PrescriptionSearchWrapper> implements Filterable {

    private static final int HIGHLIGHT_COLOR = Color.BLACK;
    private static final int MIN_SEARCH_LEN = 3;
    private static final String TAG = "AutoCompleteAdapter";
    private static final int MAX_LEVENSHTEIN_DISTANCE = 2;

    private final Filter filter;
    private final PrescriptionDBMgr currentDBMgr;
    private final MedicinesSearchActivity medicinesSearchActivity;
    private final Drawable icProspect;
    private final Comparator<PrescriptionSearchWrapper> searchSortComparator = new SearchSortComparator();

    private List<PrescriptionSearchWrapper> mData;

    MedicinesSearchAutoCompleteAdapter(MedicinesSearchActivity medicinesSearchActivity, Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.medicinesSearchActivity = medicinesSearchActivity;
        this.mData = new ArrayList<>();
        this.icProspect = new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_link_variant)
                .color(medicinesSearchActivity.color)
                .paddingDp(10)
                .sizeDp(40);
        this.filter = new MedicinesAutoCompleteFilter();
        this.currentDBMgr = DBRegistry.instance().current();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public PrescriptionSearchWrapper getItem(int index) {
        return mData.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, View item, @NonNull ViewGroup parent) {

        if (item == null) {
            final LayoutInflater inflater = medicinesSearchActivity.getLayoutInflater();
            item = inflater.inflate(R.layout.med_drop_down_item, parent, false);
        }

        if (mData.size() > position) {

            // views to fill in
            final ImageButton prospectIcon = ((ImageButton) item.findViewById(R.id.prospect_icon));
            final TextView cnView = (TextView) item.findViewById(R.id.prescription_cn);
            final TextView nameView = (TextView) item.findViewById(R.id.text1);
            final TextView doseView = (TextView) item.findViewById(R.id.text2);
            final TextView contentView = (TextView) item.findViewById(R.id.text3);
            final ImageView prView = ((ImageView) item.findViewById(R.id.presentation_image));

            // Wrapper and data
            final PrescriptionSearchWrapper wrapper = mData.get(position);
            final Prescription prescription = wrapper.prescription;
            final String match = wrapper.match;
            final String name = medicinesSearchActivity.dbMgr.shortName(prescription);
            final Presentation expectedPresentation = medicinesSearchActivity.dbMgr.expectedPresentation(prescription);


            // highlight the matches
            switch (wrapper.matchType) {
                case NAME:
                    nameView.setText(Strings.getHighlighted(name, match, HIGHLIGHT_COLOR));
                    cnView.setText(prescription.getCode());
                    break;
                case CODE:
                    cnView.setText(Strings.getHighlighted(prescription.getCode(), match, HIGHLIGHT_COLOR));
                    nameView.setText(name);
                    break;
            }

            // setup the rest of the views
            doseView.setText(prescription.getDose());
            contentView.setText(prescription.getContent());
            prospectIcon.setImageDrawable(icProspect);

            prospectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProspectUtils.openProspect(prescription, medicinesSearchActivity, true);
                }
            });

            prView.setImageDrawable(new IconicsDrawable(getContext())
                    .icon(expectedPresentation == null ? CommunityMaterial.Icon.cmd_help : Presentation.iconFor(expectedPresentation))
                    .color(ScreenUtils.equivalentNoAlpha(medicinesSearchActivity.color, 0.8f))
                    .paddingDp(10)
                    .sizeDp(72));
        }
        return item;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    static class PrescriptionSearchWrapper {
        private final Prescription prescription;
        private final MatchType matchType;
        private final String match;
        private final Integer distance;
        private final Integer matchIndex;

        public PrescriptionSearchWrapper(Prescription prescription, MatchType matchType, String match, Integer distance, Integer matchIndex) {
            this.prescription = prescription;
            this.matchType = matchType;
            this.match = match;
            this.distance = distance;
            this.matchIndex = matchIndex;
        }

        public Prescription getPrescription() {
            // Prescription should be readable for the ListView to print this adapter's info
            return prescription;
        }

        private enum MatchType implements Comparable<MatchType> {
            CODE, NAME
        }
    }

    /**
     * Sorts results in the following order:
     * <ul>
     * <li>Code matches take precedence over name matches</li>
     * <li>Name matches will be sorted by distance, then by index</li>
     * <li>Code matches will be sorted by index</li>
     * <li>Finally, ties will be sorted by alphabetical order of the prescription's name</li>
     * </ul>
     */
    private class SearchSortComparator implements Comparator<PrescriptionSearchWrapper> {
        @Override
        public int compare(PrescriptionSearchWrapper o1, PrescriptionSearchWrapper o2) {
            // if the matches are not of the same type, code matches take precedence over name matches
            final int typeOrder = o1.matchType.compareTo(o2.matchType);
            if (typeOrder != 0) {
                return typeOrder;
            }
            // sort by distance only if it's a name match (codes are always distance 0)
            if (o1.matchType == PrescriptionSearchWrapper.MatchType.NAME) {
                final int distanceOrder = o1.distance.compareTo(o2.distance);
                if (distanceOrder != 0) {
                    return distanceOrder;
                }
            }
            // sort by match index
            final int indexOrder = o1.matchIndex.compareTo(o2.matchIndex);
            if (indexOrder != 0) {
                return indexOrder;
            }
            // finally sort by prescription name
            return currentDBMgr.shortName(o1.prescription).compareTo(currentDBMgr.shortName(o2.prescription));
        }
    }

    private class MedicinesAutoCompleteFilter extends Filter {

        /**
         * Determines if we show the "Add custom med" button. Only shown if there's no exact matches for the search.
         */
        private boolean anyExactMatch;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() >= MIN_SEARCH_LEN) {
                try {
                    anyExactMatch = false;
                    final String search = constraint.toString().toLowerCase().trim();
                    final String preFilter = search.subSequence(0, MIN_SEARCH_LEN).toString();

                    // preliminary filter with the first characters of the search (exact)
                    final PreparedQuery<Prescription> prepare = DB.drugDB().prescriptions().queryBuilder()
                            .where().like(Prescription.COLUMN_NAME, "%" + preFilter + "%")
                            .or().like(Prescription.COLUMN_CODE, "%" + search + "%")
                            .prepare();
                    final CloseableIterator<Prescription> preIt = DB.drugDB().prescriptions().iterator(prepare);
                    final List<PrescriptionSearchWrapper> resultData = new ArrayList<>(500);

                    while (preIt.hasNext()) {
                        Prescription p = preIt.next();

                        final int codeIndex = p.getCode().indexOf(search);
                        if (codeIndex != -1) {
                            // code matches are only exact, fuzzy id searches make no sense
                            resultData.add(new PrescriptionSearchWrapper(p, PrescriptionSearchWrapper.MatchType.CODE, search, 0, codeIndex));
                            if (p.getCode().equals(search)) {
                                anyExactMatch = true;
                            }
                        } else {
                            final String name = currentDBMgr.shortName(p).toLowerCase();

                            final int nameIndex = name.indexOf(preFilter);
                            // check if the pre-filter is in the short name
                            if (nameIndex != -1) {
                                final String sub = name.substring(nameIndex, Math.min(nameIndex + search.length(), name.length()));
                                final int distance = LevenshteinDistance.getDefaultInstance().apply(search, sub);
                                // add to results only if distance is less than 2
                                if (distance <= MAX_LEVENSHTEIN_DISTANCE) {
                                    resultData.add(new PrescriptionSearchWrapper(p, PrescriptionSearchWrapper.MatchType.NAME, sub, distance, nameIndex));
                                }
                            }
                        }
                    }
                    preIt.close();

                    // sort results
                    Collections.sort(resultData, searchSortComparator);

                    if (!resultData.isEmpty()) {
                        final PrescriptionSearchWrapper firstResult = resultData.get(0);
                        if (firstResult.matchType == PrescriptionSearchWrapper.MatchType.NAME && firstResult.distance == 0) {
                            // Check if any word in the first result with distance 0
                            // is exactly the constraint.
                            // We need to check this because even if distance is 0 the matching
                            // may have been done with only part of a word.
                            final String sn = currentDBMgr.shortName(firstResult.getPrescription()).trim().toLowerCase();
                            final String[] split = sn.split("\\s+");
                            for (String s : split) {
                                if (s.equals(search)) {
                                    anyExactMatch = true;
                                    break;
                                }
                            }
                        }
                    }

                    filterResults.values = resultData;
                    filterResults.count = resultData.size();
                } catch (Exception e) {
                    LogUtil.e(TAG, "Exception occurred while searching for prescriptions", e);
                    filterResults.values = new ArrayList<>();
                    filterResults.count = 0;
                }
            } else {
                filterResults.values = new ArrayList<>();
                filterResults.count = 0;
            }

            LogUtil.d(TAG, "performFiltering: Results: " + filterResults.count);

            return filterResults;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mData = (List<PrescriptionSearchWrapper>) results.values;
            notifyDataSetChanged();
            medicinesSearchActivity.progressBar.setVisibility(View.GONE);
            if (results.count == 0) {
                if (constraint.length() >= MIN_SEARCH_LEN) {
                    medicinesSearchActivity.addCustomMedBtn.setText(medicinesSearchActivity.getString(R.string.add_custom_med_button_text, constraint));
                    medicinesSearchActivity.addCustomMedBtn.setVisibility(View.VISIBLE);
                    medicinesSearchActivity.emptyListText.setText(medicinesSearchActivity.getString(R.string.medicine_search_not_found_msg));
                } else {
                    medicinesSearchActivity.addCustomMedBtn.setVisibility(View.GONE);
                    medicinesSearchActivity.emptyListText.setText(medicinesSearchActivity.getString(R.string.medicine_search_empty_list_msg));
                }
            } else {
                if (!anyExactMatch) {
                    medicinesSearchActivity.addCustomMedFooter.setText(medicinesSearchActivity.getString(R.string.add_custom_med_button_text, constraint));
                    medicinesSearchActivity.addCustomMedFooter.setVisibility(View.VISIBLE);
                }
                medicinesSearchActivity.addCustomMedBtn.setVisibility(View.GONE);
            }
        }
    }
}
