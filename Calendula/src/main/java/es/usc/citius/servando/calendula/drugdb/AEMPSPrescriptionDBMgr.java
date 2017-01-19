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
import android.util.Log;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.ZipUtil;

/**
 * Created by joseangel.pineiro on 9/8/15.
 */
public class AEMPSPrescriptionDBMgr extends PrescriptionDBMgr {

    public static final String PROSPECT_URL = "https://www.aemps.gob.es/cima/dochtml/p/#ID#/Prospecto_#ID#.html";
    private static final String TAG = "AEMPSPrescriptionDBMgr";

    @Override
    public String getProspectURL(Prescription p) {
        return PROSPECT_URL.replaceAll("#ID#", p.getpID());
    }

    @Override
    public Presentation expected(Prescription p) {
        String name = p.getName();
        String content = p.getContent();
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
        } else if (n.contains("jarabe") || n.contains("frasco")) {
            return Presentation.SYRUP;
        } else if (n.contains("parche")) {
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

        Log.d(TAG, "setup: uncompressing " + downloadPath + " into " + uncompressedPath);
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

                Log.d(TAG, "call: reading from " + uncompressedPath);
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

        Log.d(TAG, "setup: cleaning up...");
        try {
            boolean delete = new File(downloadPath).delete();
            if (!delete) {
                Log.i(TAG, "setup: couldn't delete file " + downloadPath);
            }
            delete = new File(uncompressedPath).delete();
            if (!delete) {
                Log.i(TAG, "setup: couldn't delete file " + uncompressedPath);
            }
        } catch (Exception e) {
            Log.e(TAG, "setup: couldn't finish cleanup: ", e);
        }
    }

    private void updateProgress(SetupProgressListener l, int progress) {
        if (l != null) l.onProgressUpdate(progress);
    }
}
