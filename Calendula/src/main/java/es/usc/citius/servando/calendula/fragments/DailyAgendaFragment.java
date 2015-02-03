package es.usc.citius.servando.calendula.fragments;


import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.HomeActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.AgendaZoomHelper;
import es.usc.citius.servando.calendula.persistence.Routine;
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
    View userInfoFragment;
    boolean profileShown = true;
    boolean expanded = false;
    int profileFragmentHeight = 0;
    View zoomContainer;
    AgendaZoomHelper zoomHelper;
    private SlideExpandableListAdapter slideAdapter;
    private int toolbarHeight;
    private int lastVisibleItemCount;
    private Dictionary<Integer, Integer> listViewItemHeights = new Hashtable<Integer, Integer>();

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
        profileFragmentHeight = (int) getResources().getDimension(R.dimen.header_height);
        toolbarHeight = (int) getResources().getDimension(R.dimen.action_bar_height);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.user_info_fragment, userProInfoFragment)
                .commit();

        userInfoFragment = rootView.findViewById(R.id.user_info_fragment);
        zoomContainer = rootView.findViewById(R.id.zoom_container);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                items = buildItems(); // allow user to change day
//                adapter = new AgendaItemAdapter(getActivity(), R.layout.daily_view_hour, items);
//                slideAdapter = new SlideExpandableListAdapter(adapter, R.id.count_container, R.id.bottom, 1);
//                listview.setAdapter(slideAdapter);
//
//            }
//        },1000);

        zoomHelper = new AgendaZoomHelper(zoomContainer, new AgendaZoomHelper.ZoomHelperListener() {
            @Override
            public void onChange() {
                items.clear();
                items.addAll(buildItems()); // allow user to change day
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onHide() {
                //updateBackground(DateTime.now());
            }

            @Override
            public void onShow(Routine r) {
                // updateBackground(r.time().toDateTimeToday());
            }
        });


        if (Build.VERSION.SDK_INT >= 11) {
            listview.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    if (expanded && getUserVisibleHint()) { // expanded and visible
//                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)userInfoFragment.getLayoutParams();
                        int scrollY = getScroll();
                        int scrollDiff = (scrollY - lastScroll);

                        if (Math.abs(lastVisibleItemCount - visibleItemCount) <= 5) {

                            int translationY = (int) (userInfoFragment.getTranslationY() - scrollDiff);

                            Log.d(getTag(), "Scroll Y: " + scrollY + ", translationY: " + translationY + ", firstItem: " + firstVisibleItem + ", profileHeight: " + profileFragmentHeight);

                            if (translationY < -profileFragmentHeight) {
                                translationY = -profileFragmentHeight;
                            } else if (translationY >= 0) {
                                translationY = 0;
                            }

                            if (translationY < toolbarHeight - profileFragmentHeight) {
//                            ((HomeActivity) getActivity()).disableToolbarTransparency();
                                ((HomeActivity) getActivity()).hideAddButton();
                                ((HomeActivity) getActivity()).hideToolbar();
                            } else if (translationY > (toolbarHeight - profileFragmentHeight)) {
                                ((HomeActivity) getActivity()).enableToolbarTransparency();
                                ((HomeActivity) getActivity()).showAddButton();
                                ((HomeActivity) getActivity()).showToolbar();
                            }
                            userInfoFragment.setTranslationY(translationY);
                        }
                        lastScroll = scrollY;

                    }
                    lastVisibleItemCount = visibleItemCount;
                }

            });
        }

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.list_animation);
        listview.setLayoutAnimation(controller);

        new LoadDailyAgendaTask().execute(null, null, null);

        return rootView;
    }

    private int getScroll() {
        View c = listview.getChildAt(0); //this is the first visible row
        if (c != null) {
            int scrollY = -c.getTop();
            listViewItemHeights.put(listview.getFirstVisiblePosition(), c.getHeight());
            for (int i = 0; i < listview.getFirstVisiblePosition(); ++i) {
                if (listViewItemHeights.get(i) != null) // (this is a sanity check)
                    scrollY += listViewItemHeights.get(i); //add all heights of the views that are gone
            }
            return scrollY;
        }
        return 0;

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


//        items.clear();
//        items.addAll(buildItems()); // allow user to change day
//        adapter.notifyDataSetChanged();
    }


    public String getNextRoutineHour() {
        int now = DateTime.now().getHourOfDay();
        for (Routine r : Routine.findAll()) {
            if (r.time().getHourOfDay() >= now) {
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
//            if (profileShown)
//                ((HomeActivity) getActivity()).setCustomTitle("Calendula");
//            else
//                ((HomeActivity) getActivity()).setCustomTitle(Session.instance().getUser().getName());
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

        Log.d(getTag(), "Items: " + items.size());

        if (items.size() == 1) {
            addEmptyPlaceholder(items);
        }
        return items;
    }

    private void addEmptyPlaceholder(ArrayList<DailyAgendaItemStub> items) {
        DailyAgendaItemStub spacer = new DailyAgendaItemStub("");
        spacer.isEmptyPlaceholder = true;
        items.add(spacer);
    }

    private void addSpacerTop(ArrayList<DailyAgendaItemStub> items) {
        DailyAgendaItemStub spacer = new DailyAgendaItemStub("");
        spacer.isSpacer = true;
        items.add(spacer);
    }

    public void toggleViewMode() {
        //update expand/collapse flag
        expanded = !expanded;
        // get next routine item index
        final int nextRoutineHour = getNextRoutinePosition();
        // restore header if not expanded
        if (!expanded) {
            restoreHeader();
            ((HomeActivity) getActivity()).hideAddButton();
        }

        // refresh adapter items
        items.clear();
        items.addAll(buildItems());
        // set expand/collapse animation
        listview.setLayoutAnimation(getAnimationController(expanded));
        // perform update
        adapter.notifyDataSetChanged();
        // open next routine item
        slideAdapter.setLastOpenPosition(expanded ? nextRoutineHour + 1 : 1);
//        listViewItemHeights = new Hashtable<Integer, Integer>();
//        lastScroll = getScroll();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                listview.setSelection(nextRoutineHour+1);
//            }
//        }, 1000);

    }


    LayoutAnimationController getAnimationController(boolean expand) {
        return AnimationUtils.loadLayoutAnimation(getActivity(), expand ? R.anim.list_animation_expand : R.anim.list_animation);
    }

    private View getAgendaItemView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        final DailyAgendaItemStub item = items.get(position);

        View v;

        if (item.isSpacer) {
            v = layoutInflater.inflate(R.layout.daily_view_spacer, null);
        } else if (item.isEmptyPlaceholder) {
            v = layoutInflater.inflate(R.layout.daily_view_empty_placeholder, null);
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
            LinearLayout medList = (LinearLayout) v.findViewById(R.id.med_item_list);

            for (DailyAgendaItemStub.DailyAgendaItemStubElement element : item.meds) {
                View medNameView = layoutInflater.inflate(R.layout.daily_view_intake_med, null);
                ((TextView) medNameView.findViewById(R.id.med_item_name)).setText(element.medName + (element.taken ? " ✔" : ""));
                ((TextView) medNameView.findViewById(R.id.med_item_dose)).setText(element.displayDose + " " +
                        (element.presentation.units(getResources())) + (element.dose > 1 ? "s" : ""));
                ((ImageView) medNameView.findViewById(R.id.imageView)).setImageResource(element.res);
                medList.addView(medNameView);
            }

            ((TextView) v.findViewById(R.id.routines_list_item_name)).setText(item.title);
            ((TextView) v.findViewById(R.id.routines_list_item_hour)).setText((item.hour > 9 ? item.hour : "0" + item.hour) + ":");
            ((TextView) v.findViewById(R.id.routines_list_item_minute)).setText((item.minute > 9 ? item.minute : "0" + item.minute) + "");

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
        //Toast.makeText(getActivity(), " Add new routine or med here!", Toast.LENGTH_SHORT).show();
    }

    private void restoreHeader() {
        // translate header to its original position (y=0)
        ObjectAnimator.ofObject(userInfoFragment,
                "translationY", new FloatEvaluator(),
                (int) userInfoFragment.getTranslationY(), 0)
                .setDuration(500)
                .start();

        // fade home toolbar
        ((HomeActivity) getActivity()).enableToolbarTransparency();
    }

    void updatePrefs() {
        SharedPreferences settings = getActivity().getSharedPreferences(CalendulaApp.PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();


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

    public void notifyDataChange() {
        items.clear();
        items.addAll(buildItems()); // allow user to change day
        adapter.notifyDataSetChanged();
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

    public void updateBackground() {
        if (userProInfoFragment != null)
            userProInfoFragment.updateBackground();
    }


    public class LoadDailyAgendaTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            items = buildItems(); // allow user to change day
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            adapter = new AgendaItemAdapter(getActivity(), R.layout.daily_view_hour, items);
            slideAdapter = new SlideExpandableListAdapter(adapter, R.id.count_container, R.id.bottom, 1);
            listview.setAdapter(slideAdapter);
        }
    }

}