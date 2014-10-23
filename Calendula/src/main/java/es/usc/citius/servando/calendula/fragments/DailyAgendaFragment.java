package es.usc.citius.servando.calendula.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.AgendaZoomHelper;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaFragment extends Fragment implements HomeActivity.OnBackPressedListener {

    List<DailyAgendaItemStub> items = new ArrayList<DailyAgendaItemStub>();
    HomeUserInfoFragment userProInfoFragment;

    ArrayAdapter adapter = null;
    ListView listview = null;
    int lastScroll = 0;
    int lastFirstVisibleItem = 0;
    View userInfoFragment;
    boolean profileShown = true;
    boolean expanded = false;
    int profileFragmentHeight = 0;
    private SlideExpandableListAdapter slideAdapter;
    private int profileHeight;

    View zoomContainer;
    AgendaZoomHelper zoomHelper;


    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = new ArrayList<DailyAgendaItemStub>();
        items.addAll(buildItems()); // allow user to change day
    }

    public void showReminder(Routine r) {
        zoomHelper.remind(getActivity(), r);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_daily_agenda, container, false);
        listview = (ListView) rootView.findViewById(R.id.listview);

        inflater.inflate(R.layout.fragment_edit_profile, container, false);


        Log.d(getTag(), "Fragments: " + (getChildFragmentManager().getFragments() != null ? getChildFragmentManager().getFragments().size() : 0));
        userProInfoFragment = HomeUserInfoFragment.newInstance();

        getChildFragmentManager().beginTransaction()
                .replace(R.id.user_info_fragment, userProInfoFragment)
                .commit();

        userInfoFragment = rootView.findViewById(R.id.user_info_fragment);
        zoomContainer = rootView.findViewById(R.id.zoom_container);

        items = buildItems(); // allow user to change day
        adapter = new AgendaItemAdapter(getActivity(), R.layout.daily_view_hour, items);
        slideAdapter = new SlideExpandableListAdapter(adapter, R.id.count_container, R.id.bottom, 1);
        listview.setAdapter(slideAdapter);

        zoomHelper = new AgendaZoomHelper(zoomContainer, new AgendaZoomHelper.ZoomHelperListener() {
            @Override
            public void onChange() {
                items.clear();
                items.addAll(buildItems()); // allow user to change day
                adapter.notifyDataSetChanged();
            }
        });



        listview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (expanded) {
                    listview.computeScroll();
                    int scrollPosition = listview.getScrollY();
                    Log.d(getTag(), "Scroll Y: " + scrollPosition + " firstItem: " + firstVisibleItem);

                    if (firstVisibleItem > 0 && firstVisibleItem > lastFirstVisibleItem) {
                        hideProfile();
                    } else if (firstVisibleItem < lastFirstVisibleItem) {
                        showProfile();
                    }

                    lastFirstVisibleItem = firstVisibleItem;
                    lastScroll = scrollPosition;
                }
            }
        });

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_animation);
        listview.setLayoutAnimation(controller);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(CalendulaApp.PREFERENCES_NAME, 0);
        lastFirstVisibleItem = settings.getInt("agenda_last_visible_item", 0);

        items.clear();
        items.addAll(buildItems()); // allow user to change day
        adapter.notifyDataSetChanged();
    }


    public String getNextRoutineHour() {
        int now = DateTime.now().getHourOfDay();
        for(Routine r : Routine.findAll()){
            if(r.time().getHourOfDay() >= now){
                return r.time().toString("kk:mm");
            }
        }
        return "00:00";
    }


    public int getNextRoutinePosition() {
        int now = DateTime.now().getHourOfDay();
        for (Routine r : Routine.findAll()) {
            if (r.time().getHourOfDay() >= now) {
                return r.time().getHourOfDay();
            }
        }
        return 0;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            Log.d(getTag(), "Visible to user");
            if (profileShown)
                ((HomeActivity) getActivity()).setCustomTitle("Calendula");
            else
                ((HomeActivity) getActivity()).setCustomTitle(Session.instance().getUser().getName());
        }
    }

    public List<DailyAgendaItemStub> buildItems() {

        int now = DateTime.now().getHourOfDay();
        String nextRoutineTime = getNextRoutineHour();
        ArrayList<DailyAgendaItemStub> items = new ArrayList<DailyAgendaItemStub>();
        addSpacerTop(items);
        for (int i = 0; i < 24; i++) {
            List<DailyAgendaItemStub> hourItems = DailyAgendaItemStub.fromHour(i);
            for (DailyAgendaItemStub item : hourItems) {
                if (item.hasEvents && i >= now || expanded) {
                    if (item.time == nextRoutineTime)
                        item.isNext = true;
                    items.add(item);
                }
            }
        }
        return items;
    }

    private void addSpacerTop(ArrayList<DailyAgendaItemStub> items) {
        DailyAgendaItemStub spacer = new DailyAgendaItemStub("");
        spacer.isSpacer = true;
        items.add(spacer);
    }

    public void toggleViewMode() {

        expanded = !expanded;

        if(expanded)
            hideProfile();
        else
            showProfile();

        int nextRoutineHour = getNextRoutinePosition();
        items.clear();
        items.addAll(buildItems());
        listview.setLayoutAnimation(getAnimationController(expanded));
        adapter.notifyDataSetChanged();
        slideAdapter.setLastOpenPosition(expanded ? nextRoutineHour + 1 : 1);
        listview.setSelection(nextRoutineHour+1);
    }



    LayoutAnimationController getAnimationController(boolean expand){
        return AnimationUtils.loadLayoutAnimation(getActivity(), expand ? R.anim.list_animation_expand : R.anim.list_animation);
    }

    private View getAgendaItemView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        final DailyAgendaItemStub item = items.get(position);

        View v;

        if (item.isSpacer) {
            v = layoutInflater.inflate(R.layout.daily_view_spacer, null);
        } else if (!item.hasEvents && expanded) {
            v = layoutInflater.inflate(R.layout.daily_view_empty_hour, null);
            // select the correct layout
            ((TextView) v.findViewById(R.id.hour_text)).setText(item.time);
            v.findViewById(R.id.bottom).setVisibility(View.GONE);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int hour = Integer.valueOf((String) v.getTag());
                    onClickEmptyHour(hour);
                }
            });

        } else {
            v = layoutInflater.inflate(R.layout.daily_view_intake, null);
//            if (!item.hasColors) {
//                int colorIndex = RandomColorChooser.getFixedColorIdx(new Integer(item.hour));
//                item.primaryColor = RandomColorChooser.getPrimaryColor(colorIndex, getResources());
//                item.secondaryColor = RandomColorChooser.getSecondaryColor(colorIndex, getResources());
//                item.hasColors = true;
//            }
            LinearLayout medList = (LinearLayout) v.findViewById(R.id.med_item_list);

            for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {
                View medNameView = layoutInflater.inflate(R.layout.daily_view_intake_med, null);
                ((TextView) medNameView.findViewById(R.id.med_item_name)).setText(element.medName + (element.taken ? " ✔" : ""));
                ((TextView) medNameView.findViewById(R.id.med_item_dose)).setText(element.dose + " " +
                        (element.presentation.units(getResources())) + (element.dose > 1 ? "s" : ""));
                ((ImageView) medNameView.findViewById(R.id.imageView)).setImageResource(element.res);
                medList.addView(medNameView);
            }

            ((TextView) v.findViewById(R.id.routines_list_item_name)).setText(item.title);
            ((TextView) v.findViewById(R.id.routines_list_item_hour)).setText(item.hour + ":");
            ((TextView) v.findViewById(R.id.routines_list_item_minute)).setText(item.minute + "");

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomHelper.show(getActivity(), v, Routine.findById(item.id));
                }
            });
        }
        v.setTag("" + item.hour);
        return v;
    }

    private void onClickEmptyHour(int hour) {
        Toast.makeText(getActivity(), " Add new routine or med here!", Toast.LENGTH_SHORT).show();
    }

    public void hideProfile() {
        ((HomeActivity) getActivity()).setCustomTitle(Session.instance().getUser().getName());
        if (profileShown) {

            profileShown = false;
            final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_up);
            slideUp.setFillAfter(true);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(profileFragmentHeight==0){
                        profileFragmentHeight=userInfoFragment.getRootView().getHeight();
                    }
//                    userInfoFragment.setVisibility(View.GONE);
                    // set 0 height
                    ViewGroup.LayoutParams params = userInfoFragment.getLayoutParams();
                    params.height = 0;
                    userInfoFragment.setLayoutParams(params);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            userInfoFragment.startAnimation(slideUp);
        }
    }

    private void showProfile() {
        ((HomeActivity) getActivity()).setCustomTitle("Calendula");
        if (!profileShown) {
            profileShown = true;
            // reset height
            ViewGroup.LayoutParams params = userInfoFragment.getLayoutParams();
            params.height = profileFragmentHeight;
            userInfoFragment.setLayoutParams(params);
//            userInfoFragment.getLayoutParams().height=
//            userInfoFragment.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_down);
            slideDown.setFillAfter(true);
            userInfoFragment.startAnimation(slideDown);
        }
    }

    void updatePrefs() {
        SharedPreferences settings = getActivity().getSharedPreferences(CalendulaApp.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("agenda_last_visible_item", lastFirstVisibleItem);
        //editor.putBoolean("profile_shown", !profileShown && expanded);
        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePrefs();
    }

    @Override
    public boolean doBack() {
        if (zoomContainer.getVisibility() == View.VISIBLE) {
            zoomHelper.hide();
            return true;
        }
        return false;
    }

    public boolean isExpanded() {
        return expanded;
    }


    private class AgendaItemAdapter extends ArrayAdapter<DailyAgendaItemStub> {

        public AgendaItemAdapter(Context context, int layoutResourceId, List<DailyAgendaItemStub> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getAgendaItemView(position, convertView, parent);
        }
    }

}