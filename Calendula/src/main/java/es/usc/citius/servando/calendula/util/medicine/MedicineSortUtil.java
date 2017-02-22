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

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;

/**
 * Created by alvaro.brey.vilas on 21/02/17.
 */
public class MedicineSortUtil {

    public enum MedSortType {
        NAME(R.string.sort_by_name, new MedicineNameComparator()),
        mPRESENTATION(R.string.sort_by_presentation, new MedicinePresentationComparator());

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
            return o1.name().compareTo(o2.name());
        }
    }

    private static class MedicinePresentationComparator implements Comparator<Medicine> {

        @Override
        public int compare(Medicine o1, Medicine o2) {
            final Presentation p1 = o1.presentation();
            final Presentation p2 = o2.presentation();
            return Integer.valueOf(p1.ordinal()).compareTo(p2.ordinal());
        }
    }

}
