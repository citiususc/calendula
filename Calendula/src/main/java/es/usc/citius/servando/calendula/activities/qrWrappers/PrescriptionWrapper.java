package es.usc.citius.servando.calendula.activities.qrWrappers;


import java.io.Serializable;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.persistence.Prescription;

/**
 * Created by joseangel.pineiro on 10/6/15.
 */
public class PrescriptionWrapper{
    public String cn;
    public String g;
    public String sk;
    public List<PickupWrapper> pk;
    public ScheduleWrapper s;
    public Prescription prescription;
    public HomogeneousGroup group;
    public boolean exists;
    public boolean isGroup = false;

    public Holder holder(){
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

    public static PrescriptionWrapper from(Holder h){
        PrescriptionWrapper pw = new PrescriptionWrapper();
        pw.cn = h.cn;
        pw.g = h.g;
        pw.sk = h.sk;
        pw.pk = h.pk;
        pw.s = h.s;
        pw.exists = h.exists;
        pw.isGroup = h.isGroup;
        pw.group = pw.g != null ? DB.groups().findOneBy(HomogeneousGroup.COLUMN_GROUP, pw.g) : null;
        pw.prescription = pw.cn != null ? Prescription.findByCn(pw.cn) : null;
        return  pw;
    }


    public static class Holder implements Serializable{
        public String cn;
        public String g;
        public String sk;
        public List<PickupWrapper> pk;
        public ScheduleWrapper s;
        public boolean exists;
        public boolean isGroup = false;
    }
}
