package es.usc.citius.servando.calendula.pinlock;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Utility class to handle unlock state (for PIN lock)
 * <p>
 * Created by alvaro.brey.vilas on 5/05/17.
 */
public class UnlockStateManager {

    private final static Duration MAX_UNLOCK_DURATION = Duration.standardMinutes(5);
    private final static String TAG = "UnlockStateManager";
    private static UnlockStateManager theInstance;
    private DateTime unlockTimestamp;

    private UnlockStateManager() {

    }

    public static UnlockStateManager getInstance() {
        if (theInstance == null) {
            theInstance = new UnlockStateManager();
        }
        return theInstance;
    }

    /**
     * Stores unlock status
     */
    public void unlock() {
        LogUtil.v(TAG, "unlock() called");
        if (unlockTimestamp != null)
            LogUtil.d(TAG, "unlock: timestamp was already set. Overwriting.");
        unlockTimestamp = DateTime.now();
    }


    /**
     * Stores lock status
     */
    public void lock() {
        LogUtil.v(TAG, "lock() called");
        if (unlockTimestamp != null) {
            unlockTimestamp = null;
        } else {
            LogUtil.d(TAG, "lock: timestamp wasn't set. Doing nothing.");
        }
    }

    /**
     * Checks if unlock is set and not expired
     *
     * @return <code>true</code> if unlocked, false otherwise.
     */
    public boolean isUnlocked() {
        LogUtil.v(TAG, "isUnlocked() called");
        if (unlockTimestamp != null) {
            Duration diff = new Duration(unlockTimestamp, DateTime.now());
            if (diff.compareTo(MAX_UNLOCK_DURATION) <= 0) {
                LogUtil.d(TAG, "isUnlocked() returned: " + true);
                return true;
            } else {
                LogUtil.d(TAG, "isUnlocked: unlock has expired, unsetting and returning null");
                unlockTimestamp = null;
                return false;
            }
        } else {
            LogUtil.d(TAG, "isUnlocked: timestamp is null");
        }
        return false;
    }


}
