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

package es.usc.citius.servando.calendula.mvp

import android.os.Bundle

abstract class StatefulBaseActivityView<in V : IView, out P : StatefulPresenter<V>> :
    BaseActivityView<V, P>() {

    companion object {
        private const val SAVED_STATE_KEY = "PRESENTER_SAVED_STATE"
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable(SAVED_STATE_KEY, presenter.getState())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedState: Bundle?) {
        super.onRestoreInstanceState(savedState)
        savedState?.let { presenter.setState(it.getParcelable(SAVED_STATE_KEY)) }
    }

}