package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Map;

import es.usc.citius.servando.calendula.DailyDosageChecker;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.model.Dose;
import es.usc.citius.servando.calendula.model.Medicine;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

public class AgendaDetailActivity extends Activity {

    public static final String TAG = AgendaDetailActivity.class.getName();

    int hour = 0;
    LinearLayout list;
    Button doneButton = null;
    Map<Schedule, ScheduleItem> doses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getActionBar().hide();
        setContentView(R.layout.activity_agenda_detail);

        list = (LinearLayout) findViewById(R.id.reminder_list);
        doneButton = (Button) findViewById(R.id.reminder_done_button);
        hour = getIntent().getIntExtra("hour", 0);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel reminder notification
                finish();
            }
        });

        doses = ScheduleUtils.getHourScheduleItems(hour, true);

        Log.d(TAG, "Hour: " + hour + ", doses: " + doses.size());
        fillReminderList();
    }


    void fillReminderList() {

        LayoutInflater inflater = getLayoutInflater();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 15);

        for (Schedule s : doses.keySet()) {


            ScheduleItem scheduleItem = doses.get(s);
            Routine r = RoutineStore.instance().get(scheduleItem.routineId());

            final View entry = inflater.inflate(R.layout.reminder_item, null);
            entry.setTag(scheduleItem);
            Medicine med = s.getMedicine();
            Dose dose = scheduleItem.dose();

            ((TextView) entry.findViewById(R.id.med_name)).setText(med.getName());
            ((TextView) entry.findViewById(R.id.med_dose)).setText(dose.ammount()
                    + " "
                    + med.getPresentation().getUnits(getResources())
                    + " - "
                    + r.getTime().toString("kk:mm")
                    + "h");

            ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);
            final View background = entry.findViewById(R.id.reminder_item_container);

            boolean taken = DailyDosageChecker.instance().doseTaken(scheduleItem);

            if (taken) {
                checkButton.setChecked(taken);
                background.setSelected(taken);
            }

            Log.d(TAG, "Add view for dose " + med.getName() + ", taken: " + taken);

            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    background.setSelected(checked);
                    ScheduleItem dose = (ScheduleItem) (entry.getTag());
                    DailyDosageChecker.instance().setDoseTaken(dose, checked, getApplicationContext());
                }
            });

            list.addView(entry, params);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
