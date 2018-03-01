/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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

package es.usc.citius.servando.calendula.settings.database

import android.content.Context
import android.content.Intent
import android.support.annotation.StringRes
import es.usc.citius.servando.calendula.mvp.IPresenter
import es.usc.citius.servando.calendula.mvp.IView

interface DatabasePrefsContract {


    interface View : IView {
        fun setDbList(dbIds: Array<String>, dbDisplayNames: Array<String>)
        fun showSelectedDb(dbId: String)
        fun resolveString(@StringRes stringRes: Int): String
        fun showDatabaseDownloadChoice(dbId: String)
        fun showDatabaseUpdateNotAvailable()
        fun getIntent(): Intent
        fun openDatabaseSelection()
        fun askForDownloadPermission(dbId: String)
        fun hasDownloadPermission(): Boolean
    }

    interface Presenter : IPresenter<View> {
        fun currentDbUpdated(dbId: String)
        fun selectNewDb(dbId: String): Boolean
        fun onDbDownloadChoiceResult(result: Boolean)
        fun checkDatabaseUpdate(ctx: Context)
        fun onDownloadPermissionGranted(dbId: String)
    }

}