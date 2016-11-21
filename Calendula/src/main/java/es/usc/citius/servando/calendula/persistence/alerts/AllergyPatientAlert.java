package es.usc.citius.servando.calendula.persistence.alerts;

import java.util.List;

import es.usc.citius.servando.calendula.allergies.AllergenVO;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PatientAlert;

public class AllergyPatientAlert extends PatientAlert<AllergyPatientAlert.AllergyAlertInfo> {


    public AllergyPatientAlert() {
        setType(AlertType.ALLERGY_ALERT);
    }

    public AllergyPatientAlert(final Medicine medicine, final List<AllergenVO> allergens) {
        this();
        setLevel(Level.HIGH);
        setMedicine(medicine);
        setPatient(medicine.patient());
        setDetails(new AllergyAlertInfo(allergens));
    }

    public AllergyPatientAlert(PatientAlert patientAlert) {
        this();
        setPatient(patientAlert.getPatient());
        setMedicine(patientAlert.getMedicine());
        setJsonDetails(patientAlert.getJsonDetails());
        setId(patientAlert.id());
        setLevel(patientAlert.getLevel());
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
