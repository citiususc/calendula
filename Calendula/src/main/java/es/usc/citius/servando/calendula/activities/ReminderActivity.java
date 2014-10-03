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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Map;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Dose;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

public class ReminderActivity extends Activity {

    public static final String TAG = ReminderActivity.class.getName();

    LinearLayout list;
    ArrayList<ScheduleReminder> reminders = new ArrayList<ScheduleReminder>();

    Button delayButton = null;
    Button doneButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_reminder);

        list = (LinearLayout) findViewById(R.id.reminder_list);
        delayButton = (Button) findViewById(R.id.reminder_delay_button);
        doneButton = (Button) findViewById(R.id.reminder_done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        delayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fillReminderList();
    }

    void fillReminderList(){


        LayoutInflater inflater = getLayoutInflater();

        String routineId = getIntent().getStringExtra("routine_id");
        Routine routine = RoutineStore.instance().get(routineId);
        Map<Schedule,ScheduleItem> doses = ScheduleUtils.getRoutineScheduleItems(routine);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 15);

        for(Schedule s: doses.keySet()){

            ScheduleReminder reminder = new ScheduleReminder(s);
            reminders.add(reminder);

            final View entry = inflater.inflate(R.layout.reminder_item, null);
            entry.setTag(reminder);
            Medicine med = s.getMedicine();
            Dose dose = doses.get(s).dose();

            ((TextView)entry.findViewById(R.id.med_name)).setText(med.getName());
            ((TextView)entry.findViewById(R.id.med_dose)).setText(dose.ammount() + " " + med.getPresentation().getUnits(getResources()));

            ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);
            final View background = entry.findViewById(R.id.reminder_item_container);
            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    background.setSelected(checked);
                    ScheduleReminder r = (ScheduleReminder)(entry.getTag());
                    r.taken = checked;
                    onReminderChecked();
                }
            });

            list.addView(entry, params);
        }

    }

    private void onReminderChecked() {

        int total = reminders.size(), checked = 0;

        for(ScheduleReminder sr : reminders) {
            if (sr.taken) checked++;
        }

        if(checked == total){
            doneButton.getBackground().setLevel(1);
        }else{
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


    private class ScheduleReminder{
        Schedule schedule;
        boolean taken = false;

        ScheduleReminder(Schedule s){
            this.schedule = s;
        }
    }



}
