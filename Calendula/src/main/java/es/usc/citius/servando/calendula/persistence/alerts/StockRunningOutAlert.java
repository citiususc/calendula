/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
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

package es.usc.citius.servando.calendula.persistence.alerts;


import org.joda.time.LocalDate;

import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

/**
 * Represents an stock alert for an specific medicine
 */
public class StockRunningOutAlert extends PatientAlert<StockRunningOutAlert, StockRunningOutAlert.StockAlertInfo>{

    public StockRunningOutAlert(Medicine m){
        setPatient(m.patient());
        setMedicine(m);
        setType(StockRunningOutAlert.class.getCanonicalName());
        setLevel(Level.MEDIUM);
    }

    @Override
    public Class<?> getDetailsType() {
        return StockAlertInfo.class;
    }


    public static class StockAlertInfo{
        private LocalDate date;
        Double stock;

        public StockAlertInfo(){}

        public StockAlertInfo(LocalDate date, Double stock) {
            this.date = date;
            this.stock = stock;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

}
