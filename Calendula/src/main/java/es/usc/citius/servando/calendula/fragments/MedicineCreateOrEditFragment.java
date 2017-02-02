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

package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.j256.ormlite.stmt.PreparedQuery;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.LocalDate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SettingsActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.StockModule;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.medicine.StockUtils;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class MedicineCreateOrEditFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        NumberPickerDialogFragment.NumberPickerDialogHandler {


    private static final int DIALOG_STOCK_ADD = 1;
    private static final int DIALOG_STOCK_REMOVE = 2;

    private static final String TAG = "MedicineCreateOrEditFr";
    OnMedicineEditListener mMedicineEditCallback;
    Medicine mMedicine;
    Prescription mPrescription;

    Boolean showConfirmButton = true;

    @BindView(R.id.medicine_edit_name)
    TextView mNameTextView;
    @BindView(R.id.textView3)
    TextView mPresentationTv;

    Presentation selectedPresentation;

    @BindView(R.id.med_presentation_scroll)
    HorizontalScrollView presentationScroll;
    @BindView(R.id.scrollView)
    ScrollView verticalScrollView;

    @BindView(R.id.stock_layout)
    RelativeLayout stockLayout;
    @BindView(R.id.stock_units)
    TextView mStockUnits;
    @BindView(R.id.stock_estimated_duration)
    TextView mStockEstimation;
    @BindView(R.id.stock_switch)
    Switch stockSwitch;
    @BindView(R.id.btn_stock_add)
    ImageButton addBtn;
    @BindView(R.id.btn_stock_remove)
    ImageButton rmBtn;
    @BindView(R.id.btn_stock_reset)
    ImageButton resetBtn;

    boolean enableSearch = false;
    long mMedicineId;
    int pColor;
    PrescriptionDBMgr dbMgr;

    float stock = -1;
    String estimatedStockText = "";
    private String mIntentAction;

    private Unbinder unbinder;

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMgr = DBRegistry.instance().current();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_medicine, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        if (ModuleManager.isEnabled(StockModule.ID)) {
            stockLayout.setVisibility(View.VISIBLE);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        pColor = DB.patients().getActive(getActivity()).color();
        setupIcons(rootView);

        mNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence text = mNameTextView.getText();
                ((MedicinesActivity) getActivity()).showSearchView(text != null ? text.toString() : null);
            }
        });

        mNameTextView.setCompoundDrawables(null, null, new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_arrow_top_right)
                .color(pColor)
                .sizeDp(30).paddingDp(5), null);


        String none = getString(R.string.database_none_id);
        String settingUp = getString(R.string.database_setting_up);
        String value = prefs.getString(PreferenceKeys.DRUGDB_CURRENT_DB, none);
        enableSearch = !value.equals(none) && !value.equals(settingUp);

        Log.d(getTag(), "Arguments:  " + (getArguments() != null) + ", savedState: " + (savedInstanceState != null));
        if (getArguments() != null) {
            mIntentAction = getArguments().getString(CalendulaApp.INTENT_EXTRA_ACTION);
            mMedicineId = getArguments().getLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        }

        if (mMedicineId == -1 && savedInstanceState != null) {
            mMedicineId = savedInstanceState.getLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        }

        if (mMedicineId != -1) {
            mMedicine = Medicine.findById(mMedicineId);
        }

        setupMedPresentationChooser(rootView);

        mNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = mNameTextView.getText().toString();

                if (mPrescription != null && !dbMgr.shortName(mPrescription).toLowerCase().equals(name.toLowerCase())) {
                    mPrescription = null;
                }

            }
        });

        setupStockViews();
        if (mIntentAction == null) {
            mNameTextView.requestFocus();
            askForPrescriptionUsage();
        } else if ("add_stock".equals(mIntentAction)) {
            showStockDialog(DIALOG_STOCK_ADD);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mMedicine != null) {
            setMedicne(mMedicine);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean validate() {
        if (mNameTextView.getText() != null && mNameTextView.getText().length() > 0) {
            if (selectedPresentation == null) {
                hideKeyboard();
                Snack.show(R.string.medicine_no_presentation_error_message, getActivity());
                return false;
            }
            return true;
        } else {
            mNameTextView.setError(getString(R.string.medicine_no_name_error_message));
            mNameTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mNameTextView.setError(null);
                    mNameTextView.removeTextChangedListener(this);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            return false;
        }
    }

    public void scrollToMedPresentation(View view) {
        Log.d(getTag(), "Scroll to: " + view.getLeft());

        int amount = view.getLeft();
        if (amount < (0.8 * presentationScroll.getWidth())) {
            amount -= 30;
        }
        presentationScroll.smoothScrollTo(amount, 0);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMedicine != null && mMedicine.getId() != null)
            outState.putLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, mMedicine.getId());
    }

    public void setMedicne(Medicine r) {
        Log.d(getTag(), "Medicine set: " + r.name());
        mMedicine = r;
        mNameTextView.setText(mMedicine.name());
        mPresentationTv.setText(": " + mMedicine.presentation().getName(getResources()));
        selectedPresentation = mMedicine.presentation();
        selectPresentation(mMedicine.presentation());

        if (r.cn() != null) {
            Prescription p = DB.drugDB().prescriptions().findByCn(r.cn());
            if (p != null) {
                mPrescription = p;
//                mDescriptionTv.setText(p.getName());
                new ComputeEstimatedStockEndTask().execute();
            }
        }
    }

    public void setPrescription(Prescription p) {
        mNameTextView.setText(Strings.toProperCase(dbMgr.shortName(p)));
        mPrescription = p;
        Presentation pr = DBRegistry.instance().current().expected(p);
        if (pr != null) {
            mPresentationTv.setText(": " + pr.getName(getResources()));
            selectedPresentation = pr;
            selectPresentation(pr);
        }
    }

    public void setMedicineName(String medName) {
        mNameTextView.setText(medName);
        mPrescription = null;
    }

    public void clear() {
        mMedicine = null;
        mNameTextView.setText("");
    }

    public void onEdit() {

        String name = mNameTextView.getText().toString();

        if (name != null && name.length() > 0) {

            // if editing
            if (mMedicine != null) {
                mMedicine.setName(name);
                mMedicine.setStock(stockSwitch.isChecked() ? stock : -1);
                if (selectedPresentation != null) {
                    mMedicine.setPresentation(selectedPresentation);
                }
                if (mPrescription != null) {
                    mMedicine.setCn(String.valueOf(mPrescription.getCode()));
                    mMedicine.setDatabase(DBRegistry.instance().current().id());
                } else if (mPrescription == null) {
                    mMedicine.setCn(null);
                }

                if (mMedicineEditCallback != null && !checkIfDuplicate(mMedicine)) {
                    mMedicineEditCallback.onMedicineEdited(mMedicine);
                }
            }
            // if creating
            else {

                if (!validate()) {
                    return;
                }

                Medicine m = new Medicine(name);
                if (mPrescription != null) {
                    m.setCn(String.valueOf(mPrescription.getCode()));
                    m.setDatabase(DBRegistry.instance().current().id());
                }
                m.setStock(stockSwitch.isChecked() ? stock : -1);
                m.setPresentation(selectedPresentation != null ? selectedPresentation : Presentation.UNKNOWN);
                m.setPatient(DB.patients().getActive(getContext()));

                if (mMedicineEditCallback != null && !checkIfDuplicate(m)) {
                    mMedicineEditCallback.onMedicineCreated(m);
                }
            }
        } else {
            Snack.show(R.string.medicine_no_name_error_message, getActivity());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnMedicineEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnMedicineEditListener) {
            mMedicineEditCallback = (OnMedicineEditListener) activity;
        }
        if (activity instanceof ScheduleCreationActivity) {
            this.showConfirmButton = false;
        }
    }

    public void askForPrescriptionUsage() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean adviceShown = prefs.getBoolean("show_use_prescriptions_advice", false);
        boolean dbEnabled = !prefs.getString(PreferenceKeys.DRUGDB_CURRENT_DB, getString(R.string.database_none_id)).equals(getString(R.string.database_none_id));

        if (!adviceShown && !dbEnabled) {
            new MaterialStyledDialog.Builder(getActivity())
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setIcon(IconUtils.icon(getActivity(), CommunityMaterial.Icon.cmd_database, R.color.white, 100))
                    .setHeaderColor(R.color.android_blue)
                    .withDialogAnimation(true)
                    .setTitle(R.string.enable_prescriptions_dialog_title)
                    .setDescription(R.string.enable_prescriptions_dialog_message)
                    .setCancelable(false)
                    .setPositiveText(getString(R.string.enable_prescriptions_dialog_yes))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent i = new Intent(getActivity(), SettingsActivity.class);
                            i.putExtra("show_database_dialog", true);
                            startActivity(i);
                        }
                    })
                    .setNegativeText(R.string.enable_prescriptions_dialog_no)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .show();

        } else {
            if (mMedicine == null) {
                showSoftInput();
            }
        }
        prefs.edit().putBoolean("show_use_prescriptions_advice", true).apply();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceKeys.DRUGDB_CURRENT_DB.equals(key)) {
            String none = getString(R.string.database_none_id);
            String settingUp = getString(R.string.database_setting_up);
            String value = sharedPreferences.getString(PreferenceKeys.DRUGDB_CURRENT_DB, none);
            enableSearch = !value.equals(none) && !value.equals(settingUp);
            if (enableSearch) {
//                enableSearchButton();
            } else {
//                searchButton.setVisibility(View.GONE);
            }
        }
    }

    public void setupIcons(View root) {
        Context c = getActivity();
        int color = R.color.dark_grey_home;
        int size = 24;
        Drawable ic1 = IconUtils.icon(c, CommunityMaterial.Icon.cmd_pencil, color, size, 4);
        Drawable ic2 = IconUtils.icon(c, CommunityMaterial.Icon.cmd_eye, color, size, 4);
        Drawable ic3 = IconUtils.icon(c, CommunityMaterial.Icon.cmd_basket, color, size, 4);
        ((ImageView) root.findViewById(R.id.ic_med_name)).setImageDrawable(ic1);
        ((ImageView) root.findViewById(R.id.ic_med_presentation)).setImageDrawable(ic2);
        ((ImageView) root.findViewById(R.id.ic_med_stock)).setImageDrawable(ic3);
    }

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {

        float amount = (float) fullNumber;
        if (reference == DIALOG_STOCK_ADD) {
            stock = (stock == -1) ? amount : (stock + amount);
        } else if (reference == DIALOG_STOCK_REMOVE) {
            if (amount >= stock)
                stock = 0;
            else
                stock -= amount;
        }
        new ComputeEstimatedStockEndTask().execute();
    }

    void updateStockText() {
        String units = selectedPresentation != null ? selectedPresentation.units(getResources()) : Presentation.UNKNOWN.units(getResources());
        String text = stock == -1 ? "No stock info" : (stock + " " + units + "(s)");
        mStockEstimation.setVisibility(estimatedStockText != null ? View.VISIBLE : View.INVISIBLE);
        mStockEstimation.setText(estimatedStockText != null ? estimatedStockText : "");
        mStockUnits.setText(text);
    }

    void showStockDialog(int ref) {
        NumberPickerBuilder npb =
                new NumberPickerBuilder()
                        .setMinNumber(1)
                        //.setLabelText(ref == DIALOG_STOCK_ADD ? "Increase stock by" : "Decrease stock by")
                        .setDecimalVisibility(NumberPicker.VISIBLE)
                        .setPlusMinusVisibility(NumberPicker.INVISIBLE)
                        .setFragmentManager(getChildFragmentManager())
                        .setTargetFragment(this).setReference(ref)
                        .setStyleResId(R.style.BetterPickersDialogFragment_Calendula);
        npb.show();
    }

    void setupMedPresentationChooser(final View rootView) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickMedicine(view.getId(), rootView);
            }
        };
        for (View v : getViewsByTag((ViewGroup) rootView, "med_type")) {
            ImageView iv = (ImageView) v;
            iv.setOnClickListener(listener);
            switch (v.getId()) {

                case R.id.med_presentation_2:
                    iv.setImageDrawable(iconFor(Presentation.CAPSULES));
                    break;
                case R.id.med_presentation_3:
                    iv.setImageDrawable(iconFor(Presentation.EFFERVESCENT));
                    break;
                case R.id.med_presentation_4:
                    iv.setImageDrawable(iconFor(Presentation.PILLS));
                    Log.d(getTag(), "Pill");
                    break;
                case R.id.med_presentation_5:
                    iv.setImageDrawable(iconFor(Presentation.SYRUP));
                    break;
                case R.id.med_presentation_6:
                    iv.setImageDrawable(iconFor(Presentation.DROPS));
                    break;
                case R.id.med_presentation_7:
                    iv.setImageDrawable(iconFor(Presentation.SPRAY));
                    break;
                case R.id.med_presentation_8:
                    iv.setImageDrawable(iconFor(Presentation.INHALER));
                    break;
                case R.id.med_presentation_9:
                    iv.setImageDrawable(iconFor(Presentation.INJECTIONS));
                    break;
                case R.id.med_presentation_10:
                    iv.setImageDrawable(iconFor(Presentation.POMADE));
                    break;
                case R.id.med_presentation_11:
                    iv.setImageDrawable(iconFor(Presentation.PATCHES));
                    break;
            }

        }
    }

    IconicsDrawable iconFor(Presentation p) {
        return new IconicsDrawable(getContext())
                .icon(Presentation.iconFor(p))
                //.color(pColor)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(5)
                .sizeDp(80);
    }

    void onClickMedicine(int viewId, View rootView) {

        for (View v : getViewsByTag((ViewGroup) rootView, "med_type")) {
            v.setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        rootView.findViewById(viewId).setBackgroundResource(R.drawable.presentation_circle_background);

        switch (viewId) {

            case R.id.med_presentation_2:
                selectedPresentation = Presentation.CAPSULES;
                Log.d(getTag(), "Capsule");
                break;
            case R.id.med_presentation_3:
                selectedPresentation = Presentation.EFFERVESCENT;
                Log.d(getTag(), "Effervescent");
                break;
            case R.id.med_presentation_4:
                selectedPresentation = Presentation.PILLS;
                Log.d(getTag(), "Pill");
                break;
            case R.id.med_presentation_5:
                selectedPresentation = Presentation.SYRUP;
                Log.d(getTag(), "Syrup");
                break;
            case R.id.med_presentation_6:
                selectedPresentation = Presentation.DROPS;
                Log.d(getTag(), "Drops");
                break;
            case R.id.med_presentation_7:
                selectedPresentation = Presentation.SPRAY;
                Log.d(getTag(), "Spray");
                break;
            case R.id.med_presentation_8:
                selectedPresentation = Presentation.INHALER;
                Log.d(getTag(), "Drops");
                break;
            case R.id.med_presentation_9:
                selectedPresentation = Presentation.INJECTIONS;
                Log.d(getTag(), "Injection");
                break;
            case R.id.med_presentation_10:
                selectedPresentation = Presentation.POMADE;
                Log.d(getTag(), "Pomade");
                break;
            case R.id.med_presentation_11:
                selectedPresentation = Presentation.PATCHES;
                Log.d(getTag(), "Patches");
                break;
        }

        if (selectedPresentation != null) {
            mPresentationTv.setText(": " + selectedPresentation.getName(getResources()));
            updateStockText();
        }
    }

    int getPresentationViewId(Presentation pres) {
        switch (pres) {
            case INJECTIONS:
                return R.id.med_presentation_9;
            case POMADE:
                return R.id.med_presentation_10;
            case CAPSULES:
                return R.id.med_presentation_2;
            case EFFERVESCENT:
                return R.id.med_presentation_3;
            case PILLS:
                return R.id.med_presentation_4;
            case SYRUP:
                return R.id.med_presentation_5;
            case DROPS:
                return R.id.med_presentation_6;
            case SPRAY:
                return R.id.med_presentation_7;
            case INHALER:
                return R.id.med_presentation_8;
            case PATCHES:
                return R.id.med_presentation_11;
            default:
                return -1;
        }
    }


    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameTextView.getWindowToken(), 0);
    }

    private void setupStockViews() {
        Context c = getActivity();

        // TODO: 1/02/17 use some better icons, these are temporary
        Drawable add = IconUtils.icon(c, CommunityMaterial.Icon.cmd_plus, R.color.agenda_item_title, 26, 4);
        Drawable remove = IconUtils.icon(c, CommunityMaterial.Icon.cmd_minus, R.color.agenda_item_title, 26, 4);
        Drawable reset = IconUtils.icon(c, CommunityMaterial.Icon.cmd_reload, R.color.agenda_item_title, 26, 4);

        addBtn.setImageDrawable(add);
        rmBtn.setImageDrawable(remove);
        resetBtn.setImageDrawable(reset);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStockDialog(DIALOG_STOCK_ADD);
            }
        });

        rmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStockDialog(DIALOG_STOCK_REMOVE);
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: resetting stock...");
                setDefaultStock();
                updateStockText();
            }
        });

        stockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStockControlsVisibility();
                if (isChecked) {
                    // user is enabling stock first time
                    if (stock == -1)
                        setDefaultStock();

                    verticalScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            verticalScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            }
        });

        stock = mMedicine != null && mMedicine.stock() != null ? mMedicine.stock() : -1;

        if (stock > -1) {
            stockSwitch.setChecked(true);
        }

        updateStockControlsVisibility();
        updateStockText();
    }

    private void setDefaultStock() {
        if (mPrescription != null && mPrescription.getPackagingUnits() > 0) {
            stock = mPrescription.getPackagingUnits();
            new ComputeEstimatedStockEndTask().execute();
        }
    }

    private void updateStockControlsVisibility() {
        int visibility = stockSwitch.isChecked() ? View.VISIBLE : View.INVISIBLE;
        mStockUnits.setVisibility(visibility);
        mStockEstimation.setVisibility(stockSwitch.isChecked() & estimatedStockText != null ? View.VISIBLE : View.INVISIBLE);
        addBtn.setVisibility(visibility);
        rmBtn.setVisibility(visibility);
        int resetVisibility = stockSwitch.isChecked() && mPrescription != null ? View.VISIBLE : View.GONE;
        resetBtn.setVisibility(resetVisibility);
    }

    private void selectPresentation(Presentation p) {
        for (View v : getViewsByTag((ViewGroup) getView(), "med_type")) {
            v.setBackgroundColor(getResources().getColor(R.color.transparent));
        }

        if (p != null) {
            int viewId = getPresentationViewId(p);
            View view = getView().findViewById(viewId);
            view.setBackgroundResource(R.drawable.presentation_circle_background);
            mPresentationTv.setText(": " + p.getName(getResources()));
            scrollToMedPresentation(view);
        }
    }

    private boolean checkIfDuplicate(final Medicine m) {
        try {
            List<Medicine> others;
            if (m.isBoundToPrescription()) {
                final String cn = m.cn();
                final PreparedQuery<Medicine> query = DB.medicines().queryBuilder().where()
                        .eq(Medicine.COLUMN_PATIENT, m.patient())
                        .and().eq(Medicine.COLUMN_CN, cn).prepare();
                others = DB.medicines().query(query);
            } else {
                final String name = m.name();
                final Presentation presentation = m.presentation();
                final PreparedQuery<Medicine> query = DB.medicines().queryBuilder().where()
                        .eq(Medicine.COLUMN_PATIENT, m.patient())
                        .and().eq(Medicine.COLUMN_NAME, name)
                        .and().eq(Medicine.COLUMN_PRESENTATION, presentation).prepare();
                others = DB.medicines().query(query);
            }
            boolean ret = false;
            if (others != null && others.size() > 0) {
                if (others.size() > 1) //should not happen
                    Log.e(TAG, "checkIfDuplicate: multiple duplicates detected for medicine: " + m);
                final Medicine other = others.get(0);
                ret = !other.getId().equals(m.getId());
            }
            if (ret) {
                Snack.show(R.string.error_duplicate_medicine, this.getActivity());
            }
            return ret;
        } catch (SQLException e) {
            Log.e(TAG, "checkIfDuplicate: ", e);
            return false;
        }
    }

    private void showSoftInput() {
        mNameTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(mNameTextView, 0);
            }
        }, 10);

    }


    // Container Activity must implement this interface
    public interface OnMedicineEditListener {
        void onMedicineEdited(Medicine r);

        void onMedicineCreated(Medicine r);

        void onMedicineDeleted(Medicine r);
    }

    public class ComputeEstimatedStockEndTask extends AsyncTask<Void, Void, Boolean> {

        String text;

        @Override
        protected Boolean doInBackground(Void... params) {
            if (stock >= 0 && mMedicine != null) {
                Log.d(TAG, "updateStockText: medicina ok");
                List<Schedule> schedules = DB.schedules().findByMedicine(mMedicine);
                if (!schedules.isEmpty()) {
                    Log.d(TAG, "updateStockText: pautas " + schedules.size());
                    LocalDate estimatedEnd = StockUtils.getEstimatedStockEnd(schedules, stock);
                    text = StockUtils.getReadableStockDuration(estimatedEnd, getContext());
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            estimatedStockText = null;
            updateStockText();


        }

        @Override
        protected void onPostExecute(Boolean res) {
            super.onPostExecute(res);
            if (res) {
                estimatedStockText = text;
                updateStockText();
            }
        }
    }


}