package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

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

public class AllergiesActivity extends CalendulaActivity implements AllergiesAdapter.AllergiesOnLongClickListener {


    private static final String TAG = "AllergiesActivity";
    private View searchView;
    private View closeSearchButton;
    private AddFloatingActionButton addButton;
    private EditText searchEditText;
    private ListView searchList;
    private AllergiesAutocompleteAdapter searchAdapter;
    private AllergiesAdapter allergiesAdapter;
    private RecyclerView allergiesRecycler;
    private AllergiesStore store;
    private int color;
    private Activity activity;
    private Dao<PatientAllergen, Long> dao = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_allergies);
        //setup toolbar and statusbar
        color = DB.patients().getActive(this).color();
        setupToolbar(getString(R.string.title_activity_allergies), color);
        setupStatusBar(color);

        //load allergies
        store = new AllergiesStore();
        store.load(this);
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
        allergiesAdapter = new AllergiesAdapter(store, this);
        allergiesRecycler.setAdapter(allergiesAdapter);
    }

    private void setupSearchView() {
        searchView = findViewById(R.id.search_view);
        closeSearchButton = findViewById(R.id.close_search_button);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchList = (ListView) findViewById(R.id.search_list);

        closeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearchView();
            }
        });

        searchAdapter = new AllergiesAutocompleteAdapter(this, android.R.layout.simple_dropdown_item_1line, store);
        searchList.setAdapter(searchAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() >= 3) {
                    String filter = searchEditText.getText().toString();
                    searchAdapter.getFilter().filter(filter);
                }
            }
        });

        final Activity activity = this;
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardUtils.hideKeyboard(activity);
                PatientAllergen allergen = (PatientAllergen) searchAdapter.getItem(position);
                boolean ans = storeAllergen(allergen);
                if (ans) {
                    Snack.show(R.string.message_allergy_add_success, activity);
                } else {
                    Snack.show(R.string.message_allergy_add_failure, activity);
                }
                clearSearchView();
            }
        });

        searchView.setBackgroundColor(color);

        hideSearchView();
    }

    private boolean storeAllergen(PatientAllergen allergen) {
        int rows;
        try {
            rows = getDao().create(allergen);
        } catch (SQLException e) {
            Log.e(TAG, "onItemClick: couldn't create allergy", e);
            return false;
        }
        Log.d(TAG, "storeAllergen: inserted allergen into database: " + allergen);
        if (rows == 1) {
            notifyDataChanged();
            return true;
        }
        return false;
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
    }

    private void clearSearchView() {
        hideSearchView();
        searchEditText.setText("");
        searchAdapter.clear();
    }

    private Dao<PatientAllergen, Long> getDao() {
        if (dao == null)
            dao = new PatientAllergenDao(DB.helper()).getConcreteDao();
        return dao;
    }

    @Override
    public void onLongClick(PatientAllergen allergen) {
        Log.d(TAG, "onLongClick: long click received for allergen " + allergen);
        showDeleteConfirmationDialog(allergen);
    }

    void showDeleteConfirmationDialog(final PatientAllergen a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.remove_allergy_message_short), a.getName()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            DB.allergens().delete(a);
                            notifyDataChanged();
                        } catch (SQLException e) {
                            Log.e(TAG, "Couldn't delete allergen " + a, e);
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

    public void notifyDataChanged() {

        new ReloadDataTask().execute();
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

        public List<PatientAllergen> getAllergies() {
            return currentAllergies;
        }
    }

}
