/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2017 CITIUS - USC
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

package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.allergies.AllergenType;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.util.CustomViewActions;
import es.usc.citius.servando.calendula.util.CustomViewMatchers;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Created by alvaro.brey.vilas on 06/02/17.
 */

@RunWith(AndroidJUnit4.class)
public class AllergiesTest {

    @Rule
    public final ActivityTestRule<AllergiesActivity> rule =
            new ActivityTestRule<>(AllergiesActivity.class, true, false);


    @Before
    public void setup() throws SQLException {
        // drop database
        DB.dropAndCreateDatabase();
        // create some allergens
        final Patient p = DB.patients().getDefault();
        List<PatientAllergen> pas = new ArrayList<PatientAllergen>() {{
            add(new PatientAllergen("Foo", AllergenType.ACTIVE_INGREDIENT, "FOO", p));
            add(new PatientAllergen("Bar", AllergenType.EXCIPIENT, "BAR", p));
            add(new PatientAllergen("Test", AllergenType.ACTIVE_INGREDIENT, "TEST", p));
        }};
        for (PatientAllergen pa : pas) {
            DB.patientAllergens().create(pa);
        }

        PreferenceUtils.edit().remove(PreferenceKeys.ALLERGIES_WARNING_SHOWN.key()).commit();

        rule.launchActivity(new Intent());
    }

    @Test
    public void testRemoveAllergies() {

        // check db is correct
        assertEquals(3, DB.patientAllergens().count());

        // dismiss warning dialog
        onView(withText(R.string.dialog_continue_option)).perform(click());
        TestUtils.sleep(500);


        // remove last element
        onView(withId(R.id.allergies_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition(2, CustomViewActions.clickChildViewWithId(R.id.delete_button)));
        TestUtils.sleep(100);
        onView(withText(R.string.dialog_yes_option)).perform(click());

        // remove first element
        onView(withId(R.id.allergies_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, CustomViewActions.clickChildViewWithId(R.id.delete_button)));
        TestUtils.sleep(100);
        onView(withText(R.string.dialog_yes_option)).perform(click());

        // check allergens have been deleted from DB and screen
        TestUtils.sleep(100);
        assertEquals(1, DB.patientAllergens().count());
        onView(withId(R.id.allergies_recycler)).check(matches(CustomViewMatchers.withRecyclerViewSize(1)));
    }


}
