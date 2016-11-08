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

package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.prospects.ProspectUtils;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class MedicinesListFragment extends Fragment {

    public static final String PARAM_DOWNLOAD_ID = "medicinesListFragment_download_id";

    private static final String TAG = "MedicinesListFragment";

    List<Medicine> mMedicines;
    OnMedicineSelectedListener mMedicineSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicines_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.medicines_list);
        View empty = rootView.findViewById(android.R.id.empty);
        listview.setEmptyView(empty);
        mMedicines = DB.medicines().findAllForActivePatient(getContext());
        adapter = new MedicinesListAdapter(getActivity(), R.layout.medicines_list_item, mMedicines);
        listview.setAdapter(adapter);

        return rootView;
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
        Log.d(getTag(), "Medicines - Notify data change");
        new ReloadItemsTask().execute();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mMedicines = DB.medicines().findAllForActivePatient(getContext());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.clear();
            for (Medicine m : mMedicines) {
                adapter.add(m);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private View createMedicineListItem(LayoutInflater inflater, final Medicine medicine) {

        View item = inflater.inflate(R.layout.medicines_list_item, null);

        ((TextView) item.findViewById(R.id.medicines_list_item_name)).setText(medicine.name());

        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        icon.setImageDrawable(new IconicsDrawable(getContext())
                .icon(medicine.presentation().icon())
                //.color(Color.WHITE)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(8)
                .sizeDp(40));

        View overlay = item.findViewById(R.id.medicines_list_item_container);
        overlay.setTag(medicine);

        String nextPickup = medicine.nextPickup();
        if (nextPickup != null) {
            TextView stockInfo = (TextView) item.findViewById(R.id.stock_info);
            stockInfo.setText("Próxima e-Receta: " + nextPickup);
        }


        String cn = medicine.cn();
        final Prescription p = cn != null ? Prescription.findByCn(medicine.cn()) : null;
        boolean boundToPrescription = p != null;
        boolean hasProspect = (p != null && p.hasProspect);

        if (!boundToPrescription) {
            item.findViewById(R.id.imageView).setVisibility(View.GONE);
        } else {
            IconicsDrawable ic = new IconicsDrawable(getContext())
                    .icon(CommunityMaterial.Icon.cmd_file_document)
                    .colorRes(R.color.agenda_item_title)
                    .paddingDp(10)
                    .sizeDp(40);
            ((ImageView) item.findViewById(R.id.imageView)).setImageDrawable(ic);

            if (hasProspect) {
                item.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickProspect(medicine, p);
                    }
                });
            } else {
                item.findViewById(R.id.imageView).setAlpha(0.2f);
                item.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Snack.show(R.string.download_prospect_not_available_message, getActivity());
                    }
                });
            }
        }

        if (p != null && p.affectsDriving) {
            Drawable icDriv = new IconicsDrawable(getContext())
                    .icon(CommunityMaterial.Icon.cmd_comment_alert)
                    .color(Color.parseColor("#f39c12"))
                    .paddingDp(10)
                    .sizeDp(40);
            ((ImageView) item.findViewById(R.id.drive_icon)).setImageDrawable(icDriv);
            item.findViewById(R.id.drive_icon).setVisibility(View.VISIBLE);
            item.findViewById(R.id.drive_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDrivingAdvice(p);
                }
            });
        } else {
            item.findViewById(R.id.drive_icon).setVisibility(View.GONE);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Medicine m = (Medicine) view.getTag();
                if (mMedicineSelectedCallback != null && m != null) {
                    Log.d(getTag(), "Click at " + m.name());
                    mMedicineSelectedCallback.onMedicineSelected(m);
                } else {
                    Log.d(getTag(), "No callback set");
                }
            }
        };

        overlay.setOnClickListener(clickListener);
        overlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view.getTag() != null)
                    showDeleteConfirmationDialog((Medicine) view.getTag());
                return true;
            }
        });
        return item;
    }


    void onClickProspect(Medicine medicine, final Prescription p) {
        if (p != null) {
            openProspect(p);
        } else {
            Toast.makeText(getActivity(), R.string.download_prospect_not_available_message, Toast.LENGTH_SHORT).show();
            Log.d("MedicinesList", "Prospect url not available");
        }
    }


    public void openProspect(Prescription p) {
        ProspectUtils.openProspect(p,getActivity(), true);
    }

    public void showDrivingAdvice(final Prescription p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.driving_warning))
                .setTitle(getString(R.string.driving_warning_title))
                .setIcon(getResources().getDrawable(R.drawable.ic_warning_amber_48dp));
        if (p.hasProspect) {
            builder.setPositiveButton(getString(R.string.driving_warning_show_prospect), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openProspect(p);

                }
            });
        }
        builder.setNeutralButton(getString(R.string.driving_warning_gotit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    void showDeleteConfirmationDialog(final Medicine m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getString(R.string.remove_medicine_message_short), m.name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DB.medicines().deleteCascade(m, true);
                        notifyDataChange();
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

    private class MedicinesListAdapter extends ArrayAdapter<Medicine> {

        public MedicinesListAdapter(Context context, int layoutResourceId, List<Medicine> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createMedicineListItem(layoutInflater, mMedicines.get(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If the container activity has implemented the callback interface, set it as listener
        if (activity instanceof OnMedicineSelectedListener) {
            mMedicineSelectedCallback = (OnMedicineSelectedListener) activity;
        }
    }


    //
    // Container Activity must implement this interface
    //
    public interface OnMedicineSelectedListener {
        void onMedicineSelected(Medicine m);

        void onCreateMedicine();
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
        }
    }

}