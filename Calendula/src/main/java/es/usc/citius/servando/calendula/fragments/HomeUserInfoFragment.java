package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.makeramen.RoundedImageView;

import org.joda.time.DateTime;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.Screen;
import es.usc.citius.servando.calendula.util.Snack;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class HomeUserInfoFragment extends Fragment {


    ImageSwitcher background;
    View profileImageContainer;
    TextView profileUsername;
    RelativeLayout profileContainer;

    TextView monthTv;
    TextView dayTv;

    ImageView moodImg;
    RoundedImageView modFabButton;
    ListAdapter moodsAdapter;
    String[] moods;

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

    int currentBgFileIdx = 0;


    public HomeUserInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_user_info, container, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        moods = getResources().getStringArray(R.array.moods);
        monthTv = (TextView) view.findViewById(R.id.month_text);
        dayTv = (TextView) view.findViewById(R.id.day_text);

        profileUsername = (TextView) view.findViewById(R.id.profile_username);
        profileContainer = (RelativeLayout) view.findViewById(R.id.profile_container);
        profileImageContainer = view.findViewById(R.id.profile_image_container);
        background = (ImageSwitcher) view.findViewById(R.id.image_switcher);
        background.setInAnimation(in);
        background.setOutAnimation(out);

        background.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView myView = new ImageView(getActivity());
                myView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(
                                ViewGroup.LayoutParams.FILL_PARENT,
                                ViewGroup.LayoutParams.FILL_PARENT)
                );
                return myView;
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBackground();
            }
        });
        updateProfileInfo();

        modFabButton = (RoundedImageView) view.findViewById(R.id.mod_circle);
        moodImg = (ImageView) view.findViewById(R.id.mood_button);
        moodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoodsDialog();
            }
        });

        updateModButton();

        return view;
    }

    public void updateModButton() {
        int mood = mSharedPreferences.getInt("last_mood", 2);
        int color = moodColor[mood];
        int res = moodRes[mood];
        modFabButton.setImageResource(color);
        moodImg.setImageResource(res);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        background.setImageDrawable(new BitmapDrawable(getBackgroundBitmap()));
    }

    public void updateBackground() {
        background.setImageDrawable(new BitmapDrawable(getRandomBackground()));
    }

    void updateProfileInfo() {

        String displayName = mSharedPreferences.getString("display_name", "Calendula");
        profileUsername.setText(displayName);


        DateTime dt = DateTime.now();

        String dayStr = dt.dayOfMonth().getAsShortText();
        String monthStr = dt.monthOfYear().getAsShortText().toUpperCase();

        dayTv.setText(dayStr);
        monthTv.setText(monthStr);
    }

    public static HomeUserInfoFragment newInstance() {
        return new HomeUserInfoFragment();
    }

    Bitmap getBackgroundBitmap() {       
        int width = (int) Screen.getDpSize(getActivity()).x;
        int height = getResources().getDimensionPixelSize(R.dimen.header_height);
        Integer idx = mSharedPreferences.getInt("profile_background_idx", 1);
        return Screen.getResizedBitmap(getActivity(), "home_bg_" + idx + ".jpg", width, height);
    }


    Bitmap getRandomBackground() {
        int width = (int) Screen.getDpSize(getActivity()).x;
        int height = getResources().getDimensionPixelSize(R.dimen.header_height);
        int rand = (((int) (Math.random() * 1000)) % 5) + 1;
        if (rand == currentBgFileIdx) {
            rand = ((rand + 1) % 5) + 1;
        }
        currentBgFileIdx = rand;

        mSharedPreferences.edit().putInt("profile_background_idx", rand).commit();
        
        return Screen.getResizedBitmap(getActivity(), "home_bg_" + rand + ".jpg", width, height);
    }


    public SharedPreferences mSharedPreferences;

    // Listener defined by anonymous inner class.
    public SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("display_name".equals(key) && profileUsername != null) {
                profileUsername.setText(mSharedPreferences.getString("display_name", "Calendula"));
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    }


    public void showMoodsDialog() {

        moodsAdapter = new MoodListAdapter(getActivity(), R.layout.mood_list_item, moods);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setCancelable(true)
                .setTitle(getString(R.string.moods_dialog_title))
                .setAdapter(moodsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Snack.show("Mood saved!", getActivity());
                        mSharedPreferences.edit().putInt("last_mood", which).commit();
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
            v.findViewById(R.id.textView).setBackgroundColor(getResources().getColor(color));
            ((ImageView) v.findViewById(R.id.mood_image)).setImageResource(res);
            return v;
        }
    }


}
