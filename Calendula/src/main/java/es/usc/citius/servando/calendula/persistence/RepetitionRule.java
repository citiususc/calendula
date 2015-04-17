package es.usc.citius.servando.calendula.persistence;

import android.util.Log;

import com.google.ical.compat.jodatime.LocalDateIterator;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.google.ical.compat.jodatime.LocalDateIteratorFactory.createLocalDateIterator;

/**
 * Created by joseangel.pineiro on 4/16/15.
 */
public class RepetitionRule {

    private static String[] STRING_DAYS = new String[]{
            "MO", "TU", "WE", "TH", "FR", "SA", "SU"
    };

    private static int[] JAVA_DAY_INDEXES = new int[]{
            // 1 : sun (6), 2 : mon (0), 3 : tue (2)...
            -1, 6, 0, 1, 2, 3, 4, 5
    };

    private static Weekday[] WEEK_DAYS = new Weekday[]{
            Weekday.MO, Weekday.TU, Weekday.WE, Weekday.TH,
            Weekday.FR, Weekday.SA, Weekday.SU
    };

    public static String DEFAULT_ICAL_VALUE = "RRULE:FREQ=DAILY;";

    private RRule rrule;

    // cached value for week days
    private boolean[] days;

    public RepetitionRule(String ical) {
        try {
            if (ical == null || ical.isEmpty()) {
                ical = DEFAULT_ICAL_VALUE;
            }
            rrule = new RRule(ical);
            Log.d("RRule", "Creating repetition rule" + rrule.toIcal());
        } catch (ParseException p) {
            throw new RuntimeException("Error parsing RRule", p);
        }
    }

    public void setDays(boolean[] days) {
        List<WeekdayNum> byDay = new ArrayList<>(7);
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                byDay.add(new WeekdayNum(0, WEEK_DAYS[i]));
            }
        }
        rrule.setByDay(byDay);
        Log.d("ICAL", rrule.toIcal());
        // invalidate cached value
        this.days = null;
    }

    public boolean[] days() {
        if (this.days == null) {
            this.days = new boolean[7];
            for (WeekdayNum w : rrule.getByDay()) {
                this.days[JAVA_DAY_INDEXES[w.wday.javaDayNum]] = true;
            }
        }
        return this.days;
    }

    public boolean hasOccurrencesToday() throws Exception {
        return hasOccurrencesAt(LocalDate.now());
    }

    public boolean hasOccurrencesAt(LocalDate date) {
        try {

            LocalDate start = date.minusDays(1);
            LocalDate end = date.plusDays(1);
            Log.d("Schedule", "------  Start: " + date.toString("dd/MM/YY - kk:mm"));
            Log.d("Schedule", "------  End: " + end.toString("dd/MM/YY - hh:mm"));

            LocalDateIterator it = createLocalDateIterator(rrule.toIcal(), start, true);
            it.advanceTo(date);
            while (it.hasNext()) {
                if (it.next().isBefore(end)) {
                    Log.d("Schedule", "------  There are ocurrences in range");
                    return true;
                } else {
                    break;
                }
            }
            Log.d("Schedule", "------  No dates in range");
            return false;
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing ical", e);
        }
    }

    public List<LocalDate> occurrencesBetween(LocalDate start, LocalDate to) {
        try {
            LocalDate from = start.minusDays(1);

            Log.d("Schedule", "------  Start: " + from.toString("dd/MM/YY - kk:mm"));
            Log.d("Schedule", "------  End: " + to.toString("dd/MM/YY - hh:mm"));

            List<LocalDate> occurrences = new ArrayList<>();
            LocalDateIterator it = createLocalDateIterator(rrule.toIcal(), from, DateTimeZone.UTC, true);
            it.advanceTo(start);
            while (it.hasNext()) {
                LocalDate n = it.next();
                if (n.isBefore(to)) {
                    occurrences.add(n);
                    Log.d("Schedule", "          New occurrence: " + n.toString("dd/MM/YY"));
                } else {
                    break;
                }
            }
            Log.d("Schedule", "------  No dates in range");
            return occurrences;
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing ical", e);
        }
    }

    public String toIcal() {
        return rrule.toIcal();
    }


}
