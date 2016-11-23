package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.j256.ormlite.dao.Dao;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.AllergiesAdapter;
import es.usc.citius.servando.calendula.adapters.items.AllergenGroupItem;
import es.usc.citius.servando.calendula.adapters.items.AllergenGroupSubItem;
import es.usc.citius.servando.calendula.adapters.items.AllergenItem;
import es.usc.citius.servando.calendula.allergies.AllergenConversionUtil;
import es.usc.citius.servando.calendula.allergies.AllergenFacade;
import es.usc.citius.servando.calendula.allergies.AllergenListeners;
import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.allergies.AllergyAlertUtil;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientAllergenDao;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert.AllergyAlertInfo;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

public class AllergiesActivity extends CalendulaActivity implements AllergenListeners.DeleteAllergyActionListener {


    private static final String TAG = "AllergiesActivity";
    private View searchView;
    private View closeSearchButton;
    private AddFloatingActionButton addButton;
    private EditText searchEditText;
    private RecyclerView searchList;
    private FastItemAdapter<AbstractItem> searchAdapter;
    private AllergiesAdapter allergiesAdapter;
    private RecyclerView allergiesRecycler;
    private AllergiesStore store;
    private TextView allergiesPlaceholder;
    private TextView allergiesSearchPlaceholder;
    private int color;
    private Activity activity;
    private Dao<PatientAllergen, Long> dao = null;
    private List<AllergenVO> recentlyAdded;
    private FloatingActionButton selectFab;
    private LinearLayout selectLayout;
    private TextView selectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recentlyAdded = new ArrayList<>();

        activity = this;

        setContentView(R.layout.activity_allergies);

        allergiesPlaceholder = (TextView) findViewById(R.id.textview_no_allergies_placeholder);
        allergiesSearchPlaceholder = (TextView) findViewById(R.id.allergies_search_placeholder);

        selectFab = (FloatingActionButton) findViewById(R.id.accept_selection_button);
        selectLayout = (LinearLayout) findViewById(R.id.allergies_selected_layout);
        selectText = (TextView) findViewById(R.id.allergies_selected_message);

        //setup toolbar and statusbar
        color = DB.patients().getActive(this).color();
        setupToolbar(getString(R.string.title_activity_allergies), color);
        setupStatusBar(color);

        //load allergies, set placeholder if needed
        store = new AllergiesStore();
        store.load(this);
        checkPlaceholder();

        //setup recycler
        setupRecyclerView();
        //setup FAB
        addButton = (AddFloatingActionButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchView();
            }
        });
        //setup search view
        setupSearchView();


    }

    private void setupRecyclerView() {
        allergiesRecycler = (RecyclerView) findViewById(R.id.allergies_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        allergiesRecycler.setLayoutManager(llm);
        allergiesAdapter = new AllergiesAdapter(store, this, this);
        allergiesRecycler.setAdapter(allergiesAdapter);
    }

    FastAdapter.OnClickListener<AbstractItem> cl = new FastAdapter.OnClickListener<AbstractItem>() {
        @Override
        public boolean onClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {
            boolean select = !item.isSelected();

            //handle selection/deselection of groups and items
            final int type = item.getType();
            if (type != R.id.fastadapter_allergen_group_sub_item) {
                final FastAdapter<AbstractItem> fa = adapter.getFastAdapter();
                if (select) {
                    fa.select(position);
                } else {
                    fa.deselect(position);
                }

                if (type == R.id.fastadapter_allergen_group_item) {
                    AllergenGroupItem gi = (AllergenGroupItem) item;
                    final boolean expanded = gi.isExpanded();
                    for (AllergenGroupSubItem sub : gi.getSubItems()) {
                        sub.withSetSelected(select);
                    }
                    if (expanded)
                        fa.notifyDataSetChanged();
                }
            }

            //show selection confirmation
            final int selectedNumber = getSelected().size();
            if (selectedNumber > 0) {
                selectLayout.setVisibility(View.VISIBLE);
                selectText.setText(getString(R.string.allergies_selected_number, selectedNumber));
            } else {
                selectLayout.setVisibility(View.GONE);
            }


            return true;
        }
    };

    private void setupSearchView() {
        searchView = findViewById(R.id.search_view);
        closeSearchButton = findViewById(R.id.close_search_button);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchList = (RecyclerView) findViewById(R.id.search_list);
        searchList.setItemAnimator(new DefaultItemAnimator());

        closeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearch();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        searchAdapter = new FastItemAdapter<>();
        searchAdapter.withPositionBasedStateManagement(false);
        searchAdapter.withItemEvent(new AllergenGroupItem.GroupExpandClickEvent());
        searchAdapter.withSelectable(true);
        searchAdapter.withMultiSelect(true);
        searchAdapter.withSelectWithItemUpdate(true);
        searchAdapter.withOnClickListener(cl);
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
                        searchAdapter.clear();
                    }
                    checkSearchPlaceholder();
                }
            }
        });

        searchView.setBackgroundColor(color);

        selectFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFab.setOnClickListener(null);
                List<PatientAllergen> pa = new ArrayList<>();
                Patient p = DB.patients().getActive(AllergiesActivity.this);
                for (IItem i : getSelected()) {
                    AllergenVO vo = null;
                    switch (i.getType()) {
                        case R.id.fastadapter_allergen_group_sub_item:
                            vo = ((AllergenGroupSubItem) i).getVo();
                            break;
                        case R.id.fastadapter_allergen_item:
                            vo = ((AllergenItem) i).getVo();
                            break;
                        default:
                            Log.wtf(TAG, "Invalid item type in adapter: " + i);
                            break;
                    }
                    pa.add(new PatientAllergen(vo, p));
                }
                boolean ans = store.storeAllergens(pa);
                if (ans) {
                    checkPlaceholder();
                    Snack.show(getString(R.string.message_allergy_add_multiple_success), AllergiesActivity.this);
                } else {
                    Snack.show(R.string.message_allergy_add_failure, AllergiesActivity.this);
                }
                selectFab.setOnClickListener(this);
                selectLayout.setVisibility(View.GONE);
                doSearch();
            }
        });

        hideSearchView();
    }

    private void clearSearch() {
        searchAdapter.clear();
        searchEditText.setText("");
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

    private void doSearch() {
        String filter = searchEditText.getText().toString().trim();
        if (filter.length() >= 3) {
            final List<AllergenVO> allergenVOs = AllergenFacade.searchForAllergens(filter);
            allergenVOs.removeAll(store.getAllergiesVO());

            final Map<String, List<AllergenVO>> groups = new ArrayMap<>();
            groups.put("Lactosa", new ArrayList<AllergenVO>());

            final List<AllergenVO> toRemove = new ArrayList<>();

            // TODO: 23/11/16 generate groups dynamically
            for (AllergenVO vo : allergenVOs) {
                if (vo.getName().contains("Lactosa")) {
                    groups.get("Lactosa").add(vo);
                    toRemove.add(vo);
                }
            }
            allergenVOs.removeAll(toRemove);
            List<AbstractItem> items = new ArrayList<>();
            for (String s : groups.keySet()) {
                final List<AllergenVO> subs = groups.get(s);
                if (!subs.isEmpty()) {
                    AllergenGroupItem g = new AllergenGroupItem(s, "");
                    List<AllergenGroupSubItem> sub = new ArrayList<>();
                    for (AllergenVO vo : subs) {
                        final AllergenGroupSubItem e = new AllergenGroupSubItem(vo, this);
                        e.setParent(g);
                        sub.add(e);
                    }
                    g.setSubtitle(getString(R.string.allergies_group_elements_number, sub.size()));
                    g.withSubItems(sub);
                    items.add(g);
                }
            }
            for (AllergenVO vo : allergenVOs) {
                items.add(new AllergenItem(vo, AllergiesActivity.this));
            }
            Collections.sort(items, new Comparator<AbstractItem>() {
                @Override
                public int compare(AbstractItem o1, AbstractItem o2) {
                    final int o1Type = o1.getType();
                    final int o2Type = o2.getType();
                    if (o1Type != o2Type) {
                        if (o1Type == R.id.fastadapter_allergen_group_item)
                            return -1;
                        return 1;
                    } else {
                        if (o1Type == R.id.fastadapter_allergen_group_item)
                            return ((AllergenGroupItem) o1).compareTo((AllergenGroupItem) o2);
                        return ((AllergenItem) o1).compareTo((AllergenItem) o2);
                    }
                }
            });
            searchAdapter.clear();
            searchAdapter.add(items);
            checkSearchPlaceholder();
        }
    }

    private Collection<IItem> getSelected() {
        Collection<IItem> selected = new ArrayList<>();
        for (AbstractItem item : searchAdapter.getSelectedItems()) {
            switch (item.getType()) {
                case R.id.fastadapter_allergen_group_item:
                    AllergenGroupItem i = (AllergenGroupItem) item;
                    if (!i.isExpanded()) {
                        selected.addAll(i.getSubItems());
                    }
                    break;
                case R.id.fastadapter_allergen_item:
                case R.id.fastadapter_allergen_group_sub_item:
                    selected.add(item);
                    break;
                default:
                    Log.wtf(TAG, "Invalid item in search adapter: " + item);
                    break;
            }
        }
        Log.d(TAG, "getSelected() returned: " + selected.size()+" elements");
        return selected;
    }

    private void showSearchView() {
        addButton.setVisibility(View.GONE);
        searchEditText.requestFocus();
        KeyboardUtils.showKeyboard(this);
        searchView.setVisibility(View.VISIBLE);
    }

    private void hideSearchView() {
        addButton.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);
        KeyboardUtils.hideKeyboard(this);
    }

    private void closeSearchView() {
        hideSearchView();
        searchEditText.setText("");
        searchAdapter.clear();
    }

    private Dao<PatientAllergen, Long> getDao() {
        if (dao == null)
            dao = new PatientAllergenDao(DB.helper()).getConcreteDao();
        return dao;
    }

    private void showDeleteConfirmationDialog(final PatientAllergen a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Activity ac = this;
        builder.setMessage(String.format(getString(R.string.remove_allergy_message_short), a.getName()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int index = store.deleteAllergen(a);
                        if (index >= 0) {
                            checkPlaceholder();
                            doSearch();
                            allergiesAdapter.remove(index);
                        } else {
                            Snack.show(R.string.delete_allergen_error, ac);
                        }
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

    private void showNewAllergyConflictDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_allergies_detected_dialog)
                .setMessage(R.string.message_allergies_detected_dialog)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.ok), null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + (searchView.getVisibility() == View.VISIBLE));
        if (searchView.getVisibility() == View.VISIBLE) {
            hideSearchView();
        } else {
            super.onBackPressed();
        }
    }

    private void checkConflictsAndCreateAlerts(final AllergenVO allergen) {
        final List<Medicine> conflicts = AllergenFacade.checkNewMedicineAllergies(this, allergen);
        if (!conflicts.isEmpty()) {
            showNewAllergyConflictDialog();
            DB.transaction(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    final Patient patient = DB.patients().getActive(AllergiesActivity.this);
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
                            }}), AllergiesActivity.this);
                        }
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public void onDeleteAction(PatientAllergen allergen) {
        Log.d(TAG, "onDeleteAction: deleting allergy " + allergen);
        showDeleteConfirmationDialog(allergen);
    }

    private class ReloadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            store.reload();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            allergiesAdapter.notifyDataSetChanged();
            searchAdapter.notifyDataSetChanged();
            doSearch();
        }
    }

    public class AllergiesStore {

        private List<PatientAllergen> currentAllergies;
        private Context context;

        public AllergiesStore() {
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

        public void load(Context ctx) {
            context = ctx;
            reload();
        }

        public boolean storeAllergen(PatientAllergen allergen) {
            int rows;
            try {
                rows = getDao().create(allergen);
            } catch (SQLException e) {
                Log.e(TAG, "storeAllergen: couldn't create allergy", e);
                return false;
            }
            Log.d(TAG, "storeAllergen: inserted allergen into database: " + allergen);
            if (rows == 1) {
                checkConflictsAndCreateAlerts(new AllergenVO(allergen));
                currentAllergies.add(allergen);
                return true;
            }
            return false;
        }

        public boolean storeAllergens(final Collection<PatientAllergen> allergens) {
            return (boolean) DB.transaction(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    boolean res = true;
                    for (PatientAllergen allergen : allergens) {
                        res &= storeAllergen(allergen);
                    }
                    return res;
                }
            });
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
                return -1;
            }

        }

        public List<PatientAllergen> getAllergies() {
            return currentAllergies;
        }

        public boolean isEmpty() {
            return currentAllergies.isEmpty();
        }

        public List<AllergenVO> getAllergiesVO() {
            return AllergenConversionUtil.toVO(currentAllergies);
        }
    }

}
