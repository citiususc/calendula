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

package es.usc.citius.servando.calendula.adapters;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.typeface.IIcon;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;

/**
 * Created by alvaro.brey.vilas on 02/01/17.
 */
public enum HomePages {
    // attention: order is important!!!
    HOME(DailyAgendaFragment.class.getName(), R.string.app_name, GoogleMaterial.Icon.gmd_home),
    MEDICINES(MedicinesListFragment.class.getName(), R.string.title_activity_medicines, CommunityMaterial.Icon.cmd_pill),
    ROUTINES(RoutinesListFragment.class.getName(), R.string.title_activity_routines, GoogleMaterial.Icon.gmd_alarm),
    SCHEDULES(ScheduleListFragment.class.getName(), R.string.title_activity_schedules, GoogleMaterial.Icon.gmd_calendar);

    public String className;
    public int title;
    public IIcon icon;

    HomePages(String className, int title, IIcon icon) {
        this.className = className;
        this.title = title;
        this.icon = icon;
    }

    public static HomePages getPage(final int position) throws IndexOutOfBoundsException{
        return values()[position];
    }
}
