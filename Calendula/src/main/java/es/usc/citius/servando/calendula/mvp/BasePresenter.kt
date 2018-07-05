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

import kotlin.reflect.KProperty

abstract class BasePresenter<V : IView> :
    IPresenter<V> {

    private var delegate = ViewDelegate<V>()
    var view: V by delegate

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView() {
        this.delegate.view = null
    }


    fun isAttachedToView(): Boolean = this.delegate.view != null


    private class ViewDelegate<V : IView> {

        var view: V? = null

        operator fun getValue(pres: BasePresenter<V>, prop: KProperty<*>): V {
            return view ?: throw IllegalStateException("View cannot be null")
        }

        operator fun setValue(pres: BasePresenter<V>, prop: KProperty<*>, view: V) {
            this.view = view
        }
    }

}