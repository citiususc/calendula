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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.j256.ormlite.dao.Dao;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISelectionListener;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter_extensions.utilities.SubItemUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.items.allergensearch.AllergenGroupItem;
import es.usc.citius.servando.calendula.adapters.items.allergensearch.AllergenGroupSubItem;
import es.usc.citius.servando.calendula.adapters.items.allergensearch.AllergenItem;
import es.usc.citius.servando.calendula.adapters.items.allergylist.AllergyGroupItem;
import es.usc.citius.servando.calendula.adapters.items.allergylist.AllergyGroupSubItem;
import es.usc.citius.servando.calendula.adapters.items.allergylist.AllergyItem;
import es.usc.citius.servando.calendula.allergies.AllergenConversionUtil;
import es.usc.citius.servando.calendula.allergies.AllergenFacade;
import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.allergies.AllergyAlertUtil;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientAllergenDao;
import es.usc.citius.servando.calendula.persistence.AllergyGroup;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert.AllergyAlertInfo;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

@SuppressWarnings("unchecked")
public class AllergiesActivity extends CalendulaActivity {


    private static final String TAG = "AllergiesActivity";
    private final ISelectionListener<AbstractItem> selectionListener = new AllergySelectionListener();

    // main view
    @BindView(R.id.add_button)
    protected AddFloatingActionButton addButton;
    @BindView(R.id.allergies_recycler)
    protected RecyclerView allergiesRecycler;
    @BindView(R.id.textview_no_allergies_placeholder)
    protected TextView allergiesPlaceholder;
    // search view
    @BindView(R.id.search_view)
    protected View searchView;
    @BindView(R.id.close_search_button)
    protected View closeSearchButton;
    @BindView(R.id.search_edit_text)
    protected EditText searchEditText;
    @BindView(R.id.search_list)
    protected RecyclerView searchList;
    @BindView(R.id.allergies_search_placeholder)
    protected TextView allergiesSearchPlaceholder;
    @BindView(R.id.allergies_selected_layout)
    protected LinearLayout selectLayout;
    @BindView(R.id.allergies_selected_message)
    protected TextView selectText;
    // general
    @BindView(R.id.main_progress_bar)
    protected ProgressBar progressBar;

    private int color;
    private Dao<PatientAllergen, Long> dao = null;
    private FastItemAdapter<AbstractItem> searchAdapter;
    private FastItemAdapter allergiesAdapter;
    private AllergiesStore store;
    private List<AllergyGroup> groups;
    private AsyncTask searchTask = null;

    public void askForDatabase() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean validDB = prefs.getString("prescriptions_database", getString(R.string.database_none_id)).equals(getString(R.string.database_aemps_id));

        if (!validDB) {
            new MaterialStyledDialog.Builder(this)
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_database, R.color.white, 100))
                    .setHeaderColor(R.color.android_blue)
                    .withDialogAnimation(true)
                    .setTitle(R.string.title_allergies_database_required)
                    .setDescription(R.string.message_allergies_database_required)
                    .setCancelable(false)
                    .setPositiveText(getString(R.string.ok))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent i = new Intent(AllergiesActivity.this, SettingsActivity.class);
                            i.putExtra("show_database_dialog", true);
                            finish();
                            startActivity(i);
                        }
                    })
                    .setNegativeText(R.string.cancel)
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

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + (searchView.getVisibility() == View.VISIBLE));
        if (searchView.getVisibility() == View.VISIBLE) {
            hideSearchView();
        } else {
            finish();
        }
    }

    @OnClick(R.id.close_search_button)
    void clearSearch() {
        searchAdapter.clear();
        searchAdapter.deselect();
        searchAdapter.notifyDataSetChanged();
        selectText.setText(getString(R.string.allergies_selected_number, 0));
        selectLayout.setVisibility(View.GONE);
        searchEditText.setText("");
    }

    @OnClick(R.id.accept_selection_button)
    void saveAllergies() {
        hideSearchView();
        new SaveAllergiesTask().execute(getSelected());
    }

    @OnClick(R.id.add_button)
    void showSearchView() {
        addButton.setVisibility(View.GONE);
        searchEditText.requestFocus();
        KeyboardUtils.showKeyboard(this);
        searchView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchList.setVisibility(View.VISIBLE);
            }
        }, 200);
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

        //retrieve allergy groups
        groups = DB.allergyGroups().findAll();
        if (groups != null && !groups.isEmpty())
            Collections.sort(groups);

        //setup recycler
        setupAllergiesList();

        progressBar.getIndeterminateDrawable().setColorFilter(color,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        //setup search view
        setupSearchView();

        //load allergies, set placeholder if needed
        new LoadAllergiesTask().execute();

        askForDatabase();

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
                                Log.wtf(TAG, "Duplicate alerts: " + list);
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

    private void checkSearchPlaceholder() {
        if (searchAdapter.getItemCount() > 0) {
            allergiesSearchPlaceholder.setVisibility(View.GONE);
        } else {
            if (searchEditText.getText().toString().trim().length() > 3) {
                allergiesSearchPlaceholder.setText(getText(R.string.allergies_search_placeholder_no_result));
            } else {
                allergiesSearchPlaceholder.setText(getText(R.string.allergies_search_placeholder));
            }
            allergiesSearchPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void closeSearchView() {
        hideSearchView();
        searchEditText.setText("");
        searchAdapter.clear();
    }

    private void doSearch() {
        String filter = searchEditText.getText().toString().trim();
        if (filter.length() >= 3) {
            if (searchTask != null)
                searchTask.cancel(true);
            searchTask = new DoSearchTask();
            searchTask.execute(new String[]{filter});
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

    private Dao<PatientAllergen, Long> getDao() {
        if (dao == null)
            dao = new PatientAllergenDao(DB.helper()).getConcreteDao();
        return dao;
    }

    private Collection<IItem> getSelected() {
        Collection<IItem> selected = new ArrayList<>();
        final Set<IItem> items = SubItemUtil.getSelectedItems(searchAdapter);
        for (IItem item : items) {
            if (item.getType() != R.id.fastadapter_allergen_group_item)
                selected.add(item);
        }
        Log.d(TAG, "getSelected() returned: " + selected.size() + " elements");
        return selected;
    }

    private void hideAllergiesView(boolean hide) {
        final int visibility = hide ? View.GONE : View.VISIBLE;
        allergiesRecycler.setVisibility(visibility);
        addButton.setVisibility(visibility);
        if (!hide)
            checkPlaceholder();

    }

    private void hideSearchView() {
        addButton.setVisibility(View.VISIBLE);
        searchList.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.GONE);
        KeyboardUtils.hideKeyboard(this);
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
                Log.d(TAG, "onEvent() called with: view = [" + view + ", i = [" + i + "], fastAdapter = [" + fastAdapter + "], item = [" + item + "]");
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
                                Log.w(TAG, "onClick: Unexpected item type: " + item);
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
                        Log.w(TAG, "onClick: Unexpected view type on click hook: " + view);
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

    private void setupSearchView() {
        searchList.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        searchAdapter = new FastItemAdapter<>();
        searchAdapter.withPositionBasedStateManagement(false);
        searchAdapter.withItemEvent(new AllergenGroupItem.GroupExpandClickEvent());
        searchAdapter.withSelectable(true);
        searchAdapter.withMultiSelect(true);
        searchAdapter.withSelectionListener(selectionListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchView.setStateListAnimator(null);
        }
        searchView.setAnimation(null);

        searchList.setAdapter(searchAdapter);
        searchList.setLayoutManager(llm);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final String search = s.toString().trim();
                if (search.length() > 0) {
                    if (closeSearchButton.getVisibility() == View.GONE) {
                        closeSearchButton.setVisibility(View.VISIBLE);
                    }
                    doSearch();
                } else {
                    if (closeSearchButton.getVisibility() == View.VISIBLE)
                        closeSearchButton.setVisibility(View.GONE);
                    if (searchAdapter.getItemCount() > 0) {
                        searchAdapter.deselect();
                        searchAdapter.clear();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkSearchPlaceholder();
                        }
                    }, 200);
                }
            }
        });

        searchView.setBackgroundColor(color);

        hideSearchView();
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

    private String getTitle(AbstractItem i) {
        switch (i.getType()) {
            case R.id.fastadapter_allergen_group_item:
                return ((AllergenGroupItem) i).getTitle();
            case R.id.fastadapter_allergen_group_sub_item:
                return ((AllergenGroupSubItem) i).getTitle();
            case R.id.fastadapter_allergen_item:
                return ((AllergenItem) i).getTitle();
            case R.id.fastadapter_allergy_group_item:
                return ((AllergyGroupItem) i).getTitle();
            case R.id.fastadapter_allergy_group_sub_item:
                return ((AllergyGroupSubItem) i).getTitle();
            case R.id.fastadapter_allergy_item:
                return ((AllergyItem) i).getTitle();
            default:
                throw new RuntimeException("Unsupported item type");
        }
    }

    public enum SaveResult {
        OK, ERROR, ALLERGY
    }

    public class AllergiesStore {


        private List<PatientAllergen> currentAllergies;
        private Context context;

        public AllergiesStore() {
        }

        public int deleteAllergen(PatientAllergen a) {
            try {
                int index = currentAllergies.indexOf(a);
                DB.patientAllergens().delete(a);
                AllergyAlertUtil.removeAllergyAlerts(a);
                currentAllergies.remove(a);
                return index;
            } catch (SQLException e) {
                Log.e(TAG, "Couldn't delete allergen " + a, e);
                return -2;
            }

        }

        public int deleteAllergens(final List<PatientAllergen> a) {
            return (int) DB.transaction(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int res = 0;
                    for (PatientAllergen patientAllergen : a) {
                        res = deleteAllergen(patientAllergen);
                        if (res == -2)
                            break;
                    }
                    return res;
                }
            });
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
                rows = getDao().create(allergen);
            } catch (SQLException e) {
                Log.e(TAG, "storeAllergen: couldn't create allergy", e);
                return SaveResult.ERROR;
            }
            Log.d(TAG, "storeAllergen: inserted allergen into database: " + allergen);
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

    private class AllergySelectionListener implements ISelectionListener<AbstractItem> {

        @Override
        public void onSelectionChanged(AbstractItem item, boolean selected) {
            KeyboardUtils.hideKeyboard(AllergiesActivity.this);
            switch (item.getType()) {
                case R.id.fastadapter_allergen_group_item:
                    AllergenGroupItem i = (AllergenGroupItem) item;
                    final int size = i.getSubItems().size();
                    if (selected) {
                        i.setSubtitle(getString(R.string.allergies_group_elements_selected, size, size));
                    } else {
                        i.setSubtitle(getString(R.string.allergies_group_elements_number, size));
                    }
                    SubItemUtil.selectAllSubItems(searchAdapter, i, selected, true);
                    break;
                case R.id.fastadapter_allergen_group_sub_item:
                    AllergenGroupItem t = ((AllergenGroupSubItem) item).getParent();
                    final int count = SubItemUtil.countSelectedSubItems(searchAdapter, t);
                    final int s = t.getSubItems().size();
                    final int pos = searchAdapter.getAdapterPosition(t);
                    if (count > 0) {
                        t.setSubtitle(getString(R.string.allergies_group_elements_selected, s, count));
                        if (!t.isSelected()) {
                            t.withSetSelected(true);
                        }
                    } else {
                        t.setSubtitle(getString(R.string.allergies_group_elements_number, s));
                        if (t.isSelected()) {
                            t.withSetSelected(false);
                        }
                    }
                    searchAdapter.notifyItemChanged(pos);
                    break;
            }

            //show selection confirmation
            final int selectedNumber = getSelected().size();
            if (selectedNumber > 0) {
                selectLayout.setVisibility(View.VISIBLE);
                selectText.setText(getString(R.string.allergies_selected_number, selectedNumber));
            } else {
                selectLayout.setVisibility(View.GONE);
            }
        }
    }

    private class DeleteAllergyGroupTask extends AsyncTask<AllergyGroupItem, Void, Integer> {

        @Override
        protected Integer doInBackground(AllergyGroupItem... params) {
            Log.d(TAG, "doInBackground() called with: params = [" + Arrays.toString(params) + "]");
            if (params.length != 1) {
                Log.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + params.length);
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
                Log.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + params.length);
                throw new IllegalArgumentException("Invalid argument length");
            }
            int index = allergiesAdapter.getAdapterPosition(params[0]);
            store.deleteAllergen(params[0].getAllergen());
            return index;
        }
    }

    private class DoSearchTask extends AsyncTask<String, Void, List<AbstractItem>> {

        @Override
        protected void onPreExecute() {
            searchAdapter.deselect();
            searchAdapter.clear();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<AbstractItem> abstractItems) {
            searchAdapter.add(abstractItems);
            progressBar.setVisibility(View.GONE);
            checkSearchPlaceholder();
            searchTask = null;
        }

        @Override
        protected List<AbstractItem> doInBackground(String... params) {
            if (params.length != 1) {
                Log.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + params.length);
                throw new IllegalArgumentException("Invalid argument length");
            }

            final String filter = params[0];
            final List<AllergenVO> allergenVOs = AllergenFacade.searchForAllergens(filter);
            final List<AllergenVO> patientAllergies = store.getAllergiesVO();
            allergenVOs.removeAll(patientAllergies);

            List<AbstractItem> items = new ArrayList<>();

            final int highlightColor = ContextCompat.getColor(AllergiesActivity.this, R.color.black);
            if (groups != null && !groups.isEmpty()) {
                //find words for groups
                final Map<String, Pattern> groupPatterns = new ArrayMap<>();
                for (AllergyGroup group : groups) {
                    String regex = "\\b(" + group.getExpression() + ")\\b";
                    Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                    groupPatterns.put(group.getName(), p);
                }

                final Map<String, List<AllergenVO>> groups = new ArrayMap<>();
                final List<AllergenVO> toRemove = new ArrayList<>();

                for (AllergenVO vo : allergenVOs) {
                    for (String k : groupPatterns.keySet()) {
                        Pattern p = groupPatterns.get(k);
                        if (p.matcher(vo.getName()).find()) {
                            if (groups.keySet().contains(k)) {
                                groups.get(k).add(vo);
                            } else {
                                ArrayList<AllergenVO> vos = new ArrayList<>();
                                vos.add(vo);
                                groups.put(k, vos);
                            }
                            toRemove.add(vo);
                            break;
                        }
                    }
                }

                // sort elements into groups
                allergenVOs.removeAll(toRemove);
                for (String s : groups.keySet()) {
                    final List<AllergenVO> subs = groups.get(s);
                    if (!subs.isEmpty()) {
                        AllergenGroupItem g = new AllergenGroupItem(s, "");
                        List<AllergenGroupSubItem> sub = new ArrayList<>();
                        for (AllergenVO vo : subs) {
                            final AllergenGroupSubItem e = new AllergenGroupSubItem(vo, AllergiesActivity.this);
                            e.setTitleSpannable(Strings.getHighlighted(vo.getName(), filter, highlightColor));
                            sub.add(e);
                        }
                        g.setSubtitle(getString(R.string.allergies_group_elements_number, sub.size()));
                        g.setTitleSpannable(Strings.getHighlighted(s, filter, highlightColor));
                        Collections.sort(sub, new Comparator<AllergenGroupSubItem>() {
                            @Override
                            public int compare(AllergenGroupSubItem o1, AllergenGroupSubItem o2) {
                                return o1.getTitle().compareTo(o2.getTitle());
                            }
                        });
                        g.withSubItems(sub);
                        items.add(g);
                    }
                }
            }

            for (AllergenVO vo : allergenVOs) {
                final AllergenItem e = new AllergenItem(vo, AllergiesActivity.this);
                e.setTitleSpannable(Strings.getHighlighted(e.getTitle(), filter, highlightColor));
                items.add(e);
            }
            Collections.sort(items, new Comparator<AbstractItem>() {
                @Override
                public int compare(AbstractItem o1, AbstractItem o2) {
                    final String f = filter.toLowerCase();
                    final String t1 = getTitle(o1).toLowerCase().trim();
                    boolean c1 = t1.contains(f);
                    final String t2 = getTitle(o2).toLowerCase().trim();
                    boolean c2 = t2.toLowerCase().trim().contains(f);

                    if (c1 && !c2) {
                        return -1;
                    } else if (c2 && !c1) {
                        return 1;
                    } else if (c1) { //if c1 is true, c2 is true at this point too
                        int i1 = t1.toLowerCase().trim().indexOf(filter.toLowerCase());
                        int i2 = t2.toLowerCase().trim().indexOf(filter.toLowerCase());
                        if (i1 == i2) {
                            return t1.compareTo(t2);
                        } else {
                            return i1 < i2 ? -1 : 1;
                        }
                    } else {
                        return t1.compareTo(t2);
                    }
                }
            });

            return items;
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

    private class SaveAllergiesTask extends AsyncTask<Collection<IItem>, Void, SaveAllergiesTask.Result> {

        @SafeVarargs
        @Override
        protected final Result doInBackground(Collection<IItem>... items) {
            if (items.length != 1) {
                Log.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + items.length);
                throw new IllegalArgumentException("Invalid argument length");
            }
            List<PatientAllergen> pa = new ArrayList<>();
            Patient p = DB.patients().getActive(AllergiesActivity.this);
            for (IItem i : items[0]) {
                switch (i.getType()) {
                    case R.id.fastadapter_allergen_group_sub_item:
                        final AllergenGroupSubItem item = (AllergenGroupSubItem) i;
                        pa.add(new PatientAllergen(item.getVo(), p, item.getParent().getTitle()));
                        break;
                    case R.id.fastadapter_allergen_item:
                        final AllergenItem item1 = (AllergenItem) i;
                        pa.add(new PatientAllergen(item1.getVo(), p));
                        break;
                    default:
                        Log.wtf(TAG, "Invalid item type in adapter: " + i);
                        break;
                }

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
                clearSearch();
                searchList.invalidate();
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
            closeSearchView();
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
