package es.usc.citius.servando.calendula.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaItemStub {

    public static String[] medNames = {"Ibuprofeno", "Ramipril", "Atrovent","Digoxina"};

    public int hour;
    public boolean hasEvents;
    public List<String> meds;

    public int primaryColor = -1;
    public int secondaryColor = -1;
    public boolean hasColors = false;

    public DailyAgendaItemStub(int hour){
        this.hour = hour;
    }

    public static DailyAgendaItemStub random(int hour){

        DailyAgendaItemStub item = new DailyAgendaItemStub(hour);

        if(hour%4==0){
            item.hasEvents=true;
            int medCount = (int)Math.ceil(Math.random()*4);
                Random r = new Random();
                item.meds = new ArrayList<String>();
                for(int i = 0; i< medCount;i++){
                    int idx = r.nextInt(medNames.length);
                    String med = medNames[idx];
                    item.meds.add(med);
                }
        }

        return item;
    }




}
