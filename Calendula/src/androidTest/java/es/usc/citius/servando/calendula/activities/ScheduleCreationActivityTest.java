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

package es.usc.citius.servando.calendula.activities;

import android.content.res.Resources;
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
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ScheduleCreationActivityTest extends ActivityInstrumentationTestCase2<ScheduleCreationActivity> {

    boolean[] days = new boolean[]{true, false, true, false, true, false, true};
    private ScheduleCreationActivity mActivity;


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
        final Patient defaultPatient = DB.patients().getDefault();
        r = new Routine(defaultPatient, new LocalTime(9, 0), "Breakfast");
        r.save();
        r = new Routine(defaultPatient, new LocalTime(13, 0), "Lunch");
        r.save();
        r = new Routine(defaultPatient, new LocalTime(21, 0), "Dinner");
        r.save();
        // create and save some meds
        m = new Medicine("Ibuprofen", Presentation.PILLS);
        m.setPatient(defaultPatient);
        m.save();
        m = new Medicine("AAS", Presentation.CAPSULES);
        m.setPatient(defaultPatient);
        m.save();
        m = new Medicine("Aspirin", Presentation.EFFERVESCENT);
        m.setPatient(defaultPatient);
        m.save();

        // set edit intent
        mActivity = getActivity();
        TestUtils.unlockScreen(mActivity);
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


        // click "next" button
        onView(withId(R.id.schedule_help_button)).perform(click());
        TestUtils.sleep(500);

        // Check 3 routines are displayed
        onView(withText("Breakfast")).check(matches(isDisplayed()));
        onView(withText("Lunch")).check(matches(isDisplayed()));
        onView(withText("Dinner")).check(matches(isDisplayed()));

        // click "next" button
        onView(withId(R.id.schedule_help_button)).perform(click());
        TestUtils.sleep(500);

        // unselect tu, thu and sat
        onView(withText(R.string.schedule_day_selector_tu)).perform(click());
        TestUtils.sleep(100);
        onView(withText(R.string.schedule_day_selector_th)).perform(click());
        TestUtils.sleep(100);
        onView(withText(R.string.schedule_day_selector_sa)).perform(click());


        // go to summary page
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText(R.string.summary))).perform(click());
        onView(withId(R.id.sched_summary_medi_dailyfreq)).check(matches(withText(selected)));
        onView(withId(R.id.sched_summary_medname)).check(matches(withText("Aspirin")));
        Resources resources = mActivity.getResources();
        String daySummary = String.format("%s, %s, %s %s %s", resources.getString(R.string.day_monday_short), resources.getString(R.string.day_wednesday_short), resources.getString(R.string.day_friday_short), resources.getString(R.string.and), resources.getString(R.string.day_sunday_short));
        onView(withId(R.id.sched_summary_medi_days)).check(matches(withText(daySummary)));

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
        assertEquals("Aspirin", s.medicine().getName());
        // select days are mon, wed,fry and sun
        assertEquals(Arrays.toString(days), Arrays.toString(s.days()));
        // there are 3 schedule items
        assertEquals(3, items.size());
        // every schedule item...
        for (ScheduleItem i : items) {
            // has a routine
            assertNotNull(i.getRoutine());
            // and a schedule
            assertNotNull(i.getSchedule());
            // has a dose of 1
            assertEquals(1.0f, i.getDose());
            // has an associated DailyScheduleItem
            DailyScheduleItem dsi = DB.dailyScheduleItems().findByScheduleItem(i);
            // which is not null
            assertNotNull(dsi);
            // and is not set as taken
            assertFalse(dsi.getTakenToday());
        }
    }
}