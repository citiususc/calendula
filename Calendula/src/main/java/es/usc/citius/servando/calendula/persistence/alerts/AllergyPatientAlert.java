package es.usc.citius.servando.calendula.persistence.alerts;

import es.usc.citius.servando.calendula.persistence.PatientAlert;

public class AllergyPatientAlert extends PatientAlert<AllergyPatientAlert.AllergyAlertInfo> {


    public AllergyPatientAlert() {
        setType(AlertType.ALLERGY_ALERT);
    }

    @Override
    public Class<?> getDetailsType() {
        return AllergyAlertInfo.class;
    }

    @Override
    public boolean hasDetails() {
        return true;
    }


    public static class AllergyAlertInfo {
        private String prescriptionIdentifier;
        private String allergenIdentifier;

        public String getPrescriptionIdentifier() {
            return prescriptionIdentifier;
        }

        public void setPrescriptionIdentifier(String prescriptionIdentifier) {
            this.prescriptionIdentifier = prescriptionIdentifier;
        }

        public String getAllergenIdentifier() {
            return allergenIdentifier;
        }

        public void setAllergenIdentifier(String allergenIdentifier) {
            this.allergenIdentifier = allergenIdentifier;
        }
    }
}
