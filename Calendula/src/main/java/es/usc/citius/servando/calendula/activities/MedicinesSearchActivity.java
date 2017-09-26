/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.zxing.integration.android.IntentIntegrator;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;

public class MedicinesSearchActivity extends CalendulaActivity {

    public static final String EXTRA_SEARCH_TERM = "MedicinesSearchActivity.extras.EXTRA_SEARCH_TERM";
    public static final String RETURN_EXTRA_PRESCRIPTION = "MedicineSearchActivity.return.extras.PRESCRIPTION";
    public static final String RETURN_EXTRA_PRESCRIPTION_NAME = "MedicineSearchActivity.return.extras.PRESCRIPTION_NAME";

    private static final int MIN_SEARCH_LEN = 3;
    private static final String TAG = "MedicinesSearchActivity";

    @BindView(R.id.search_view)
    View searchView;
    @BindView(R.id.search_edit_text)
    EditText searchEditText;
    @BindView(R.id.close_search_button)
    ImageButton closeSearchButton;
    @BindView(R.id.back_button)
    ImageButton backButton;
    @BindView(R.id.textView10)
    TextView emptyListText;
    @BindView(R.id.search_list)
    ListView searchList;
    @BindView(R.id.main_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.add_custom_med_btn)
    Button addCustomMedBtn;
    @BindView(R.id.barcode_scan_btn)
    Button barcodeBtn;


    Button addCustomMedFooter;

    ArrayAdapter<Prescription> adapter;

    int color;
    PrescriptionDBMgr dbMgr;

    private String lastSearch = "";


    public void doScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setOrientationLocked(true);
        integrator.setTimeout(30 * 1000);
        integrator.setBeepEnabled(false);
        integrator.setPrompt(getString(R.string.scan_barcode));
        integrator.initiateScan();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void askForDatabase() {

        new MaterialStyledDialog.Builder(this)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_database, R.color.white, 100))
                .setHeaderColor(R.color.android_blue)
                .withDialogAnimation(true)
                .setTitle(R.string.enable_prescriptions_dialog_title)
                .setDescription(R.string.enable_prescriptions_dialog_message)
                .setCancelable(false)
                .setPositiveText(getString(R.string.enable_prescriptions_dialog_yes))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent i = new Intent(MedicinesSearchActivity.this, SettingsActivity.class);
                        i.putExtra(SettingsActivity.EXTRA_SHOW_DB_DIALOG, true);
                        startActivity(i);
                        PreferenceUtils.edit().putBoolean(PreferenceKeys.MEDICINES_USE_PRESCRIPTIONS_SHOWN.key(), true).apply();
                    }
                })
                .setNegativeText(R.string.enable_prescriptions_dialog_no)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                        PreferenceUtils.edit().putBoolean(PreferenceKeys.MEDICINES_USE_PRESCRIPTIONS_SHOWN.key(), true).apply();
                    }
                })
                .show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines_search);
        ButterKnife.bind(this);

        color = DB.patients().getActive(this).color();
        setupToolbar(null, color);
        setupStatusBar(color);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        dbMgr = DBRegistry.instance().current();

        adapter = new AutoCompleteAdapter(this, R.layout.med_drop_down_item);
        searchList.setAdapter(adapter);
        searchList.setEmptyView(findViewById(android.R.id.empty));

        View footerView = getLayoutInflater().inflate(R.layout.medicine_search_footer, null, false);
        addCustomMedFooter = (Button) footerView.findViewById(R.id.add_custom_med_btn);
        searchList.addFooterView(footerView);

        addCustomMedFooter.setCompoundDrawables(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(color)
                .sizeDp(25).paddingDp(5), null, null, null);

        ((ImageView) findViewById(R.id.med_list_empty_icon)).setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_magnify)
                .colorRes(R.color.black_20)
                .sizeDp(80).paddingDp(5)
        );

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addCustomMedFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCustomMed();
            }
        });

        closeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LogUtil.d(TAG, "afterTextChanged: " + s.toString());
                progressBar.setVisibility(View.VISIBLE);
                addCustomMedFooter.setVisibility(View.GONE);
                String filter = searchEditText.getText().toString();
                adapter.getFilter().filter(filter);
            }
        });

        final String search = getIntent().getStringExtra(EXTRA_SEARCH_TERM);
        if(search!=null){
            searchEditText.setText(search);
        }

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Prescription p = (Prescription) parent.getItemAtPosition(position);
                Intent i = new Intent();
                i.putExtra(RETURN_EXTRA_PRESCRIPTION, p);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        searchList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.hideKeyboard(MedicinesSearchActivity.this);
                return false;
            }
        });

        Drawable icClose = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_close_circle)
                .colorRes(R.color.white)
                .actionBar();

        Drawable icBack = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_arrow_left)
                .colorRes(R.color.white)
                .actionBar();

        Drawable icBarcode = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_barcode)
                .colorRes(R.color.black)
                .actionBar();

        if (!PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, CalendulaApp.getContext().getString(R.string.database_none_id))
                .equals(CalendulaApp.getContext().getString(R.string.database_none_id))) {
            barcodeBtn.setCompoundDrawables(null, null, icBarcode, null);
            barcodeBtn.setCompoundDrawablePadding(10);
            barcodeBtn.setVisibility(View.VISIBLE);
            barcodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doScan();
                }
            });
        } else {
            if (!PreferenceUtils.getBoolean(PreferenceKeys.MEDICINES_USE_PRESCRIPTIONS_SHOWN, false)) {
                askForDatabase();
            }
        }


        closeSearchButton.setImageDrawable(icClose);
        backButton.setImageDrawable(icBack);
        addCustomMedBtn.setVisibility(View.GONE);
        searchView.setBackgroundColor(color);
        searchList.setDivider(null);
        searchList.setDividerHeight(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String contents = data.getStringExtra("SCAN_RESULT");
            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
            // Handle successful scan
            LogUtil.d(TAG, "onActivityResult: " + contents + ", " + format);
            if (contents != null) {
                final Prescription p = getPrescriptionFromBarcode(contents);
                if (p != null) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            //hideSearchView();
                            //((MedicineCreateOrEditFragment) getViewPagerFragment(0)).setPrescription(p);
                            searchEditText.setText(p.getCode());
                        }
                    });
                } else {
                    LogUtil.d(TAG, "onCreate: " + p);
                    Toast.makeText(this, R.string.medicine_not_found_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.medicine_not_found_error, Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            // Handle cancel
            LogUtil.d(TAG, "onActivityResult: Cancel");
        }
    }

    @OnClick(R.id.add_custom_med_btn)
    protected void addCustomMed() {
        final String searchText = searchEditText.getText().toString().trim();
        Intent i = new Intent();
        i.putExtra(RETURN_EXTRA_PRESCRIPTION_NAME, searchText);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private Prescription getPrescriptionFromBarcode(String barcode) {
        int len = barcode.length();
        String cn = len > 6 ? barcode.substring(len - 7, len - 1) : null;
        LogUtil.d(TAG, "getPrescriptionFromBarcode: " + cn);
        return DB.drugDB().prescriptions().findByCn(cn);
    }

    // Search adapter
    public class AutoCompleteAdapter extends ArrayAdapter<Prescription> implements Filterable {
        int hColor = Color.parseColor("#000000");
        int cnColor = Color.WHITE;//ScreenUtils.equivalentNoAlpha(color,0.8f);
        int cnHColor = hColor;
        Drawable icProspect;
        private List<Prescription> mData;
        final Filter filter = new Filter() {

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
                progressBar.setVisibility(View.GONE);
                if (results.count == 0) {
                    if (constraint.length() >= MIN_SEARCH_LEN && !TextUtils.isEmpty(constraint)) {
                        addCustomMedBtn.setText(getString(R.string.add_custom_med_button_text, constraint));
                        addCustomMedBtn.setVisibility(View.VISIBLE);
                        emptyListText.setText(getString(R.string.medicine_search_not_found_msg));
                    } else {
                        addCustomMedBtn.setVisibility(View.GONE);
                        emptyListText.setText(getString(R.string.medicine_search_empty_list_msg));
                    }
                } else {
                    if (!exactMatch) {
                        addCustomMedFooter.setText(getString(R.string.add_custom_med_button_text, constraint));
                        addCustomMedFooter.setVisibility(View.VISIBLE);
                    }
                    addCustomMedBtn.setVisibility(View.GONE);
                }
            }
        };

        public AutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mData = new ArrayList<>();
            icProspect = new IconicsDrawable(getContext())
                    .icon(CommunityMaterial.Icon.cmd_link_variant)
                    .color(color)
                    .paddingDp(10)
                    .sizeDp(40);
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
                final LayoutInflater inflater = getLayoutInflater();
                item = inflater.inflate(R.layout.med_drop_down_item, null);
            }
            if (mData.size() > position) {

                final Prescription p = mData.get(position);

                String name = dbMgr.shortName(p);
                Presentation pres = dbMgr.expectedPresentation(p);

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

                cnView.setTextColor(cnColor);
                if (lowerCode.contains(search)) {
                    cnView.setText(Strings.getHighlighted(p.getCode(), search, cnHColor));
                    nameView.setText(Strings.toProperCase(name));
                } else {
                    cnView.setText(p.getCode());
                    if (lowerName.contains(search)) {
                        nameView.setText(Strings.getHighlighted(name, search, hColor));
                    } else {
                        String minSearch = search.substring(0, MIN_SEARCH_LEN);
                        int index = lowerName.indexOf(minSearch);
                        if (index >= 0) {
                            nameView.setText(Strings.getHighlighted(name, index, Math.min(index + searchLength, name.length() - 1), hColor));
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
                        ProspectUtils.openProspect(p, MedicinesSearchActivity.this, true);
                    }
                });

                prView.setImageDrawable(new IconicsDrawable(getContext())
                        .icon(pres == null ? CommunityMaterial.Icon.cmd_help : Presentation.iconFor(pres))
                        .color(ScreenUtils.equivalentNoAlpha(color, 0.8f))
                        .paddingDp(10)
                        .sizeDp(72));
            }
            return item;
        }

        @Override
        public Filter getFilter() {
            return filter;
        }
    }
}
