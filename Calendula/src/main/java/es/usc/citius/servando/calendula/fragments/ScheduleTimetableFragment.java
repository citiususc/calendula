package es.usc.citius.servando.calendula.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;
import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrenceFormatter;
import com.doomonafireball.betterpickers.recurrencepicker.RecurrencePickerDialog;
import com.google.ical.values.Frequency;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.persistence.ScheduleItemComparator;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;


/**
 * Created by joseangel.pineiro on 12/11/13.
 */
public class ScheduleTimetableFragment extends Fragment implements NumberPickerDialogFragment.NumberPickerDialogHandler, RecurrencePickerDialog.OnRecurrenceSetListener {


    public static final String TAG = ScheduleTimetableFragment.class.getName();
    public static final int REPEAT_EVERYDAY = 0;
    public static final int REPEAT_SPECIFIC_DAYS = 1;
    public static final int REPEAT_INTERVAL = 2;
    public static final int REPEAT_CUSTOM = 3;


    final Frequency[] FREQ = new Frequency[]{Frequency.DAILY, Frequency.WEEKLY, Frequency.MONTHLY};

    LinearLayout timetableContainer;
    int timesPerDay = 1;

    Spinner scheduleSpinner;
    Spinner repeatTypeSpinner;
    Spinner freqSpinner;
    ScheduleItemComparator scheduleItemComparator = new ScheduleItemComparator();

    NumberPicker intervalPicker;
    NumberPicker intervalUnitsPicker;
    Button buttonScheduleStart;
    Button buttonScheduleEnd;
    Button intervalEditText;
    //RadioGroup radioGroup;

    View daySelectionBox;
    View customRepeatBox;
    TextView ruleText;


    int repeatType = 1;
    Frequency frequency = Frequency.DAILY;
    int interval = 2;
    boolean ignoreNextEvent = true;
    private String rule;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_timetable, container, false);
        timetableContainer = (LinearLayout) rootView.findViewById(R.id.schedule_timetable_container);
        setupScheduleSpinner(rootView);
        setupDaySelectionListeners(rootView);
        setupStartEndDatePickers(rootView);
        return rootView;
    }

    private void setupRepetitions(final View rooView) {
        repeatTypeSpinner = (Spinner) rooView.findViewById(R.id.repeat_type_spinner);
        freqSpinner = (Spinner) rooView.findViewById(R.id.freq_spinner);
        daySelectionBox = rooView.findViewById(R.id.day_selector_box);
        customRepeatBox = rooView.findViewById(R.id.custom_repeat_box);
        intervalEditText = (Button) rooView.findViewById(R.id.interval_edit_text);
        ruleText = (TextView) rooView.findViewById(R.id.rule_text);
        ruleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecurrencePickerDialog();
            }
        });


        repeatTypeSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.schedule_repeat_types)));
        repeatTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!ignoreNextEvent) {
                    setRepeatType(position, rooView, true);
                } else {
                    setRepeatType(position, rooView, false);
                    ignoreNextEvent = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        freqSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.schedule_repeat_frequency_units)));
        freqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setFrequency(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        intervalEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIntervalPickerDIalog();
            }
        });

        /*intervalEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int i = Integer.parseInt(intervalEditText.getText().toString());
                    ScheduleCreationHelper.instance().setIcalInterval(i);
                    Log.d(TAG, "Set interval: " + i);
                } catch (Exception e) {
                    ScheduleCreationHelper.instance().setIcalInterval(-1);
                }
            }
        });*/

        int _repeatType = ScheduleCreationHelper.instance().getRepeatType();
        Frequency _freq = ScheduleCreationHelper.instance().getFrequency();
        int _interval = ScheduleCreationHelper.instance().getIcalInterval();
        int freqIndex = _freq == Frequency.MONTHLY ? 2 : _freq == Frequency.WEEKLY ? 1 : 0; // DAILY by default
        setRepeatType(_repeatType, rooView, true);
        checkSelectedDays(rooView, ScheduleCreationHelper.instance().getSelectedDays());
        ignoreNextEvent = true;
        repeatTypeSpinner.setSelection(repeatType);
        freqSpinner.setSelection(freqIndex);
        intervalEditText.setText(String.valueOf(_interval > 0 ? _interval : 2));
    }

    void setupStartEndDatePickers(View rootView) {

        final DateTime today = DateTime.now();
        buttonScheduleStart = (Button) rootView.findViewById(R.id.button_set_start);
        buttonScheduleEnd = (Button) rootView.findViewById(R.id.button_set_end);
        buttonScheduleStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                LocalDate d = new LocalDate(year, monthOfYear, dayOfMonth);
                                buttonScheduleStart.setText(d.toString(getString(R.string.schedule_limits_date_format)));
                            }
                        }, today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
                dpd.show();
            }
        });

        buttonScheduleEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                LocalDate d = new LocalDate(year, monthOfYear, dayOfMonth);
                                buttonScheduleEnd.setText(d.toString(getString(R.string.schedule_limits_date_format)));
                            }
                        }, today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
                dpd.show();
            }
        });

        buttonScheduleEnd.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Do you want this schedule to continue indefinitely?")
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.dialog_yes_option), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                buttonScheduleEnd.setText(getString(R.string.never));
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_no_option), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });
    }

    private void setFrequency(int freq) {
        frequency = FREQ[freq];
        ScheduleCreationHelper.instance().setFrequency(frequency);
        Log.d(TAG, "Frquency set to " + frequency.name());
        if (frequency == Frequency.WEEKLY) {
            daySelectionBox.setVisibility(View.VISIBLE);
        } else {
            daySelectionBox.setVisibility(View.GONE);
        }
    }

    private void setRepeatType(int type, View v, boolean updateUi) {
        repeatType = type;
        Log.d(TAG, "Set repeat type: " + repeatType);
        ScheduleCreationHelper.instance().setRepeatType(type);
        if (updateUi) {
            if (repeatType == REPEAT_EVERYDAY) {
                checkAllDays(v);
                daySelectionBox.setVisibility(View.VISIBLE);
                customRepeatBox.setVisibility(View.GONE);
                ruleText.setVisibility(View.GONE);
            } else if (repeatType == REPEAT_SPECIFIC_DAYS) {
                if (ScheduleCreationHelper.instance().getDays(getActivity()).length == 7) {
                    checkToday(v);
                } else {
                    checkSelectedDays(v, ScheduleCreationHelper.instance().getSelectedDays());
                }
                daySelectionBox.setVisibility(View.VISIBLE);
                customRepeatBox.setVisibility(View.GONE);
                ruleText.setVisibility(View.GONE);
            } else if (repeatType == REPEAT_INTERVAL) {
                if (ScheduleCreationHelper.instance().getIcalInterval() <= 0) {
                    ScheduleCreationHelper.instance().setIcalInterval(2);
                }
                intervalEditText.setText(ScheduleCreationHelper.instance().getIcalInterval() + "");
                ScheduleCreationHelper.instance().setFrequency(frequency);
                if (frequency == Frequency.WEEKLY) {
                    daySelectionBox.setVisibility(View.VISIBLE);
                } else {
                    daySelectionBox.setVisibility(View.GONE);
                }
                ruleText.setVisibility(View.GONE);
                customRepeatBox.setVisibility(View.VISIBLE);
            } else {
                daySelectionBox.setVisibility(View.GONE);
                customRepeatBox.setVisibility(View.GONE);
                ruleText.setVisibility(View.VISIBLE);
                showRecurrencePickerDialog();
            }
        }
    }

    private void checkAllDays(View v) {
        checkSelectedDays(v, new boolean[]{true, true, true, true, true, true, true});
    }

    private void checkToday(View v) {
        boolean[] days = new boolean[]{false, false, false, false, false, false, false};
        days[LocalDate.now().getDayOfWeek() - 1] = true;
        checkSelectedDays(v, days);
    }

    private void setupRepetitionPickers(View rootView) {

        final String[] unit = getResources().getStringArray(R.array.schedule_repeat_units);

        intervalPicker = (NumberPicker) rootView.findViewById(R.id.interval_picker);
        intervalUnitsPicker = (NumberPicker) rootView.findViewById(R.id.interval_units_picker);


        intervalPicker.setMaxValue(31);
        intervalPicker.setMinValue(1);
        intervalPicker.setWrapSelectorWheel(false);
        intervalPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        intervalUnitsPicker.setMaxValue(unit.length - 1);
        intervalUnitsPicker.setMinValue(0);
        intervalUnitsPicker.setWrapSelectorWheel(false);
        intervalUnitsPicker.setDisplayedValues(unit);
        intervalUnitsPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        Frequency freq = ScheduleCreationHelper.instance().getFrequency();
        int interv = ScheduleCreationHelper.instance().getIcalInterval();
        frequency = freq != null ? freq : frequency;
        interval = interv >= 0 ? interv : 2;
        intervalPicker.setValue(interval);
        intervalUnitsPicker.setValue(frequency == Frequency.DAILY ? 0 : frequency == Frequency.WEEKLY ? 1 : 2);

        intervalPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                interval = newVal;
                ScheduleCreationHelper.instance().setIcalInterval(interval);
            }
        });

        intervalUnitsPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                frequency = FREQ[newVal];
                ScheduleCreationHelper.instance().setFrequency(frequency);
            }
        });


    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        timesPerDay = ScheduleCreationHelper.instance().getTimesPerDay();
        scheduleSpinner.setSelection(ScheduleCreationHelper.instance().getSelectedScheduleIdx());
        setupRepetitions(view);
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
                ScheduleCreationHelper.instance().setSelectedScheduleIdx(i);
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
                ScheduleCreationHelper.instance().setTimesPerDay(timesPerDay);
                break;
            }
        }

        List<Routine> routines = Routine.findAll();
        addTimetableEntries(timesPerDay, routines);
    }


    void setupDaySelectionListeners(final View rootView) {

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView text = ((TextView) view);
                int index;
                switch (text.getId()) {
                    case R.id.day_mo:
                        ScheduleCreationHelper.instance().toggleSelectedDay(0);
                        index = 0;
                        break;
                    case R.id.day_tu:
                        ScheduleCreationHelper.instance().toggleSelectedDay(1);
                        index = 1;
                        break;
                    case R.id.day_we:
                        ScheduleCreationHelper.instance().toggleSelectedDay(2);
                        index = 2;
                        break;
                    case R.id.day_th:
                        index = 3;
                        ScheduleCreationHelper.instance().toggleSelectedDay(3);
                        break;
                    case R.id.day_fr:
                        ScheduleCreationHelper.instance().toggleSelectedDay(4);
                        index = 4;
                        break;
                    case R.id.day_sa:
                        ScheduleCreationHelper.instance().toggleSelectedDay(5);
                        index = 5;
                        break;
                    case R.id.day_su:
                        ScheduleCreationHelper.instance().toggleSelectedDay(6);
                        index = 6;
                        break;
                    default:
                        return;
                }

                boolean daySelected = ScheduleCreationHelper.instance().getSelectedDays()[index];

                if (!daySelected) {
                    ((TextView) view).setTextAppearance(getActivity(), R.style.schedule_day_unselected);
                    view.setBackgroundResource(R.drawable.dayselector_circle_unselected);
                } else {
                    ((TextView) view).setTextAppearance(getActivity(), R.style.schedule_day_selected);
                    view.setBackgroundResource(R.drawable.dayselector_circle);
                }

                boolean allDaysSelected = ScheduleCreationHelper.instance().getDays(getActivity()).length == 7;

                if (repeatType == REPEAT_EVERYDAY && !allDaysSelected) {
                    setRepeatType(REPEAT_SPECIFIC_DAYS, rootView, false);
                    ignoreNextEvent = true;
                    repeatTypeSpinner.setSelection(1);
                } else if (repeatType == REPEAT_SPECIFIC_DAYS && allDaysSelected) {
                    repeatTypeSpinner.setSelection(0);
                    repeatType = REPEAT_EVERYDAY;
                }

                Log.d(TAG, "All days selected: " + allDaysSelected + ", repeatType: " + repeatType);
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


    void showIntervalPickerDIalog() {
        NumberPickerBuilder npb = new NumberPickerBuilder()
                .setDecimalVisibility(NumberPicker.INVISIBLE)
                .setMinNumber(1)
                .setMaxNumber(31)
                .setPlusMinusVisibility(NumberPicker.INVISIBLE)
                .setFragmentManager(getChildFragmentManager())
                .setTargetFragment(this)
                .setStyleResId(R.style.BetterPickersDialogFragment_Calendula);
        npb.show();
    }

    void showRecurrencePickerDialog() {
        RecurrencePickerDialog dialog = new RecurrencePickerDialog();
        Bundle b = new Bundle();
        dialog.setOnRecurrenceSetListener(this);
        dialog.show(getChildFragmentManager(), "REC");
    }


    void addTimetableEntries(int timesPerDay, List<Routine> routines) {

        Collections.sort(ScheduleCreationHelper.instance().getScheduleItems(), scheduleItemComparator);

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

            if (i < ScheduleCreationHelper.instance().getScheduleItems().size()) {
                ScheduleItem toCopy = ScheduleCreationHelper.instance().getScheduleItems().get(i);
                s = new ScheduleItem(null, toCopy.routine(), toCopy.dose());
            } else {
                s = new ScheduleItem(null, (i < routines.size()) ? routines.get(i) : null, 1);
            }

            if (s != null) {
                scheduleItems.add(s);
            }

            View view = buildTimetableEntry(s, routineNames, enableDelete);
            timetableContainer.addView(view, params);
        }

        ScheduleCreationHelper.instance().setScheduleItems(scheduleItems);

//        for (ScheduleItem i : ScheduleCreationHelper.instance().getScheduleItems())
//            Log.d(TAG, "addTimetableEntries (end): " + i.getId() + ", " + i.routine().name() + ", " + i.dose());

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
                    ScheduleCreationHelper.instance().getScheduleItems().remove(timetableContainer.indexOfChild(entry));
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
        for (ScheduleItem si : ScheduleCreationHelper.instance().getScheduleItems()) {
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

        Log.d(TAG, "Checking selected days");

        ScheduleCreationHelper.instance().setSelectedDays(days);
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

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {

        intervalEditText.setText("" + number);
        ScheduleCreationHelper.instance().setIcalInterval(number);
    }

    @Override
    public void onRecurrenceSet(String s) {

        EventRecurrence e = new EventRecurrence();
        e.parse(s);
        String repeatString = EventRecurrenceFormatter.getRepeatString(getActivity(), getResources(), e, true);
        ruleText.setText(repeatString);
        this.rule = s;
        ScheduleCreationHelper.instance().setRule(rule);
        Log.d(TAG, "Recurrence: " + repeatString);
    }
}