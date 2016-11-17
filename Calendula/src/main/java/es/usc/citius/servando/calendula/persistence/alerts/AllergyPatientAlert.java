package es.usc.citius.servando.calendula.persistence.alerts;

import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

public class AllergyPatientAlert extends PatientAlert<AllergyPatientAlert.AllergyAlertInfo> {


    public AllergyPatientAlert() {
        setType(AlertType.ALLERGY_ALERT);
    }

    public AllergyPatientAlert(final Patient patient, final Medicine medicine, final AllergenVO allergen) {
        this();
        setDetails(new AllergyAlertInfo(medicine, allergen));
        setPatient(patient);
    }

    @Override
    public Class<?> getDetailsType() {
        return AllergyAlertInfo.class;
    }

    public static class AllergyAlertInfo {
        private Medicine medicine;
        private AllergenVO allergen;

        public AllergyAlertInfo() {
        }

        public AllergyAlertInfo(Medicine medicine, AllergenVO allergen) {
            this.medicine = medicine;
            this.allergen = allergen;
        }

        public Medicine getMedicine() {
            return medicine;
        }

        public void setMedicine(Medicine medicine) {
            this.medicine = medicine;
        }

        public AllergenVO getAllergen() {
            return allergen;
        }

        public void setAllergen(AllergenVO allergen) {
            this.allergen = allergen;
        }
    }
}
