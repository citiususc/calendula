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

import android.content.Intent;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.ical.values.Frequency;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.HomePagerActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.adapters.AlertViewRecyclerAdapter;
import es.usc.citius.servando.calendula.adapters.HomePages;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.persistence.RepetitionRule;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.alerts.StockRunningOutAlert;
import es.usc.citius.servando.calendula.util.CustomViewActions;
import es.usc.citius.servando.calendula.util.CustomViewMatchers;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by alvaro.brey.vilas on 06/02/17.
 */

@RunWith(AndroidJUnit4.class)
public class StockAlertTest {

    @Rule
    public final ActivityTestRule<HomePagerActivity> rule =
            new ActivityTestRule<>(HomePagerActivity.class, true, false);
    private static final String DATABASE = "TEST_DB";


    @Before
    public void setUp() throws SQLException {
        // drop database
        DB.dropAndCreateDatabase();
        // set some vars
        PreferenceUtils.edit().putString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), DATABASE).apply();
        // create some allergens
        final Patient p = DB.patients().getDefault();
        Medicine m = new Medicine();
        m.setPatient(p);
        m.setDatabase(PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null));
        m.setPresentation(Presentation.UNKNOWN);
        m.setName("TEST_MEDICINE");
        m.setStock(3.0F);
        DB.medicines().saveAndFireEvent(m);
        PreferenceUtils.edit().putBoolean(PreferenceKeys.HOME_INTRO_SHOWN.key(), true).commit();

        final RepetitionRule repetitionRule = new RepetitionRule();
        repetitionRule.setFrequency(Frequency.HOURLY);
        repetitionRule.setInterval(4);

        Schedule schedule = new Schedule();
        schedule.setMedicine(m);
        schedule.setType(Schedule.SCHEDULE_TYPE_HOURLY);
        schedule.setDose(4.0F);
        schedule.setRepetition(repetitionRule);
        schedule.setPatient(p);
        DB.schedules().saveAndFireEvent(schedule);


        rule.launchActivity(new Intent());

        TestUtils.unlockScreen(rule.getActivity());
    }

    @After
    public void tearDown() {
        PreferenceUtils.edit().remove(PreferenceKeys.DRUGDB_CURRENT_DB.key()).apply();
    }

    @Test
    public void testViewAlert() {
        final HomePagerActivity activity = rule.getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.showPagerItem(HomePages.MEDICINES.ordinal());
            }
        });


        onView(withId(R.id.medicines_list)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, CustomViewActions.clickChildViewWithId(R.id.imageView)));

        TestUtils.sleep(400);

        onView(withId(R.id.rv)).check(matches(CustomViewMatchers.withRecyclerViewSize(1)));
        onView(withId(R.id.rv)).check(matches(StockAlertMatcher()));
    }


    private static Matcher<View> StockAlertMatcher() {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(final View view) {
                RecyclerView r = (RecyclerView) view;
                AlertViewRecyclerAdapter a = (AlertViewRecyclerAdapter) r.getAdapter();
                final PatientAlert patientAlert = a.getItem(0);
                return patientAlert instanceof StockRunningOutAlert;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Check that this recyclerview contains the adequate stock alert item");
            }
        };
    }
}
