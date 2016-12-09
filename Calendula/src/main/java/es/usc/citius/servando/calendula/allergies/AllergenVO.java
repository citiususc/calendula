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

package es.usc.citius.servando.calendula.allergies;

import android.os.Parcel;
import android.os.Parcelable;

import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;

/**
 * Created by alvaro.brey.vilas on 15/11/16.
 */

public class AllergenVO implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.name);
        dest.writeString(this.identifier);
    }

    protected AllergenVO(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : AllergenType.values()[tmpType];
        this.name = in.readString();
        this.identifier = in.readString();
    }

    public static final Parcelable.Creator<AllergenVO> CREATOR = new Parcelable.Creator<AllergenVO>() {
        @Override
        public AllergenVO createFromParcel(Parcel source) {
            return new AllergenVO(source);
        }

        @Override
        public AllergenVO[] newArray(int size) {
            return new AllergenVO[size];
        }
    };
}
