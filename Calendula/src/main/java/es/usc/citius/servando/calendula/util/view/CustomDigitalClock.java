package es.usc.citius.servando.calendula.util.view;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Calendar;

/**
 * You have to make a clone of the file DigitalClock.java to use in your application, modify in the following manner:-
 * private final static String m12 = "h:mm aa"; private final static String m24 = "k:mm";
 */

public class CustomDigitalClock extends TextView {

    private final static String m12 = "kk:mm";
    private final static String m24 = "kk:mm";
    Calendar mCalendar;
    String mFormat;
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

    private void initClock(Context context) {
        Resources r = context.getResources();

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);

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

    /**
     * Pulls 12/24 mode from system settings
     */
    private boolean get24HourMode() {
        return DateFormat.is24HourFormat(getContext());
    }

    private void setFormat() {
        if (get24HourMode()) {
            mFormat = m24;
        } else {
            mFormat = m12;
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