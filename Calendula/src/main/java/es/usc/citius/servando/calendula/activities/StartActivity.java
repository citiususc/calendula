package es.usc.citius.servando.calendula.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.user.User;
import es.usc.citius.servando.calendula.util.Settings;

/**
 * Start activity:
 *
 * - Show splash
 * - Check if session is open and forward user to home screen
 * - If not
 *      - Try to resume session
 *          - If success, forward user to home screen
 *          - Else, redirect to login screen
 *
 */
public class StartActivity extends Activity {

    ImageView brand;
    ImageView splashLogo;

    public static final int ACTION_DEFAULT = 1;
    public static final int ACTION_SHOW_REMINDERS = 2;

    int action = ACTION_DEFAULT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        //Remove title bar

        setContentView(R.layout.activity_start);

        try {
            Settings.instance().load(getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        action = getIntent().getIntExtra("action",ACTION_DEFAULT);

        new UserResumeSessionTask().execute((Void)null);
        startAnimations();
    }

    private void startAnimations() {
        RotateAnimation rotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setRepeatCount(Animation.INFINITE);
        rotateAnim.setDuration(10000);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(1500);

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
        brandFaceIn.setDuration(3000);
        brand.startAnimation(brandFaceIn);
    }

    private void stopAnimations(){
        brand.clearAnimation();
        splashLogo.clearAnimation();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserResumeSessionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                if (Session.getInstance().isOpen()){
                    return true;
                }

                try {
                    // Show splash
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }

                User defaultUser =new User();
                defaultUser.setName("User");
                Session.getInstance().create(getApplicationContext(),defaultUser);
                return true;

                //return Session.getInstance().resume(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override

        protected void onPostExecute(final Boolean success) {
            //stopAnimations();
            if (success) {
                switch (action){
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
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
            }
            finish();
            StartActivity.this.overridePendingTransition(0,0);
        }

        @Override
        protected void onCancelled() {

        }
    }




}
