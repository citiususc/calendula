package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import es.usc.citius.servando.calendula.DummyDataGenerator;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.store.MedicineStore;
import es.usc.citius.servando.calendula.store.RoutineStore;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.user.User;

/**
 * Start activity:
 * <p/>
 * - Show splash
 * - Check if session is open and forward user to home screen
 * - If not
 * - Try to resume session
 * - If success, forward user to home screen
 * - Else, redirect to login screen
 */
public class StartActivity extends Activity {

    ImageView brand;
    ImageView splashLogo;

    public static final String STATUS_SESSION_OPEN = "STATUS_SESSION_OPEN";
    public static final String STATUS_SESSION_RESUMED = "STATUS_SESSION_RESUMED";
    public static final String STATUS_NO_SESSION = "STATUS_NO_SESSION";


    public static final int ACTION_DEFAULT = 1;
    public static final int ACTION_SHOW_REMINDERS = 2;


    int action = ACTION_DEFAULT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        action = getIntent().getIntExtra("action", ACTION_DEFAULT);
        new UserResumeSessionTask().execute((Void) null);
        startAnimations();
    }

    private void startAnimations() {

        RotateAnimation rotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setRepeatCount(Animation.INFINITE);
        rotateAnim.setFillAfter(true);
        rotateAnim.setDuration(4000);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(rotateAnim);

        // Start animating the image
        splashLogo = (ImageView) findViewById(R.id.splash_logo);
        splashLogo.startAnimation(animation);

        brand = (ImageView) findViewById(R.id.splash_brand);
        Animation brandFaceIn = new AlphaAnimation(0, 1);
        brandFaceIn.setInterpolator(new DecelerateInterpolator());
        brandFaceIn.setStartOffset(500);
        brandFaceIn.setDuration(1000);
        brand.startAnimation(brandFaceIn);
    }

    private void stopAnimations() {
        brand.clearAnimation();
        splashLogo.clearAnimation();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserResumeSessionTask extends AsyncTask<Void, Void, String> {

        private void keepSplashVisible(int seconds){
            // Show splash
            try {
                Thread.sleep(seconds*1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            boolean sessionIsOpen = Session.getInstance().isOpen();
            try {
                // session is open
                if (sessionIsOpen) {
                    return STATUS_SESSION_OPEN;
                }
                // session is closed but there is a session stored
                else if (Session.getInstance().open(getApplicationContext())) {
                    keepSplashVisible(2);
                    return STATUS_SESSION_RESUMED;
                }
                // there is no previous session
                else {
                    keepSplashVisible(3);

                    // Add some default data
                    if (RoutineStore.instance().size() == 0) {
                        DummyDataGenerator.fillRoutineStore(getApplicationContext());
                    }
                    // create default session
                    User defaultUser = new User();
                    defaultUser.setName("Walter");
                    Session.getInstance().create(getApplicationContext(), defaultUser);
                    return STATUS_NO_SESSION;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return STATUS_NO_SESSION;
        }

        @Override
        protected void onPostExecute(final String result) {

            stopAnimations();

            if(STATUS_SESSION_OPEN.equals(result) || STATUS_SESSION_RESUMED.equals(result)){
                switch (action) {
                    case ACTION_SHOW_REMINDERS:
                        Intent i = new Intent(getBaseContext(), ReminderActivity.class);
                        i.putExtra("routine_id", getIntent().getStringExtra("routine_id"));
                        startActivity(i);
                        break;
                    default:
                        startActivity(new Intent(getBaseContext(), HomeActivity.class));
                        break;
                }
            }else{
                // user first time in the app
                Intent welcome = new Intent(getBaseContext(), HomeActivity.class);
                welcome.putExtra("welcome", true);
                startActivity(welcome);
            }




            finish();
            StartActivity.this.overridePendingTransition(0, 0);
        }

        @Override
        protected void onCancelled() {

        }
    }


}
