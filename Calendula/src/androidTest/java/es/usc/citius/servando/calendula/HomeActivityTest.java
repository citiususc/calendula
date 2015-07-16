package es.usc.citius.servando.calendula;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {


    private HomeActivity mActivity;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();
        mActivity = getActivity();
    }

    @Test
    public void testOpenCloseSettings() {
        // open nav drawer
        onView(withContentDescription("Abrir menu")).perform(click());
        TestUtils.sleep(1500);
        // scroll bottom
        onView(withId(R.id.left_drawer)).perform(swipeUp());
        // click settings option
        onView(withId(R.id.text_settings)).perform(click());
        TestUtils.sleep(1000);
        // check header is visible
        onView(withText(R.string.pref_header_general)).check(matches(isDisplayed()));
        // press back and check we are in home
        Espresso.pressBack();
        onView(withId(R.id.profile_username)).check(matches(isDisplayed()));
    }

    @Test
    public void testExpandAgenda() {
        onView(withId(R.id.action_expand)).perform(click());
        TestUtils.sleep(500);
        onView(withId(R.id.action_expand)).perform(click());
        TestUtils.sleep(500);
        onView(withId(R.id.daily_agenda_fragment_root)).perform(swipeUp());
        TestUtils.sleep(500);
        onView(withId(R.id.add_button)).check(matches(not(isDisplayed())));
        TestUtils.sleep(500);
        onView(withId(R.id.daily_agenda_fragment_root)).perform(swipeDown());
        TestUtils.sleep(500);
    }

}