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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.items.allergylist.AllergyGroupItem;
import es.usc.citius.servando.calendula.adapters.items.allergylist.AllergyGroupSubItem;
import es.usc.citius.servando.calendula.adapters.items.allergylist.AllergyItem;
import es.usc.citius.servando.calendula.allergies.AllergenConversionUtil;
import es.usc.citius.servando.calendula.allergies.AllergenFacade;
import es.usc.citius.servando.calendula.allergies.AllergenGroupWrapper;
import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.allergies.AllergyAlertUtil;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert.AllergyAlertInfo;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

@SuppressWarnings("unchecked")
public class AllergiesActivity extends CalendulaActivity {


    private static final String TAG = "AllergiesActivity";
    // main view
    @BindView(R.id.add_button)
    protected AddFloatingActionButton addButton;
    @BindView(R.id.allergies_recycler)
    protected RecyclerView allergiesRecycler;
    @BindView(R.id.textview_no_allergies_placeholder)
    protected TextView allergiesPlaceholder;

    // general
    @BindView(R.id.main_progress_bar)
    protected ProgressBar progressBar;

    private int color;
    private FastItemAdapter allergiesAdapter;
    private AllergiesStore store;


    @OnClick(R.id.add_button)
    void showSearchView() {
        Intent i = new Intent(this, AllergiesSearchActivity.class);
        startActivityForResult(i, AllergiesSearchActivity.REQUEST_NEW_ALLERGIES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AllergiesSearchActivity.REQUEST_NEW_ALLERGIES && resultCode == Activity.RESULT_OK) {
            final ArrayList<AllergenGroupWrapper> result = data.getParcelableArrayListExtra("result");
            new SaveAllergiesTask().execute(result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergies);
        ButterKnife.bind(this);

        //setup toolbar and status bar
        final Patient patient = DB.patients().getActive(this);
        color = patient.color();
        final String name = patient.name();
        setupToolbar(getString(R.string.relation_user_possession_thing, name, getString(R.string.title_activity_allergies)), color);
        setupStatusBar(color);

        //initialize allergies store
        store = new AllergiesStore();

        //setup recycler
        setupAllergiesList();

        progressBar.getIndeterminateDrawable().setColorFilter(color,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        //load allergies, set placeholder if needed
        new LoadAllergiesTask().execute();

        showWarningIfNeeded();
    }

    private void showWarningIfNeeded() {
        final SharedPreferences prefs = PreferenceUtils.instance().preferences();
        if (!prefs.getBoolean(PreferenceKeys.ALLERGIES_WARNING_SHOWN.key(), false)) {
            new MaterialStyledDialog.Builder(this)
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setIcon(IconUtils.icon(this, GoogleMaterial.Icon.gmd_alert_circle, R.color.white, 100))
                    .setHeaderColor(R.color.android_orange_dark)
                    .withDialogAnimation(true)
                    .setTitle(R.string.important_info)
                    .setDescription(R.string.message_allergies_experimental_warning)
                    .setCancelable(false)
                    .setPositiveText(getString(R.string.dialog_continue_option))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            prefs.edit().putBoolean(PreferenceKeys.ALLERGIES_WARNING_SHOWN.key(), true).apply();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeText(R.string.dialog_get_me_out)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                            finish();
                        }
                    })
                    .show();
        }
    }

    private boolean checkConflictsAndCreateAlerts(final AllergenVO allergen) {
        final List<Medicine> conflicts = AllergenFacade.checkNewMedicineAllergies(this, allergen);
        if (!conflicts.isEmpty()) {
            DB.transaction(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (Medicine conflict : conflicts) {
//                        AlertManager.createAlert(new AllergyPatientAlert(conflict, allergen), AllergiesActivity.this);
                        final List<PatientAlert> list = AllergyAlertUtil.getAlertsForMedicine(conflict);
                        if (list.size() > 0) {
                            if (list.size() == 1) {
                                AllergyPatientAlert a = (AllergyPatientAlert) list.get(0).map();
                                final AllergyAlertInfo d = a.getDetails();
                                d.getAllergens().add(allergen);
                                a.setDetails(d);
                                DB.alerts().save(a);
                            } else {
                                LogUtil.wtf(TAG, "Duplicate alerts: " + list);
                            }
                        } else {
                            AlertManager.createAlert(new AllergyPatientAlert(conflict, new ArrayList<AllergenVO>() {{
                                add(allergen);
                            }}));
                        }
                    }
                    return null;
                }
            });
        }
        return !conflicts.isEmpty();
    }

    private void checkPlaceholder() {
        if (allergiesPlaceholder.getVisibility() == View.VISIBLE) {
            if (!store.isEmpty())
                allergiesPlaceholder.setVisibility(View.GONE);
        } else if (store.isEmpty()) {
            allergiesPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private List<AbstractItem> getAllergyItems() {
        final List<PatientAllergen> allergies = new ArrayList<>(store.getAllergies());

        List<AbstractItem> items = new ArrayList<>(allergies.size());
        List<PatientAllergen> toRemove = new ArrayList<>();

        Map<String, List<AllergyGroupSubItem>> groups = new HashMap<>();

        for (PatientAllergen allergen : allergies) {
            final String group = allergen.getGroup();
            if (group != null && !group.isEmpty()) {
                if (!groups.keySet().contains(group)) {
                    groups.put(group, new ArrayList<AllergyGroupSubItem>());
                }
                groups.get(group).add(new AllergyGroupSubItem(allergen, this));
                toRemove.add(allergen);
            }
        }
        allergies.removeAll(toRemove);
        for (String key : groups.keySet()) {
            AllergyGroupItem g = new AllergyGroupItem(key, this);
            g.withSubItems(groups.get(key));
            items.add(g);
        }
        for (PatientAllergen allergen : allergies) {
            items.add(new AllergyItem(allergen, this));
        }
        return items;
    }

    private void hideAllergiesView(boolean hide) {
        final int visibility = hide ? View.GONE : View.VISIBLE;
        allergiesRecycler.setVisibility(visibility);
        addButton.setVisibility(visibility);
        if (!hide)
            checkPlaceholder();

    }

    private void setupAllergiesList() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        allergiesRecycler.setLayoutManager(llm);
        allergiesAdapter = new FastItemAdapter<>();
        allergiesAdapter.withSelectable(false);
        allergiesAdapter.withItemEvent(new ClickEventHook<AbstractItem>() {

            @Override
            public void onClick(View view, int i, FastAdapter fastAdapter, AbstractItem item) {
                LogUtil.d(TAG, "onClick() called with: view = [" + view + ", i = [" + i + "], fastAdapter = [" + fastAdapter + "], item = [" + item + "]");
                switch (view.getId()) {
                    case R.id.delete_button:
                        // check if group or item
                        switch (item.getType()) {
                            case R.id.fastadapter_allergy_group_item:
                                showDeleteConfirmationDialog((AllergyGroupItem) item);
                                break;
                            case R.id.fastadapter_allergy_item:
                                showDeleteConfirmationDialog((AllergyItem) item);
                                break;
                            default:
                                LogUtil.w(TAG, "onClick: Unexpected item type: " + item);
                                break;
                        }
                        break;
                    case R.id.group_button:
                        AllergyGroupItem g = (AllergyGroupItem) item;
                        boolean expand = !g.isExpanded();
                        float angle = expand ? 180 : 0;
                        ViewCompat.animate(view).rotation(angle);
                        if (expand)
                            allergiesAdapter.expand(i);
                        else
                            allergiesAdapter.collapse(i);
                        break;
                    default:
                        LogUtil.w(TAG, "onClick: Unexpected view type on click hook: " + view);
                        break;
                }
            }

            @Nullable
            @Override
            public List<View> onBindMany(@NonNull RecyclerView.ViewHolder viewHolder) {
                List<View> vl = new ArrayList<>();
                if (viewHolder instanceof AllergyGroupItem.ViewHolder) {
                    AllergyGroupItem.ViewHolder vh = (AllergyGroupItem.ViewHolder) viewHolder;
                    vl.add(vh.deleteButton);
                    vl.add(vh.dropButton);
                    return vl;
                } else if (viewHolder instanceof AllergyItem.ViewHolder) {
                    vl.add(((AllergyItem.ViewHolder) viewHolder).deleteButton);
                    return vl;
                }
                return null;
            }
        });
        allergiesRecycler.setAdapter(allergiesAdapter);
    }

    private void showDeleteConfirmationDialog(final AllergyItem a) {
        showDeleteConfirmationDialog(getString(R.string.remove_allergy_message_short, a.getAllergen().getName()), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                new DeleteAllergyTask().execute(a);
                dialog.dismiss();
            }
        }, new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.cancel();
            }
        });
    }

    private void showDeleteConfirmationDialog(final AllergyGroupItem a) {
        showDeleteConfirmationDialog(getString(R.string.remove_allergy_message_short, a.getTitle()), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                new DeleteAllergyGroupTask().execute(a);
                dialog.dismiss();
            }
        }, new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.cancel();
            }
        });
    }

    private void showDeleteConfirmationDialog(String message, MaterialDialog.SingleButtonCallback onPositive, MaterialDialog.SingleButtonCallback onNegative) {
        new MaterialStyledDialog.Builder(this)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_delete, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setDescription(message)
                .setCancelable(true)
                .setNegativeText(getString(R.string.dialog_no_option))
                .setPositiveText(getString(R.string.dialog_yes_option))
                .onPositive(onPositive)
                .onNegative(onNegative)
                .show();
    }

    private void showNewAllergyConflictDialog() {
        new MaterialStyledDialog.Builder(this)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_exclamation, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setTitle(R.string.title_allergies_detected_dialog)
                .setDescription(R.string.message_allergies_detected_dialog)
                .setCancelable(false)
                .setPositiveText(getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public enum SaveResult {
        OK, ERROR, ALLERGY
    }

    public class AllergiesStore {


        private List<PatientAllergen> currentAllergies;
        private Context context;

        public AllergiesStore() {
        }

        public int deleteAllergen(PatientAllergen a, boolean notify) {
            try {
                int index = currentAllergies.indexOf(a);
                DB.patientAllergens().delete(a);
                AllergyAlertUtil.removeAllergyAlerts(a);
                currentAllergies.remove(a);
                if (notify) {
                    CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
                }
                return index;
            } catch (SQLException e) {
                LogUtil.e(TAG, "Couldn't delete allergen " + a, e);
                return -2;
            }

        }

        public int deleteAllergens(final List<PatientAllergen> a) {
            final int res = (int) DB.transaction(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int res = 0;
                    for (PatientAllergen patientAllergen : a) {
                        res = deleteAllergen(patientAllergen, false);
                        if (res == -2)
                            break;
                    }
                    return res;
                }
            });
            CalendulaApp.eventBus().post(PersistenceEvents.MEDICINE_EVENT);
            return res;
        }

        public List<PatientAllergen> getAllergies() {
            return currentAllergies;
        }

        public List<AllergenVO> getAllergiesVO() {
            return AllergenConversionUtil.toVO(currentAllergies);
        }

        public boolean isEmpty() {
            return currentAllergies.isEmpty();
        }

        public void load(Context ctx) {
            context = ctx;
            reload();
        }

        public void reload() {
            currentAllergies = DB.patientAllergens().findAllForActivePatient(context);
            Collections.sort(currentAllergies, new Comparator<PatientAllergen>() {
                @Override
                public int compare(PatientAllergen o1, PatientAllergen o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }

        public SaveResult storeAllergen(PatientAllergen allergen) {
            int rows;
            try {
                rows = DB.patientAllergens().create(allergen);
            } catch (SQLException e) {
                LogUtil.e(TAG, "storeAllergen: couldn't create allergy", e);
                return SaveResult.ERROR;
            }
            LogUtil.d(TAG, "storeAllergen: inserted allergen into database: " + allergen);
            if (rows == 1) {
                final boolean r = checkConflictsAndCreateAlerts(new AllergenVO(allergen));
                currentAllergies.add(allergen);
                if (r)
                    return SaveResult.ALLERGY;
                return SaveResult.OK;

            }
            return SaveResult.ERROR;
        }

        public SaveResult storeAllergens(final Collection<PatientAllergen> allergens) {
            return (SaveResult) DB.transaction(new Callable<SaveResult>() {
                @Override
                public SaveResult call() throws Exception {
                    SaveResult res = SaveResult.OK;
                    for (PatientAllergen allergen : allergens) {
                        final SaveResult r = storeAllergen(allergen);
                        if (r == SaveResult.ALLERGY && res != SaveResult.ERROR)
                            res = SaveResult.ALLERGY;
                        if (r == SaveResult.ERROR)
                            res = SaveResult.ERROR;
                    }
                    return res;
                }
            });
        }
    }

    private class DeleteAllergyGroupTask extends AsyncTask<AllergyGroupItem, Void, Integer> {

        @Override
        protected Integer doInBackground(AllergyGroupItem... params) {
            LogUtil.d(TAG, "doInBackground() called with: params = [" + Arrays.toString(params) + "]");
            if (params.length != 1) {
                LogUtil.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + params.length);
                throw new IllegalArgumentException("Invalid argument length");
            }
            int index = allergiesAdapter.getAdapterPosition(params[0]);

            final List<AllergyGroupSubItem> subItems = params[0].getSubItems();
            final List<PatientAllergen> allergens = new ArrayList<>(subItems.size());
            for (AllergyGroupSubItem subItem : subItems) {
                allergens.add(subItem.getAllergen());
            }

            int k = store.deleteAllergens(allergens);
            return k >= -1 ? index : k;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer index) {
            if (index >= 0) {
                store.reload();
                checkPlaceholder();
                allergiesAdapter.collapse(index);
                allergiesAdapter.remove(index);
            } else {
                Snack.show(R.string.delete_allergen_error, AllergiesActivity.this);
            }
            progressBar.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPlaceholder();
                }
            }, 200);
            hideAllergiesView(false);
        }


    }

    private class DeleteAllergyTask extends AsyncTask<AllergyItem, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer index) {
            if (index >= 0) {
                store.reload();
                checkPlaceholder();
                allergiesAdapter.remove(index);
            } else {
                Snack.show(R.string.delete_allergen_error, AllergiesActivity.this);
            }
            progressBar.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPlaceholder();
                }
            }, 200);
            hideAllergiesView(false);
        }

        @Override
        protected Integer doInBackground(AllergyItem... params) {
            if (params.length != 1) {
                LogUtil.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + params.length);
                throw new IllegalArgumentException("Invalid argument length");
            }
            int index = allergiesAdapter.getAdapterPosition(params[0]);
            store.deleteAllergen(params[0].getAllergen(), true);
            return index;
        }
    }

    private class LoadAllergiesTask extends AsyncTask<Void, Void, List<AbstractItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideAllergiesView(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<AbstractItem> items) {
            allergiesAdapter.add(items);
            progressBar.setVisibility(View.GONE);
            checkPlaceholder();
            hideAllergiesView(false);
        }

        @Override
        protected List<AbstractItem> doInBackground(Void... params) {
            store.load(AllergiesActivity.this);
            return getAllergyItems();
        }
    }

    private class SaveAllergiesTask extends AsyncTask<Collection<AllergenGroupWrapper>, Void, SaveAllergiesTask.Result> {

        @SafeVarargs
        @Override
        protected final Result doInBackground(Collection<AllergenGroupWrapper>... items) {
            if (items.length != 1) {
                LogUtil.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + items.length);
                throw new IllegalArgumentException("Invalid argument length");
            }
            final Collection<AllergenGroupWrapper> ws = items[0];
            Collection<PatientAllergen> pa = new ArrayList<>(ws.size());
            Patient p = DB.patients().getActive(AllergiesActivity.this);
            for (AllergenGroupWrapper w : ws) {
                if (w.getGroup() != null)
                    pa.add(new PatientAllergen(w.getVo(), p, w.getGroup()));
                else
                    pa.add(new PatientAllergen(w.getVo(), p));
            }

            final SaveResult r = store.storeAllergens(pa);
            return new Result(r == SaveResult.OK || r == SaveResult.ALLERGY, r == SaveResult.ALLERGY, getAllergyItems());
        }

        @Override
        protected void onPostExecute(Result res) {
            progressBar.setVisibility(View.GONE);
            if (res.saved) {
                if (!res.allergyItems.isEmpty()) {
                    allergiesAdapter.set(res.allergyItems);
                    store.reload();
                }
                if (res.allergies)
                    showNewAllergyConflictDialog();
                hideAllergiesView(false);
                Snack.show(getString(R.string.message_allergy_add_multiple_success), AllergiesActivity.this);
            } else {
                Snack.show(R.string.message_allergy_add_failure, AllergiesActivity.this);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideAllergiesView(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        class Result {
            final boolean saved;
            final boolean allergies;
            final List<AbstractItem> allergyItems;

            public Result(boolean saved, boolean allergies, List<AbstractItem> allergyItems) {
                this.saved = saved;
                this.allergies = allergies;
                this.allergyItems = allergyItems;
            }
        }


    }

}
