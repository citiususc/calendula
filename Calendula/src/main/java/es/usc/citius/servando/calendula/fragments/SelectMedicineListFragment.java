package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class SelectMedicineListFragment extends Fragment {


    List<Medicine> mMedicines;
    ArrayAdapter adapter;
    ListView listview;
    long selectedId = -1;

    ScheduleCreationActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_medicine_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.medicines_list);

        Medicine med = ScheduleCreationHelper.instance().getSelectedMed();
        if (med != null)
            selectedId = med.getId();


        mMedicines = Medicine.findAll();
        adapter = new MedicinesListAdapter(getActivity(), R.layout.medicines_list_item, mMedicines);
        listview.setAdapter(adapter);
        return rootView;
    }

    private View createMedicineListItem(LayoutInflater inflater, final Medicine medicine) {

        final View item = inflater.inflate(R.layout.select_medicines_list_item, null);

        ((TextView) item.findViewById(R.id.medicines_list_item_name)).setText(medicine.name());

        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        icon.setImageDrawable(getResources().getDrawable(medicine.presentation().getDrawable()));

        View overlay = item.findViewById(R.id.medicines_list_item_container);
        overlay.setTag(medicine);

        if (selectedId == medicine.getId()) {
            item.findViewById(R.id.selection_indicator).setVisibility(View.VISIBLE);
            item.findViewById(R.id.selection_mask).setVisibility(View.VISIBLE);
            item.findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
        } else {
            item.findViewById(R.id.selection_indicator).setVisibility(View.INVISIBLE);
            item.findViewById(R.id.selection_mask).setVisibility(View.INVISIBLE);
            item.findViewById(R.id.imageView2).setVisibility(View.INVISIBLE);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Medicine m = (Medicine) view.getTag();
                selectedId = m.getId();
                if (mActivity != null) {
                    mActivity.onMedicineSelected(m);
                }
                adapter.notifyDataSetChanged();
            }
        };
        overlay.setOnClickListener(clickListener);
        return item;
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
        if (activity instanceof ScheduleCreationActivity) {
            mActivity = (ScheduleCreationActivity) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }
}