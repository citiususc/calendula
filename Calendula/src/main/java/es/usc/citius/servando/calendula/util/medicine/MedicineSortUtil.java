/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2017 CITIUS - USC
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.util.medicine;

import android.support.annotation.StringRes;

import java.util.Comparator;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.Presentation;

/**
 * Created by alvaro.brey.vilas on 21/02/17.
 */
public class MedicineSortUtil {

    public enum MedSortType {
        NAME(R.string.sort_by_name, new MedicineNameComparator()),
        PRESENTATION(R.string.sort_by_presentation, new MedicinePresentationComparator()),
        ALERTS(R.string.sort_by_alerts, new MedicineAlertComparator());

        private int displayName;
        private Comparator<Medicine> comparator;

        MedSortType(@StringRes int displayName, Comparator<Medicine> comparator) {
            this.displayName = displayName;
            this.comparator = comparator;
        }

        @Override
        public String toString() {
            return CalendulaApp.getContext().getString(displayName);
        }

        public Comparator<Medicine> comparator() {
            return comparator;
        }
    }

    private static class MedicineNameComparator implements Comparator<Medicine> {
        @Override
        public int compare(Medicine o1, Medicine o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private static class MedicinePresentationComparator implements Comparator<Medicine> {

        @Override
        public int compare(Medicine o1, Medicine o2) {
            final Presentation p1 = o1.getPresentation();
            final Presentation p2 = o2.getPresentation();
            return Integer.valueOf(p1.ordinal()).compareTo(p2.ordinal());
        }
    }

    private static class MedicineAlertComparator implements Comparator<Medicine> {

        @Override
        public int compare(Medicine o1, Medicine o2) {
            final List<PatientAlert> a1 = DB.alerts().findByMedicineSortByLevel(o1);
            final List<PatientAlert> a2 = DB.alerts().findByMedicineSortByLevel(o2);
            if (a1.size() == 0 || a2.size() == 0) {
                return Integer.valueOf(a2.size()).compareTo(a1.size());
            }
            return Integer.valueOf(a2.get(0).getLevel()).compareTo(a1.get(0).getLevel());
        }
    }

}
