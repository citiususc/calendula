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
    }

    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }


    @Test
    public void testEditMedicine() {

        assertEquals(1, DB.medicines().count());
        assertEquals(MEDICINE_NAME, DB.medicines().findAll().get(0).name());

        TestUtils.sleep(200);
        // select capsules presentation
        onView(withId(R.id.med_presentation_2))
                .perform(click());
        TestUtils.sleep(200);

        // click save
        onView(withId(R.id.add_button))
                .perform(click());

        // find edited med and do assertions
        Medicine m = DB.medicines().findOneBy(Medicine.COLUMN_NAME, MEDICINE_NAME);
        assertEquals(1, DB.medicines().count());
        assertNotNull(m);
        assertEquals(Presentation.CAPSULES, m.presentation());
    }


}