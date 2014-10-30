package es.usc.citius.servando.calendula.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class HomeUserInfoFragment extends Fragment {


    ImageSwitcher background;
    View profileImageContainer;
    TextView profileUsername;
    RelativeLayout profileContainer;

    public HomeUserInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_user_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);

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

        background.setImageDrawable(getResources().getDrawable(R.drawable.home_bg_day));


        updateBackground(DateTime.now());
        profileUsername = (TextView) view.findViewById(R.id.profile_username);
        updateProfileInfo();

    }

    public void updateBackground(DateTime time) {
        background.setImageResource(getBackgroundResource(time));
    }

    void updateProfileInfo() {

        User u = Session.instance().getUser();
        profileUsername.setText(u.getName());
        Bitmap profileImage = Session.instance().getUserProfileImage(getActivity());
        if (profileImage != null) {
//            profileImageView.setImageBitmap(profileImage);
        }

    }

    public void showEditProfileDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        final EditUserProfileFragment editUserProfileFragment = new EditUserProfileFragment();
        editUserProfileFragment.setOnProfileEditListener(new EditUserProfileFragment.OnProfileEditListener() {
            @Override
            public void onProfileEdited(User u) {
                editUserProfileFragment.dismiss();
                updateProfileInfo();
            }
        });
        editUserProfileFragment.show(fm, "fragment_edit_profile");
    }

    public static HomeUserInfoFragment newInstance() {
        return new HomeUserInfoFragment();
    }

    int getBackgroundResource(DateTime time) {
        int hour = time.getHourOfDay();
        //
        if ((hour >= 7 && hour <= 9) || (hour >= 19 && hour <= 21)) {
            return R.drawable.home_bg_sunset;
//            return R.drawable.home_bg_calendula;
        } else if (hour > 9 && hour < 19) {
//            return R.drawable.home_bg_calendula;
            return R.drawable.home_bg_day;
        } else {
//            return R.drawable.home_bg_calendula;
            return R.drawable.home_bg_1;
        }
    }


}
