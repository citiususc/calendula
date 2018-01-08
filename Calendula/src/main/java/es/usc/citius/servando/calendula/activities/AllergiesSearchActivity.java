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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISelectionListener;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter_extensions.utilities.SubItemUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import es.usc.citius.servando.calendula.allergies.AllergenGroupWrapper;
import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.AllergyGroup;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.KeyboardUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.Strings;

@SuppressWarnings("unchecked")
public class AllergiesSearchActivity extends CalendulaActivity {


    public static final int REQUEST_NEW_ALLERGIES = 1;
    private static final String TAG = "AllergiesSearchAct";
    private final ISelectionListener<AbstractItem> selectionListener = new AllergySelectionListener();

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
    @BindView(R.id.main_progress_bar)
    protected ProgressBar progressBar;
    @BindView(R.id.accept_selection_button)
    protected FloatingActionButton acceptFab;
    @BindView(R.id.search_layout)
    protected RelativeLayout searchLayout;

    private FastItemAdapter<AbstractItem> searchAdapter;
    private List<AllergyGroup> groups;
    private DoSearchTask searchTask = null;
    private List<AllergenVO> patientAllergies;

    @Override
    public void onBackPressed() {
        cancel();
    }

    /**
     * @return <code>true</code> if db was valid when called, <code>false</code> otherwise
     */
    public boolean askForDatabaseIfNeeded() {

        SharedPreferences prefs = PreferenceUtils.instance().preferences();
        boolean validDB = prefs.getString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), getString(R.string.database_none_id)).equals(getString(R.string.database_aemps_id));

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
                            Intent i = new Intent(AllergiesSearchActivity.this, SettingsActivity.class);
                            i.putExtra(SettingsActivity.EXTRA_SHOW_DB_DIALOG, true);
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
        return validDB;
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
        acceptFab.setEnabled(false);
        Intent returnIntent = new Intent();
        final List<AbstractItem> selected = getSelected();
        ArrayList<AllergenGroupWrapper> vos = new ArrayList<>();
        for (IItem i : selected) {
            switch (i.getType()) {
                case R.id.fastadapter_allergen_group_sub_item:
                    final AllergenGroupSubItem item = (AllergenGroupSubItem) i;
                    vos.add(new AllergenGroupWrapper(item.getVo(), item.getParent().getTitle()));
                    break;
                case R.id.fastadapter_allergen_item:
                    final AllergenItem item1 = (AllergenItem) i;
                    vos.add(new AllergenGroupWrapper(item1.getVo()));
                    break;
                default:
                    LogUtil.wtf(TAG, "Invalid item type in adapter: " + i);
                    break;
            }

        }
        returnIntent.putParcelableArrayListExtra("result", vos);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @OnClick(R.id.back_arrow)
    void cancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergies_search);
        ButterKnife.bind(this);

        int color = DB.patients().getActive(this).getColor();
        searchLayout.setBackgroundColor(color);
        setupStatusBar(color);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        final boolean valid = askForDatabaseIfNeeded();
        if (valid) {
            loadData();
        }


    }

    private void loadData() {
        //retrieve allergy groups
        groups = DB.allergyGroups().findAllOrderByPrecedence();

        patientAllergies = AllergenConversionUtil.toVO(DB.patientAllergens().findAllForActivePatient(this));

        //setup search view
        setupSearchView();
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

    private List<AbstractItem> getSelected() {
        final List<AbstractItem> items = new ArrayList<>();
        for (AbstractItem item : searchAdapter.getAdapterItems()) {
            switch (item.getType()) {
                case R.id.fastadapter_allergen_item:
                    if (item.isSelected()) {
                        items.add(item);
                    }
                    break;
                case R.id.fastadapter_allergen_group_item:
                    final List<AllergenGroupSubItem> subItems = ((AllergenGroupItem) item).getSubItems();
                    for (AllergenGroupSubItem subItem : subItems) {
                        if (subItem.isSelected())
                            items.add(subItem);
                    }
                    break;
            }
        }

        LogUtil.d(TAG, "getSelected() returned: " + items.size() + " elements");
        return items;
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

        searchEditText.requestFocus();
        KeyboardUtils.showKeyboard(this);
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

    private class AllergySelectionListener implements ISelectionListener<AbstractItem> {

        @Override
        public void onSelectionChanged(AbstractItem item, boolean selected) {
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
                KeyboardUtils.hideKeyboard(AllergiesSearchActivity.this);
            } else {
                selectLayout.setVisibility(View.GONE);
            }
        }
    }

    private class DoSearchTask extends AsyncTask<String, Void, List<AbstractItem>> {

        @Override
        protected void onPreExecute() {
            searchAdapter.clear();
            selectLayout.setVisibility(View.GONE);
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
                LogUtil.e(TAG, "doInBackground: invalid argument length. Expected 1, got " + params.length);
                throw new IllegalArgumentException("Invalid argument length");
            }

            final String filter = params[0];
            final List<AllergenVO> allergenVOs = AllergenFacade.searchForAllergens(filter);
            if (patientAllergies != null)
                allergenVOs.removeAll(patientAllergies);

            final List<AbstractItem> items = new ArrayList<>();

            final int highlightColor = ContextCompat.getColor(AllergiesSearchActivity.this, R.color.black);
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
                            final AllergenGroupSubItem e = new AllergenGroupSubItem(vo, AllergiesSearchActivity.this);
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
                final AllergenItem e = new AllergenItem(vo, AllergiesSearchActivity.this);
                e.setTitleSpannable(Strings.getHighlighted(e.getTitle(), filter, highlightColor));
                items.add(e);
            }
            Collections.sort(items, new AllergenSearchComparator(filter));

            return items;
        }
    }

    private class AllergenSearchComparator implements Comparator<AbstractItem> {

        private final String filter;

        private AllergenSearchComparator(final String filter) {
            this.filter = filter;
        }

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
                    //if the index is the same, prioritize groups
                    final int ty1 = o1.getType();
                    final int ty2 = o2.getType();
                    final int gid = R.id.fastadapter_allergen_group_item;
                    if (ty1 == gid && ty2 != gid) {
                        return -1;
                    } else if (ty1 != gid && ty2 == gid) {
                        return 1;
                    } else {
                        return t1.compareTo(t2);
                    }
                } else {
                    return i1 < i2 ? -1 : 1;
                }
            } else {
                return t1.compareTo(t2);
            }
        }
    }
}



