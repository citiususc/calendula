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

package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;

/**
 * Models an user
 */
@DatabaseTable(tableName = "PrescriptionAllergens")
public class PrescriptionAllergen {

    public enum AllergenType {
        ACTIVE_INGREDIENT, EXCIPIENT
    }

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ALLERGEN_TYPE = "Type";
    public static final String COLUMN_REALID = "RealID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PRESCRIPTION = "Prescription";


    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = COLUMN_ALLERGEN_TYPE, uniqueCombo = true)
    private PrescriptionAllergen.AllergenType type;

    @DatabaseField(columnName = COLUMN_REALID, uniqueCombo = true)
    private int realId;

    @DatabaseField(columnName = COLUMN_PRESCRIPTION, foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private Prescription prescription;

    public PrescriptionAllergen() {
    }

    public PrescriptionAllergen(String name, AllergenType type, int realId, Prescription prescription) {
        this.name = name;
        this.type = type;
        this.realId = realId;
        this.prescription = prescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AllergenType getType() {
        return type;
    }

    public void setType(AllergenType type) {
        this.type = type;
    }

    public int getRealId() {
        return realId;
    }

    public void setRealId(int realId) {
        this.realId = realId;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrescriptionAllergen that = (PrescriptionAllergen) o;

        if (realId != that.realId) return false;
        if (type != that.type) return false;
        return prescription.equals(that.prescription);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + realId;
        result = 31 * result + prescription.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PrescriptionAllergen{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", realId=" + realId +
                ", prescription=" + prescription +
                '}';
    }
}
