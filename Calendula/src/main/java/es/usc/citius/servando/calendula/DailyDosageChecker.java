package es.usc.citius.servando.calendula;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.model.DailyScheduleItem;
import es.usc.citius.servando.calendula.model.Routine;
import es.usc.citius.servando.calendula.model.Schedule;
import es.usc.citius.servando.calendula.model.ScheduleItem;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.util.GsonUtil;
import es.usc.citius.servando.calendula.util.ScheduleUtils;

/**
 * Created by castrelo on 4/10/14.
 */
public class DailyDosageChecker {

    private static final String DAILY_DOSAGE_FILE_NAME = "dailyDosage.json";
    private static DailyDosageChecker instance = new DailyDosageChecker();

    DateTime date = null;

    List<DailyScheduleItem> dailySchedule;


    public DailyDosageChecker(){}


    public static DailyDosageChecker instance(){
        return instance;
    }

    public void updateDailySchedule(Context ctx){

        if(date==null) {
            try {
                // try to load from file
                load(ctx);
            } catch (Exception e) {
                // cant load, so create new for today
                createDailySchedule();
            }
        }


        DateTime now = DateTime.now();
        // check if the saved dosage is for today
        if(date==null || date.getDayOfYear()!=now.getDayOfYear() || dailySchedule==null || dailySchedule.isEmpty()){
            createDailySchedule();
        }else{
            updateDailySchedule();
        }

        Log.d("Dosage", "Daily schedule updated: " + new Gson().toJson(dailySchedule));
    }

    private void createDailySchedule(){
        Log.d("Dosage", "Creating daily schedule");
        date=DateTime.now();
        dailySchedule=new ArrayList<DailyScheduleItem>();
        // create a list with all day doses
        //TODO: what to do with doses in past?
        for(Routine r : RoutineStore.instance().asList()){
            Map<Schedule, ScheduleItem> routineDoses = ScheduleUtils.getRoutineScheduleItems(r,true);
            for(ScheduleItem s : routineDoses.values()){
                dailySchedule.add(new DailyScheduleItem(s,false));
            }
        }
    }


    private void updateDailySchedule(){
        // create a list with all day doses
        //TODO: what to do with doses in past?
        for(Routine r : RoutineStore.instance().asList()){
            Map<Schedule, ScheduleItem> routineDoses = ScheduleUtils.getRoutineScheduleItems(r,true);
            for(ScheduleItem s : routineDoses.values()){
                if(!scheduleExists(s))
                    dailySchedule.add(new DailyScheduleItem(s,false));
            }
        }
    }

    private boolean scheduleExists(ScheduleItem i){
        for(DailyScheduleItem d: dailySchedule) {
            if (i.id().equals(d.getScheduleItem().id()))
                return true;
        }
        return false;
    }

    public boolean doseTaken(ScheduleItem dose){

        for (DailyScheduleItem i : dailySchedule){
            if(i.getScheduleItem().id().equals(dose.id()))
                return i.takenToday();
        }
        return false;
    }

    public void setDoseTaken(ScheduleItem dose, boolean taken, Context ctx) {
        Log.d("Dosage","Set dose taken: ");
        for (DailyScheduleItem i : dailySchedule) {
            Log.d("Dosage", i.getScheduleItem().id() + "  vs  " + dose.id());
            if (i.getScheduleItem().id().equals(dose.id())) {
                i.setTakenToday(taken);
                Log.d("Dosage","Set dose taken " + dose.id() + ", taken: " + taken);
                break;

            }
        }

        save(ctx);
    }


    public void save(Context context){
        // open session file where user data is stored
        FileOutputStream out = null;
        try {
            out = context.openFileOutput(DAILY_DOSAGE_FILE_NAME, Context.MODE_PRIVATE);
            String json = GsonUtil.get().toJson(this);
            out.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(out != null) out.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }

    public void load(Context context) throws IOException{
        // open session file where user data is stored
        FileInputStream is = null;
        try {
            is = context.openFileInput(DAILY_DOSAGE_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            DailyDosageChecker checker = GsonUtil.get().fromJson(reader, DailyDosageChecker.class);
            if(checker!=null) {
                this.dailySchedule = checker.dailySchedule;
                this.date = checker.date;
            }
            Log.d(DailyDosageChecker.class.getName(), "Daily dosage loaded");
        }finally {

            try {
                is.close();
            }catch (Exception unhandled){
                //do nothing
            }
        }

    }

}
