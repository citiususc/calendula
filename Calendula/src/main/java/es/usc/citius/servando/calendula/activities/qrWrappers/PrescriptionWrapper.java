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

package es.usc.citius.servando.calendula.activities.qrWrappers;


import java.io.Serializable;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;

/**
 * Created by joseangel.pineiro on 10/6/15.
 */
public class PrescriptionWrapper {
    public String cn;
    public String g;
    public String sk;
    public List<PickupWrapper> pk;
    public ScheduleWrapper s;
    public Prescription prescription;
    public HomogeneousGroup group;
    public boolean exists;
    public boolean isGroup = false;

    public static PrescriptionWrapper from(Holder h) {
        PrescriptionWrapper pw = new PrescriptionWrapper();
        pw.cn = h.cn;
        pw.g = h.g;
        pw.sk = h.sk;
        pw.pk = h.pk;
        pw.s = h.s;
        pw.exists = h.exists;
        pw.isGroup = h.isGroup;
        pw.group = pw.g != null ? DB.drugDB().homogeneousGroups().findOneBy(HomogeneousGroup.COLUMN_HOMOGENEOUS_GROUP_ID, pw.g) : null;
        pw.prescription = pw.cn != null ? DB.drugDB().prescriptions().findByCn(pw.cn) : null;
        return pw;
    }

    public Holder holder() {
        Holder h = new Holder();
        h.cn = cn;
        h.g = g;
        h.sk = sk;
        h.pk = pk;
        h.s = s;
        h.exists = exists;
        h.isGroup = isGroup;
        return h;
    }

    public static class Holder implements Serializable {
        public String cn;
        public String g;
        public String sk;
        public List<PickupWrapper> pk;
        public ScheduleWrapper s;
        public boolean exists;
        public boolean isGroup = false;
    }
}
