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

package es.usc.citius.servando.calendula.activities;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import es.usc.citius.servando.calendula.persistence.Presentation;

class PresentationTagMatcher extends BaseMatcher {

    Presentation p;

    public PresentationTagMatcher(Presentation p) {
        this.p = p;
    }

    @Override
    public boolean matches(Object item) {
        return item != null && item.equals(p);
    }

    @Override
    public void describeTo(Description description) {

    }
}
