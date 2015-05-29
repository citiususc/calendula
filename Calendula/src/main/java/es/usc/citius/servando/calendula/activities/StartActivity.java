package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.user.Session;

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
    TextView quote;
    ImageView splashLogo;

    public static final String STATUS_SESSION_OPEN = "STATUS_SESSION_OPEN";
    public static final String STATUS_SESSION_RESUMED = "STATUS_SESSION_RESUMED";
    public static final String STATUS_NO_SESSION = "STATUS_NO_SESSION";

    public static final int ACTION_DEFAULT = 1;
    public static final int ACTION_SHOW_REMINDERS = 2;

    int action = ACTION_DEFAULT;
    boolean mustShowSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        action = getIntent().getIntExtra("action", ACTION_DEFAULT);
        new UserResumeSessionTask().execute((Void) null);
        mustShowSplash = mustShowSplashForAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().setStatusBarColor(
                getResources().getColor(R.color.activity_background_color));
        }

        if (mustShowSplash)
        {
            startAnimations();
        }
        //        if(PopulatePrescriptionDBService.needUpdate(getApplicationContext())) {
        //            PopulatePrescriptionDBService.updateIfNeeded(getApplicationContext());
        //        }
    }

    private boolean mustShowSplashForAction(int action)
    {
        return true;//!(action == ACTION_DELAY_ROUTINE || action == ACTION_CANCEL_ROUTINE);
    }

    private void startAnimations()
    {

        RotateAnimation rotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
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
        quote = (TextView) findViewById(R.id.splash_quote);
        Animation brandFaceIn = new AlphaAnimation(0, 1);
        brandFaceIn.setInterpolator(new DecelerateInterpolator());
        brandFaceIn.setStartOffset(500);
        brandFaceIn.setDuration(1000);
        brand.startAnimation(brandFaceIn);
        quote.startAnimation(brandFaceIn);
    }

    private void stopAnimations()
    {
        brand.clearAnimation();
        splashLogo.clearAnimation();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserResumeSessionTask extends AsyncTask<Void, Void, String> {

        boolean sessionIsOpen;

        private void keepSplashVisible(int seconds)
        {
            // Show splash
            try
            {
                if (action == ACTION_DEFAULT)
                {
                    Thread.sleep(seconds * 2500);
                }
            } catch (InterruptedException e)
            {
                // do nothing
            }
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            sessionIsOpen = Session.instance().isOpen();
        }

        @Override
        protected String doInBackground(Void... params)
        {

            try
            {
                // session is open
                if (sessionIsOpen)
                {
                    return STATUS_SESSION_OPEN;
                }
                // session is closed but there is a session stored
                else if (Session.instance().open(getApplicationContext()))
                {
                    if (mustShowSplash)
                    {
                        keepSplashVisible(1);
                    }
                    return STATUS_SESSION_RESUMED;
                }
                // there is no previous session
                else
                {
                    if (mustShowSplash)
                    {
                        keepSplashVisible(1);
                    }
                    // create default session
                    //User defaultUser = new User();
                    //defaultUser.setName("Calendula");
                    Session.instance().open(getApplicationContext());
                    return STATUS_NO_SESSION;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            return STATUS_NO_SESSION;
        }

        @Override
        protected void onPostExecute(final String result)
        {

            if (mustShowSplash)
            {
                stopAnimations();
            }

            if (STATUS_SESSION_OPEN.equals(result) || STATUS_SESSION_RESUMED.equals(result))
            {
                Log.d("StartActivity", "Action: " + action);
                switch (action)
                {

                    case ACTION_SHOW_REMINDERS:

                        Intent i = new Intent(getBaseContext(), HomeActivity.class);
                        i.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID,
                            getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1));
                        i.putExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID,
                            getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID,
                                -1));
                        i.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID,
                            getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1));
                        i.putExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID,
                            getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID,
                                -1));
                        i.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME,
                            getIntent().getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME));

                        Log.d("StartActivity", i.toString());

                        ReminderNotification.cancel(StartActivity.this);
                        startActivity(i);
                        break;
                    default:
                        startActivity(new Intent(getBaseContext(), HomeActivity.class));
                        break;
                }
            } else
            {
                // user first time in the app
                Intent welcome = new Intent(getBaseContext(), HomeActivity.class);
                welcome.putExtra("welcome", true);
                startActivity(welcome);
            }

            finish();
            StartActivity.this.overridePendingTransition(0, 0);
        }

        @Override
        protected void onCancelled()
        {

        }
    }

    @Override
    public void onBackPressed()
    {
        // do nothing
    }
}
