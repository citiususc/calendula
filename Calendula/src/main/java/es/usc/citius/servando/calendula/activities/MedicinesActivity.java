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

package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.MedicineCreateOrEditFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;

public class MedicinesActivity extends CalendulaActivity implements MedicineCreateOrEditFragment.OnMedicineEditListener {

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
    MenuItem removeItem;
    View searchView;
    EditText searchEditText;
    View closeSearchButton;
    FloatingActionButton addButton;
    ListView searchList;
    ArrayAdapter<Prescription> adapter;
    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicines);
        color = DB.patients().getActive(this).color();
        setupToolbar(null, color);
        setupStatusBar(color);

        processIntent();

        TextView title = ((TextView) findViewById(R.id.textView2));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        searchView = findViewById(R.id.search_view);
        closeSearchButton = findViewById(R.id.close_search_button);
        addButton = (FloatingActionButton) findViewById(R.id.add_button);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchList = (ListView) findViewById(R.id.search_list);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
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

        title.setBackgroundColor(color);
        searchView.setBackgroundColor(color);

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
                final Prescription p = mData.get(position);
                ((TextView) item.findViewById(R.id.text1)).setText(p.shortName() + (p.generic ? " (G)" : ""));
                ((TextView) item.findViewById(R.id.text2)).setText(p.dose);
                ((TextView) item.findViewById(R.id.text3)).setText(p.content);
                ((TextView) item.findViewById(R.id.text4)).setText(Strings.toCamelCase(p.name, " "));

                ((TextView) item.findViewById(R.id.text1)).setTextColor(Color.parseColor("#222222"));
                ((TextView) item.findViewById(R.id.text4)).setTextColor(color);
                ImageView prospectIcon = ((ImageView) item.findViewById(R.id.prospect_icon));

                Drawable icProspect = new IconicsDrawable(getContext())
                        .icon(CommunityMaterial.Icon.cmd_file_document)
                        .colorRes(R.color.agenda_item_title)
                        .paddingDp(10)
                        .sizeDp(40);

                prospectIcon.setImageDrawable(icProspect);
                prospectIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openProspect(p);
                    }
                });

                Presentation pres = p.expectedPresentation();
                if (pres != null) {

                    Drawable ic = new IconicsDrawable(getContext())
                            .icon(Presentation.iconFor(p.expectedPresentation()))
                            .colorRes(R.color.agenda_item_title)
                            .paddingDp(10)
                            .sizeDp(72);

                    ((ImageView) item.findViewById(R.id.presentation_image)).setImageDrawable(ic);
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

    public void openProspect(Prescription p) {
        final String url = MedicinesListFragment.PROSPECT_URL.replaceAll("#ID#", p.pid);
        Intent i = new Intent(this, WebViewActivity.class);
        WebViewActivity.WebViewRequest request = new WebViewActivity.WebViewRequest(url);
        request.setCustomCss("prospectView.css");
        request.setConnectionErrorMessage(getString(R.string.message_prospect_connection_error));
        request.setNotFoundErrorMessage(getString(R.string.message_prospect_not_found_error));
        request.setLoadingMessage(getString(R.string.message_prospect_loading));
        request.setTitle(getString(R.string.title_prospect_webview));
        request.setCacheType(WebViewActivity.WebViewRequest.CacheType.NO_CACHE);
        request.setJavaScriptEnabled(true);
        i.putExtra(WebViewActivity.PARAM_WEBVIEW_REQUEST, request);
        this.startActivity(i);
    }


}
