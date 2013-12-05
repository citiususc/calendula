package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.store.MedicineStore;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class MedicineCreateOrEditFragment extends Fragment {

    OnMedicineEditListener mMedicineEditCallback;
    Medicine mMedicine;

    TextView mNameTextView;
    Button mConfirmButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_medicine, container, false);
        mNameTextView = (TextView) rootView.findViewById(R.id.medicine_edit_name);
        mConfirmButton = (Button) rootView.findViewById(R.id.medicine_button_ok);
        mConfirmButton.setText(getString(mMedicine == null ? R.string.create_medicine_button_text : R.string.edit_medicine_button_text));
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEdit();
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey("medicine")) {
            mMedicine = MedicineStore.getInstance().getByName(savedInstanceState.getString("medicine"));
            if (mMedicine != null)
                mConfirmButton.setText(getString(R.string.edit_routine_button_text));
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMedicine != null)
            outState.putString("medicine", mMedicine.getName());
    }

    public void setMedicne(Medicine r) {
        Log.d(getTag(), "Medicine set: " + r.getName());
        mMedicine = r;
        mNameTextView.setText(mMedicine.getName());
        mConfirmButton.setText(getString(R.string.edit_medicine_button_text));

    }

    public void clear() {
        mMedicine = null;
        mNameTextView.setText("");
        mConfirmButton.setText(getString(R.string.create_medicine_button_text));
    }


    private void onEdit() {

        String name = mNameTextView.getText().toString();

        // if editing
        if (mMedicine != null) {
            mMedicine.setName(name);
            if (mMedicineEditCallback != null) {
                mMedicineEditCallback.onMedicineEdited(mMedicine);
            }
        }
        // if creating
        else {
            Medicine m = new Medicine(name);
            MedicineStore.getInstance().addMedicine(m);
            if (mMedicineEditCallback != null) {
                mMedicineEditCallback.onMedicineCreated(m);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.d(getTag(), "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnMedicineEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnMedicineEditListener) {
            mMedicineEditCallback = (OnMedicineEditListener) activity;
        }
    }

    // Container Activity must implement this interface
    public interface OnMedicineEditListener {
        public void onMedicineEdited(Medicine r);

        public void onMedicineCreated(Medicine r);
    }

}