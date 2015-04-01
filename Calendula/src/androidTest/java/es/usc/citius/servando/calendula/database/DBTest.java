package es.usc.citius.servando.calendula.database;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

public class DBTest extends AndroidTestCase {

    public static final String TAG = "DBTest";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        Log.d(TAG, "Setting up DB tests...");
        DB.init(new RenamingDelegatingContext(getContext(), "_test"));
        DB.dropAndCreateDatabase();
    }

    @Test
    public void testDaoSave() throws Exception {
        Routine r = new Routine(new LocalTime(0, 0), "Test");
        Medicine m = new Medicine("TestMed", Presentation.CAPSULES);
        Schedule s = new Schedule(m);
        ScheduleItem i = new ScheduleItem(s, r);
        DailyScheduleItem d = new DailyScheduleItem(i);
        d.setTakenToday(true);

        // save some stuff
        DB.routines().save(r);
        DB.medicines().save(m);
        DB.schedules().save(s);
        DB.scheduleItems().save(i);
        DB.dailyScheduleItems().save(d);

        // verify id property created
        assertNotNull(r.getId());
        assertNotNull(m.getId());
        assertNotNull(s.getId());
        assertNotNull(i.getId());
        assertNotNull(d.getId());
    }

    @Test
    public void testDaoFind() throws Exception {

        testDaoSave();
        assertEquals(DB.routines().findOneBy(Routine.COLUMN_NAME, "Test").time(), new LocalTime(0, 0));
        assertEquals(DB.medicines().findAll().get(0).name(), "TestMed");
        assertEquals(DB.schedules().findAll().get(0).items().size(), 1);
    }


}