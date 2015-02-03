package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Persistence;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class MedicinesListFragment extends Fragment {


    List<Medicine> mMedicines;
    OnMedicineSelectedListener mMedicineSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medicines_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.medicines_list);
        mMedicines = Medicine.findAll();
        adapter = new MedicinesListAdapter(getActivity(), R.layout.medicines_list_item, mMedicines);
        listview.setAdapter(adapter);
        return rootView;
    }


    public void notifyDataChange() {
        Log.d(getTag(), "Medicines - Notify data change");
        new ReloadItemsTask().execute();
    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mMedicines = Medicine.findAll();

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
        icon.setImageDrawable(getResources().getDrawable(medicine.presentation().getDrawable()));

        View overlay = item.findViewById(R.id.medicines_list_item_container);
        overlay.setTag(medicine);

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


    void showDeleteConfirmationDialog(final Medicine m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(String.format(getString(R.string.remove_medicine_message_short), m.name()))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Persistence.instance().deleteCascade(m);
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
        public void onMedicineSelected(Medicine m);

        public void onCreateMedicine();
    }
}