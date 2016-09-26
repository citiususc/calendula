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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.scheduling;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import es.usc.citius.servando.calendula.CalendulaApp;

/**
 * Encapsulates extras of alarm intents
 */
public class AlarmIntentParams implements Parcelable {

    public static final String TAG = "AlarmIntentParams";

    public static final String DATE_FORMAT = "dd/MM/YYYY";
    public static final String TIME_FORMAT = "kk:mm";

    public int action = -1;
    public long routineId = -1;
    public long scheduleId = -1;
    public String scheduleTime = "";
    public String  date = "";

    public AlarmIntentParams() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(action);
        out.writeLong(routineId);
        out.writeLong(scheduleId);
        out.writeString(scheduleTime);
        out.writeString(date);
    }

    public static final Parcelable.Creator<AlarmIntentParams> CREATOR = new Parcelable.Creator<AlarmIntentParams>() {
        public AlarmIntentParams createFromParcel(Parcel in) {
            return new AlarmIntentParams(in);
        }

        public AlarmIntentParams[] newArray(int size) {
            return new AlarmIntentParams[size];
        }
    };

    private AlarmIntentParams(Parcel in) {
        action = in.readInt();
        routineId = in.readLong();
        scheduleId = in.readLong();
        scheduleTime = in.readString();
        date = in.readString();
    }

    public static AlarmIntentParams forRoutine(Long routineId, LocalDate date, boolean delayed){
        AlarmIntentParams params = new AlarmIntentParams();
        params.action = delayed ? CalendulaApp.ACTION_ROUTINE_DELAYED_TIME : CalendulaApp.ACTION_ROUTINE_TIME;
        params.routineId = routineId;
        params.date = date.toString(DATE_FORMAT);
        Log.d(TAG, "forRoutine: " + params.toString());
        return params;
    }

    public static AlarmIntentParams forSchedule(Long scheduleId, LocalTime time, LocalDate date, boolean delayed){
        AlarmIntentParams params = new AlarmIntentParams();
        params.action = delayed ? CalendulaApp.ACTION_HOURLY_SCHEDULE_DELAYED_TIME : CalendulaApp.ACTION_HOURLY_SCHEDULE_TIME;
        params.scheduleId = scheduleId;
        params.scheduleTime = time.toString(TIME_FORMAT);
        params.date = date.toString(DATE_FORMAT);
        Log.d(TAG, "forSchedule: " + params.toString());
        return params;
    }

    public static AlarmIntentParams forDailyUpdate() {
        AlarmIntentParams params = new AlarmIntentParams();
        params.action = CalendulaApp.ACTION_DAILY_ALARM;
        return params;
    }

    public LocalDate date() {
        return DateTimeFormat.forPattern(DATE_FORMAT).parseLocalDate(date);
    }

    public LocalTime scheduleTime() {
        return DateTimeFormat.forPattern(TIME_FORMAT).parseLocalTime(scheduleTime);
    }

    public DateTime dateTime() {
        if(!TextUtils.isEmpty(date) && !TextUtils.isEmpty(scheduleTime))
            return date().toDateTime(scheduleTime());
        else if(!TextUtils.isEmpty(date))
            return date().toDateTimeAtStartOfDay();
        else if(!TextUtils.isEmpty(scheduleTime))
            return LocalDate.now().toDateTime(scheduleTime());

        return null;
    }

    @Override
    public String toString() {
        return "AlarmIntentParams{" +
                "action=" + action +
                ", routineId=" + routineId +
                ", scheduleId=" + scheduleId +
                ", scheduleTime='" + scheduleTime + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlarmIntentParams params = (AlarmIntentParams) o;

        if (action != params.action) return false;
        if (routineId != params.routineId) return false;
        if (scheduleId != params.scheduleId) return false;
        if (!scheduleTime.equals(params.scheduleTime)) return false;
        return date.equals(params.date);

    }

    @Override
    public int hashCode() {
        int result = action;
        result = 31 * result + (int) (routineId ^ (routineId >>> 32));
        result = 31 * result + (int) (scheduleId ^ (scheduleId >>> 32));
        result = 31 * result + scheduleTime.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }
}
