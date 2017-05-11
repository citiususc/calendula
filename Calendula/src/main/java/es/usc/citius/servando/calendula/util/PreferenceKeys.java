/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2017 CITIUS - USC
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

import android.support.annotation.StringRes;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;

/**
 * Created by alvaro.brey.vilas on 05/01/17.
 */
public enum PreferenceKeys {

    DRUGDB_CURRENT_DB(R.string.prefkey_drugdb_current_db),
    DRUGDB_ENABLE_DRUGDB(R.string.prefkey_drugdb_enable_drugdb),
    DRUGDB_LAST_VALID(R.string.prefkey_drugdb_last_valid),
    DRUGDB_VERSION(R.string.prefkey_drugdb_version);

    @StringRes
    private final int stringId;

    PreferenceKeys(int stringId) {
        this.stringId = stringId;
    }


    public String key() {
        return toString();
    }

    @Override
    public String toString() {
        return CalendulaApp.getContext().getString(stringId);
    }
}
