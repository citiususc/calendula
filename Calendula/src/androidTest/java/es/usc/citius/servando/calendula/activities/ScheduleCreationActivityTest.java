package es.usc.citius.servando.calendula.activities;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ScheduleCreationActivityTest extends ActivityInstrumentationTestCase2<ScheduleCreationActivity> {

    private ScheduleCreationActivity mActivity;
    boolean[] days = new boolean[]{true, false, true, false, true, false, true};


    public ScheduleCreationActivityTest() {
        super(ScheduleCreationActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;

        Routine r;
        Medicine m;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();

        // create and save some routines
        r = new Routine(new LocalTime(9, 0), "Breakfast");
        r.save();
        r = new Routine(new LocalTime(13, 0), "Lunch");
        r.save();
        r = new Routine(new LocalTime(21, 0), "Dinner");
        r.save();
        // create and save some meds
        m = new Medicine("Ibuprofen", Presentation.PILLS);
        m.save();
        m = new Medicine("AAS", Presentation.CAPSULES);
        m.save();
        m = new Medicine("Aspirin", Presentation.EFFERVESCENT);
        m.save();

        // set edit intent
        mActivity = getActivity();
    }

    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }

    @Test
    public void testScheduleCreation() {

        assertEquals(0, DB.schedules().count());

        String[] schedules = mActivity.getResources().getStringArray(R.array.schedules_array);
        String selected = schedules[2];
        // ensure we have a populated db
        assertEquals(3, DB.routines().count());
        assertEquals(3, DB.medicines().count());

        // visit all pager pages
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.medicine))).perform(click());
        TestUtils.sleep(200);
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.schedule))).perform(click());
        TestUtils.sleep(100);
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.summary))).perform(click());
        TestUtils.sleep(100);
        // return to first page
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.medicine))).perform(click());
        // select aspirin, will send us to 2nd page
        TestUtils.sleep(100);
        onView(withText("Aspirin")).perform(click());
        TestUtils.sleep(500);
        // select 3 times a day schedule
        onView(withId(R.id.schedules_spinner)).perform(click());
        onView(withText(selected)).perform(click());
        TestUtils.sleep(100);
        onView(withId(R.id.pager)).perform(swipeUp());
        TestUtils.sleep(500);
        // unselect tu, thu and sat
        onView(withText(R.string.schedule_day_selector_tu)).perform(click());
        TestUtils.sleep(100);
        onView(withText(R.string.schedule_day_selector_th)).perform(click());
        TestUtils.sleep(100);
        onView(withText(R.string.schedule_day_selector_sa)).perform(click());

        // Check 3 routines are displayed
        onView(withText("Breakfast")).check(matches(isDisplayed()));
        onView(withText("Lunch")).check(matches(isDisplayed()));
        onView(withText("Dinner")).check(matches(isDisplayed()));

        // go to summary page
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.summary))).perform(click());
        onView(withId(R.id.sched_summary_medi_dailyfreq)).check(matches(withText(selected)));
        onView(withId(R.id.sched_summary_medname)).check(matches(withText("Aspirin")));
        onView(withId(R.id.sched_summary_medi_days)).check(matches(withText("Lun, Mie, Vie y Dom")));

        TestUtils.sleep(500);
        // click save
        onView(withId(R.id.add_button)).perform(click());

        // find schedule and do assertions
        assertEquals(1, DB.schedules().count());
        Schedule s = DB.schedules().findAll().get(0);
        List<ScheduleItem> items = s.items();

        // schedule medicine is not null
        assertNotNull(s.medicine());
        // medicine is aspirin
        assertEquals("Aspirin", s.medicine().name());
        // select days are mon, wed,fry and sun
        assertEquals(Arrays.toString(days), Arrays.toString(s.days()));
        // there are 3 schedule items
        assertEquals(3, items.size());
        // every schedule item...
        for (ScheduleItem i : items) {
            // has a routine
            assertNotNull(i.routine());
            // and a schedule
            assertNotNull(i.schedule());
            // has a dose of 1
            assertEquals(1.0f, i.dose());
            // has an associated DailyScheduleItem
            DailyScheduleItem dsi = DB.dailyScheduleItems().findByScheduleItem(i);
            // which is not null
            assertNotNull(dsi);
            // and is not set as taken
            assertFalse(dsi.takenToday());
        }
    }
}