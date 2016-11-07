package es.usc.citius.servando.calendula.util;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.HtmlCacheDAO;
import es.usc.citius.servando.calendula.persistence.HtmlCacheEntry;

/**
 * Created by alvaro.brey on 31/10/16.
 */
public class HtmlCacheManager {


    public static final Integer DEFAULT_TTL_MILLIS = 300000; //5 minutes

    private static HtmlCacheManager theInstance = null;

    private Dao<HtmlCacheEntry, Long> dao = null;

    private final static String TAG = "HtmlCacheManager";

    private HtmlCacheManager() {
    }

    public static HtmlCacheManager getInstance() {
        //lazy initialization
        if (theInstance == null)
            theInstance = new HtmlCacheManager();
        return theInstance;
    }

    private HtmlCacheEntry retrieve(final int hashCode) {
        final Dao<HtmlCacheEntry, Long> dao = getDao();
        try {
            List<HtmlCacheEntry> htmlCacheEntries = dao.queryForEq(HtmlCacheEntry.COLUMN_HASHCODE, hashCode);
            if (htmlCacheEntries.isEmpty()) {
                return null;
            } else if (htmlCacheEntries.size() > 1) {
                Log.w(TAG, "Inconsistent state of cache: hashcode" + hashCode + " is not unique. Deleting all copies.");
                for (HtmlCacheEntry entry : htmlCacheEntries) {
                    remove(entry);
                }
                return null;
            } else {
                if (checkTtl(htmlCacheEntries.get(0))) {
                    return htmlCacheEntries.get(0);
                } else {
                    Log.d(TAG, "retrieve: Deleting invalid entry with hashCode: " + hashCode);
                    remove(htmlCacheEntries.get(0));
                    return null;
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "retrieve: ", e);
            return null;
        }
    }


    private boolean checkTtl(HtmlCacheEntry entry) {
        final Date timestamp = entry.getTimestamp();
        long diff = DateTime.now().getMillis() - timestamp.getTime();

        if (entry.getTtl() != null) {
            return diff < entry.getTtl();
        } else {
            return diff < DEFAULT_TTL_MILLIS;
        }
    }

    public boolean isCached(final String url) {
        final int hashCode = url.hashCode();
        HtmlCacheEntry entry = retrieve(hashCode);
        return entry != null;
    }


    public String get(final String url) {
        final int hashCode = url.hashCode();
        HtmlCacheEntry entry = retrieve(hashCode);
        if (entry != null)
            return entry.getData();
        return null;
    }

    public boolean put(final String url, final String data, final Integer ttlMillis) {
        final int hashCode = url.hashCode();
        HtmlCacheEntry entry = retrieve(hashCode);
        if (entry != null)
            remove(entry);

        HtmlCacheEntry newEntry = new HtmlCacheEntry(hashCode, new Date(DateTime.now().getMillis()), data, ttlMillis);
        try {
            Log.d(TAG, "put: writing entry: " + newEntry);
            return getDao().create(newEntry) == 1;
        } catch (SQLException e) {
            Log.e(TAG, "put: ", e);
            return false;
        }

    }

    public boolean remove(final String url) {
        final int hashCode = url.hashCode();
        HtmlCacheEntry entry = retrieve(hashCode);
        if (entry != null) {
            Log.d(TAG, "remove: removing entry: " + entry);
            return remove(entry);
        } else {
            Log.d(TAG, "remove: entry does not exist");
            return false;
        }
    }

    private boolean remove(HtmlCacheEntry entry) {
        try {
            return getDao().delete(entry) == 1;
        } catch (SQLException e) {
            Log.e(TAG, "remove: ", e);
            return false;
        }
    }

    private void clearCache() {
        try {
            for (HtmlCacheEntry entry : getDao().queryForAll()) {
                remove(entry);
            }
        } catch (SQLException e) {
            Log.e(TAG, "clearCache: ", e);
        }
    }


    private Dao<HtmlCacheEntry, Long> getDao() {
        if (dao == null)
            dao = new HtmlCacheDAO(DB.helper()).getConcreteDao();
        return dao;
    }


}
