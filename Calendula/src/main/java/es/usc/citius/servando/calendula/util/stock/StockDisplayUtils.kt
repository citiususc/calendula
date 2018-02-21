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

package es.usc.citius.servando.calendula.util.stock

import android.content.Context
import android.content.Intent
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import es.usc.citius.servando.calendula.CalendulaApp
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.activities.MedicinesActivity
import es.usc.citius.servando.calendula.persistence.Medicine
import es.usc.citius.servando.calendula.util.IconUtils
import es.usc.citius.servando.calendula.util.LogUtil

/**
 * Created by joseangel.pineiro on 17/11/16.
 */
object StockDisplayUtils {


    private const val TAG = "StockDisplayUtils"
    private const val MAX_DAYS = 21

    @JvmStatic
    fun getReadableStockDuration(estimatedEnd: StockCalculator.StockEnd, ctx: Context): String =
        when (estimatedEnd) {
            is StockCalculator.StockEnd.OnDate -> {
                LogUtil.d(
                    TAG,
                    "updateStockText: estimated end date is " + estimatedEnd.date.toString("dd/MM")
                )
                if (estimatedEnd.days < MAX_DAYS) {
                    ctx.getString(R.string.stock_enough_for_days, estimatedEnd.days)
                } else {
                    ctx.getString(
                        R.string.stock_enough_for_weeks_days,
                        (estimatedEnd.days / 7).toInt(),
                        estimatedEnd.days % 7
                    )
                }
            }
            StockCalculator.StockEnd.OverMax -> ctx.getString(R.string.stock_enough_for_upper_limit)
        }


    @JvmStatic
    fun showStockRunningOutDialog(context: Context, m: Medicine, days: Long?) {


        val msg = context.getString(
            R.string.stock_running_out_dialog_message,
            m.stock!!.toInt(),
            m.presentation.units(context.resources, m.stock!!.toDouble()),
            m.name,
            days
        )

        MaterialStyledDialog.Builder(context)
            .setTitle(context.getString(R.string.stock_running_out_dialog_title, m.name))
            .setStyle(Style.HEADER_WITH_ICON)
            .setIcon(IconUtils.icon(context, m.presentation.icon(), R.color.white, 48))
            .setHeaderColor(R.color.android_orange_dark)
            .withDialogAnimation(true)
            .setDescription(msg)
            .setPositiveText(R.string.manage_stock)
            .onPositive { dialog, which ->
                val i = Intent(context, MedicinesActivity::class.java)
                i.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, m.id)
                context.startActivity(i)
            }
            .setNeutralText(R.string.tutorial_understood)
            .onNeutral { dialog, which -> dialog.dismiss() }
            .show()

    }


}
