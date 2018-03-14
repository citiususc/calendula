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
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;


public class MedicinesActivityEditTest extends ActivityInstrumentationTestCase2<MedicinesActivity> {

    public static final String MEDICINE_NAME = "Aspirin";

    private MedicinesActivity mActivity;

    public MedicinesActivityEditTest() {
        super(MedicinesActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();

        // create medicine
        Medicine created = new Medicine(MEDICINE_NAME, Presentation.EFFERVESCENT);
        created.setPatient(DB.patients().getDefault());
        created.save();

        // set edit intent
        Intent i = new Intent();
        i.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, created.getId());
        setActivityIntent(i);


        mActivity = getActivity();
        TestUtils.unlockScreen(mActivity);
    }

    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }


    @Test
    public void testEditMedicine() {

        assertEquals(1, DB.medicines().count());
        assertEquals(MEDICINE_NAME, DB.medicines().findAll().get(0).getName());

        TestUtils.sleep(1500);
        // select capsules presentation
        onView(withTagValue(new PresentationTagMatcher(Presentation.CAPSULES)))
                .perform(click());
        TestUtils.sleep(200);

        // click save
        onView(withId(R.id.add_button))
                .perform(click());

        // find edited med and do assertions
        Medicine m = DB.medicines().findOneBy(Medicine.COLUMN_NAME, MEDICINE_NAME);
        assertEquals(1, DB.medicines().count());
        assertNotNull(m);
        assertEquals(Presentation.CAPSULES, m.getPresentation());
    }


}