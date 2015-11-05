package es.usc.citius.servando.calendula.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;

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

    public static final int ACTION_DEFAULT = 1;
    public static final int ACTION_SHOW_REMINDERS = 2;
    int action = ACTION_DEFAULT;
    private View reveal;
    private View bg;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        action = getIntent().getIntExtra("action", ACTION_DEFAULT);
        if(action != ACTION_DEFAULT || CalendulaApp.isOpen()){
            CalendulaApp.open(true);
            handleAction();
            overridePendingTransition(0, 0);
            finish();
        }else{
            CalendulaApp.open(true);
            bg = findViewById(R.id.background);
            brand = (ImageView) findViewById(R.id.splash_brand);
            quote = (TextView) findViewById(R.id.splash_quote);
            splashLogo = (ImageView) findViewById(R.id.splash_logo);
            reveal = findViewById(R.id.reveal);
            bg.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.activity_background_color));
            reveal.post(new Runnable() {
                @Override
                public void run() {
                    playInAnimation();
                }
            });
        }
    }


    private void handleAction(){
        switch (action) {
            case ACTION_SHOW_REMINDERS:
                Intent i = new Intent(getBaseContext(), HomeActivity.class);
                i.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID,getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1));
                i.putExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID,getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_ROUTINE_ID, -1));
                i.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID,getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1));
                i.putExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID,getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_DELAY_SCHEDULE_ID, -1));
                i.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, getIntent().getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME));
                ReminderNotification.cancel(StartActivity.this);
                startActivity(i);
                break;
            default:
                showHome();
                break;
        }
    }


    void showHome(){
        Intent intent = new Intent(getBaseContext(), HomeActivity.class);
        startActivity(intent);
    }

    private void playInAnimation(){
        backgroundRevealIn();

        ScaleAnimation scaleAnim = new ScaleAnimation(0.7f,1,0.7f,1,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillEnabled(true);
        scaleAnim.setInterpolator(new AccelerateInterpolator());

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(500);

        RotateAnimation rotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setInterpolator(new DecelerateInterpolator());
        rotateAnim.setDuration(3000);
        rotateAnim.setFillAfter(true);

        AnimationSet inAnimation = new AnimationSet(false);
        inAnimation.addAnimation(fadeIn);
        inAnimation.addAnimation(scaleAnim);
        inAnimation.addAnimation(rotateAnim);
        inAnimation.setStartOffset(0);

        Animation brandFaceIn = new AlphaAnimation(0, 1);
        brandFaceIn.setInterpolator(new DecelerateInterpolator());
        brandFaceIn.setStartOffset(300);
        brandFaceIn.setDuration(500);

        splashLogo.startAnimation(inAnimation);
        brand.startAnimation(brandFaceIn);
        quote.startAnimation(brandFaceIn);

        splashLogo.postDelayed(new Runnable() {
            @Override
            public void run() {
                playOutAnimation();
            }
        }, 2500);
    }


    private void playOutAnimation(){

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(300);

        AnimationSet outAnimation = new AnimationSet(false);
        outAnimation.addAnimation(fadeOut);
        outAnimation.setAnimationListener(new AnimationAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                splashLogo.setVisibility(View.INVISIBLE);
            }
        });

        backgroundRevealOut();
        splashLogo.startAnimation(outAnimation);
    }


    private void backgroundRevealIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reveal.setVisibility(View.INVISIBLE);
            // get the center for the clipping circle
            int cx = bg.getWidth()/2 ;
            int cy = bg.getTop();
            // get the final radius for the clipping circle
            int finalRadius =  (int) Math.hypot(bg.getWidth(), bg.getHeight());
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(reveal, cx, cy, 0, finalRadius);
            anim.setInterpolator(new OvershootInterpolator());
            // make the view visible and start the animation
            reveal.setVisibility(View.VISIBLE);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    bg.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.activity_background_color));
                }
            });
            anim.setDuration(1000).start();

        }
    }

    private void backgroundRevealOut() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = bg.getWidth()/2 ;
            int cy = bg.getTop();
            // get the final radius for the clipping circle
            int finalRadius = (int) Math.hypot(bg.getWidth(), bg.getHeight());
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(reveal, cx, cy, finalRadius, 0);
            anim.setInterpolator(new AccelerateInterpolator());
            // make the view visible and start the animation
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    reveal.setVisibility(View.INVISIBLE);
                    handleAction();
                    overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                    finish();
                }
            });
            reveal.setVisibility(View.VISIBLE);
            anim.setDuration(300).start();

        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }


    private abstract class AnimationAdapter implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
