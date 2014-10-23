package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;
import es.usc.citius.servando.calendula.util.Screen;

/**
 * Created by joseangel.pineiro on 12/4/13.
 */
public class MedicineCreateOrEditFragment extends Fragment {

    OnMedicineEditListener mMedicineEditCallback;
    Medicine mMedicine;

    Boolean showConfirmButton = true;
    TextView mNameTextView;
    Button mConfirmButton;
    Presentation selectedPresentation;
    HorizontalScrollView presentationScroll;

    boolean showcaseShown = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_medicine, container, false);
        final String[] names = Medicine.findAllMedicineNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, names);

        mNameTextView = (TextView) rootView.findViewById(R.id.medicine_edit_name);
        ((AutoCompleteTextView) mNameTextView).setAdapter(adapter);
        ((AutoCompleteTextView) mNameTextView).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                String name = (String) parent.getItemAtPosition(pos);
                mMedicine = Medicine.findByName(name);
                Log.d(getTag(), "Medicine selected: " + name + ", " + (mMedicine == null));
                hideKeyboard();
                selectPresentation(mMedicine != null ? mMedicine.presentation() : null);
            }
        });

        presentationScroll = (HorizontalScrollView) rootView.findViewById(R.id.med_presentation_scroll);
        mConfirmButton = (Button) rootView.findViewById(R.id.medicine_button_ok);
        if (showConfirmButton) {
            mConfirmButton.setText(getString(mMedicine == null ? R.string.create_medicine_button_text : R.string.edit_medicine_button_text));
            mConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEdit();
                }
            });
        } else {
            mConfirmButton.setVisibility(View.GONE);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey("medicine")) {
            mMedicine = Medicine.findById(savedInstanceState.getLong("medicine"));
            if (mMedicine != null)
                mConfirmButton.setText(getString(R.string.edit_routine_button_text));
        }

        setupMedPresentationChooser(rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ScheduleCreationHelper.instance().getSelectedMed() != null) {
            setMedicne(ScheduleCreationHelper.instance().getSelectedMed());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        showShowCase();
    }

    public boolean validate() {
        if (mNameTextView.getText() != null && mNameTextView.getText().length() > 0) {
            return true;
        } else {
            mNameTextView.setError("Please, type a name");
            mNameTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mNameTextView.setError(null);
                    mNameTextView.removeTextChangedListener(this);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            return false;
        }
    }


    void setupMedPresentationChooser(final View rootView) {

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickMedicine(view.getId(), rootView);
            }
        };

        for (View v : getViewsByTag((ViewGroup) rootView, "med_type")) {
            v.setOnClickListener(listener);
        }
    }


    void onClickMedicine(int viewId, View rootView) {

        for (View v : getViewsByTag((ViewGroup) rootView, "med_type")) {
            v.setBackgroundColor(getResources().getColor(R.color.white));
        }
        rootView.findViewById(viewId).setBackgroundResource(R.drawable.presentation_circle_background);

        switch (viewId) {
            case R.id.med_presentation_1:
                selectedPresentation = Presentation.INJECTIONS;
                Log.d(getTag(), "Injection");
                break;
            case R.id.med_presentation_2:
                selectedPresentation = Presentation.CAPSULES;
                Log.d(getTag(), "Capsule");
                break;
            case R.id.med_presentation_3:
                selectedPresentation = Presentation.EFFERVESCENT;
                Log.d(getTag(), "Effervescent");
                break;
            case R.id.med_presentation_4:
                selectedPresentation = Presentation.PILLS;
                Log.d(getTag(), "Pill");
                break;
            case R.id.med_presentation_5:
                selectedPresentation = Presentation.SYRUP;
                Log.d(getTag(), "Syrup");
                break;
            case R.id.med_presentation_6:
                selectedPresentation = Presentation.DROPS;
                Log.d(getTag(), "Drops");
                break;
        }

    }


    public void scrollToMedPresentation(View view) {
        Log.d(getTag(), "Scroll to: " + view.getLeft());

        int amount = view.getLeft();
        if (amount < (0.8 * presentationScroll.getWidth())) {
            amount -= 30;
        }
        presentationScroll.scrollTo(amount, 0);

    }


    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMedicine != null)
            outState.putLong("medicine", mMedicine.getId());
    }

    public void setMedicne(Medicine r) {
        Log.d(getTag(), "Medicine set: " + r.name());
        mMedicine = r;
        mNameTextView.setText(mMedicine.name());
        mConfirmButton.setText(getString(R.string.edit_medicine_button_text));
        selectPresentation(mMedicine.presentation());
    }

    private void selectPresentation(Presentation p) {
        for (View v : getViewsByTag((ViewGroup) getView(), "med_type")) {
            v.setBackgroundColor(getResources().getColor(R.color.white));
        }
        if (p != null) {
            int viewId = getPresentationViewId(p);
            View view = getView().findViewById(viewId);
            view.setBackgroundResource(R.drawable.presentation_circle_background);
            scrollToMedPresentation(view);
        }
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
            if (selectedPresentation != null) {
                mMedicine.setPresentation(selectedPresentation);
            }
            if (mMedicineEditCallback != null) {
                mMedicineEditCallback.onMedicineEdited(mMedicine);
            }
        }
        // if creating
        else {
            Medicine m = new Medicine(name);
            m.setPresentation(selectedPresentation != null ? selectedPresentation : Presentation.UNKNOWN);
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
        if (activity instanceof ScheduleCreationActivity) {
            this.showConfirmButton = false;
        }
    }


    public Medicine getMedicineFromView() {

        if (validate()) {
            String name = mNameTextView.getText().toString();
            // look for it in the med store
            if (mMedicine == null) {
                mMedicine = Medicine.findByName(name);
                Log.d(getTag(), "Looking for " + name + " in med store returned " + (mMedicine == null ? "null" : "a valid med"));
            }
            // if it wasn't on the store, create a new med
            if (mMedicine == null) {
                Log.d(getTag(), " Creating medicine " + name);
                mMedicine = new Medicine(name);
            }
            // in both cases, update the med presentation if any selected
            if (selectedPresentation != null) {
                mMedicine.setPresentation(selectedPresentation);
            } else if (mMedicine.presentation() == null) {
                mMedicine.setPresentation(Presentation.PILLS);// TODO change to unknown
            }
        }
        // TODO: Set other properties
        return mMedicine;
    }


    int getPresentationViewId(Presentation pres) {
        switch (pres) {
            case INJECTIONS:
                return R.id.med_presentation_1;
            case CAPSULES:
                return R.id.med_presentation_2;
            case EFFERVESCENT:
                return R.id.med_presentation_3;
            case PILLS:
                return R.id.med_presentation_4;
            case SYRUP:
                return R.id.med_presentation_5;
            case DROPS:
                return R.id.med_presentation_6;
            default:
                return -1;
        }
    }


    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameTextView.getWindowToken(), 0);
    }


    // Container Activity must implement this interface
    public interface OnMedicineEditListener {
        public void onMedicineEdited(Medicine r);

        public void onMedicineCreated(Medicine r);
    }

}