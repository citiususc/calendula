/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util;


import android.support.v4.util.Pair;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PickupInfo;

/**
 * Created by joseangel.pineiro on 4/07/16.
 */
public class PickupUtils {

    private static final String TAG = "PickupUtils";
    private final int MAX_DAYS = 10;

    List<PickupInfo> pickups;
    List<PickupInfo> urgentMeds;
    Map<LocalDate, List<PickupInfo>> pickupsMap = new HashMap<>();
    Pair<LocalDate, List<PickupInfo>> bestDay;
    HashMap<Long, Patient> colorCache = new HashMap<>();

    public PickupUtils(List<PickupInfo> pickups) {
        this.pickups = pickups;
        this.bestDay = null;
        Collections.sort(this.pickups, PickupInfo.PickupComparator.instance);
        pickupsMap.clear();
        for (PickupInfo pk : pickups) {
            if (!pickupsMap.containsKey(pk.getFrom())) {
                pickupsMap.put(pk.getFrom(), new ArrayList<PickupInfo>());
            }
            pickupsMap.get(pk.getFrom()).add(pk);
        }
    }

    /**
     * Get urgent meds, that are meds we can take within a margin of one or two days
     * before the next intake is delayed
     *
     * @return the meds
     */
    public List<PickupInfo> urgentMeds() {
        if (urgentMeds == null) {
            urgentMeds = new ArrayList<>();
            LocalDate now = LocalDate.now();
            for (PickupInfo p : pickups) {
                if (!p.isTaken() && p.getTo().isAfter(now) && p.getFrom().plusDays(MAX_DAYS - 3).isBefore(now)) {
                    urgentMeds.add(p);
                }
            }
        }
        return urgentMeds;
    }

    public Pair<LocalDate, List<PickupInfo>> getBestDay() {

        if (this.bestDay != null) {
            return this.bestDay;
        }

        HashMap<LocalDate, List<PickupInfo>> bestDays = new HashMap<>();
        if (pickups.size() > 0) {
            LocalDate today = LocalDate.now();
            LocalDate first = LocalDate.now();
            LocalDate now = LocalDate.now().minusDays(MAX_DAYS);

            if (now.getDayOfWeek() == DateTimeConstants.SUNDAY) {
                now = now.plusDays(1);
            }

            // get the date of the first med we can take from 10 days ago
            for (PickupInfo p : pickups) {
                if (p.getFrom().isAfter(now) && !p.isTaken()) {
                    first = p.getFrom();
                    break;
                }
            }

            for (int i = 0; i < 10; i++) {
                LocalDate d = first.plusDays(i);
                if (!d.isAfter(today) && d.getDayOfWeek() != DateTimeConstants.SUNDAY) {
                    // only take care of days after today that are not sundays
                    continue;
                }

                // compute the number of meds we cant take for each day
                for (PickupInfo p : pickups) {
                    // get the pickup take secure interval
                    DateTime iStart = p.getFrom().toDateTimeAtStartOfDay();
                    DateTime iEnd = p.getFrom().plusDays(MAX_DAYS - 1).toDateTimeAtStartOfDay();
                    Interval interval = new Interval(iStart, iEnd);
                    // add the pickup to the daily list if we can take it
                    if (!p.isTaken() && interval.contains(d.toDateTimeAtStartOfDay())) {
                        if (!bestDays.containsKey(d)) {
                            bestDays.put(d, new ArrayList<PickupInfo>());
                        }
                        bestDays.get(d).add(p);
                    }
                }
            }

            // select the day with the highest number of meds
            int bestDayCount = 0;
            LocalDate bestOption = null;
            Set<LocalDate> localDates = bestDays.keySet();
            ArrayList<LocalDate> sorted = new ArrayList<>(localDates);
            Collections.sort(sorted);
            for (LocalDate day : sorted) {
                List<PickupInfo> pks = bestDays.get(day);
                LogUtil.d(TAG, day.toString("dd/MM/YYYY") + ": " + pks.size());
                if (pks.size() >= bestDayCount) {
                    bestDayCount = pks.size();
                    bestOption = day;
                    if (bestOption.getDayOfWeek() == DateTimeConstants.SUNDAY) {
                        bestOption = bestOption.minusDays(1);
                    }
                }
            }
            if (bestOption != null) {
                this.bestDay = new Pair<>(bestOption, bestDays.get(bestOption));
                return this.bestDay;
            }
        }

        return null;
    }

    public Patient getPatient(PickupInfo p) {
        Long id = p.getMedicine().getId();
        if (!colorCache.containsKey(id)) {
            Medicine m = DB.medicines().findById(p.getMedicine().getId());
            Patient patient = DB.patients().findById(m.getPatient().getId());
            colorCache.put(id, patient);
        }
        return colorCache.get(id);
    }

//    private List<PickupInfo> canTakeAt(LocalDate d) {
//        List<PickupInfo> all = pickups;
//        List<PickupInfo> canTake = new ArrayList<>();
//        List<Long> medIds = new ArrayList<>();
//        for (PickupInfo p : all) {
//            DateTime iStart = p.from().toDateTimeAtStartOfDay();
//            DateTime iEnd = p.from().plusDays(MAX_DAYS-1).toDateTimeAtStartOfDay();
//            Interval interval = new Interval(iStart, iEnd);
//            if (!p.taken() && interval.contains(d.toDateTimeAtStartOfDay())) {
//                if (!medIds.contains(p.medicine().getId())) {
//                    canTake.add(p);
//                    medIds.add(p.medicine().getId());
//                }
//            }
//        }
//        return canTake;
//    }


    public List<PickupInfo> pickups() {
        return pickups;
    }

    public Map<LocalDate, List<PickupInfo>> pickupsMap() {
        return pickupsMap;
    }
}
