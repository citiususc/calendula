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

package es.usc.citius.servando.calendula.modules.modules;

import android.content.Context;

import es.usc.citius.servando.calendula.modules.CalendulaModule;

/**
 * Created by alvaro.brey.vilas on 12/12/16.
 */

public class AllergiesModule extends CalendulaModule {

    public static final String ID = "CALENDULA_ALLERGIES_MODULE";

    private static final String TAG = "AllergiesModule";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void onApplicationStartup(Context ctx) {
        // stub
    }
}
