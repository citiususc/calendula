/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.makeramen.RoundedImageView;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.Snack;
import pl.droidsonroids.gif.GifDrawable;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class HomeProfileMgr {


    public static final int BG_COUNT = 8;
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
    //CustomDigitalClock clock;
    TextView monthTv;
    TextView dayTv;
    ImageView moodImg;
    View blurMask;
    RoundedImageView modFabButton;
    ListAdapter moodsAdapter;
    ImageView bottomShadow;
    String[] moods;
    int currentBgFileIdx = 0;
    private Activity context;
    private View rootView;
    View profileInfo;
    View santaContainer;
    ImageView santaImv;
    boolean santaMode = false;
    ImageButton santaButton;
    SharedPreferences preferences;
    Toast t;
    // "santa1.gif", "santa2.gif",
    final List<String> images = Arrays.asList("house_smoke.gif", "santa3.gif", "santa4.gif", "santa5.gif", "christmas_tree.gif");

    public HomeProfileMgr() {

    }

    public void init(View view, final Activity ctx){
        this.context = ctx;
        this.rootView = view;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

//        Animation in = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
//        Animation out = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
        moods = ctx.getResources().getStringArray(R.array.moods);
        monthTv = (TextView) view.findViewById(R.id.month_text);
        dayTv = (TextView) view.findViewById(R.id.day_text);
        //clock = (CustomDigitalClock) view.findViewById(R.id.home_clock);
        bottomShadow = (ImageView) view.findViewById(R.id.bottom_shadow);
        profileInfo = view.findViewById(R.id.profile_info);
        blurMask = view.findViewById(R.id.blur_mask);
        santaContainer = view.findViewById(R.id.santa_container);
        santaImv = (ImageView) view.findViewById(R.id.image_santa);
        santaButton = (ImageButton) view.findViewById(R.id.santa_mode_button);
        santaButton.setImageDrawable(new IconicsDrawable(ctx, CommunityMaterial.Icon.cmd_pine_tree)
                .sizeDp(36)
                .paddingDp(5)
                .colorRes(R.color.white));

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

        boolean disableChristmasMode = preferences.getBoolean("disable_christmas_mode", false);
        LocalDate today = LocalDate.now();
        if(!disableChristmasMode && today.getMonthOfYear() == 12 && (today.getDayOfMonth() >= 23 && today.getDayOfMonth() <= 31)){
            try {
                int current = preferences.getInt("current_santa_image",0);
                santaImv.setImageDrawable(new GifDrawable(ctx.getAssets(), images.get(current)));
                santaContainer.setVisibility(View.VISIBLE);
                blurMask.setAlpha(0.5f);
                santaButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(t != null){
                            t.cancel();
                        }
                        t = Toast.makeText(context, R.string.christmas_remove_message, Toast.LENGTH_SHORT);
                        t.show();
                        rotateChristmasImage();
                    }
                });
                santaButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        santaContainer.setVisibility(View.GONE);
                        preferences.edit().putBoolean("disable_christmas_mode", true).apply();
                        t = Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT);
                        t.show();
                        return true;
                    }
                });
                if(!preferences.getBoolean("christmas_message_shown_2016",false)){
                    showChristmasDialog();
                    preferences.edit().putBoolean("christmas_message_shown_2016",true).apply();
                }
                santaMode = true;
            }catch (Exception e){
                santaContainer.setVisibility(View.GONE);
            }
        }else{
            santaContainer.setVisibility(View.GONE);
        }

        Picasso.with(context)
            .load("file:///android_asset/" + getBackgroundPath(ctx))
            .into(background);

        background.post(new Runnable() {
            @Override
            public void run() {
                bottomShadow.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
                background.animate().alpha(1).setDuration(200);
            }
        });

        background.postDelayed(new Runnable() {
            @Override
            public void run() {
                profileInfo.setVisibility(View.VISIBLE);
                profileInfo.setAlpha(0);
                profileInfo.animate().alpha(santaMode ? 0.5f : 1).setDuration(400);
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


    private void showChristmasDialog(){

        new MaterialStyledDialog.Builder(context)
                .setTitle(R.string.christmas_dialog_title)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(context, CommunityMaterial.Icon.cmd_pine_tree, R.color.white, 48))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(false)
                .setDescription(R.string.christmas_dialog_description)
                .setPositiveText(R.string.christmas_dialog_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void rotateChristmasImage() {
        try{
            int current = preferences.getInt("current_santa_image",0);
            int next = (current+1)%images.size();
            santaImv.setImageDrawable(new GifDrawable(context.getAssets(), images.get(next)));
            preferences.edit().putInt("current_santa_image",next).apply();
        }catch (Exception e){}
    }


    public void updateBackground() {
        Picasso.with(context)
            .load("file:///android_asset/" + getRandomBackgroundPath())
            .centerCrop()
            .resize(background.getWidth(), background.getHeight())
            .placeholder(background.getDrawable())
            .into(background);

        CalendulaApp.eventBus().post(new BackgroundUpdatedEvent());
    }

    void updateProfileInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayName = preferences.getString("display_name", "Calendula");
        profileUsername.setText(displayName);
        updateDate();
    }

    public void updateDate() {
        DateTime dt = DateTime.now();
        String dayStr = dt.dayOfMonth().getAsShortText();
        String monthStr = dt.monthOfYear().getAsShortText().toUpperCase();
        dayTv.setText(dayStr);
        monthTv.setText(monthStr);
    }


    static String getBackgroundPath(Context ctx){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        Integer idx = preferences.getInt("profile_background_idx", 1);
        return "home_bg_" + idx + ".jpg";
    }

//    Bitmap getBackgroundBitmap() {
//        int width = (int) ScreenUtils.getDpSize(context).x;
//        int height = context.getResources().getDimensionPixelSize(R.dimen.header_height);
//        return ScreenUtils.getResizedBitmap(context, getBackgroundPath(), width, height);
//    }

//    Bitmap getRandomBackground() {
//        int width = (int) ScreenUtils.getDpSize(context).x;
//        int height = context.getResources().getDimensionPixelSize(R.dimen.header_height);
//        return ScreenUtils.getResizedBitmap(context, getRandomBackgroundPath(), width, height);
//    }

    public String getRandomBackgroundPath() {
        int rand = (((int) (Math.random() * 1000)) % BG_COUNT) + 1;
        if (rand == currentBgFileIdx) {
            rand = ((rand + 1) % BG_COUNT) + 1;
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

    public void onCollapse() {
        profileInfo.animate().alpha(0);
    }

    public void onExpand() {
        profileInfo.animate().alpha(1);
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


    private static HashMap<String, Integer> cache = new HashMap<>();

    public static int colorForCurrent(Context ctx){

        String path = getBackgroundPath(ctx);
        int color = Color.BLACK;
        if(!cache.containsKey(path)) {
            Bitmap bm = getBitmapFromAsset(ctx, path);
            if(bm != null) {
                Palette p = Palette.generate(bm);
                color = p.getVibrantColor(color);
            }
            cache.put(path, color);
        }
        return cache.get(path);
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }


    public class BackgroundUpdatedEvent {}
}
