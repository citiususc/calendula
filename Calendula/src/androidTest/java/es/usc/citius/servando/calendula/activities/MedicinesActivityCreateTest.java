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

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class MedicinesActivityCreateTest extends ActivityInstrumentationTestCase2<MedicinesActivity> {

    public static final String NAME = "aspirin";

    private MedicinesActivity mActivity;

    public MedicinesActivityCreateTest() {
        super(MedicinesActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();

        // reset preferences
        PreferenceUtils.edit()
                .remove(PreferenceKeys.MEDICINES_USE_PRESCRIPTIONS_SHOWN.key())
                .remove(PreferenceKeys.DRUGDB_ENABLE_DRUGDB.key())
                .remove(PreferenceKeys.DRUGDB_CURRENT_DB.key())
                .remove(PreferenceKeys.DRUGDB_LAST_VALID.key())
                .commit();

        mActivity = getActivity();
        TestUtils.unlockScreen(mActivity);
    }


    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }

    @Test
    public void testCreateMedicine() {

        assertEquals(DB.medicines().count(), 0);


        // dismiss "use prescriptions DB" dialog
        onView(withText(R.string.enable_prescriptions_dialog_no))
                .perform(click());

        // type name
        onView(withId(R.id.search_edit_text))
                .perform(typeText(NAME), ViewActions.closeSoftKeyboard());
        // close Soft Keyboard
        TestUtils.closeKeyboard();

        //click "add custom med"
        onView(withId(R.id.add_custom_med_btn))
                .perform(click());

        // select capsules presentation
        onView(withId(R.id.med_presentation_2))
                .perform(click());
        // click save
        onView(withId(R.id.add_button))
                .perform(click());

        Medicine m = DB.medicines().findOneBy(Medicine.COLUMN_NAME, NAME);
        assertNotNull(m);
        assertEquals(NAME, m.getName());
        assertEquals(Presentation.CAPSULES, m.getPresentation());
    }


}