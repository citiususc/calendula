package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.TestUtils;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


public class MedicinesActivityEditTest extends ActivityInstrumentationTestCase2<MedicinesActivity> {

    public static final String NAME_BEFORE_EDIT = "Aspirin";
    public static final String NAME_AFTER_EDIT = "Paracetamol";

    private MedicinesActivity mActivity;

    public MedicinesActivityEditTest() {
        super(MedicinesActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();

        // create medicine
        Medicine created = new Medicine(NAME_BEFORE_EDIT, Presentation.EFFERVESCENT);
        created.save();

        // set edit intent
        Intent i = new Intent();
        i.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, created.getId());
        setActivityIntent(i);

        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }


    @Test
    public void testEditMedicine() {

        assertEquals(1, DB.medicines().count());
        assertEquals(NAME_BEFORE_EDIT, DB.medicines().findAll().get(0).name());

        // type name
        onView(withId(R.id.medicine_edit_name))
                .perform(clearText())
                .perform(typeText(NAME_AFTER_EDIT));
        // close Soft Keyboard
        TestUtils.closeKeyboard();
        // select capsules presentation
        onView(withId(R.id.med_presentation_2))
                .perform(click());
        // click save
        onView(withId(R.id.add_button))
                .perform(click());

        // find edited med and do assertions
        Medicine m = DB.medicines().findOneBy(Medicine.COLUMN_NAME, NAME_AFTER_EDIT);
        assertEquals(1, DB.medicines().count());
        assertNotNull(m);
        assertEquals(NAME_AFTER_EDIT, m.name());
        assertEquals(Presentation.CAPSULES, m.presentation());
    }


}