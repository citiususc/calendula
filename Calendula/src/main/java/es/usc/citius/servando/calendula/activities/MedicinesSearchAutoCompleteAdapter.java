package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.text.TextUtils;
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

import org.apache.commons.lang3.StringUtils;

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
public class MedicinesSearchAutoCompleteAdapter extends ArrayAdapter<Prescription> implements Filterable {

    private static final int CN_TEXT_COLOR = Color.WHITE;
    private static final int HIGHLIGHT_COLOR = Color.BLACK;
    private static final int MIN_SEARCH_LEN = 3;
    private static final String TAG = "AutoCompleteAdapter";

    private final Filter filter;
    private final MedicinesSearchActivity medicinesSearchActivity;
    private final Drawable icProspect;

    private List<Prescription> mData;
    private String lastSearch = "";

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
    }


    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Prescription getItem(int index) {
        return mData.get(index);
    }

    @Override
    public View getView(int position, View item, ViewGroup parent) {

        if (item == null) {
            final LayoutInflater inflater = medicinesSearchActivity.getLayoutInflater();
            item = inflater.inflate(R.layout.med_drop_down_item, null);
        }
        if (mData.size() > position) {

            final Prescription p = mData.get(position);

            String name = medicinesSearchActivity.dbMgr.shortName(p);
            Presentation pres = medicinesSearchActivity.dbMgr.expectedPresentation(p);

            final String search = lastSearch.toLowerCase();
            final int searchLength = lastSearch.length();
            final String lowerCode = p.getCode().toLowerCase();
            final String lowerName = name.toLowerCase();

            ImageButton prospectIcon = ((ImageButton) item.findViewById(R.id.prospect_icon));
            TextView cnView = (TextView) item.findViewById(R.id.prescription_cn);
            TextView nameView = (TextView) item.findViewById(R.id.text1);
            TextView doseView = (TextView) item.findViewById(R.id.text2);
            TextView contentView = (TextView) item.findViewById(R.id.text3);
            ImageView prView = ((ImageView) item.findViewById(R.id.presentation_image));

            cnView.setTextColor(CN_TEXT_COLOR);
            if (lowerCode.contains(search)) {
                cnView.setText(Strings.getHighlighted(p.getCode(), search, HIGHLIGHT_COLOR));
                nameView.setText(Strings.toProperCase(name));
            } else {
                cnView.setText(p.getCode());
                if (lowerName.contains(search)) {
                    nameView.setText(Strings.getHighlighted(name, search, HIGHLIGHT_COLOR));
                } else {
                    String minSearch = search.substring(0, MIN_SEARCH_LEN);
                    int index = lowerName.indexOf(minSearch);
                    if (index >= 0) {
                        nameView.setText(Strings.getHighlighted(name, index, Math.min(index + searchLength, name.length() - 1), HIGHLIGHT_COLOR));
                    } else {
                        nameView.setText(Strings.toProperCase(name));
                    }
                }
            }

            doseView.setText(p.getDose());
            contentView.setText(p.getContent());
            prospectIcon.setImageDrawable(icProspect);

            prospectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProspectUtils.openProspect(p, medicinesSearchActivity, true);
                }
            });

            prView.setImageDrawable(new IconicsDrawable(getContext())
                    .icon(pres == null ? CommunityMaterial.Icon.cmd_help : Presentation.iconFor(pres))
                    .color(ScreenUtils.equivalentNoAlpha(medicinesSearchActivity.color, 0.8f))
                    .paddingDp(10)
                    .sizeDp(72));
        }
        return item;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class MedicinesAutoCompleteFilter extends Filter {

        private boolean exactMatch;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() >= MIN_SEARCH_LEN && !TextUtils.isEmpty(constraint)) {
                try {
                    exactMatch = false;
                    final String search = constraint.toString().toLowerCase().trim();
                    final String preFilter = search.subSequence(0, MIN_SEARCH_LEN).toString();

                    // preliminary filter with the first characters of the search (exact)
                    final PreparedQuery<Prescription> prepare = DB.drugDB().prescriptions().queryBuilder()
                            .where().like(Prescription.COLUMN_NAME, "%" + preFilter + "%")
                            .or().like(Prescription.COLUMN_CODE, "%" + search + "%")
                            .prepare();
                    final CloseableIterator<Prescription> preIt = DB.drugDB().prescriptions().iterator(prepare);
                    final List<Prescription> prescriptions = new ArrayList<>(500);
                    final LongSparseArray<Pair<Integer, Integer>> metaInfo = new LongSparseArray<>(500);

                    final PrescriptionDBMgr current = DBRegistry.instance().current();

                    while (preIt.hasNext()) {
                        Prescription p = preIt.next();

                        if (p.getCode().contains(search)) {
                            prescriptions.add(p);
                            if (p.getCode().equals(search)) {
                                exactMatch = true;
                            }
                        } else {
                            final String name = current.shortName(p).toLowerCase();

                            final int idx = name.indexOf(preFilter);
                            // check if the pre-filter is in the short name
                            if (idx >= 0) {
                                final String sub = name.substring(idx, Math.min(idx + search.length(), name.length()));
                                final int distance = StringUtils.getLevenshteinDistance(search, sub);
                                // add to results only if distance is less than 2
                                if (distance <= 2) {
                                    prescriptions.add(p);
                                    metaInfo.put(p.getId(), new Pair<>(distance, idx));
                                }
                            }
                        }
                    }
                    preIt.close();

                    // sort results by distance, then by index, then by name
                    // give priority to code matches over name matches
                    Collections.sort(prescriptions, new Comparator<Prescription>() {
                        @Override
                        public int compare(Prescription o1, Prescription o2) {
                            final Pair<Integer, Integer> meta1 = metaInfo.get(o1.getId());
                            final Pair<Integer, Integer> meta2 = metaInfo.get(o2.getId());

                            if (meta1 == null || (meta2 == null)) {
                                // null means it was a code match (no distance info)
                                // code match has priority over name match
                                if (meta2 != null) {
                                    return -1;
                                } else if (meta1 != null) {
                                    return 1;
                                } else {
                                    return current.shortName(o1).compareTo(current.shortName(o2));
                                }
                            } else {
                                final int i = meta1.first.compareTo(meta2.first);
                                if (i == 0) {
                                    final int i2 = meta1.second.compareTo(meta2.second);
                                    if (i2 == 0) {
                                        return current.shortName(o1).compareTo(current.shortName(o2));
                                    }
                                    return i2;
                                }
                                return i;
                            }
                        }
                    });

                    if (!prescriptions.isEmpty()) {
                        final Prescription first = prescriptions.get(0);
                        if (metaInfo.get(first.getId()) != null && metaInfo.get(first.getId()).first == 0) {
                            // Check if any word in the first result with distance 0
                            // is exactly the constraint.
                            // We need to check this because even if distance is 0 the matching
                            // may have been done with only part of a word.
                            final String sn = current.shortName(first).trim().toLowerCase();
                            if (sn.equals(search)) {
                                exactMatch = true;
                            } else {
                                final String[] split = sn.split("\\s+");
                                for (String s : split) {
                                    if (s.equals(search)) {
                                        exactMatch = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    //Fetcher.fetchNames(constraint.toString());
                    filterResults.values = prescriptions;
                    filterResults.count = prescriptions.size();
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
            mData = (List<Prescription>) results.values;
            notifyDataSetChanged();
            lastSearch = constraint.toString();
            medicinesSearchActivity.progressBar.setVisibility(View.GONE);
            if (results.count == 0) {
                if (constraint.length() >= MIN_SEARCH_LEN && !TextUtils.isEmpty(constraint)) {
                    medicinesSearchActivity.addCustomMedBtn.setText(medicinesSearchActivity.getString(R.string.add_custom_med_button_text, constraint));
                    medicinesSearchActivity.addCustomMedBtn.setVisibility(View.VISIBLE);
                    medicinesSearchActivity.emptyListText.setText(medicinesSearchActivity.getString(R.string.medicine_search_not_found_msg));
                } else {
                    medicinesSearchActivity.addCustomMedBtn.setVisibility(View.GONE);
                    medicinesSearchActivity.emptyListText.setText(medicinesSearchActivity.getString(R.string.medicine_search_empty_list_msg));
                }
            } else {
                if (!exactMatch) {
                    medicinesSearchActivity.addCustomMedFooter.setText(medicinesSearchActivity.getString(R.string.add_custom_med_button_text, constraint));
                    medicinesSearchActivity.addCustomMedFooter.setVisibility(View.VISIBLE);
                }
                medicinesSearchActivity.addCustomMedBtn.setVisibility(View.GONE);
            }
        }
    }
}
