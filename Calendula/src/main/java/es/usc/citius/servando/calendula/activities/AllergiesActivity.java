package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.AllergiesAdapter;
import es.usc.citius.servando.calendula.adapters.AllergiesAutocompleteAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.PatientAllergenDao;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.Snack;

public class AllergiesActivity extends CalendulaActivity implements AllergenListeners.DeleteAllergyActionListener, AllergenListeners.AddAllergyActionListener {


    private static final String TAG = "AllergiesActivity";
    private View searchView;
    private View closeSearchButton;
    private AddFloatingActionButton addButton;
    private EditText searchEditText;
    private RecyclerView searchList;
    private AllergiesAutocompleteAdapter searchAdapter;
    private AllergiesAdapter allergiesAdapter;
    private RecyclerView allergiesRecycler;
    private AllergiesStore store;
    private TextView allergiesPlaceholder;
    private TextView allergiesSearchPlaceholder;
    private int color;
    private Activity activity;
    private Dao<PatientAllergen, Long> dao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        setContentView(R.layout.activity_allergies);

        allergiesPlaceholder = (TextView) findViewById(R.id.textview_no_allergies_placeholder);
        allergiesSearchPlaceholder= (TextView) findViewById(R.id.allergies_search_placeholder);

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

        searchAdapter = new AllergiesAutocompleteAdapter(store, this, this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
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
                    if (search.length() >= 3) {
                        doSearch();
                    }
                } else {
                    if (closeSearchButton.getVisibility() == View.VISIBLE)
                        closeSearchButton.setVisibility(View.GONE);
                    if (searchAdapter.getItemCount() > 0)
                        searchAdapter.clear();
                }
                checkSearchPlaceholder(search.length());
            }
        });

        searchView.setBackgroundColor(color);

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

    private void checkSearchPlaceholder(int textLength){
        if(textLength>=3){
            allergiesSearchPlaceholder.setVisibility(View.GONE);
        }else {
            allergiesSearchPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void doSearch() {
        String filter = searchEditText.getText().toString().trim();
        searchAdapter.getFilter().filter(filter);
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

    void showDeleteConfirmationDialog(final PatientAllergen a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Activity ac = this;
        builder.setMessage(String.format(getString(R.string.remove_allergy_message_short), a.getName()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int index = store.deleteAllergen(a);
                        if (index >= 0) {
                            checkPlaceholder();
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

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + (searchView.getVisibility() == View.VISIBLE));
        if (searchView.getVisibility() == View.VISIBLE) {
            hideSearchView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAddAction(PatientAllergen allergen) {
        KeyboardUtils.hideKeyboard(this);
        boolean ans = store.storeAllergen(allergen);
        if (ans) {
            searchAdapter.remove(allergen);
            checkPlaceholder();
            Snack.show(getString(R.string.message_allergy_add_success, allergen.getName()), this);
        } else {
            Snack.show(R.string.message_allergy_add_failure, this);
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
            currentAllergies = DB.allergens().findAllForActivePatient(context);
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
                currentAllergies.add(allergen);
                return true;
            }
            return false;
        }

        public int deleteAllergen(PatientAllergen a) {
            try {
                int index = currentAllergies.indexOf(a);
                DB.allergens().delete(a);
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
    }

}
