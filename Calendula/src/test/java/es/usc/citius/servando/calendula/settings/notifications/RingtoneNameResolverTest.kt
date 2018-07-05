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

package es.usc.citius.servando.calendula.settings.notifications

import es.usc.citius.servando.calendula.BuildConfig
import es.usc.citius.servando.calendula.R
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class RingtoneNameResolverTest {

    lateinit var resolver: RingtoneNameResolver

    @Before
    fun setUp() {
        resolver = RingtoneNameResolver(RuntimeEnvironment.application)
    }

    @Test
    fun resolveNull() {
        Assert.assertEquals(
            "Wrong resolution for null",
            RuntimeEnvironment.application.getString(R.string.ringtone_none),
            resolver.resolveRingtoneName(null)
        )
    }
}