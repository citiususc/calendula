package es.usc.citius.servando.calendula.database;

import android.support.test.InstrumentationRegistry;
import android.test.InstrumentationTestCase;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;

public class DBTest extends InstrumentationTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
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

        PickupInfo pk = new PickupInfo();
        pk.setFrom(LocalDate.parse("2015-01-01"));
        pk.setTo(LocalDate.parse("2015-02-05"));
        pk.setMedicine(m);

        // save some stuff
        DB.routines().save(r);
        DB.medicines().save(m);
        DB.schedules().save(s);
        DB.scheduleItems().save(i);
        DB.dailyScheduleItems().save(d);
        DB.pickups().save(pk);

        // verify id property created
        assertNotNull(r.getId());
        assertNotNull(m.getId());
        assertNotNull(s.getId());
        assertNotNull(i.getId());
        assertNotNull(d.getId());
        assertNotNull(pk.getId());

        assertEquals(DB.pickups().findByMedicine(m).get(0).getFrom(), LocalDate.parse("2015-01-01"));
    }

    @Test
    public void testDaoFind() throws Exception {

        testDaoSave();
        assertEquals(DB.routines().findOneBy(Routine.COLUMN_NAME, "Test").getTime(), new LocalTime(0, 0));
        assertEquals(DB.medicines().findAll().get(0).getName(), "TestMed");
        assertEquals(DB.schedules().findAll().get(0).items().size(), 1);
    }


}