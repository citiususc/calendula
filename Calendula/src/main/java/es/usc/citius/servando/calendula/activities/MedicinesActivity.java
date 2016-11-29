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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;

public class MedicinesActivity extends CalendulaActivity implements MedicineCreateOrEditFragment.OnMedicineEditListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
//    RoutinesListFragment listFragment;
//    RoutineCreateOrEditFragment editFragment;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    Long mMedicineId;
    MenuItem removeItem;
    View searchView;
    EditText searchEditText;
    ImageButton closeSearchButton;
    ImageButton backButton;
    Button addCustomMedBtn;
    TextView emptyListText;
    FloatingActionButton addButton;
    ListView searchList;
    ArrayAdapter<Prescription> adapter;
    int color;

    PrescriptionDBMgr dbMgr;

    private String intentAction;
    private String intentSearchText = null;


    private final static String TAG = MedicinesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);
        color = DB.patients().getActive(this).color();
        setupToolbar(null, color);
        setupStatusBar(color);

        dbMgr = DBRegistry.instance().current();

        processIntent();

        TextView title = ((TextView) findViewById(R.id.textView2));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        searchView = findViewById(R.id.search_view);
        closeSearchButton = (ImageButton) findViewById(R.id.close_search_button);
        backButton = (ImageButton) findViewById(R.id.back_button);
        addCustomMedBtn = (Button) findViewById(R.id.add_custom_med_btn);
        emptyListText = (TextView) findViewById(R.id.textView10);
        addButton = (FloatingActionButton) findViewById(R.id.add_button);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchList = (ListView) findViewById(R.id.search_list);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        adapter = new AutoCompleteAdapter(this, R.layout.med_drop_down_item);
        searchList.setAdapter(adapter);
        searchList.setEmptyView(findViewById(android.R.id.empty));

        addCustomMedBtn.setCompoundDrawables(new IconicsDrawable(this)
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

        addCustomMedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchView();
                ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).setMedicineName(searchEditText.getText().toString().trim());
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

        Drawable icClose = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_close_circle)
                .colorRes(R.color.white)
                .actionBar();

        Drawable icBack = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_arrow_left)
                .colorRes(R.color.white)
                .actionBar();

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
    }


    private void processIntent() {
        mMedicineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        intentAction = getIntent().getStringExtra(CalendulaApp.INTENT_EXTRA_ACTION);
        intentSearchText = getIntent().getStringExtra("search_text");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.medicines, menu);
        removeItem = menu.findItem(R.id.action_remove);
        removeItem.setVisible(mMedicineId != -1 ? true : false);
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

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
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
        private List<Prescription> mData;
        int minCharsToSearch = 2;
        int hColor = Color.parseColor("#000000");
        int cnColor = Color.WHITE;//ScreenUtils.equivalentNoAlpha(color,0.8f);
        int cnHColor = hColor;
        Drawable icProspect;

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
                String match = searchEditText.getText().toString().trim();
                Presentation pres = dbMgr.expected(p);

                ImageButton prospectIcon = ((ImageButton) item.findViewById(R.id.prospect_icon));
                TextView cnView = (TextView) item.findViewById(R.id.prescription_cn);
                TextView nameView = (TextView) item.findViewById(R.id.text1);
                TextView doseView = (TextView) item.findViewById(R.id.text2);
                TextView contentView = (TextView) item.findViewById(R.id.text3);
                ImageView prView = ((ImageView) item.findViewById(R.id.presentation_image));

                cnView.setTextColor(cnColor);
                nameView.setText(Strings.getHighlighted(name, match, hColor));
                doseView.setText(p.getDose());
                contentView.setText(p.getContent());
                cnView.setText(Strings.getHighlighted(p.getCode(), match, cnHColor));
                prospectIcon.setImageDrawable(icProspect);

                prospectIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProspectUtils.openProspect(p, MedicinesActivity.this, false);
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
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null && constraint.length() >= minCharsToSearch && !TextUtils.isEmpty(constraint)) {
                        try {
                            List<Prescription> prescriptions = DB.drugDB().prescriptions().findByNameOrCn(constraint.toString(), 500);
                            mData = prescriptions;//Fetcher.fetchNames(constraint.toString());
                            filterResults.values = mData;
                            filterResults.count = mData.size();
                        } catch (Exception e) {
                            Log.e("myException", e.getMessage());
                            filterResults.values = null;
                            filterResults.count = 0;
                        }
                    } else {
                        mData = new ArrayList<>();
                        filterResults.values = mData;
                        filterResults.count = 0;
                    }

                    Log.d(TAG, "performFiltering: Results: " + filterResults.count);

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                    if (results.count == 0) {
                        if (constraint.length() >= minCharsToSearch && !TextUtils.isEmpty(constraint)) {
                            addCustomMedBtn.setText(getString(R.string.add_custom_med_button_text, constraint));
                            addCustomMedBtn.setVisibility(View.VISIBLE);
                            emptyListText.setText(getString(R.string.medicine_search_not_found_msg));
                        } else {
                            emptyListText.setText(getString(R.string.medicine_search_empty_list_msg));
                        }
                    } else {
                        addCustomMedBtn.setVisibility(View.GONE);
                    }
                }
            };
            return myFilter;
        }
    }
}
