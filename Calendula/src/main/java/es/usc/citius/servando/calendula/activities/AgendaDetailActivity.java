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

import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.ScheduleUtils;

public class AgendaDetailActivity extends Activity {

    public static final String TAG = AgendaDetailActivity.class.getName();

    int hour = 0;
    LinearLayout list;
    Button doneButton = null;
    List<ScheduleItem> doses;
    boolean totalChecked;

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
                if (totalChecked) {
                    ReminderNotification.cancel(getApplicationContext());
                }
                finish();
            }
        });
        doses = ScheduleUtils.getHourScheduleItems(hour, true);
        Log.d(TAG, "Hour: " + hour + ", doses: " + doses.size());
        fillReminderList();
    }

    public String getDisplayableDose(int dose, Medicine m, Routine r) {
        return dose
                + " "
                + m.presentation().units(getResources())
                + " - "
                + r.time().toString("kk:mm")
                + "h";
    }


    void fillReminderList() {

        LayoutInflater inflater = getLayoutInflater();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 15);

        for (ScheduleItem scheduleItem : doses) {
            final Routine r = scheduleItem.routine();
            final Medicine med = scheduleItem.schedule().medicine();
            final DailyScheduleItem dsi = DailyScheduleItem.findByScheduleItem(scheduleItem);
            final View entry = inflater.inflate(R.layout.reminder_item, null);
            final View background = entry.findViewById(R.id.reminder_item_container);
            final ToggleButton checkButton = (ToggleButton) entry.findViewById(R.id.check_button);

            ((TextView) entry.findViewById(R.id.med_name)).setText(med.name());
            ((TextView) entry.findViewById(R.id.med_dose)).setText(getDisplayableDose((int) scheduleItem.dose(), med, r));

            entry.setTag(dsi);

            checkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    background.setSelected(checked);
                    DailyScheduleItem dailyScheduleItem = (DailyScheduleItem) entry.getTag();
                    dailyScheduleItem.setTakenToday(true);
                    dailyScheduleItem.save();
                }
            });

            if (dsi.takenToday()) {
                checkButton.setChecked(true);
                background.setSelected(true);
            }

            list.addView(entry, params);
        }

    }

    private void onReminderChecked() {
        int total = doses.size();
        int checked = 0;

        for (ScheduleItem s : doses) {
            boolean taken = DailyScheduleItem.findByScheduleItem(s).takenToday();
            if (taken)
                checked++;
        }
        if (checked == total) {
            doneButton.getBackground().setLevel(1);
            totalChecked = true;
        } else {
            doneButton.getBackground().setLevel(0);
            totalChecked = false;
        }
        Log.d(TAG, "Checked " + checked + " meds. Total:" + total);
    }


    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
