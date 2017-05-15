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

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.database.HtmlCacheDAO;
import es.usc.citius.servando.calendula.persistence.HtmlCacheEntry;

/**
 * Created by alvaro.brey on 31/10/16.
 */
public class HtmlCacheManager {


    public static final Long DEFAULT_TTL_MILLIS = Duration.standardMinutes(5).getMillis(); //5 minutes
    private static final String TAG = "HtmlCacheManager";
    private static HtmlCacheManager theInstance = null;
    private Dao<HtmlCacheEntry, Long> dao = null;

    private HtmlCacheManager() {
    }

    public static HtmlCacheManager getInstance() {
        //lazy initialization
        if (theInstance == null)
            theInstance = new HtmlCacheManager();
        return theInstance;
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

    public boolean put(final String url, final String data, final Duration ttlDuration) {
        final Long ttl = ttlDuration == null ? DEFAULT_TTL_MILLIS : ttlDuration.getMillis();
        final int hashCode = url.hashCode();
        HtmlCacheEntry entry = retrieve(hashCode);
        if (entry != null)
            remove(entry);

        HtmlCacheEntry newEntry = new HtmlCacheEntry(hashCode, new Date(DateTime.now().getMillis()), data, ttl);
        try {
            LogUtil.d(TAG, "put: writing entry: " + newEntry);
            return getDao().create(newEntry) == 1;
        } catch (SQLException e) {
            LogUtil.e(TAG, "put: ", e);
            return false;
        }

    }

    public boolean remove(final String url) {
        final int hashCode = url.hashCode();
        HtmlCacheEntry entry = retrieve(hashCode);
        if (entry != null) {
            LogUtil.d(TAG, "remove: removing entry: " + entry);
            return remove(entry);
        } else {
            LogUtil.d(TAG, "remove: entry does not exist");
            return false;
        }
    }

    /**
     * Removes all invalid entries from cache.
     *
     * @return number of removed entries
     */
    public Integer purgeCache() {

        try {
            Integer count = (Integer) DB.transaction(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    Integer count = 0;
                    CloseableIterator<HtmlCacheEntry> iterator = getDao().iterator();
                    while (iterator.hasNext()) {
                        HtmlCacheEntry entry = iterator.nextThrow();
                        if (!checkTtl(entry))
                            remove(entry);
                        count++;
                    }
                    iterator.close();

                    return count;
                }
            });
            LogUtil.v(TAG, "purgeCache: purged " + count + " entries");
            return count;
        } catch (Exception e) {
            return -1;
        }
    }

    private HtmlCacheEntry retrieve(final int hashCode) {
        final Dao<HtmlCacheEntry, Long> dao = getDao();
        try {
            List<HtmlCacheEntry> htmlCacheEntries = dao.queryForEq(HtmlCacheEntry.COLUMN_HASHCODE, hashCode);
            if (htmlCacheEntries.isEmpty()) {
                return null;
            } else if (htmlCacheEntries.size() > 1) {
                LogUtil.w(TAG, "Inconsistent state of cache: hashcode" + hashCode + " is not unique. Deleting all copies.");
                for (HtmlCacheEntry entry : htmlCacheEntries) {
                    remove(entry);
                }
                return null;
            } else {
                if (checkTtl(htmlCacheEntries.get(0))) {
                    return htmlCacheEntries.get(0);
                } else {
                    LogUtil.d(TAG, "retrieve: Deleting invalid entry with hashCode: " + hashCode);
                    remove(htmlCacheEntries.get(0));
                    return null;
                }
            }
        } catch (SQLException e) {
            LogUtil.e(TAG, "retrieve: ", e);
            return null;
        }
    }

    private boolean checkTtl(HtmlCacheEntry entry) {
        final Date timestamp = entry.getTimestamp();
        long diff = DateTime.now().getMillis() - timestamp.getTime();

        return diff < entry.getTtl();
    }

    private boolean remove(HtmlCacheEntry entry) {
        try {
            return getDao().delete(entry) == 1;
        } catch (SQLException e) {
            LogUtil.e(TAG, "remove: ", e);
            return false;
        }
    }

    private void clearCache() {
        try {
            for (HtmlCacheEntry entry : getDao().queryForAll()) {
                remove(entry);
            }
        } catch (SQLException e) {
            LogUtil.e(TAG, "clearCache: ", e);
        }
    }

    private Dao<HtmlCacheEntry, Long> getDao() {
        if (dao == null)
            dao = new HtmlCacheDAO(DB.helper()).getConcreteDao();
        return dao;
    }


}
