/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.j256.ormlite.stmt.QueryBuilder;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.database.PrescriptionDAO;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;

/**
 * Adapter for autocompleting medicine search.
 */
public class MedicinesSearchAutoCompleteAdapter extends ArrayAdapter<MedicinesSearchAutoCompleteAdapter.PrescriptionSearchWrapper> implements Filterable {

    static final int MIN_SEARCH_LEN = 3;
    private static final int HIGHLIGHT_COLOR = Color.BLACK;
    private static final int MAX_LEVENSHTEIN_DISTANCE = 2;
    private static final String TAG = "MedicinesSearchAutoComp";

    private final Comparator<PrescriptionSearchWrapper> searchSortComparator = new SearchSortComparator();
    private final Drawable icProspect;
    private final Filter filter;
    private final int patientColor;
    private final MedicineSearchListener resultListener;
    private final PrescriptionDBMgr currentDBMgr;

    private List<PrescriptionSearchWrapper> mData;

    MedicinesSearchAutoCompleteAdapter(Context context, int textViewResourceId, @Nullable MedicineSearchListener listener) {
        super(context, textViewResourceId);
        this.resultListener = listener;
        this.mData = new ArrayList<>();
        this.patientColor = DB.patients().getActive(context).getColor();
        this.icProspect = new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_link_variant)
                .color(patientColor)
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


        ViewHolder holder;
        if (item == null) {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            item = inflater.inflate(R.layout.med_drop_down_item, parent, false);
            holder = new ViewHolder(item);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        if (mData.size() > position) {

            // Wrapper and data
            final PrescriptionSearchWrapper wrapper = mData.get(position);
            final Prescription prescription = wrapper.prescription;
            final String match = wrapper.match;
            final String name = currentDBMgr.shortName(prescription);
            final Presentation expectedPresentation = currentDBMgr.expectedPresentation(prescription);


            // highlight the matches
            switch (wrapper.matchType) {
                case NAME:
                    holder.nameView.setText(Strings.getHighlighted(name, match, HIGHLIGHT_COLOR));
                    holder.cnView.setText(prescription.getCode());
                    break;
                case CODE:
                    holder.cnView.setText(Strings.getHighlighted(prescription.getCode(), match, HIGHLIGHT_COLOR));
                    holder.nameView.setText(name);
                    break;
            }

            // setup the rest of the views
            holder.doseView.setText(prescription.getDose());
            holder.contentView.setText(prescription.getContent());
            holder.prospectIcon.setImageDrawable(icProspect);

            holder.prospectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProspectUtils.openProspect(prescription, getContext(), true);
                }
            });

            holder.prView.setImageDrawable(new IconicsDrawable(getContext())
                    .icon(expectedPresentation == null ? CommunityMaterial.Icon.cmd_help : expectedPresentation.icon())
                    .color(ScreenUtils.equivalentNoAlpha(patientColor, 0.8f))
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

    public interface MedicineSearchListener {
        public void onFilterResults(@NonNull final String constraint, final boolean anyExactMatch, final int resultCount);
    }

    static class ViewHolder {

        @BindView(R.id.prospect_icon)
        ImageButton prospectIcon;
        @BindView(R.id.prescription_cn)
        TextView cnView;
        @BindView(R.id.text1)
        TextView nameView;
        @BindView(R.id.text2)
        TextView doseView;
        @BindView(R.id.text3)
        TextView contentView;
        @BindView(R.id.presentation_image)
        ImageView prView;

        public ViewHolder(View rootView) {
            ButterKnife.bind(this, rootView);
        }
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

        final QueryBuilder<Prescription, Long> queryBuilder = DB.drugDB().prescriptions().queryBuilder();
        final PrescriptionDAO prescriptionDAO = DB.drugDB().prescriptions();

        /**
         * Determines if we show the "Add custom med" button. Only shown if there's no exact matches for the search.
         */
        private boolean anyExactMatch;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            anyExactMatch = false;

            if (constraint != null && constraint.length() >= MIN_SEARCH_LEN) {
                try {

                    final String search = constraint.toString().toLowerCase().trim();
                    final String preFilter = search.subSequence(0, MIN_SEARCH_LEN).toString();
                    final List<PrescriptionSearchWrapper> resultData = new ArrayList<>(500);


                    // 1: Match by code (full search constraint and exact match)
                    queryBuilder.reset();
                    final PreparedQuery<Prescription> codeQuery = queryBuilder
                            .where().like(Prescription.COLUMN_CODE, "%" + search + "%")
                            .prepare();
                    final CloseableIterator<Prescription> codeIterator = prescriptionDAO.iterator(codeQuery);
                    while (codeIterator.hasNext()) {
                        Prescription p = codeIterator.next();
                        final int codeIndex = p.getCode().indexOf(search);
                        resultData.add(new PrescriptionSearchWrapper(p, PrescriptionSearchWrapper.MatchType.CODE, search, 0, codeIndex));
                        if (p.getCode().equals(search)) {
                            anyExactMatch = true;
                        }

                    }
                    codeIterator.close();

                    // 2. Match by name (pre-search with MIN_SEARCH_LEN, and then fuzzy)
                    queryBuilder.reset();
                    final PreparedQuery<Prescription> nameQuery = queryBuilder
                            .where().like(Prescription.COLUMN_NAME, "%" + preFilter + "%")
                            .prepare();
                    final CloseableIterator<Prescription> nameIterator = prescriptionDAO.iterator(nameQuery);
                    while (nameIterator.hasNext()) {
                        Prescription p = nameIterator.next();
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
                    nameIterator.close();

                    // 3. Sort results
                    Collections.sort(resultData, searchSortComparator);

                    // Check if first result is an exact match
                    if (!anyExactMatch && !resultData.isEmpty()) {
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
            if (resultListener != null) {
                resultListener.onFilterResults(constraint.toString(), anyExactMatch, results.count);
            }
        }
    }
}
