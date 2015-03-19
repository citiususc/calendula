package es.usc.citius.servando.calendula.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.persistence.ScheduleItemComparator;


/**
 * Created by joseangel.pineiro on 12/11/13.
 */
public class ScheduleConfirmationFragment extends Fragment {

    public static final String TAG = ScheduleConfirmationFragment.class.getName();
    public static final String ARG_PRESCRIPTION = "ARG_PRESCRIPTION";

    LinearLayout timetableContainer;
    int timesPerDay = 1;
    ConfirmSchedulesActivity.PrescriptionWrapper prescriptionWrapper;
    Spinner scheduleSpinner;
    float doses[] = null;
    public int selectedScheduleIdx;
    List<ScheduleItem> sItems = new ArrayList<ScheduleItem>();

    private boolean[] selectedDays = new boolean[]{true, true, true, true, true, true, true}; // 7 days

    ScheduleItemComparator scheduleItemComparator = new ScheduleItemComparator();

    public static ScheduleConfirmationFragment newInstance(ConfirmSchedulesActivity.PrescriptionWrapper pw) {
        ScheduleConfirmationFragment fragment = new ScheduleConfirmationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRESCRIPTION, pw);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_confirmation, container, false);

        prescriptionWrapper = (ConfirmSchedulesActivity.PrescriptionWrapper)getArguments().getSerializable(ARG_PRESCRIPTION);
        if(prescriptionWrapper!=null && prescriptionWrapper.sched!= null){
            Log.d(TAG, "Wrapper: " + prescriptionWrapper.cn + "," + (prescriptionWrapper.sched.dose));
            setDefaultValues();
        }
        timetableContainer = (LinearLayout) rootView.findViewById(R.id.schedule_timetable_container);
        setupScheduleSpinner(rootView);
        setupDaySelectionListeners(rootView);
    
        
        return rootView;
    }

    private void setDefaultValues() {

        ConfirmSchedulesActivity.ScheduleWrapper sw = prescriptionWrapper.sched;
        
        if(sw.period != -1){
            int times = 24 / sw.period;
            float dose = sw.dose; //   dose / times ?
            timesPerDay = times;
            doses = new float[timesPerDay];
            for (int i = 0; i < doses.length; i++) {
                doses[i] = dose;
            }
            Log.d(TAG," Values: " + timesPerDay + ", " + dose);
        }else{
            Log.d(TAG,"Period not avilable");
            
        }

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scheduleSpinner.setSelection(timesPerDay>0?timesPerDay-1:0);
        checkSelectedDays(view, selectedDays);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setupScheduleSpinner(View rootView) {
        scheduleSpinner = (Spinner) rootView.findViewById(R.id.schedules_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.schedules_array, R.layout.spinner_text_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the spinner
        scheduleSpinner.setAdapter(adapter);

        scheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {            

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = (String) adapterView.getItemAtPosition(i);
                selectedScheduleIdx = i;
                Log.d(getTag(), "Selected: " + selected);
                onScheduleSelected(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });
    }

    void onScheduleSelected(String selection) {

        String schedules[] = getResources().getStringArray(R.array.schedules_array);

        // obtain times per day from selected schedule
        for (int i = 0; i < schedules.length; i++) {
            if (schedules[i].equalsIgnoreCase(selection)) {
                timesPerDay = i + 1;
                break;
            }
        }

        List<Routine> routines = Routine.findAll();
        addTimetableEntries(timesPerDay, routines);
    }


    void setupDaySelectionListeners(View rootView) {

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView text = ((TextView) view);
                int index;
                switch (text.getId()) {
                    case R.id.day_mo:
                        toggleSelectedDay(0);
                        index = 0;
                        break;
                    case R.id.day_tu:
                        toggleSelectedDay(1);
                        index = 1;
                        break;
                    case R.id.day_we:
                        toggleSelectedDay(2);
                        index = 2;
                        break;
                    case R.id.day_th:
                        index = 3;
                        toggleSelectedDay(3);
                        break;
                    case R.id.day_fr:
                        toggleSelectedDay(4);
                        index = 4;
                        break;
                    case R.id.day_sa:
                        toggleSelectedDay(5);
                        index = 5;
                        break;
                    case R.id.day_su:
                        toggleSelectedDay(6);
                        index = 6;
                        break;
                    default:
                        return;
                }

                boolean daySelected = selectedDays[index];

                if (!daySelected) {
                    ((TextView) view).setTextAppearance(getActivity(), R.style.schedule_day_unselected);
                    view.setBackgroundResource(R.drawable.dayselector_circle_unselected);
                } else {
                    ((TextView) view).setTextAppearance(getActivity(), R.style.schedule_day_selected);
                    view.setBackgroundResource(R.drawable.dayselector_circle);
                }


            }
        };

        rootView.findViewById(R.id.day_mo).setOnClickListener(listener);
        rootView.findViewById(R.id.day_tu).setOnClickListener(listener);
        rootView.findViewById(R.id.day_we).setOnClickListener(listener);
        rootView.findViewById(R.id.day_th).setOnClickListener(listener);
        rootView.findViewById(R.id.day_fr).setOnClickListener(listener);
        rootView.findViewById(R.id.day_sa).setOnClickListener(listener);
        rootView.findViewById(R.id.day_su).setOnClickListener(listener);


    }


    void addTimetableEntries(int timesPerDay, List<Routine> routines) {

        Collections.sort(sItems, scheduleItemComparator);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        String[] routineNames = getUpdatedRoutineNames();
        timetableContainer.removeAllViews();


        List<ScheduleItem> scheduleItems = new ArrayList<ScheduleItem>();

        boolean enableDelete = timesPerDay > 1;

        for (int i = 0; i < timesPerDay; i++) {
            // try to get previous routine from state holder
            ScheduleItem s;

            if (i < sItems.size()) {
                ScheduleItem toCopy = sItems.get(i);
                s = new ScheduleItem(null, toCopy.routine(), toCopy.dose());
            } else {
                float dose = (doses != null && doses.length > i) ? doses[i] : 1;
                s = new ScheduleItem(null, (i < routines.size()) ? routines.get(i) : null, dose);
            }

            if (s != null) {
                scheduleItems.add(s);
            }

            View view = buildTimetableEntry(s, routineNames, enableDelete);
            timetableContainer.addView(view, params);
        }

        sItems = scheduleItems;
    }


    String[] getUpdatedRoutineNames() {

        List<Routine> routines = Routine.findAll();

        int j = 0;
        String[] routineNames = new String[routines.size() + 1];
        for (Routine r : routines) {
            routineNames[j++] = r.name();
        }

        routineNames[routineNames.length - 1] = getString(R.string.create_new_routine);

        return routineNames;
    }


    View buildTimetableEntry(ScheduleItem r, String[] routineNames, boolean enableDelete) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View entry = inflater.inflate(R.layout.schedule_timetable_entry, null);
        updateEntryTime(r.routine(), entry);
        setupScheduleEntrySpinners(entry, r, routineNames);

        if (enableDelete) {
            entry.findViewById(R.id.entry_remove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sItems.remove(timetableContainer.indexOfChild(entry));
                    scheduleSpinner.setSelection(timesPerDay - 2);
                }
            });
        } else {
            entry.findViewById(R.id.entry_remove).setVisibility(View.INVISIBLE);
        }
        return entry;
    }

    void updateRoutineSelectionAdapter(final View entryView, Spinner routineSpinner, String[] routineNames) {
        ArrayAdapter<String> routineAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, routineNames);
        routineAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        routineSpinner.setAdapter(routineAdapter);
    }


    private void setupScheduleEntrySpinners(final View entryView, ScheduleItem scheduleItem, String[] routineNames) {

        final Spinner routineSpinner = (Spinner) entryView.findViewById(R.id.entry_routine_spinner);
        final TextView doseTv = (TextView) entryView.findViewById(R.id.entry_dose_textview);
//        final Spinner doseSpinner = (Spinner) entryView.findViewById(R.id.entry_dose_spinner);

        doseTv.setTag(scheduleItem);
        routineSpinner.setTag(scheduleItem);

        // set up the routine selection adapter
        updateRoutineSelectionAdapter(entryView, routineSpinner, routineNames);

        if (scheduleItem != null && scheduleItem.routine() != null) {
            String routineName = scheduleItem.routine().name();
            int index = Arrays.asList(routineNames).indexOf(routineName);
            routineSpinner.setSelection(index);
        } else {
            routineSpinner.setSelection(routineNames.length - 1);
        }


        doseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDosePickerDialog((ScheduleItem) v.getTag(), (TextView) v);
            }
        });

        // set up the dose selection adapter
//        ArrayAdapter<String> doseAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, doses);
//        doseAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        doseSpinner.setAdapter(doseAdapter);
        // select 1 pill by default
//        doseSpinner.setSelection((int) scheduleItem.dose() - 1); // dose "1" is located at the index "0", and so on

        Log.d(TAG, "Dose: " + scheduleItem.dose() + ", " + scheduleItem.displayDose());
            
        doseTv.setText(scheduleItem.displayDose());

        // setup listeners
        routineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = (String) adapterView.getItemAtPosition(i);
                Routine r = Routine.findByName(selected);
                ScheduleItem item = ((ScheduleItem) routineSpinner.getTag());

                if (r != null) {
                    updateEntryTime(r, entryView);

                } else {
                    updateEntryTime(null, entryView);
                    showAddNewRoutineDialog(entryView);
                }
                Log.d(TAG, "Updated routine to " + (r != null ? r.name() : "NULL") + " on item " + item.getId());
                item.setRoutine(r);


                logScheduleItems();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        routineSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    if (((String) routineSpinner.getSelectedItem()).equalsIgnoreCase(getString(R.string.create_new_routine))) {
                        showAddNewRoutineDialog(entryView);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    private void logScheduleItems() {
        for (ScheduleItem si : sItems) {
            Log.d("TAG", (si.routine() != null ? si.routine().name() : "NONE") + ", " + si.dose() + " ****************************");
        }
    }


    void updateEntryTime(Routine r, View entry) {
        String hourText;
        String minuteText;
        if (r != null) {
            hourText = (r.time().getHourOfDay() < 10 ? "0" + r.time().getHourOfDay() : r.time().getHourOfDay()) + ":";
            minuteText = (r.time().getMinuteOfHour() < 10 ? "0" + r.time().getMinuteOfHour() : r.time().getMinuteOfHour()) + "";
        } else {
            hourText = "--:";
            minuteText = "--";
        }

        ((TextView) entry.findViewById(R.id.hour_text)).setText(hourText);
        ((TextView) entry.findViewById(R.id.minute_text)).setText(minuteText);
    }


    void showAddNewRoutineDialog(final View entryView) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        final RoutineCreateOrEditFragment addRoutineFragment = new RoutineCreateOrEditFragment();
        addRoutineFragment.setOnRoutineEditListener(new RoutineCreateOrEditFragment.OnRoutineEditListener() {
            @Override
            public void onRoutineEdited(Routine r) {
                // do nothing
            }

            @Override
            public void onRoutineDeleted(Routine r) {
                // do nothing
            }


            @Override
            public void onRoutineCreated(final Routine r) {
                Spinner rSpinner = (Spinner) entryView.findViewById(R.id.entry_routine_spinner);
                String names[] = getUpdatedRoutineNames();
                updateRoutineSelectionAdapter(entryView, rSpinner, names);

                Log.d(TAG, "Routine name: " + r.name());
                Log.d(TAG, "Routine time: " + r.time().toString("hh:mm"));
                Log.d(TAG, "Names: " + Arrays.toString(names));

                int selection = Arrays.asList(names).indexOf(r.name());
                rSpinner.setSelection(selection);

                updateEntryTime(r, entryView);
                addRoutineFragment.dismiss();
            }
        });


        addRoutineFragment.show(fm, "fragment_edit_name");
    }

    public boolean validate(){
        
        // check dose as in prescriptions
        return true;
    }
    
    public Schedule getSchedule(){        
        Schedule s = new Schedule();
        s.setDays(selectedDays);        
        return s;
    }

    public List<ScheduleItem> getScheduleItems(){
        return sItems;
    }

    void showDosePickerDialog(final ScheduleItem item, final TextView tv) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        final DosePickerFragment dosePickerFragment = new DosePickerFragment();
        Bundle arguments = new Bundle();
        arguments.putDouble("dose", item.dose());
        dosePickerFragment.setArguments(arguments);

        dosePickerFragment.setOnDoseSelectedListener(new DosePickerFragment.OnDoseSelectedListener() {
            @Override
            public void onDoseSelected(double dose) {
                Log.d(TAG, "Set dose " + dose + " to item " + item.routine().name() + ", " + item.getId());
                item.setDose((float) dose);
                tv.setText(item.displayDose());

                logScheduleItems();

            }
        });

        dosePickerFragment.show(fm, "fragment_select_dose");
    }

    void checkSelectedDays(View rootView, boolean[] days) {

        ((TextView) rootView.findViewById(R.id.day_mo)).setTextAppearance(getActivity(), days[0] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        rootView.findViewById(R.id.day_mo).setBackgroundResource(days[0] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

        ((TextView) rootView.findViewById(R.id.day_tu)).setTextAppearance(getActivity(),
                days[1] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        (rootView.findViewById(R.id.day_tu)).setBackgroundResource(days[1] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

        ((TextView) rootView.findViewById(R.id.day_we)).setTextAppearance(getActivity(),
                days[2] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        (rootView.findViewById(R.id.day_we)).setBackgroundResource(days[2] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

        ((TextView) rootView.findViewById(R.id.day_th)).setTextAppearance(getActivity(),
                days[3] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        (rootView.findViewById(R.id.day_th)).setBackgroundResource(days[3] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

        ((TextView) rootView.findViewById(R.id.day_fr)).setTextAppearance(getActivity(),
                days[4] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        (rootView.findViewById(R.id.day_fr)).setBackgroundResource(days[4] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

        ((TextView) rootView.findViewById(R.id.day_sa)).setTextAppearance(getActivity(),
                days[5] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        (rootView.findViewById(R.id.day_sa)).setBackgroundResource(days[5] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

        ((TextView) rootView.findViewById(R.id.day_su)).setTextAppearance(getActivity(),
                days[6] ? R.style.schedule_day_selected : R.style.schedule_day_unselected);
        (rootView.findViewById(R.id.day_su)).setBackgroundResource(days[6] ? R.drawable.dayselector_circle : R.drawable.dayselector_circle_unselected);

    }

    public void toggleSelectedDay(int i) {
        selectedDays[i] = !selectedDays[i];
    }


}