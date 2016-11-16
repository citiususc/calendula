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

package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SettingsActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;
import es.usc.citius.servando.calendula.util.Snack;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class MedicineCreateOrEditFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    OnMedicineEditListener mMedicineEditCallback;
    Medicine mMedicine;
    Prescription mPrescription;

    Boolean showConfirmButton = true;
    AutoCompleteTextView mNameTextView;
    TextView mPresentationTv;
    TextView mDescriptionTv;
    ImageView searchButton;
    Presentation selectedPresentation;
    HorizontalScrollView presentationScroll;

    boolean enableSearch = false;
    long mMedicineId;
    String cn;
    int pColor;
    PrescriptionDBMgr dbMgr;

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mNameTextView = (AutoCompleteTextView) rootView.findViewById(R.id.medicine_edit_name);
        mPresentationTv = (TextView) rootView.findViewById(R.id.textView3);
        mDescriptionTv = (TextView) rootView.findViewById(R.id.medicine_edit_description);
        searchButton = (ImageView) rootView.findViewById(R.id.search_button);
        mNameTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                Prescription p = (Prescription) parent.getItemAtPosition(pos);
                String shortName = dbMgr.shortName(p);
                mNameTextView.setText(shortName);
                mDescriptionTv.setText(p.getName());
                hideKeyboard();

                // save referenced prescription to med
                cn = String.valueOf(p.getCode());
            }
        });

        pColor = DB.patients().getActive(getActivity()).color();

        mDescriptionTv.setTextColor(pColor);
        mPresentationTv.setTextColor(pColor);

        String none = getString(R.string.database_none_id);
        String settingUp = getString(R.string.database_setting_up);
        String value = prefs.getString("prescriptions_database", none);
        enableSearch = !value.equals(none) && !value.equals(settingUp);
        if (enableSearch) {
            enableSearchButton();
        } else {
            searchButton.setVisibility(View.GONE);
        }

        presentationScroll = (HorizontalScrollView) rootView.findViewById(R.id.med_presentation_scroll);

        Log.d(getTag(), "Arguments:  " + (getArguments() != null) + ", savedState: " + (savedInstanceState != null));
        if (getArguments() != null) {

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
                    mDescriptionTv.setText("");
                }

            }
        });
        mNameTextView.requestFocus();
        askForPrescriptionUsage();
        return rootView;
    }

    private void enableSearchButton() {
        searchButton.setVisibility(View.VISIBLE);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = mNameTextView.getText();
                ((MedicinesActivity) getActivity()).showSearchView(editable != null ? editable.toString() : null);
            }
        });
    }

    public void showDeleteConfirmationDialog(final Medicine m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // "Remove " + m.name() + "?"
        builder.setMessage(String.format(getString(R.string.remove_medicine_message_short), m.name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mMedicineEditCallback != null)
                            mMedicineEditCallback.onMedicineDeleted(m);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
            mPresentationTv.setText(selectedPresentation.getName(getResources()));
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
        mPresentationTv.setText(mMedicine.presentation().getName(getResources()));
        selectedPresentation = mMedicine.presentation();
        selectPresentation(mMedicine.presentation());

        if (r.cn() != null) {
            Prescription p = DB.drugDB().prescriptions().findByCn(r.cn());
            if (p != null) {
                mPrescription = p;
                mDescriptionTv.setText(p.getName());
            }
        }
    }

    public void setPrescription(Prescription p) {
        mNameTextView.setText(dbMgr.shortName(p));
        mDescriptionTv.setText(p.getName());

        mPrescription = p;

        Presentation pr = DBRegistry.instance().current().expected(p);
        if (pr != null) {
            mPresentationTv.setText(pr.getName(getResources()));
            selectedPresentation = pr;
            selectPresentation(pr);
        }
    }

    private void selectPresentation(Presentation p) {
        for (View v : getViewsByTag((ViewGroup) getView(), "med_type")) {
            v.setBackgroundColor(getResources().getColor(R.color.transparent));
        }

        if (p != null) {
            int viewId = getPresentationViewId(p);
            View view = getView().findViewById(viewId);
            view.setBackgroundResource(R.drawable.presentation_circle_background);

            mPresentationTv.setText(p.getName(getResources()));
            scrollToMedPresentation(view);
        }
    }

    public void clear() {
        mMedicine = null;
        mNameTextView.setText("");
//        mConfirmButton.setText(getString(R.string.create_medicine_button_text));
    }


    public void onEdit() {

        String name = mNameTextView.getText().toString();

        if (name != null && name.length() > 0) {

            // if editing
            if (mMedicine != null) {
                mMedicine.setName(name);
                if (selectedPresentation != null) {
                    mMedicine.setPresentation(selectedPresentation);
                }
                if (mPrescription != null && mPrescription.shortName().toLowerCase().equals(mMedicine.name().toLowerCase())) {
                    mMedicine.setCn(String.valueOf(mPrescription.getCode()));
                } else if (mPrescription == null) {
                    mMedicine.setCn(null);
                }

                if (mMedicineEditCallback != null) {
                    mMedicineEditCallback.onMedicineEdited(mMedicine);
                }
            }
            // if creating
            else {

                if (!validate()) {
                    return;
                }

                Medicine m = new Medicine(name);
                if (mPrescription != null && mPrescription.shortName().toLowerCase().equals(m.name().toLowerCase())) {
                    m.setCn(String.valueOf(mPrescription.getCode()));
                    m.setDatabase(DBRegistry.instance().current().id());
                }
                m.setPresentation(selectedPresentation != null ? selectedPresentation : Presentation.UNKNOWN);
                m.setPatient(DB.patients().getActive(getContext()));
                if (mMedicineEditCallback != null) {
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

    public void askForPrescriptionUsage() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean adviceShown = prefs.getBoolean("show_use_prescriptions_advice", false);
        boolean dbEnabled = !prefs.getString("prescriptions_database", getString(R.string.database_none_id)).equals(getString(R.string.database_none_id));

        if (!adviceShown && !dbEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.enable_prescriptions_dialog_title));
            builder.setCancelable(false);
            builder.setMessage(getString(R.string.enable_prescriptions_dialog_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.enable_prescriptions_dialog_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(getActivity(), SettingsActivity.class);
                            i.putExtra("show_database_dialog", true);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(getString(R.string.enable_prescriptions_dialog_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            showSoftInput();
        }
        prefs.edit().putBoolean("show_use_prescriptions_advice", true).commit();

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("prescriptions_database".equals(key)) {
            String none = getString(R.string.database_none_id);
            String settingUp = getString(R.string.database_setting_up);
            String value = sharedPreferences.getString("prescriptions_database", none);
            enableSearch = !value.equals(none) && !value.equals(settingUp);
            if (enableSearch) {
                enableSearchButton();
            } else {
                searchButton.setVisibility(View.GONE);
            }
        }
    }


    // Container Activity must implement this interface
    public interface OnMedicineEditListener {
        void onMedicineEdited(Medicine r);

        void onMedicineCreated(Medicine r);

        void onMedicineDeleted(Medicine r);
    }

    public class AutoCompleteAdapter extends ArrayAdapter<Prescription> implements Filterable {
        private List<Prescription> mData;

        public AutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            mData = new ArrayList<Prescription>();
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
                final LayoutInflater inflater = getActivity().getLayoutInflater();
                item = inflater.inflate(R.layout.med_drop_down_item, null);
            }

            Prescription p = mData.get(position);
            ((TextView) item.findViewById(R.id.text1)).setText(p.shortName()); //  + (p.generic?" (G)":"")
            ((TextView) item.findViewById(R.id.text2)).setText(mData.get(position).getName());
            ((TextView) item.findViewById(R.id.text3)).setText("(" + p.getDose() + ")");
            return item;
        }

        @Override
        public Filter getFilter() {
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // A class that queries a web API, parses the data and returns an ArrayList<Style>
                        try {
                            List<Prescription> prescriptions = DB.drugDB().prescriptions().findByName(constraint.toString(), 20);
                            /*List<String> names = new ArrayList<String>();
                            for(Prescription p : prescriptions)
                                names.add(p.name);
                                */
                            mData = prescriptions;//Fetcher.fetchNames(constraint.toString());
                        } catch (Exception e) {
                            Log.e("myException", e.getMessage());
                        }
                        // Now assign the values and count to the FilterResults object
                        filterResults.values = mData;
                        filterResults.count = mData.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence contraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return myFilter;
        }
    }

    public class PopulatePrescriptionDatabaseTask extends AsyncTask<String, String, Void> {


        ProgressDialog dialog;

        @Override
        protected Void doInBackground(String... params) {
            new PopulatePrescriptionDBService().updateIfNeeded(getActivity());
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.enable_prescriptions_progress_messgae));
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            enableSearchButton();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().putBoolean("enable_prescriptions_db", true).commit();
            Snack.show(R.string.enable_prescriptions_finish_message, getActivity());
        }
    }


}