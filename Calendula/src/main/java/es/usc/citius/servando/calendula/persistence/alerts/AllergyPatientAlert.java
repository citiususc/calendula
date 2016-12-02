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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.persistence.alerts;

import java.util.List;

import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

public class AllergyPatientAlert extends PatientAlert<AllergyPatientAlert, AllergyPatientAlert.AllergyAlertInfo> {


    public AllergyPatientAlert() {
        setType(AllergyPatientAlert.class.getCanonicalName());
    }

    public AllergyPatientAlert(final Medicine medicine, final List<AllergenVO> allergens) {
        this();
        setLevel(Level.HIGH);
        setMedicine(medicine);
        setPatient(medicine.patient());
        setDetails(new AllergyAlertInfo(allergens));
    }

    @Override
    public Class<?> getDetailsType() {
        return AllergyAlertInfo.class;
    }

    public static class AllergyAlertInfo {
        private List<AllergenVO> allergens;

        public AllergyAlertInfo() {
        }

        public AllergyAlertInfo(List<AllergenVO> allergens) {
            this.allergens = allergens;
        }

        public List<AllergenVO> getAllergens() {
            return allergens;
        }

        public void setAllergens(List<AllergenVO> allergens) {
            this.allergens = allergens;
        }
    }
}
