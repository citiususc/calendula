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
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

package es.usc.citius.servando.calendula.util.view;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.widget.AppCompatTextView;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import java.util.Calendar;

/**
 * You have to make a clone of the file DigitalClock.java to use in your application, modify in the following manner:-
 * private final static String m12 = "h:mm aa"; private final static String m24 = "k:mm";
 */

public class CustomDigitalClock extends AppCompatTextView {

    private final static String m12 = "kk:mm";
    private final static String m24 = "kk:mm";
    private final static String m12sec = "kk:mm:ss";
    private final static String m24sec = "kk:mm:ss";
    Calendar mCalendar;
    String mFormat;
    boolean showSeconds = false;
    private FormatChangeObserver mFormatChangeObserver = new FormatChangeObserver();
    private Runnable mTicker;
    private Handler mHandler;
    private boolean mTickerStopped = false;

    public CustomDigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public CustomDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    public void setShowSeconds(boolean showSeconds) {
        this.showSeconds = showSeconds;
        setFormat();
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped)
                    return;
                mCalendar.setTimeInMillis(System.currentTimeMillis());

                // DateTime time = DateTime.now();
                //
                // String hour = time.getHourOfDay() < 10 ? ("0" + time.getHourOfDay()) : ("" + time.getMinuteOfDay());
                // String min = time.getMinuteOfHour() < 10 ? ("0" + time.getMinuteOfHour()) : ("" +
                // time.getMinuteOfHour());
                //
                // String text = Html.fromHtml("<b>" + hour + "</b>:" + min).toString();
                // setText(text);
                if (showSeconds)
                    setText("Ahora - " + DateFormat.format(mFormat, mCalendar));
                else
                    setText(DateFormat.format(mFormat, mCalendar));
                invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    private void initClock(Context context) {
        Resources r = context.getResources();

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
    }

    /**
     * Pulls 12/24 mode from system settings
     */
    private boolean get24HourMode() {
        return DateFormat.is24HourFormat(getContext());
    }

    private void setFormat() {
        if (get24HourMode()) {
            mFormat = showSeconds ? m24sec : m24;
        } else {
            mFormat = showSeconds ? m12sec : m12;
        }
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            setFormat();
        }
    }
}