package es.usc.citius.servando.calendula.adherence;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;

/**
 *
 */
public class AdherenceSummary {

    public List<AdherenceSummaryItem> items;

    public AdherenceSummary(){
        items = new ArrayList<>();
    }

    public AdherenceSummary with(List<DailyScheduleItem> ditems){
        for (DailyScheduleItem i : ditems) {
            items.add(new AdherenceSummaryItem(i));
        }
        return this;
    }

}
