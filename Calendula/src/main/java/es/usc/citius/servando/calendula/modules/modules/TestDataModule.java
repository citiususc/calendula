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

package es.usc.citius.servando.calendula.modules.modules;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.ical.values.Frequency;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.activities.PatientDetailActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.modules.CalendulaModule;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.RepetitionRule;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.util.AvatarMgr;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.SettingsProperties;
import es.usc.citius.servando.calendula.util.SettingsPropertiesKeys;

/**
 * Created by alvaro.brey.vilas on 21/03/17.
 */
public class TestDataModule extends CalendulaModule {

    public static final String ID = "CALENDULA_TEST_DATA_MODULE";

    private static final String TAG = "TestDataModule";

    private static final int PATIENT_NUMBER = 2;
    private static final int MEDICINE_NUMBER = 6;
    private static final int ROUTINE_NUMBER = 6;
    private static final int SCHEDULE_NUMBER = 4;

    @Override
    public String getId() {
        return ID;
    }


    private String getAvatar(final int i) {
        switch (i + 1) {
            case 1:
                return AvatarMgr.AVATAR_1;
            case 2:
                return AvatarMgr.AVATAR_2;
            case 3:
                return AvatarMgr.AVATAR_3;
            case 4:
                return AvatarMgr.AVATAR_4;
            case 5:
                return AvatarMgr.AVATAR_5;
            case 6:
                return AvatarMgr.AVATAR_6;
            case 7:
                return AvatarMgr.AVATAR_7;
            case 8:
                return AvatarMgr.AVATAR_8;
            case 9:
                return AvatarMgr.AVATAR_9;
            case 10:
                return AvatarMgr.AVATAR_10;
            case 11:
                return AvatarMgr.AVATAR_11;
            case 12:
                return AvatarMgr.AVATAR_12;
            case 13:
                return AvatarMgr.AVATAR_13;
            case 14:
                return AvatarMgr.AVATAR_14;
            case 15:
                return AvatarMgr.AVATAR_15;
            default:
                return AvatarMgr.DEFAULT_AVATAR;
        }
    }

    @Override
    protected void onApplicationStartup(final Context ctx) {
        // generate some default data to test UI and the likes

        final boolean testDataGenerated = PreferenceUtils.getBoolean(PreferenceKeys.TEST_DATA_GENERATED, false);
        if (testDataGenerated && SettingsProperties.instance().get(SettingsPropertiesKeys.GENERATE_TEST_DATA).equals("yes")) {
            Log.d(TAG, "onApplicationStartup: Test data already generated, skipping");
        } else {
            Log.d(TAG, "onApplicationStartup: Generating test data");

            DB.transaction(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // patients
                    final List<Patient> patients = generatePatients(ctx);

                    // medicines
                    final Map<Patient, List<Medicine>> medicines = generateMedicines(patients);

                    // routines
                    final Map<Patient, List<Routine>> routines = generateRoutines(patients);

                    // schedules !
                    final Map<Patient, List<Schedule>> schedules = generateSchedules(patients, medicines, routines, ctx);
                    return null;
                }
            });

            Log.d(TAG, "onApplicationStartup: Test data generation finished");
            PreferenceUtils.edit().putBoolean(PreferenceKeys.TEST_DATA_GENERATED.key(), true).apply();
        }

    }


    @NonNull
    private List<Patient> generatePatients(Context ctx) {
        Log.d(TAG, "Creating patients");
        List<Patient> patients = new ArrayList<>(PATIENT_NUMBER);
        final Patient def = DB.patients().getDefault();
        DB.patients().removeCascade(def);
        for (int i = 0; i < PATIENT_NUMBER; i++) {
            Patient p = new Patient();
            p.setDefault(i == 0);
            p.setName("Test Patient " + (i + 1));
            p.setAvatar(getAvatar(i));
            p.setColor(Color.parseColor(PatientDetailActivity.COLORS[i % PatientDetailActivity.COLORS.length]));
            p.setDefault(false);
            DB.patients().save(p);
            Log.d(TAG, "Created patient " + p);
            patients.add(p);
        }
        DB.patients().setActive(patients.get(0));
        Log.d(TAG, String.format("Created %d patients", PATIENT_NUMBER));
        return patients;
    }

    private Map<Patient, List<Medicine>> generateMedicines(List<Patient> patients) {
        Log.d(TAG, "Creating medicines");
        Map<Patient, List<Medicine>> medicines = new HashMap<>();
        for (Patient patient : patients) {
            medicines.put(patient, new ArrayList<Medicine>());
        }
        final int presentationTypes = Presentation.values().length;
        for (int i = 0; i < MEDICINE_NUMBER; i++) {
            Medicine m = new Medicine();
            m.setName("Test Medicine " + (i + 1));
            m.setPresentation(Presentation.values()[i % presentationTypes]);
            final Patient patient = patients.get(i % PATIENT_NUMBER);
            m.setPatient(patient);
            m.setStock((float) (20 * i % (50 - MEDICINE_NUMBER)));
            DB.medicines().save(m);
            Log.d(TAG, "Created medicine " + m);
            medicines.get(patient).add(m);
        }
        Log.d(TAG, String.format("Created %d medicines", MEDICINE_NUMBER));
        return medicines;
    }

    private Map<Patient, List<Routine>> generateRoutines(List<Patient> patients) {
        Log.d(TAG, "Creating routines");
        Map<Patient, List<Routine>> routines = new HashMap<>();
        for (Patient patient : patients) {
            routines.put(patient, new ArrayList<Routine>());
        }
        for (int i = 0; i < ROUTINE_NUMBER; i++) {
            Routine r = new Routine();
            final Patient patient = patients.get(i % PATIENT_NUMBER);
            r.setPatient(patient);
            r.setName("Test routine " + (i + 1));
            r.setTime(LocalTime.MIDNIGHT.plusHours(5 * i % 24).plusMinutes(6 * i % 60));
            DB.routines().save(r);
            Log.d(TAG, "Created routine " + r);
            routines.get(patient).add(r);
        }
        Log.d(TAG, String.format("Created %d routines", ROUTINE_NUMBER));
        return routines;
    }

    private Map<Patient, List<Schedule>> generateSchedules(List<Patient> patients, Map<Patient, List<Medicine>> medicines, Map<Patient, List<Routine>> routines, Context ctx) {
        Log.d(TAG, "Creating schedules");
        Map<Patient, List<Schedule>> schedules = new HashMap<>();
        for (Patient patient : patients) {
            schedules.put(patient, new ArrayList<Schedule>());
        }

        for (int i = 0; i < SCHEDULE_NUMBER; i++) {
            Schedule s = new Schedule();
            Patient p = patients.get(i % PATIENT_NUMBER);
            Medicine m = medicines.get(p).get(i % medicines.get(p).size());
            s.setPatient(p);
            s.setType(Schedule.SCHEDULE_TYPE_HOURLY);
            s.setMedicine(m);
            s.setDose(1F);
            RepetitionRule rule = new RepetitionRule();
            rule.setFrequency(Frequency.HOURLY);
            rule.setInterval((6 * i % 12) + 6);
            s.setRepetition(rule);
            s.setStart(LocalDate.now());
            s.setStartTime(LocalTime.now().plusHours(i).plusMinutes((i * 30) % 60));

            // TODO: 22/03/17 add some variety

            DB.schedules().save(s);
            Log.d(TAG, "Created schedule " + s);
            schedules.get(p).add(s);
        }
        DailyAgenda.instance().setupForToday(ctx, true); // update agenda
        return schedules;
    }
}
