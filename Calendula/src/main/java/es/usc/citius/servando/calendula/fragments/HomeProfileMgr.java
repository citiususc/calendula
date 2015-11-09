package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.ScreenUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.view.CustomDigitalClock;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class HomeProfileMgr {



    int[] moodRes = new int[]{
            R.drawable.mood_1,
            R.drawable.mood_2,
            R.drawable.mood_3,
            R.drawable.mood_4,
            R.drawable.mood_5,
    };
    int[] moodColor = new int[]{
            R.color.android_red,
            R.color.android_orange,
            R.color.white,
            R.color.android_blue,
            R.color.android_green
    };

    ImageView background;
    View profileImageContainer;
    TextView profileUsername;
    RelativeLayout profileContainer;
    CustomDigitalClock clock;
    TextView monthTv;
    TextView dayTv;
    ImageView moodImg;
    RoundedImageView modFabButton;
    ListAdapter moodsAdapter;
    ImageView bottomShadow;
    String[] moods;
    int currentBgFileIdx = 0;
    private Activity context;
    private View rootView;
    View profileInfo;

    public HomeProfileMgr() {

    }

    public void init(View view, final Activity ctx, final Runnable taskCb){
        this.context = ctx;
        this.rootView = view;

//        Animation in = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
//        Animation out = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
        moods = ctx.getResources().getStringArray(R.array.moods);
        monthTv = (TextView) view.findViewById(R.id.month_text);
        dayTv = (TextView) view.findViewById(R.id.day_text);
        clock = (CustomDigitalClock) view.findViewById(R.id.home_clock);
        bottomShadow = (ImageView) view.findViewById(R.id.bottom_shadow);
        profileInfo = view.findViewById(R.id.profile_info);

        profileUsername = (TextView) view.findViewById(R.id.profile_username);
        profileContainer = (RelativeLayout) view.findViewById(R.id.profile_container);
        profileImageContainer = view.findViewById(R.id.profile_image_container);
        background = (ImageView) view.findViewById(R.id.image_switcher);

        modFabButton = (RoundedImageView) view.findViewById(R.id.mod_circle);
        moodImg = (ImageView) view.findViewById(R.id.mood_button);
        moodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoodsDialog();
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBackground();
            }
        });

        updateModButton();
        updateProfileInfo();

        profileInfo.setVisibility(View.INVISIBLE);
        background.setVisibility(View.INVISIBLE);
        bottomShadow.setVisibility(View.INVISIBLE);

        Picasso.with(context)
            .load("file:///android_asset/" + getBackgroundPath())
            .into(background);

        background.postDelayed(new Runnable() {
            @Override
            public void run() {
                bottomShadow.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
                background.animate().alpha(1).setDuration(200);
            }
        },100);

        background.postDelayed(new Runnable() {
            @Override
            public void run() {
                profileInfo.setVisibility(View.VISIBLE);
                profileInfo.setAlpha(0);
                profileInfo.animate().alpha(1).setDuration(400);
                if(taskCb!=null) {
                    background.post(taskCb);
                }
            }
        },300);

    }


    public void updateModButton() {
        int mood = PreferenceManager.getDefaultSharedPreferences(context).getInt("last_mood", 2);
        int color = moodColor[mood];
        int res = moodRes[mood];
        modFabButton.setImageResource(color);
        moodImg.setImageResource(res);
    }


    public void updateBackground() {
        Picasso.with(context)
                .load("file:///android_asset/" + getRandomBackgroundPath())
                .resize(background.getWidth(),background.getHeight())
                .placeholder(background.getDrawable())
                .centerCrop()
                .into(background);
    }

    void updateProfileInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayName = preferences.getString("display_name", "Calendula");
        profileUsername.setText(displayName);
        DateTime dt = DateTime.now();
        String dayStr = dt.dayOfMonth().getAsShortText();
        String monthStr = dt.monthOfYear().getAsShortText().toUpperCase();
        dayTv.setText(dayStr);
        monthTv.setText(monthStr);
    }


    String getBackgroundPath(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Integer idx = preferences.getInt("profile_background_idx", 1);
        return "home_bg_" + idx + ".jpg";
    }

    Bitmap getBackgroundBitmap() {
        int width = (int) ScreenUtils.getDpSize(context).x;
        int height = context.getResources().getDimensionPixelSize(R.dimen.header_height);
        return ScreenUtils.getResizedBitmap(context, getBackgroundPath(), width, height);
    }

    Bitmap getRandomBackground() {
        int width = (int) ScreenUtils.getDpSize(context).x;
        int height = context.getResources().getDimensionPixelSize(R.dimen.header_height);
        return ScreenUtils.getResizedBitmap(context, getRandomBackgroundPath(), width, height);
    }

    public String getRandomBackgroundPath() {
        int rand = (((int) (Math.random() * 1000)) % 5) + 1;
        if (rand == currentBgFileIdx) {
            rand = ((rand + 1) % 5) + 1;
        }
        currentBgFileIdx = rand;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt("profile_background_idx", rand).commit();
        return "home_bg_" + rand + ".jpg";

    }

    public void showMoodsDialog() {

        moodsAdapter = new MoodListAdapter(context, R.layout.mood_list_item, moods);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setCancelable(true)
                .setTitle(context.getString(R.string.moods_dialog_title))
                .setAdapter(moodsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        Snack.show("Mood saved!", context);
                        preferences.edit().putInt("last_mood", which).commit();
                        updateModButton();
                    }
                }).show();
    }

    public class MoodListAdapter extends ArrayAdapter<String> {


        public MoodListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public MoodListAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.mood_list_item, null);
            }
            int res = moodRes[position];
            int color = moodColor[position];
            v.findViewById(R.id.textView).setBackgroundColor(context.getResources().getColor(color));
            ((ImageView) v.findViewById(R.id.mood_image)).setImageResource(res);
            return v;
        }
    }


}
