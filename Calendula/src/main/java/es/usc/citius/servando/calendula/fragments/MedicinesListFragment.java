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

package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicineInfoActivity;
import es.usc.citius.servando.calendula.adapters.items.MedicineItem;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.medicine.MedicineSortUtil.MedSortType;
import es.usc.citius.servando.calendula.util.view.CollapseExpandAnimator;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class MedicinesListFragment extends Fragment {

    private static final String TAG = "MedicinesListFragment";

    List<Medicine> mMedicines;
    OnMedicineSelectedListener mMedicineSelectedCallback;

    @BindView(R.id.medicines_list)
    RecyclerView recyclerView;
    @BindView(android.R.id.empty)
    View emptyView;
    @BindView(R.id.sort_layout)
    View sortLayout;
    @BindView(R.id.medicine_sort_spinner)
    AppCompatSpinner sortSpinner;
    @BindView(R.id.med_list_container)
    View medListContainer;

    FastItemAdapter<MedicineItem> adapter;
    Handler handler;
    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicines_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        handler = new Handler();
        mMedicines = DB.medicines().findAllForActivePatient(getContext());
        setupRecyclerView();
        setupSortSpinner();
        medListContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!isSortCollapsed()) {
                    toggleSort();
                }
                return false;
            }
        });
        updateViewVisibility();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void notifyDataChange() {
        LogUtil.d(TAG, "Medicines - Notify data change");
        new ReloadItemsTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If the container activity has implemented the callback interface, set it as listener
        if (activity instanceof OnMedicineSelectedListener) {
            mMedicineSelectedCallback = (OnMedicineSelectedListener) activity;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        CalendulaApp.eventBus().register(this);
    }

    @Override
    public void onStop() {
        CalendulaApp.eventBus().unregister(this);
        super.onStop();
    }

    // Method called from the event bus
    @SuppressWarnings("unused")
    public void onEvent(Object evt) {
        if (evt instanceof PersistenceEvents.ActiveUserChangeEvent) {
            notifyDataChange();
        } else if (evt instanceof PersistenceEvents.ModelCreateOrUpdateEvent) {
            if (((PersistenceEvents.ModelCreateOrUpdateEvent) evt).clazz.equals(Medicine.class)) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataChange();
                    }
                });
            }
        }
    }

    public void toggleSort() {
        LogUtil.d(TAG, "toggleSort() called");
        if (isSortCollapsed()) {
            int targetHeight = (int) getResources().getDimension(R.dimen.sort_bar_height);
            CollapseExpandAnimator.expand(sortLayout, 100, targetHeight);
        } else {
            CollapseExpandAnimator.collapse(sortLayout, 100, 0);
        }
    }

    void openMedicineInfoActivity(Medicine medicine, boolean showAlerts) {
        Intent i = new Intent(getActivity(), MedicineInfoActivity.class);
        i.putExtra("medicine_id", medicine.getId());
        i.putExtra("show_alerts", showAlerts);
        getActivity().startActivity(i);
    }

    void showDeleteConfirmationDialog(final Medicine m) {
        String message;
        if (!DB.schedules().findByMedicine(m).isEmpty()) {
            message = String.format(getString(R.string.remove_medicine_message_long), m.name());
        } else {
            message = String.format(getString(R.string.remove_medicine_message_short), m.name());
        }

        new MaterialStyledDialog.Builder(getActivity())
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(getActivity(), CommunityMaterial.Icon.cmd_pill, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setTitle(getString(R.string.remove_medicine_dialog_title))
                .setDescription(message)
                .setCancelable(true)
                .setNeutralText(getString(R.string.dialog_no_option))
                .setPositiveText(getString(R.string.dialog_yes_option))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DB.medicines().deleteCascade(m, true);
                        notifyDataChange();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .show();

    }

    private boolean isSortCollapsed() {
        final boolean collapsed = sortLayout.getLayoutParams().height == 0;
        LogUtil.d(TAG, "isSortCollapsed() returned: " + collapsed);
        return collapsed;
    }

    private void setupSortSpinner() {
        ArrayAdapter<MedSortType> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.sort_spinner_item, MedSortType.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP); //change caret color
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final MedSortType type = (MedSortType) parent.getItemAtPosition(position);
                Comparator<Medicine> cmp = type.comparator();
                if (cmp != null) {
                    Collections.sort(mMedicines, cmp);
                    updateAdapterItems();
                } else {
                    LogUtil.e(TAG, "onItemSelected: null comparator! wrong sort type?");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //noop
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        adapter = new FastItemAdapter<>();
        adapter.withSelectable(false);
        adapter.withPositionBasedStateManagement(false);
        for (Medicine mMedicine : mMedicines) {
            adapter.add(new MedicineItem(mMedicine));
        }
        adapter.withOnLongClickListener(new FastAdapter.OnLongClickListener<MedicineItem>() {
            @Override
            public boolean onLongClick(View v, IAdapter<MedicineItem> adapter, MedicineItem item, int position) {
                showDeleteConfirmationDialog(item.getMedicine());
                return true;
            }
        });

        adapter.withItemEvent(new ClickEventHook<MedicineItem>() {
            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof MedicineItem.MedicineViewHolder)
                    return ((MedicineItem.MedicineViewHolder) viewHolder).alertIcon;
                return null;
            }

            @Override
            public void onClick(View v, int position, FastAdapter<MedicineItem> fastAdapter, MedicineItem item) {
                openMedicineInfoActivity(item.getMedicine(), true);
            }
        });

        adapter.withOnClickListener(new FastAdapter.OnClickListener<MedicineItem>() {
            @Override
            public boolean onClick(View v, IAdapter<MedicineItem> adapter, MedicineItem item, int position) {
                if (mMedicineSelectedCallback != null && item != null && item.getMedicine() != null)
                    mMedicineSelectedCallback.onMedicineSelected(item.getMedicine());
                return true;
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!isSortCollapsed()) {
                    toggleSort();
                }
                return false;
            }
        });
    }

    private void updateViewVisibility() {
        if (mMedicines.size() > 0) {
            emptyView.setVisibility(View.GONE);
            sortLayout.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            sortLayout.setVisibility(View.GONE);
        }
    }

    private void updateAdapterItems() {
        adapter.clear();
        for (Medicine m : mMedicines) {
            adapter.add(new MedicineItem(m));
        }
        adapter.notifyAdapterDataSetChanged();
    }

    //
    // Container Activity must implement this interface
    //
    public interface OnMedicineSelectedListener {
        void onMedicineSelected(Medicine m);

        void onCreateMedicine();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            LogUtil.d(TAG, "Reloading items...");
            mMedicines = DB.medicines().findAllForActivePatient(getContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final MedSortType sortType = (MedSortType) sortSpinner.getSelectedItem();
            Collections.sort(mMedicines, sortType.comparator());
            updateViewVisibility();
            updateAdapterItems();
            LogUtil.d(TAG, "Reloaded items, count: " + mMedicines.size());
        }
    }

}