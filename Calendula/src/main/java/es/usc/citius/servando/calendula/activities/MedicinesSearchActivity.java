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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

public class MedicinesSearchActivity extends CalendulaActivity implements MedicinesSearchAutoCompleteAdapter.MedicineSearchListener {

    public static final String EXTRA_SEARCH_TERM = "MedicinesSearchActivity.extras.EXTRA_SEARCH_TERM";
    public static final String RETURN_EXTRA_PRESCRIPTION = "MedicineSearchActivity.return.extras.PRESCRIPTION";
    public static final String RETURN_EXTRA_PRESCRIPTION_NAME = "MedicineSearchActivity.return.extras.PRESCRIPTION_NAME";

    private static final String TAG = "MedicinesSearchActivity";

    @BindView(R.id.search_view)
    View searchView;
    @BindView(R.id.search_edit_text)
    EditText searchEditText;
    @BindView(R.id.close_search_button)
    ImageButton clearSearchButton;
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
    @BindView(android.R.id.empty)
    View emptyView;
    @BindView(R.id.med_list_empty_icon)
    ImageView emptyIcon;


    private Button addCustomMedFooter;
    private MedicinesSearchAutoCompleteAdapter adapter;

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
    @UiThread
    public void onFilterResults(@NonNull String constraint, boolean anyExactMatch, int resultCount) {
        progressBar.setVisibility(View.GONE);
        if (resultCount == 0) {
            if (constraint.length() >= MedicinesSearchAutoCompleteAdapter.MIN_SEARCH_LEN) {
                addCustomMedBtn.setText(getString(R.string.add_custom_med_button_text, constraint));
                addCustomMedBtn.setVisibility(View.VISIBLE);
                emptyListText.setText(getString(R.string.medicine_search_not_found_msg));
            } else {
                addCustomMedBtn.setVisibility(View.GONE);
                emptyListText.setText(getString(R.string.medicine_search_empty_list_msg));
            }
        } else {
            if (!anyExactMatch) {
                addCustomMedFooter.setText(getString(R.string.add_custom_med_button_text, constraint));
                addCustomMedFooter.setVisibility(View.VISIBLE);
            }
            addCustomMedBtn.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.close_search_button)
    void resetSearch() {
        searchEditText.setText("");
        adapter.clear();
        addCustomMedBtn.setVisibility(View.GONE);
        addCustomMedFooter.setVisibility(View.GONE);
        emptyListText.setText(getString(R.string.medicine_search_empty_list_msg));
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines_search);
        ButterKnife.bind(this);

        final int patientColor = DB.patients().getActive(this).getColor();
        setupToolbar(null, patientColor);
        setupStatusBar(patientColor);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        adapter = new MedicinesSearchAutoCompleteAdapter(this, R.layout.med_drop_down_item, this);
        searchList.setAdapter(adapter);
        searchList.setEmptyView(emptyView);

        View footerView = getLayoutInflater().inflate(R.layout.medicine_search_footer, null, false);
        addCustomMedFooter = (Button) footerView.findViewById(R.id.add_custom_med_btn);
        searchList.addFooterView(footerView);

        addCustomMedFooter.setCompoundDrawables(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(patientColor)
                .sizeDp(25).paddingDp(5), null, null, null);

        emptyIcon.setImageDrawable(new IconicsDrawable(this)
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
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                addCustomMedFooter.setVisibility(View.GONE);
                String filter = searchEditText.getText().toString();
                adapter.getFilter().filter(filter);
            }
        });

        final String search = getIntent().getStringExtra(EXTRA_SEARCH_TERM);
        if (search != null) {
            searchEditText.setText(search);
        }

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Prescription p = ((MedicinesSearchAutoCompleteAdapter.PrescriptionSearchWrapper) parent.getItemAtPosition(position)).getPrescription();
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

        if (!PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, CalendulaApp.getContext().getString(R.string.database_none_id))
                .equals(CalendulaApp.getContext().getString(R.string.database_none_id))) {
            Drawable icBarcode = new IconicsDrawable(this)
                    .icon(CommunityMaterial.Icon.cmd_barcode)
                    .colorRes(R.color.black)
                    .actionBar();
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


        clearSearchButton.setImageDrawable(icClose);
        backButton.setImageDrawable(icBack);
        addCustomMedBtn.setVisibility(View.GONE);
        searchView.setBackgroundColor(patientColor);
        searchList.setDivider(null);
        searchList.setDividerHeight(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String contents = data.getStringExtra(Intents.Scan.RESULT);
            String format = data.getStringExtra(Intents.Scan.RESULT_FORMAT);
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

}
