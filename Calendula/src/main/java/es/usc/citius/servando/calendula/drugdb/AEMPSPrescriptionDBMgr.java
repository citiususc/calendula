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

package es.usc.citius.servando.calendula.drugdb;

import android.util.Log;

import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.Strings;

/**
 * Created by joseangel.pineiro on 9/8/15.
 */
public class AEMPSPrescriptionDBMgr extends PrescriptionDBMgr {

    public static final String PROSPECT_URL = "https://www.aemps.gob.es/cima/dochtml/p/#ID#/Prospecto_#ID#.html";

    @Override
    public boolean hasWebProspects() {
        return false;
    }

    @Override
    public String getProspectURL(Prescription p) {
        return PROSPECT_URL.replaceAll("#ID#", p.pid);
    }

    @Override
    public Prescription fromCsv(String csvLine, String separator) {

        String[] values = csvLine.split(separator);

        if (values.length != 9) {
            throw new RuntimeException("Invalid CSV. Input string must contain exactly 6 members. " + csvLine);
        }

        Prescription p = new Prescription();
        p.cn = values[0];
        p.pid = values[1];
        p.name = values[2];
        p.dose = values[3];
        p.content = values[5];
        p.generic = getBoolean(values[6]);
        p.affectsDriving = getBoolean(values[7]);
        p.hasProspect = getBoolean(values[8]);

        try {
            p.packagingUnits = Float.valueOf(values[4].replaceAll(",", "."));
        } catch (Exception e) {
            Log.w("Prescription.class", "Unable to parse med " + p.pid + " packagingUnits");
            p.packagingUnits = -1f;
        }
        return p;
    }

    @Override
    public Presentation expected(Prescription p) {
        String name = p.name;
        String content = p.content;
        return expected(name, content);
    }

    @Override
    public Presentation expected(String name, String content) {

        String n = name.toLowerCase() + " " + content.toLowerCase();
        if (n.contains("comprimidos")) {
            return Presentation.PILLS;
        } else if (n.contains("capsulas") || n.contains("cápsulas")) {
            return Presentation.CAPSULES;
        } else if (n.contains("inhala")) {
            return Presentation.INHALER;
        } else if (n.contains("viales") || n.contains("jeringa") || n.contains("perfusi") || n.contains("inyectable")) {
            return Presentation.INJECTIONS;
        } else if (n.contains("gotas") || n.contains("colirio")) {
            return Presentation.DROPS;
        } else if (n.contains("sobres")) {
            return Presentation.EFFERVESCENT;
        } else if (n.contains("tubo") || n.contains("crema") || n.contains("pomada")) {
            return Presentation.POMADE;
        } else if (n.contains("pulverizacion nasal") || n.contains("pulverización nasal") || n.contains("spray")) {
            return Presentation.SPRAY;
        } else if (n.contains("jarabe")) {
            return Presentation.SYRUP;
        }else if (n.contains("parche")) {
                return Presentation.PATCHES;
        } else if (n.contains("suspension oral")) {
            if (!n.contains("polvo") && !n.contains("granulado")) {
                return Presentation.SYRUP;
            } else if (!n.contains("polvo")) {
                // granulado
            } else {
                // sobres
            }
        }

        return null;
    }

    @Override
    public String shortName(Prescription p) {
        try {
            String[] parts = p.name.split(" ");
            String s = parts[0].toLowerCase();
            if ((s.contains("acido") || s.contains("ácido")) && parts.length > 1) {
                return Strings.toCamelCase(s + " " + parts[1], " ");
            }
            return Strings.toProperCase(s);

        } catch (Exception e) {
            return p.name;
        }
    }

}
