package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Map;

import es.usc.citius.servando.calendula.AlarmScheduler;
import es.usc.citius.servando.calendula.DailyDosageChecker;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Dose;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.util.ScheduleCreationHelper;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

public class ReminderActivity extends Activity {

    public static final String TAG = ReminderActivity.class.getName();

    LinearLayout list;
    //ArrayList<ScheduleReminder> reminders = new ArrayList<ScheduleReminder>();

    Button delayButton = null;
    Button doneButton = null;
    Map<Schedule,ScheduleItem> doses;
    Spinner delaySpinner;
    String routineId;
    Routine routine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_reminder);

        list = (LinearLayout) findViewById(R.id.reminder_list);
        delayButton = (Button) findViewById(R.id.reminder_delay_button);
        doneButton = (Button) findViewById(R.id.reminder_done_button);
        routineId = getIntent().getStringExtra("routine_id");
        routine = RoutineStore.instance().get(routineId);

        setupScheduleSpinner();

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel reminder notification
                ReminderNotification.cancel(getApplicationContext());
                finish();
            }
        });

        delayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delaySpinner.performClick();
                ReminderNotification.cancel(getApplicationContext());
                AlarmScheduler.instance().delayAlarm(routine,5*60*1000,getApplicationContext());
                Toast.makeText(getApplicationContext(),"Reminder delayed " + 5 + " mins",Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        doses = ScheduleUtils.getRoutineScheduleItems(routine,true);
        fillReminderList();
    }



    private void setupScheduleSpinner() {
//        delaySpinner = (Spinner) findViewById(R.id.delays_spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.delays_array, R.layout.spinner_text_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        delaySpinner.setAdapter(adapter);
//
//        delaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//
//        });
    }


    void fillReminderList(){

        LayoutInflater inflater = getLayoutInflater();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 15);

        for(Schedule s: doses.keySet()){

            //ScheduleReminder reminder = new ScheduleReminder(s);
            //reminders.add(reminder);

            ScheduleItem scheduleItem = doses.get(s);

            final View entry = inflater.inflate(R.layout.reminder_item, null);
            entry.setTag(scheduleItem);
            Medicine med = s.getMedicine();
            Dose dose = scheduleItem.dose();

            ((TextView)entry.findViewById(R.id.med_name)).setText(med.getName());
            ((TextView)entry.findViewById(R.id.med_dose)).setText(dose.ammount() + " " + med.getPresentation().getUnits(getResources()));

            ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);
            final View background = entry.findViewById(R.id.reminder_item_container);

            boolean taken = DailyDosageChecker.instance().doseTaken(scheduleItem);

            if(taken){
                checkButton.setChecked(taken);
                background.setSelected(taken);
            }

            Log.d(TAG,"Add view for dose " + med.getName() + ", taken: " + taken);

            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        background.setSelected(checked);
                        ScheduleItem dose = (ScheduleItem) (entry.getTag());
                        DailyDosageChecker.instance().setDoseTaken(dose, checked, getApplicationContext());
                        onReminderChecked();
                    }
                });

            list.addView(entry, params);
        }

    }

    private void onReminderChecked() {

        int total = doses.size();
        int checked=0;

        for(ScheduleItem d : doses.values()) {
            boolean taken = DailyDosageChecker.instance().doseTaken(d);
            Log.d("Dosage","Dose taken?" + d.id() + " taken: " + taken);
            if(taken)
                checked++;
        }

        if(checked == total){
            delayButton.setVisibility(View.INVISIBLE);
            doneButton.getBackground().setLevel(1);
        }else{
            delayButton.setVisibility(View.VISIBLE);
            doneButton.getBackground().setLevel(0);
        }

        Log.d(TAG, "Checked " + checked + " meds. Total:" + total);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




}
