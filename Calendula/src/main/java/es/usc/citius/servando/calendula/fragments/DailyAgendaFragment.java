package es.usc.citius.servando.calendula.fragments;


import android.content.Context;
import android.content.Intent;
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

import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.AgendaDetailActivity;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.user.Session;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.RandomColorChooser;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaFragment extends Fragment {

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
        items = buildItems(); // allow user to change day
        adapter = new AgendaItemAdapter(getActivity(), R.layout.daily_view_hour, items);
        listview.setAdapter(new SlideExpandableListAdapter(adapter, R.id.count_container, R.id.bottom, 1));
        profileFragmentHeight = userInfoFragment.getHeight();


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

    private void launchActivity(Class activityCls) {
        Intent intent = new Intent(getActivity(), activityCls);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        items.clear();
        items.addAll(buildItems()); // allow user to change day
        int currentHour = DateTime.now().getHourOfDay();
        adapter.notifyDataSetChanged();
        // Restore preferences
        SharedPreferences settings = getActivity().getSharedPreferences(CalendulaApp.PREFERENCES_NAME, 0);
        profileShown = settings.getBoolean("profile_shown", true);
        lastFirstVisibleItem = settings.getInt("agenda_last_visible_item", 0);
        int scrollY = settings.getInt("scroll_position_y", 0);
        listview.scrollTo(0, scrollY);
        userInfoFragment.setVisibility(profileShown ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (profileShown)
                ((HomeActivity) getActivity()).setCustomTitle("Calendula");
            else
                ((HomeActivity) getActivity()).setCustomTitle(Session.instance().getUser().getName());
        }
    }

    public List<DailyAgendaItemStub> buildItems() {

        int now = DateTime.now().getHourOfDay();
        ArrayList<DailyAgendaItemStub> items = new ArrayList<DailyAgendaItemStub>();

        addSpacerTop(items);
        boolean isNext = true;

        for (int i = 0; i < 24; i++) {
            DailyAgendaItemStub item = DailyAgendaItemStub.fromRoutine(i);
            if (item.hasEvents && i >= now || expanded) {
                if (expanded && isNext) {
                    item.isNext = true;
                    isNext = false;
                }
                items.add(item);
            }
        }
        return items;
    }

    private void addSpacerTop(ArrayList<DailyAgendaItemStub> items) {
        DailyAgendaItemStub spacer = new DailyAgendaItemStub(0);
        spacer.isSpacer = true;
        items.add(spacer);
    }

    public void toogleExpand() {

        expanded = !expanded;

        if (!expanded) {
            showProfile();
            ((HomeActivity) getActivity()).hideAddButton(true);
        } else {

            ((HomeActivity) getActivity()).showAddButton(true);
        }

//        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_animation);
//        listview.setLayoutAnimation(controller);
//        listview.getLayoutAnimation().start();

        items.clear();
        items.addAll(buildItems());
        adapter.notifyDataSetChanged();

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(),
                expanded ? R.anim.list_animation_expand : R.anim.list_animation);
        listview.setLayoutAnimation(controller);
        listview.getLayoutAnimation().start();


//        if(!expanded) {
//            int index = getNextIndexToRemove();
//            if(index>=0){
//                removeListItemRecursive(index);
//            }
//        }
    }

    public int getNextIndexToRemove() {
        int removeIndex = -1;
        View v;
        for (int i = 0; i < items.size(); i++) {
            DailyAgendaItemStub item = items.get(i);

            if (!item.isSpacer && !item.hasEvents) {
                v = listview.getChildAt(i);
                if (v != null) {
                    removeIndex = i;
                    break;
                }
            }
        }
        return removeIndex;
    }

    void removeListItemRecursive(int removeIndex) {

        if (removeIndex >= 0) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
            anim.setDuration(100);
            View v = listview.getChildAt(removeIndex);
            final int toRemove = removeIndex;
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    items.remove(toRemove);
                    adapter.clear();
                    for (DailyAgendaItemStub is : items) {
                        adapter.add(is);
                    }
                    adapter.notifyDataSetChanged();
                    int next = getNextIndexToRemove();
                    if (next >= 0)

                        removeListItemRecursive(next);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            v.startAnimation(anim);
        }
    }

    private View getExpandedView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        final int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
        int minute = new GregorianCalendar().get(Calendar.MINUTE);

        final DailyAgendaItemStub item = items.get(position);


        View v;

        if (!item.isSpacer) {
            if (!item.hasColors) {
                int colorIndex = RandomColorChooser.getFixedColorIdx(new Integer(item.hour));
                item.primaryColor = RandomColorChooser.getPrimaryColor(colorIndex, getResources());
                item.secondaryColor = RandomColorChooser.getSecondaryColor(colorIndex, getResources());
                item.hasColors = true;
            }
            v = layoutInflater.inflate(R.layout.daily_view_hour, null);
            // select the correct layout
            if (!item.hasEvents) {
                v.findViewById(R.id.hide_if_empty).setVisibility(View.GONE);
                v.findViewById(R.id.current_hour_spacer).setVisibility(View.VISIBLE);
            } else {
                LinearLayout medList = (LinearLayout) v.findViewById(R.id.med_item_list);
                boolean isFirst = true;
                for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {

                    View medNameView = layoutInflater.inflate(R.layout.daily_agenda_item_med, null);
                    ((TextView) medNameView.findViewById(R.id.med_item_name)).setText(element.medName + (element.taken ? " ✔" : ""));
                    if (isFirst) {
                        ((TextView) medNameView.findViewById(R.id.bottom_current_hour_text)).setText(String.valueOf((item.hour < 10 ? ("0" + item.hour) : item.hour)));
                        isFirst = false;
                    } else {
                        medNameView.findViewById(R.id.bottom_current_hour_text).setVisibility(View.INVISIBLE);
                    }
                    // change colors
                    medNameView.setBackgroundColor(item.primaryColor);
                    ((TextView) medNameView.findViewById(R.id.bottom_current_hour_text)).setTextColor(item.primaryColor);
                    ((TextView) medNameView.findViewById(R.id.bottom_current_minute_text)).setTextColor(item.secondaryColor);

                    ((TextView) medNameView.findViewById(R.id.bottom_current_minute_text)).setText(element.minute);
                    medList.addView(medNameView);
                }

                View moreView = layoutInflater.inflate(R.layout.daily_agenda_item_more, null);
                moreView.setBackgroundColor(item.primaryColor);
                moreView.findViewById(R.id.more_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Intent intent = new Intent(getActivity(), AgendaDetailActivity.class);
                        intent.putExtra("hour", item.hour);
                        startActivity(intent);
                        getActivity().overridePendingTransition(0, 0);
                    }
                });
                medList.addView(moreView);
                // set number of meds to take
                ((TextView) v.findViewById(R.id.count_text)).setText(String.valueOf(item.meds.size()));

            }

            // enable hour indicator
            if (hour == item.hour) {
                v.findViewById(R.id.hour_text).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.current_hour_indicator).setVisibility(View.VISIBLE);
                TextView hText = (TextView) v.findViewById(R.id.current_hour_text);
                TextView mText = (TextView) v.findViewById(R.id.current_minute_text);
                hText.setText(String.valueOf(hour));
                mText.setText(":" + String.valueOf(minute));
                hText.setVisibility(View.VISIBLE);
                mText.setVisibility(View.VISIBLE);
                v.findViewById(R.id.current_minute_text).setVisibility(View.VISIBLE);
            }

            // change background color
            v.findViewById(R.id.agenda_item_top_bg).setBackgroundColor(item.primaryColor);
            v.findViewById(R.id.item_spacer_top).setBackgroundColor(item.primaryColor);
            v.findViewById(R.id.item_spacer_bottom).setBackgroundColor(item.primaryColor);
            // set hour label
            ((TextView) v.findViewById(R.id.hour_text)).setText(String.valueOf((item.hour > 9 ? item.hour : "0" + item.hour) + ":00"));
        } else {
            v = layoutInflater.inflate(R.layout.daily_view_spacer, null);
        }
        return v;
    }

    private View getSummaryView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        final DailyAgendaItemStub item = items.get(position);

        View v;

        if (item.isSpacer) {
            v = layoutInflater.inflate(R.layout.daily_view_spacer, null);
        } else if (!item.hasEvents && expanded) {
            v = layoutInflater.inflate(R.layout.daily_view_empty_hour, null);
            // select the correct layout
            ((TextView) v.findViewById(R.id.hour_text)).setText(String.valueOf((item.hour > 9 ? item.hour : "0" + item.hour) + ":00"));
            v.findViewById(R.id.bottom).setVisibility(View.GONE);

        } else {
            v = layoutInflater.inflate(R.layout.daily_view_intake, null);
            if (!item.hasColors) {
                int colorIndex = RandomColorChooser.getFixedColorIdx(new Integer(item.hour));
                item.primaryColor = RandomColorChooser.getPrimaryColor(colorIndex, getResources());
                item.secondaryColor = RandomColorChooser.getSecondaryColor(colorIndex, getResources());
                item.hasColors = true;
            }
            LinearLayout medList = (LinearLayout) v.findViewById(R.id.med_item_list);

            for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {
                View medNameView = layoutInflater.inflate(R.layout.daily_view_intake_med, null);
                ((TextView) medNameView.findViewById(R.id.med_item_name)).setText(element.medName + (element.taken ? " ✔" : ""));
                ((TextView) medNameView.findViewById(R.id.med_item_dose)).setText(element.dose + " " +
                        (element.presentation.units(getResources())) + (element.dose > 1 ? "s" : ""));
                ((ImageView) medNameView.findViewById(R.id.imageView)).setImageResource(element.res);
                medList.addView(medNameView);
            }
            List<Routine> rs = Routine.findInHour(item.hour);
            String title = rs.size() > 0 ? rs.get(0).name() + " (" + item.meds.size() + " MEDS)" : "";

            ((TextView) v.findViewById(R.id.routines_list_item_name)).setText(title);
            ((TextView) v.findViewById(R.id.routines_list_item_hour)).setText(item.hour + ":");
            ((TextView) v.findViewById(R.id.routines_list_item_minute)).setText("00");

            v.findViewById(R.id.routine_list_item_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View arrow = v.findViewById(R.id.count_container);
                    arrow.performClick();

//                    final int from = arrow.getRotation()==0?0:180;
//                    final int to = arrow.getRotation()==0?180:0;
//                    RotateAnimation anim = new RotateAnimation(from,to,0.5f,0.5f);
//                    anim.setInterpolator(getActivity(),android.R.anim.bounce_interpolator);
//                    anim.setAnimationListener(new Animation.AnimationListener() {
//                        @Override
//                        public void onAnimationStart(Animation animation) {
//
//                        }
//                        @Override
//                        public void onAnimationEnd(Animation animation) {
//                            arrow.setRotation(to);
//                        }
//                        @Override
//                        public void onAnimationRepeat(Animation animation) {
//
//                        }
//                    });
//                    arrow.startAnimation(anim);

                }
            });

        }
        return v;
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
                    userInfoFragment.setVisibility(View.GONE);
                    userInfoFragment.setTranslationY(-userInfoFragment.getTranslationY());
                    // translate
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            userInfoFragment.startAnimation(slideUp);
            ((HomeActivity) getActivity()).hideAddButton(false);
        }
    }

    public void showProfile() {
        ((HomeActivity) getActivity()).setCustomTitle("Calendula");
        if (!profileShown) {
            profileShown = true;
            userInfoFragment.setVisibility(View.VISIBLE);
            Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_slide_down);
            slideDown.setFillAfter(true);
            userInfoFragment.setTranslationY(0);
            userInfoFragment.startAnimation(slideDown);
        }
    }

    void updatePrefs() {
        SharedPreferences settings = getActivity().getSharedPreferences(CalendulaApp.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("profile_shown", profileShown);
        editor.putInt("scroll_position_y", listview.getScrollY());
        editor.putInt("agenda_last_visible_item", lastFirstVisibleItem);
        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        updatePrefs();
    }

    private class AgendaItemAdapter extends ArrayAdapter<DailyAgendaItemStub> {

        public AgendaItemAdapter(Context context, int layoutResourceId, List<DailyAgendaItemStub> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return //expanded ? getExpandedView(position, convertView, parent) :
                    getSummaryView(position, convertView, parent);
        }

    }

}