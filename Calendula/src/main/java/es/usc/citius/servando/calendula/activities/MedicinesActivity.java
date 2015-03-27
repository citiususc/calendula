package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;

public class MedicinesActivity extends ActionBarActivity implements MedicineCreateOrEditFragment.OnMedicineEditListener {

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
    Toolbar toolbar;
    MenuItem removeItem;
    View searchView;
    EditText searchEditText;
    View closeSearchButton;
    FloatingActionButton addButton;
    ListView searchList;
    ArrayAdapter<Prescription> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.android_blue_statusbar));
        }

        processIntent();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        searchView = findViewById(R.id.search_view);
        closeSearchButton = findViewById(R.id.close_search_button);
        addButton = (FloatingActionButton) findViewById(R.id.add_button);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchList = (ListView) findViewById(R.id.search_list);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(new InsetDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_48dp), 10, 10, 10, 10));
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        boolean create = getIntent().getBooleanExtra("create", false);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MedicineCreateOrEditFragment) getViewPagerFragment(0)).onEdit();
            }
        });

        closeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearchView();
            }
        });

        adapter = new AutoCompleteAdapter(this, R.layout.med_drop_down_item);
        searchList.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
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

        hideSearchView();


    }


    private void processIntent() {
        mMedicineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
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
//            case R.id.action_search:
//                showSearchView();
//                return true;
            default:
                finish();
                return true;
        }
    }


    public void showSearchView(final String text) {
        addButton.setVisibility(View.GONE);
        searchEditText.requestFocus();
        searchView.setVisibility(View.VISIBLE);

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
        }, 30);

    }

    public void hideSearchView() {
        searchView.setVisibility(View.GONE);
        addButton.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    @Override
    public void onMedicineEdited(Medicine m) {
        DB.medicines().saveAndFireEvent(m);
        Snack.show(getString(R.string.medicine_edited_message), this);
        finish();
    }

    @Override
    public void onMedicineCreated(Medicine m) {
        DB.medicines().saveAndFireEvent(m);
        CalendulaApp.eventBus().post(new PersistenceEvents.MedicineAddedEvent(m.getId()));
        Toast.makeText(this, getString(R.string.medicine_created_message), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onMedicineDeleted(Medicine m) {
        Snack.show(getString(R.string.medicine_deleted_message), this);
        DB.medicines().deleteCascade(m, true);
        finish();
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
            f.setArguments(args);
            return f;
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }

    }

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility() == View.VISIBLE) {
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

    // Search adapter


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
                final LayoutInflater inflater = getLayoutInflater();
                item = inflater.inflate(R.layout.med_drop_down_item, null);
            }
            if (mData.size() > position) {
                Prescription p = mData.get(position);
                ((TextView) item.findViewById(R.id.text1)).setText(p.shortName() + (p.generic ? " (G)" : ""));
                ((TextView) item.findViewById(R.id.text2)).setText(p.dose);
                ((TextView) item.findViewById(R.id.text3)).setText(p.content);
                ((TextView) item.findViewById(R.id.text4)).setText(Strings.toCamelCase(p.name, " "));
                Presentation pres = p.expectedPresentation();
                if (pres != null) {
                    ((ImageView) item.findViewById(R.id.presentation_image)).setImageResource(pres.getDrawable());
                } else {
                    ((ImageView) item.findViewById(R.id.presentation_image)).setImageDrawable(null);
                }
            }
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
//                            if(constraint==null || constraint.length() < 3){
//                                // Now assign the values and count to the FilterResults object
//                                filterResults.values = null;
//                                filterResults.count = 0;
//                            }else{
                            List<Prescription> prescriptions = Prescription.findByName(constraint.toString(), 50);
                            mData = prescriptions;//Fetcher.fetchNames(constraint.toString());
                            // Now assign the values and count to the FilterResults object
                            filterResults.values = mData;
                            filterResults.count = mData.size();
//                            }

                        } catch (Exception e) {
                            Log.e("myException", e.getMessage());
                            filterResults.values = null;
                            filterResults.count = 0;
                        }

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


}
