package es.usc.citius.servando.calendula.persistence.alerts;

import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

public class AllergyPatientAlert extends PatientAlert<AllergyPatientAlert.AllergyAlertInfo> {


    public AllergyPatientAlert() {
        setType(AlertType.ALLERGY_ALERT);
    }

    public AllergyPatientAlert(final Medicine medicine, final AllergenVO allergen) {
        this();
        setMedicine(medicine);
        setPatient(medicine.patient());
        setDetails(new AllergyAlertInfo(medicine, allergen));
    }

    @Override
    public Class<?> getDetailsType() {
        return AllergyAlertInfo.class;
    }

    public static class AllergyAlertInfo {
        private AllergenVO allergen;

        public AllergyAlertInfo() {
        }

        public AllergyAlertInfo(Medicine medicine, AllergenVO allergen) {
            this.allergen = allergen;
        }

        public AllergenVO getAllergen() {
            return allergen;
        }

        public void setAllergen(AllergenVO allergen) {
            this.allergen = allergen;
        }
    }
}
