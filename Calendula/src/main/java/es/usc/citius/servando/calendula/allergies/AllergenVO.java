package es.usc.citius.servando.calendula.allergies;

import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 15/11/16.
 */

public class AllergenVO {

    private AllergenType type;
    private String name;
    private String identifier;


    public AllergenVO(AllergenType type, String name, String identifier) {
        this.type = type;
        this.name = name;
        this.identifier = identifier;
    }

    public AllergenVO(Excipient excipient) {
        this.type = AllergenType.EXCIPIENT;
        this.name = excipient.getName();
        this.identifier = excipient.getExcipientID();
    }

    public AllergenVO(ActiveIngredient activeIngredient) {
        this.type = AllergenType.ACTIVE_INGREDIENT;
        this.name = activeIngredient.getName();
        this.identifier = activeIngredient.getActiveIngredientCode();
    }

    public AllergenVO(PatientAllergen allergen) {
        this.type = allergen.getType();
        this.name = allergen.getName();
        this.identifier = allergen.getIdentifier();
    }

    public AllergenType getType() {
        return type;
    }

    public void setType(AllergenType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AllergenVO that = (AllergenVO) o;

        if (type != that.type) return false;
        return identifier.equals(that.identifier);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + identifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AllergenVO{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
