package es.usc.citius.servando.calendula.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.user.User;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class HomeUserInfoFragment extends Fragment {


    ImageView profileImageView;
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
        profileContainer = (RelativeLayout) view.findViewById(R.id.profile_container);
        profileImageContainer = view.findViewById(R.id.profile_image_container);
        profileImageView = (ImageView) view.findViewById(R.id.profile_image);
        profileUsername = (TextView) view.findViewById(R.id.profile_username);
        updateProfileInfo();
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });
    }

    void updateProfileInfo() {

        User u = Session.instance().getUser();
        profileUsername.setText(u.getName());
        Bitmap profileImage = Session.instance().getUserProfileImage(getActivity());
        if (profileImage != null) {
            profileImageView.setImageBitmap(profileImage);
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
}
