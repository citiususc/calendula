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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.zxing.integration.android.IntentIntegrator;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.PreparedQuery;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenFacade;
import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.allergies.AllergyAlertUtil;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.DrivingCautionAlert;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;

public class MedicinesActivity extends CalendulaActivity implements MedicineCreateOrEditFragment.OnMedicineEditListener {

    public static final int MIN_SEARCH_LEN = 3;
    private final static String TAG = MedicinesActivity.class.getSimpleName();

//    RoutinesListFragment listFragment;
//    RoutineCreateOrEditFragment editFragment;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    @BindView(R.id.pager)
    ViewPager mViewPager;

    String qrData;
    Long mMedicineId;
    MenuItem removeItem;

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
    @BindView(R.id.add_button)
    FloatingActionButton addButton;
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
    private String intentAction;
    private String intentSearchText = null;

    private String lastSearch = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.medicines, menu);
        removeItem = menu.findItem(R.id.action_remove);
        removeItem.setVisible(mMedicineId != -1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).showDeleteConfirmationDialog(Medicine.findById(mMedicineId));
                return true;
            default:
                finish();
                return true;
        }
    }

    public void showSearchView(final String text) {
        addButton.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        searchEditText.requestFocus();
        if (text != null) {
            searchEditText.setText(text);
            searchEditText.setSelection(text.length());
            adapter.getFilter().filter(text);
        }

        searchEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(searchEditText, 0);
            }
        }, 300);

    }

    public void hideSearchView() {
        searchView.setBackgroundColor(color);
        searchView.setVisibility(View.GONE);
        addButton.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    @Override
    public void onMedicineEdited(final Medicine m) {
        // check for allergies
        if (m.isBoundToPrescription()) {
            final List<AllergenVO> vos = AllergenFacade.checkAllergies(this, DB.drugDB().prescriptions().findByCn(m.cn()));
            if (!vos.isEmpty()) {
                showAllergyDialog(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        updateMedicine(m, vos);
                    }
                });
            } else {
                updateMedicine(m, null);
            }
        } else {
            updateMedicine(m, null);
        }
    }

    @Override
    public void onMedicineCreated(final Medicine m) {

        // check for allergies
        if (m.isBoundToPrescription()) {
            final List<AllergenVO> vos = AllergenFacade.checkAllergies(this, DB.drugDB().prescriptions().findByCn(m.cn()));
            if (!vos.isEmpty()) {
                showAllergyDialog(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        createMedicine(m, vos);
                    }
                });
            } else {
                createMedicine(m, null);
            }
        } else {
            createMedicine(m, null);
        }
    }

    @Override
    public void onMedicineDeleted(Medicine m) {
        Toast.makeText(this, getString(R.string.medicine_deleted_message), Toast.LENGTH_SHORT).show();
        DB.medicines().deleteCascade(m, true);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility() == View.VISIBLE && mMedicineId != -1) {
            hideSearchView();
        } else {
            super.onBackPressed();
        }
    }

    public void doScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setOrientationLocked(true);
        integrator.setTimeout(30 * 1000);
        integrator.setBeepEnabled(false);
        integrator.setPrompt(getString(R.string.scan_barcode));
        integrator.initiateScan();
    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);
        ButterKnife.bind(this);

        color = DB.patients().getActive(this).color();
        setupToolbar(null, color);
        setupStatusBar(color);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        dbMgr = DBRegistry.instance().current();

        processIntent();

        TextView title = ((TextView) findViewById(R.id.textView2));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).onEdit();
            }
        });

        closeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchView("");
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
                Log.d(TAG, "afterTextChanged: " + s.toString());
                progressBar.setVisibility(View.VISIBLE);
                addCustomMedFooter.setVisibility(View.GONE);
                String filter = searchEditText.getText().toString();
                adapter.getFilter().filter(filter);
            }
        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Prescription p = (Prescription) parent.getItemAtPosition(position);
                hideSearchView();
                ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).setPrescription(p);
            }
        });

        searchList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.hideKeyboard(MedicinesActivity.this);
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

        barcodeBtn.setCompoundDrawables(null, null, icBarcode, null);
        barcodeBtn.setCompoundDrawablePadding(10);
        closeSearchButton.setImageDrawable(icClose);
        backButton.setImageDrawable(icBack);
        addCustomMedBtn.setVisibility(View.GONE);
        title.setBackgroundColor(color);
        searchView.setBackgroundColor(color);
        searchList.setDivider(null);
        searchList.setDividerHeight(0);

        if (mMedicineId == -1 || intentSearchText != null) {
            showSearchView(intentSearchText);
        } else {
            hideSearchView();
        }
        barcodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String contents = data.getStringExtra("SCAN_RESULT");
            String format = data.getStringExtra("SCAN_RESULT_FORMAT");
            // Handle successful scan
            Log.d(TAG, "onActivityResult: " + contents + ", " + format);
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
                    Log.d(TAG, "onCreate: " + p);
                    Toast.makeText(this, R.string.medicine_not_found_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.medicine_not_found_error, Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_CANCELED) {
            // Handle cancel
            Log.d("SCAN", "onActivityResult: Cancel");
        }
    }

    @OnClick(R.id.add_custom_med_btn)
    protected void addCustomMed() {
        hideSearchView();
        ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).setMedicineName(searchEditText.getText().toString().trim());
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private Prescription getPrescriptionFromBarcode(String barcode) {
        int len = barcode.length();
        String cn = len > 6 ? barcode.substring(len - 7, len - 1) : null;
        Log.d(TAG, "getPrescriptionFromBarcode: " + cn);
        return DB.drugDB().prescriptions().findByCn(cn);
    }

    private void processIntent() {
        mMedicineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        intentAction = getIntent().getStringExtra(CalendulaApp.INTENT_EXTRA_ACTION);
        intentSearchText = getIntent().getStringExtra("search_text");
        qrData = getIntent().getStringExtra("qr_data");
    }

    private void showAllergyDialog(final MaterialDialog.SingleButtonCallback onOk) {
        new MaterialStyledDialog.Builder(this)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_exclamation, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setTitle(R.string.title_medicine_allergy_alert)
                .setDescription(R.string.message_medicine_alert)
                .setCancelable(true)
                .setNegativeText(R.string.cancel)
                .setPositiveText(getString(R.string.ok))
                .onPositive(onOk)
                .show();
    }

    private void updateMedicine(final Medicine m, final List<AllergenVO> allergies) {
        try {
            if (allergies != null) {
                createAllergyAlerts(m, allergies);
            }
            removeOldAlerts(m);
            DB.medicines().saveAndFireEvent(m);
            Toast.makeText(this, getString(R.string.medicine_edited_message), Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException | SQLException e) {
            Snack.show(R.string.medicine_save_error_message, this);
        }
    }

    /**
     * Removes obsolete alerts for a medicine that is going to be updated.
     * This MUST be called before calling the persistence method.
     *
     * @param m the medicine
     */
    private void removeOldAlerts(final Medicine m) throws SQLException {
        Medicine old = DB.medicines().findById(m.getId());
        if (!m.cn().equals(old.cn())) { // if prescription didn't change, don't check for alerts
            AllergyAlertUtil.removeAllergyAlerts(m);
        }
    }

    private void createMedicine(final Medicine m, final List<AllergenVO> allergies) {
        try {
            DB.transaction(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    DB.medicines().save(m);
                    if (allergies != null) {
                        createAllergyAlerts(m, allergies);
                    }

                    if (m.isBoundToPrescription()) {
                        Prescription p = DB.drugDB().prescriptions().findByCn(m.cn());
                        if (p.getAffectsDriving()) {
                            AlertManager.createAlert(new DrivingCautionAlert(m));
                        }
                    }

                    DB.medicines().fireEvent();
                    return null;
                }
            });

            CalendulaApp.eventBus().post(new PersistenceEvents.MedicineAddedEvent(m.getId()));
            Toast.makeText(this, getString(R.string.medicine_created_message), Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException e) {
            Snack.show(R.string.medicine_save_error_message, this);
            Log.e(TAG, "createMedicine: ", e);
        }
    }

    private void createAllergyAlerts(final Medicine m, final List<AllergenVO> allergies) throws RuntimeException {

        AllergyPatientAlert alert = new AllergyPatientAlert(m, allergies);
        AlertManager.createAlert(alert);

    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f = new MedicineCreateOrEditFragment();
            Bundle args = new Bundle();
            args.putLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, mMedicineId);
            args.putString(CalendulaApp.INTENT_EXTRA_ACTION, intentAction);
            f.setArguments(args);
            return f;
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }

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
                        Log.e(TAG, "Exception occurred while searching for prescriptions", e);
                        filterResults.values = new ArrayList<>();
                        filterResults.count = 0;
                    }
                } else {
                    filterResults.values = new ArrayList<>();
                    filterResults.count = 0;
                }

                Log.d(TAG, "performFiltering: Results: " + filterResults.count);

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
                Presentation pres = dbMgr.expected(p);

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
                            nameView.setText(Strings.getHighlighted(name, index, index + searchLength, hColor));
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
                        ProspectUtils.openProspect(p, MedicinesActivity.this, true);
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
