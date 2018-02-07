/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

package es.usc.citius.servando.calendula.allergies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alvaro.brey.vilas on 9/12/16.
 */

public class AllergenGroupWrapper implements Parcelable {

    public static final Parcelable.Creator<AllergenGroupWrapper> CREATOR = new Parcelable.Creator<AllergenGroupWrapper>() {
        @Override
        public AllergenGroupWrapper createFromParcel(Parcel source) {
            return new AllergenGroupWrapper(source);
        }

        @Override
        public AllergenGroupWrapper[] newArray(int size) {
            return new AllergenGroupWrapper[size];
        }
    };
    private AllergenVO vo;
    private String group;

    public AllergenGroupWrapper(AllergenVO vo, String group) {
        this.vo = vo;
        this.group = group;
    }

    public AllergenGroupWrapper(AllergenVO vo) {
        this.vo = vo;
    }

    protected AllergenGroupWrapper(Parcel in) {
        this.vo = in.readParcelable(AllergenVO.class.getClassLoader());
        this.group = in.readString();
    }

    public AllergenVO getVo() {
        return vo;
    }

    public void setVo(AllergenVO vo) {
        this.vo = vo;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.vo, flags);
        dest.writeString(this.group);
    }
}
