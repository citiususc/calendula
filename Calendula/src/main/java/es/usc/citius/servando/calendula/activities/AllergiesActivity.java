package es.usc.citius.servando.calendula.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.j256.ormlite.dao.Dao;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert;
import es.usc.citius.servando.calendula.persistence.alerts.AllergyPatientAlert.AllergyAlertInfo;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.alerts.AlertManager;

public class AllergiesActivity extends CalendulaActivity implements AllergyGroupItem.AllergyGroupListener {


    private static final String TAG = "AllergiesActivity";
    private View searchView;
    private View closeSearchButton;
    private AddFloatingActionButton addButton;
    private EditText searchEditText;
    private RecyclerView searchList;
    private FastItemAdapter<AbstractItem> searchAdapter;
    private FastItemAdapter allergiesAdapter;
    private RecyclerView allergiesRecycler;
    private AllergiesStore store;
    private TextView allergiesPlaceholder;
    private TextView allergiesSearchPlaceholder;
    private int color;
    private Dao<PatientAllergen, Long> dao = null;
    private FloatingActionButton selectFab;
    private LinearLayout selectLayout;
    private TextView selectText;
    private FastAdapter.OnClickListener<AbstractItem> cl = new FastAdapter.OnClickListener<AbstractItem>() {
        @Override
        public boolean onClick(View v, IAdapter<AbstractItem> adapter, AbstractItem item, int position) {

            KeyboardUtils.hideKeyboard(AllergiesActivity.this);
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
    private ProgressBar progressBar;
    private AsyncTask searchTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //initialize allergies store
        store = new AllergiesStore();

        //setup recycler
        setupAllergiesList();
        //setup FAB
        addButton = (AddFloatingActionButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchView();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        progressBar.getIndeterminateDrawable().setColorFilter(DB.patients().getActive(this).color(),
                android.graphics.PorterDuff.Mode.MULTIPLY);

        //setup search view
        setupSearchView();

        //load allergies, set placeholder if needed
        new LoadAllergiesTask().execute();


    }

    private void setupAllergiesList() {
        allergiesRecycler = (RecyclerView) findViewById(R.id.allergies_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        allergiesRecycler.setLayoutManager(llm);
        allergiesAdapter = new FastItemAdapter<>();
        allergiesAdapter.withSelectable(false);
        allergiesAdapter.withItemEvent(new ClickEventHook<AllergyItem>() {
            @Override
            public void onClick(View v, int position, FastAdapter<AllergyItem> fastAdapter, AllergyItem item) {
                if (v != null)
                    showDeleteConfirmationDialog(item);
            }

            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof AllergyItem.ViewHolder)
                    return ((AllergyItem.ViewHolder) viewHolder).deleteButton;
                return null;
            }

        });
        allergiesRecycler.setAdapter(allergiesAdapter);
    }

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
                selectLayout.setVisibility(View.GONE);
                new SaveAllergiesTask().execute(getSelected());
            }
        });

        hideSearchView();
    }

    private void hideAllergiesView(boolean hide) {
        final int visibility = hide ? View.GONE : View.VISIBLE;
        allergiesRecycler.setVisibility(visibility);
        addButton.setVisibility(visibility);
        if (!hide)
            checkPlaceholder();

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
            AllergyGroupItem g = new AllergyGroupItem(key, this, this);
            g.withSubItems(groups.get(key));
            items.add(g);
        }
        for (PatientAllergen allergen : allergies) {
            items.add(new AllergyItem(allergen, this));
        }
        return items;
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
            if (searchTask != null)
                searchTask.cancel(true);
            searchTask = new DoSearchTask();
            searchTask.execute(new String[]{filter});
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
        Log.d(TAG, "getSelected() returned: " + selected.size() + " elements");
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

    private void showDeleteConfirmationDialog(final AllergyItem a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.remove_allergy_message_short), a.getAllergen().getName()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeleteAllergyTask().execute(a);
                        dialog.dismiss();
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

    private void showDeleteConfirmationDialog(final AllergyGroupItem a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.remove_allergy_message_short), a.getTitle()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeleteAllergyGroupTask().execute(a);
                        dialog.dismiss();
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
            finish();
        }
    }

    private boolean checkConflictsAndCreateAlerts(final AllergenVO allergen) {
        final List<Medicine> conflicts = AllergenFacade.checkNewMedicineAllergies(this, allergen);
        if (!conflicts.isEmpty()) {
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
        return !conflicts.isEmpty();
    }

    @Override
    public void expandButton(AllergyGroupItem item, boolean expanded) {
        Log.d(TAG, "expandButton() called with: item = [" + item + "], expanded = [" + expanded + "]");
        final int pos = allergiesAdapter.getAdapterPosition(item);
        if (expanded)
            allergiesAdapter.expand(pos);
        else
            allergiesAdapter.collapse(pos);
    }

    @Override
    public void deleteButton(AllergyGroupItem item) {
        showDeleteConfirmationDialog(item);
    }

    public enum SaveResult {
        OK, ERROR, ALLERGY
    }

    private class SaveAllergiesTask extends AsyncTask<Collection<IItem>, Void, SaveAllergiesTask.Result> {

        @Override
        protected Result doInBackground(Collection<IItem>... items) {
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

        class Result {
            boolean saved;
            boolean allergies;
            List<AbstractItem> allergyItems;

            public Result(boolean saved, boolean allergies, List<AbstractItem> allergyItems) {
                this.saved = saved;
                this.allergies = allergies;
                this.allergyItems = allergyItems;
            }
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
            closeSearchView();
        }


    }

    private class DoSearchTask extends AsyncTask<String, Void, List<AbstractItem>> {

        @Override
        protected void onPreExecute() {
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

            //find words for groups
            final Map<String, List<AllergenVO>> groups = new ArrayMap<>();
            final List<AllergenVO> toRemove = new ArrayList<>();
//            String lookRegex = "\\b" + filter;
//            Pattern look = Pattern.compile(lookRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            String getRegex = ".*\\b(" + filter.trim() + "\\w*).*";
            Log.d(TAG, "doInBackground: regex is " + getRegex);
            Pattern search = Pattern.compile(getRegex, Pattern.CASE_INSENSITIVE);
            for (AllergenVO allergenVO : allergenVOs) {
                String name = allergenVO.getName();
                final Matcher matcher = search.matcher(name);
                if (matcher.matches()) {
                    String groupname = matcher.group().trim().toLowerCase();
                    groupname = Character.toUpperCase(groupname.charAt(0)) + groupname.substring(1);

                    if (groups.keySet().contains(groupname)) {
                        groups.get(groupname).add(allergenVO);
                    } else {
                        groups.put(groupname, new ArrayList<>(Collections.singletonList(allergenVO)));
                    }
                    toRemove.add(allergenVO);
                }
            }

            // delete groups with only one element
            final Set<String> keySet = new HashSet<>(groups.keySet());
            for (String k : keySet) {
                final List<AllergenVO> members = groups.get(k);
                if (members.size() == 1) {
                    groups.remove(k);
                    toRemove.remove(members.get(0));
                }
            }

            // sort elements into groups
            allergenVOs.removeAll(toRemove);
            List<AbstractItem> items = new ArrayList<>();
            for (String s : groups.keySet()) {
                final List<AllergenVO> subs = groups.get(s);
                if (!subs.isEmpty()) {
                    AllergenGroupItem g = new AllergenGroupItem(s, "");
                    List<AllergenGroupSubItem> sub = new ArrayList<>();
                    for (AllergenVO vo : subs) {
                        final AllergenGroupSubItem e = new AllergenGroupSubItem(vo, AllergiesActivity.this);
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
            List<AbstractItem> items = getAllergyItems();
            return items;
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
            checkPlaceholder();
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

    private class DeleteAllergyGroupTask extends AsyncTask<AllergyGroupItem, Void, Integer> {

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
            checkPlaceholder();
            hideAllergiesView(false);
        }

        @Override
        protected Integer doInBackground(AllergyGroupItem... params) {
            Log.d(TAG, "doInBackground() called with: params = [" + params + "]");
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
            return k >= 0 ? index : k;
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

        public boolean isEmpty() {
            return currentAllergies.isEmpty();
        }

        public List<AllergenVO> getAllergiesVO() {
            return AllergenConversionUtil.toVO(currentAllergies);
        }
    }

}
