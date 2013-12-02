package es.usc.citius.servando.calendula.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.DailyAgendaItemStub;
import es.usc.citius.servando.calendula.util.RandomColorChooser;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class DailyAgendaFragment extends Fragment {

    List<DailyAgendaItemStub> items;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_daily_agenda, container, false);
        final ListView listview = (ListView) rootView.findViewById(R.id.listview);

        items = buildItems(); // allow user to change day

        ListAdapter adapter = new AgendaItemAdapter(getActivity(),R.layout.daily_view_hour,items);
        listview.setAdapter(new SlideExpandableListAdapter(adapter,R.id.count_container,R.id.bottom));
        return rootView;
    }

    public List<DailyAgendaItemStub> buildItems() {

        ArrayList<DailyAgendaItemStub> items = new ArrayList<DailyAgendaItemStub>();

        for(int i = 0; i < 24; i++){
            DailyAgendaItemStub item = DailyAgendaItemStub.random(i);
            items.add(item);
        }
        return items;
    }


    private class AgendaItemAdapter extends ArrayAdapter<DailyAgendaItemStub>{

        public AgendaItemAdapter(Context context, int layoutResourceId, List<DailyAgendaItemStub> items){
            super(context,layoutResourceId,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

           final LayoutInflater layoutInflater = getActivity().getLayoutInflater();

           int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
           int minute = new GregorianCalendar().get(Calendar.MINUTE);

            DailyAgendaItemStub item = items.get(position);

            if(!item.hasColors){
                int colorIndex = RandomColorChooser.getNextColorIndex();
                item.primaryColor = RandomColorChooser.getPrimaryColor(colorIndex,getResources());
                item.secondaryColor = RandomColorChooser.getSecondaryColor(colorIndex,getResources());
                item.hasColors=true;
            }

            View v = layoutInflater.inflate(R.layout.daily_view_hour, null);

            // select the correct layout
            if(!item.hasEvents){
                v.findViewById(R.id.hide_if_empty).setVisibility(View.GONE);
                v.findViewById(R.id.current_hour_spacer).setVisibility(View.VISIBLE);
            }else{
                LinearLayout medList = (LinearLayout) v.findViewById(R.id.med_item_list);
                boolean isFirst = true;
                for(String s : item.meds){
                    View medNameView = layoutInflater.inflate(R.layout.daily_agenda_item_med,null);
                    ((TextView)medNameView.findViewById(R.id.med_item_name)).setText(s);
                    if(isFirst){
                        ((TextView)medNameView.findViewById(R.id.bottom_current_hour_text)).setText(String.valueOf(item.hour<10?("0"+item.hour):item.hour));
                       isFirst=false;
                    }else{
                        medNameView.findViewById(R.id.bottom_current_hour_text).setVisibility(View.INVISIBLE);
                    }
                    // change colors
                    medNameView.setBackgroundColor(item.primaryColor);
                    ((TextView)medNameView.findViewById(R.id.bottom_current_hour_text)).setTextColor(item.primaryColor);
                    ((TextView)medNameView.findViewById(R.id.bottom_current_minute_text)).setTextColor(item.secondaryColor);

                    ((TextView)medNameView.findViewById(R.id.bottom_current_minute_text)).setText("00"); // TODO set proper minute
                    medList.addView(medNameView);
                }
                // set number of meds to take
                ((TextView) v.findViewById(R.id.count_text)).setText(String.valueOf(item.meds.size()));

            }

            // enable hour indicator
            if(hour == item.hour){
                v.findViewById(R.id.hour_text).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.current_hour_indicator).setVisibility(View.VISIBLE);
                TextView hText = (TextView) v.findViewById(R.id.current_hour_text);
                TextView mText = (TextView) v.findViewById(R.id.current_minute_text);
                hText.setText(String.valueOf(hour));
                mText.setText(String.valueOf(minute));
                hText.setVisibility(View.VISIBLE);
                mText.setVisibility(View.VISIBLE);
                v.findViewById(R.id.current_minute_text).setVisibility(View.VISIBLE);
            }

            // change background color
            v.findViewById(R.id.agenda_item_top_bg).setBackgroundColor(item.primaryColor);
            v.findViewById(R.id.item_spacer_top).setBackgroundColor(item.primaryColor);
            v.findViewById(R.id.item_spacer_bottom).setBackgroundColor(item.primaryColor);
            // set hour label
            ((TextView) v.findViewById(R.id.hour_text)).setText(String.valueOf(item.hour));
            return v;
        }
    }
}