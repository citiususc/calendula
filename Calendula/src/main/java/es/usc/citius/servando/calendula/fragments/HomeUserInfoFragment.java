package es.usc.citius.servando.calendula.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.joda.time.DateTime;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.user.User;
import es.usc.citius.servando.calendula.util.Screen;

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

    public HomeUserInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_user_info, container, false);
        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);

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

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateBackground();
    }

    public void updateBackground() {
        background.setImageDrawable(new BitmapDrawable(getBackgroundBitmap()));
    }
    void updateProfileInfo() {

        User u = Session.instance().getUser();
        profileUsername.setText(u.getName());
        //Bitmap profileImage = Session.instance().getUserProfileImage(getActivity());

        DateTime dt = DateTime.now();

        String dayStr = dt.dayOfMonth().getAsShortText();
        String monthStr = dt.monthOfYear().getAsShortText().toUpperCase();

        dayTv.setText(dayStr);
        monthTv.setText(monthStr);
    }

//    public void showEditProfileDialog() {
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        final EditUserProfileFragment editUserProfileFragment = new EditUserProfileFragment();
//        editUserProfileFragment.setOnProfileEditListener(new EditUserProfileFragment.OnProfileEditListener() {
//            @Override
//            public void onProfileEdited(User u) {
//                editUserProfileFragment.dismiss();
//                updateProfileInfo();
//            }
//        });
//        editUserProfileFragment.show(fm, "fragment_edit_profile");
//    }

    public static HomeUserInfoFragment newInstance() {
        return new HomeUserInfoFragment();
    }

    Bitmap getBackgroundBitmap() {

        int width = (int) Screen.getDpSize(getActivity()).x;
        int height = getResources().getDimensionPixelSize(R.dimen.header_height);

        int rand = (((int) (Math.random() * 1000)) % 8) + 1;

        return Screen.getResizedBitmap(getActivity(), "home_bg_" + rand + ".jpg", width, height);
    }

}
