package es.usc.citius.servando.calendula.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.Target;

import java.util.HashMap;

import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 2/4/15.
 */
public class AppTutorial {

    public static final String WELCOME = "WELCOME";
    public static final String HOME_INFO = "HOME_INFO";
    public static final String ROUTINES_INFO = "ROUTINES_INFO";
    public static final String MEDICINES_INFO = "MEDICINES_INFO";
    public static final String SCHEDULES_INFO = "SCHEDULES_INFO";
    private static final String TAG = "AppTutorial";
    ShowcaseView showcaseView;
    HashMap<String, ShowcaseInfo> stages = new HashMap<String, ShowcaseInfo>();


    public void show(final String stage, Activity activity) {

        Log.d(TAG, "Showing stage " + stage);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        final String preference_key = "tutorial_" + stage + "_shown";
        boolean stageShown = prefs.getBoolean(preference_key, false);

        if (!stageShown && stages.containsKey(stage)) {

            if (showcaseView != null) {
                showcaseView.setVisibility(View.GONE);
                showcaseView = null;
            }


            Log.d(TAG, "Creating showcase");

            final ShowcaseInfo info = stages.get(stage);

            showcaseView = new ShowcaseView.Builder(activity)
                    .setTarget(info.target != null ? info.target : Target.NONE)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(info.title)
                    .setContentText(info.detail)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            prefs.edit().putBoolean(preference_key, true).commit();
                            if (info.listener != null) {
                                Log.d(TAG, "Call listener onHide");
                                info.listener.onHide();
                            }
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }
                    })
                    .build();
            showcaseView.show();
        } else {
            Log.d("AppTutorial", "Stage '" + stage + "' already shown!");
        }

    }


    public void show(final String firstStage, final String secondStage, final Activity activity) {

        Log.d(TAG, "Showing stages " + firstStage + " and " + secondStage);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        final String preference_key = "tutorial_" + firstStage + "_shown";
        boolean stageShown = prefs.getBoolean(preference_key, false);

        if (!stageShown && stages.containsKey(firstStage)) {

            if (showcaseView != null) {
                showcaseView.setVisibility(View.GONE);
                showcaseView = null;
            }


            final ShowcaseInfo info = stages.get(firstStage);

            showcaseView = new ShowcaseView.Builder(activity)
                    .setTarget(info.target != null ? info.target : Target.NONE)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .setContentTitle(info.title)
                    .setContentText(info.detail)
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            prefs.edit().putBoolean(preference_key, true).commit();
                            show(secondStage, activity);
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }
                    })
                    .build();
            showcaseView.show();
        } else {
            Log.d("AppTutorial", "Stage '" + secondStage + "' already shown!");
        }

    }

    public void reset(Activity activity) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("tutorial_" + WELCOME + "_shown", false);
        editor.putBoolean("tutorial_" + HOME_INFO + "_shown", false);
        editor.putBoolean("tutorial_" + ROUTINES_INFO + "_shown", false);
        editor.putBoolean("tutorial_" + MEDICINES_INFO + "_shown", false);
        editor.putBoolean("tutorial_" + SCHEDULES_INFO + "_shown", false);
        editor.commit();
    }


    public void hide() {
        if (showcaseView != null) {
            showcaseView.hide();
        }
    }

    public void init(final Activity activity, PagerSlidingTabStrip tabs) {


        PointF size = Screen.getDpSize(activity);
        int width = (int) (size.x * Screen.getDensity(activity));
        int gap = width / 8;
        int pointY = gap * 2;

        PointTarget pth = new PointTarget(new Point(width / 2, width / 3));
        PointTarget ptr = new PointTarget(new Point(gap * 3, pointY));
        PointTarget ptm = new PointTarget(new Point(gap * 5, pointY));
        PointTarget pts = new PointTarget(new Point(gap * 7, pointY));

        Log.d(TAG, "Point " + pointY + ", " + tabs.getScrollOffset() + ", " + tabs.getChildAt(0).getId());

        stages.put(WELCOME, new ShowcaseInfo(R.string.tutorial_welcome_title, R.string.tutorial_welcome_text, pth));
        stages.put(HOME_INFO, new ShowcaseInfo(R.string.tutorial_homeinfo_title, R.string.tutorial_homeinfo_text, new PointTarget(new Point(0, 0)), new ShowcaseListener() {
            @Override
            public void onHide() {
                Log.d(TAG, "Called listener onHide");
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).onNavigationItemSelected(1, 0);
                }
            }
        }));
        stages.put(ROUTINES_INFO, new ShowcaseInfo(R.string.tutorial_routine_title, R.string.tutorial_routine_text, ptr, new ShowcaseListener() {
            @Override
            public void onHide() {
                Log.d(TAG, "Called listener onHide");
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).onNavigationItemSelected(2, 0);
                }
            }
        }));
        stages.put(MEDICINES_INFO, new ShowcaseInfo(R.string.tutorial_meds_title, R.string.tutorial_meds_text, ptm, new ShowcaseListener() {
            @Override
            public void onHide() {
                Log.d(TAG, "Called listener onHide");
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).onNavigationItemSelected(3, 0);
                }
            }
        }));
        stages.put(SCHEDULES_INFO, new ShowcaseInfo(R.string.tutorial_schedule_title, R.string.tutorial_schedule_text, pts));

    }


    private interface ShowcaseListener {
        void onHide();
    }


    private class ShowcaseInfo {
        public int title;
        public int detail;
        public Target target;
        public ShowcaseListener listener;

        private ShowcaseInfo(int title, int detail, Target target) {
            this.title = title;
            this.detail = detail;
            this.target = target;
        }

        private ShowcaseInfo(int title, int detail, Target target, ShowcaseListener l) {
            this.title = title;
            this.detail = detail;
            this.target = target;
            this.listener = l;
        }


    }


}
