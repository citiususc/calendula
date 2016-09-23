//package es.usc.citius.servando.calendula;
//
//import android.support.test.InstrumentationRegistry;
//import android.test.ActivityInstrumentationTestCase2;
//
//import org.junit.Before;
//
//import es.usc.citius.servando.calendula.database.DB;
//
//public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {
//
//
//    private HomeActivity mActivity;
//
//    public HomeActivityTest() {
//        super(HomeActivity.class);
//    }
//
//    @Before
//    public void setUp() throws Exception {
//        super.setUp();
//        CalendulaApp.disableReceivers = true;
//        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
//        DB.init(getInstrumentation().getContext());
//        DB.dropAndCreateDatabase();
//        mActivity = getActivity();
//    }
//
//}