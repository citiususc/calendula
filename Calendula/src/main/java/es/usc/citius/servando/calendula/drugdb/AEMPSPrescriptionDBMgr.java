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

package es.usc.citius.servando.calendula.drugdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PresentationForm;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.ZipUtil;

/**
 * Created by joseangel.pineiro on 9/8/15.
 */
public class AEMPSPrescriptionDBMgr extends PrescriptionDBMgr {

    public static final String PROSPECT_URL = "https://www.aemps.gob.es/cima/dochtml/p/#ID#/Prospecto_#ID#.html";
    private static final String TAG = "AEMPSPrescriptionDBMgr";

    @Override
    public String getProspectURL(Prescription p) {
        return PROSPECT_URL.replaceAll("#ID#", p.getPID());
    }

    @Override
    public Presentation expectedPresentation(Prescription p) {
        // try to get presentation directly from database
        final Presentation presentation = expectedPresentation(p.getPresentationForm());
        if(!presentation.equals(Presentation.UNKNOWN)){
            return presentation;
        }
        // if not successful, try to infer it from the name
        String name = p.getName();
        String content = p.getContent();
        return expectedPresentation(name, content);
    }

    @Override
    public Presentation expectedPresentation(String name, String content) {

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
        } else if (n.contains("jarabe") || n.contains("frasco")) {
            return Presentation.SYRUP;
        } else if (n.contains("parche")) {
            return Presentation.PATCHES;
        } else if (n.contains("suspension oral")) {
            //FIXME
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
        String dose = p.getDose().trim();
        String originalName = p.getName();
        String doseFirstPart = dose.contains(" ") ? dose.split(" ")[0] : dose;

        if (doseFirstPart != null && originalName.contains(doseFirstPart)) {
            int index = originalName.indexOf(doseFirstPart);
            return originalName.substring(0, index);
        }
        return originalName;
    }

    @Override
    public void setup(final Context ctx, final String downloadPath, final SetupProgressListener l) throws Exception {

        final ConnectionSource connection = DB.helper().getConnectionSource();
        final String basePath = downloadPath.replaceAll("/[^/]*$", "");
        final String uncompressedPath = basePath + "/AEMPS.sql";

        LogUtil.d(TAG, "setup: uncompressing " + downloadPath + " into " + uncompressedPath);
        ZipUtil.unzip(new File(downloadPath), new File(basePath));

        TransactionManager.callInTransaction(connection, new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                DBRegistry.instance().clear();

                BufferedReader br;
                String line;
                int progressUpdateBy;
                int lines = 0;
                int i = 0;

                br = new BufferedReader(new InputStreamReader(new FileInputStream(uncompressedPath)));
                // count file lines (for progress updating)
                while (br.readLine() != null) {
                    lines++;
                }
                br.close();
                progressUpdateBy = lines / 20;
                updateProgress(l, 0);

                LogUtil.d(TAG, "call: reading from " + uncompressedPath);
                br = new BufferedReader(new InputStreamReader(new FileInputStream(uncompressedPath)));

                SQLiteDatabase database = DB.helper().getWritableDatabase();
                while ((line = br.readLine()) != null) {
                    if (l != null && i % progressUpdateBy == 0) {
                        int progress = (int) (((float) i / lines) * 100);
                        l.onProgressUpdate(progress);
                    }
                    // exec line content as raw sql
                    database.execSQL(line);
                    i++;
                }
                br.close();
                return null;
            }
        });

        LogUtil.d(TAG, "setup: cleaning up...");
        try {
            boolean delete = new File(downloadPath).delete();
            if (!delete) {
                LogUtil.i(TAG, "setup: couldn't delete file " + downloadPath);
            }
            delete = new File(uncompressedPath).delete();
            if (!delete) {
                LogUtil.i(TAG, "setup: couldn't delete file " + uncompressedPath);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "setup: couldn't finish cleanup: ", e);
        }
    }

    private Presentation expectedPresentation(@NonNull final String presentationFormId) {

        switch (presentationFormId) {
            case "4": // Capsula
            case "5": // Capsula liberacion modificada
                return Presentation.CAPSULES;

            case "10": // Comprimido
            case "11": // Comprimido bucal / Para chupar
            case "12": // Comprimido bucodispersable / Liotab
            case "13": // Comprimido efervescente
            case "14": // Comprimido liberacion modificada
            case "15": // Comprimido masticable
            case "16": // Comprimido sublingual
                return Presentation.PILLS;

            case "44": // Solución / Suspensión oral efervescente
            case "45": // Polvo / Granulado liberación modificada
                return Presentation.EFFERVESCENT;

            case "18": // Crema
            case "23": // Emulsion
            case "24": // Gel
            case "25": // Gel oftalmico
            case "26": // Gel / Pasta / Liquido bucal
            case "43": // Pasta
            case "47": // Pomada
            case "48": // Pomada oftalmica
            case "71": // Pomada oftalmica / otica
                return Presentation.POMADE;

            case "32": // Inhalación endotraqueopulmonar
            case "33": // Inhalacion pulmonar
                return Presentation.INHALER;

            case "34": // Inyectable
            case "35": // Inyectable perfusion
                return Presentation.INJECTIONS;

            case "42": // Parche transdermico
                return Presentation.PATCHES;

            case "53": // Producto uso nasal
                return Presentation.SPRAY;

        }
        return Presentation.UNKNOWN;
    }

    private void updateProgress(SetupProgressListener l, int progress) {
        if (l != null) l.onProgressUpdate(progress);
    }
}
