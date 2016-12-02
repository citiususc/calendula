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

package es.usc.citius.servando.calendula.util;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Abstract service that has to be inherited in order to provide a
 * notification to the user when an event is happening.
 *
 * @author jovche.mitrejchevski
 */
public abstract class WakeIntentService extends IntentService {

    /**
     * Name of the lock.
     */
    private static final String LOCK_NAME_STATIC = "es.usc.citius.servando.calendula.lock";
    private static PowerManager.WakeLock lockStatic = null;

    /**
     * Constructor.
     *
     * @param name Name of the service.
     */
    public WakeIntentService(String name) {
        super(name);
    }

    /**
     * Obtain a lock.
     *
     * @param context Context
     */
    public static void acquireStaticLock(Context context) {
        getLock(context).acquire();
    }

    /**
     * Synchronized obtaining of the lock.
     *
     * @param context Context.
     * @return WakeLock object.
     */
    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager powManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            lockStatic = powManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true);
        }
        return (lockStatic);
    }

    /**
     * Has to be overrated in the class that will inherit from this one and here is where
     * the user of this class takes action to notify the user about the event.
     *
     * @param intent Intent.
     */
    public abstract void doReminderWork(Intent intent);

    /**
     * Called when an intent is starting up this service.
     *
     * @param intent Intent
     */
    @Override
    final protected void onHandleIntent(Intent intent) {
        try {
            doReminderWork(intent);
        } finally {
            try {
                PowerManager.WakeLock wakeLock = getLock(this);
                if (wakeLock.isHeld())
                    wakeLock.release();
            }catch (Exception e){
                // do nothing
            }
        }
    }
}
