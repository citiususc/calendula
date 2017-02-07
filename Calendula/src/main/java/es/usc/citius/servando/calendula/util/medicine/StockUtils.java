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

package es.usc.citius.servando.calendula.util.medicine;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.IconUtils;

/**
 * Created by joseangel.pineiro on 17/11/16.
 */
public class StockUtils {

    private static final String TAG = "StockUtils";

    public static String getReadableStockDuration(LocalDate estimatedEnd, Context ctx) {
        String response;
        if (estimatedEnd != null) {
            Log.d(TAG, "updateStockText: estimado " + estimatedEnd.toString("dd/MM"));
            long days = new Duration(DateTime.now().withTimeAtStartOfDay(), estimatedEnd.toDateTimeAtStartOfDay()).getStandardDays();
            if (days < 21) {
                response = ctx.getString(R.string.stock_enough_for_days, days);
            } else {
                response = ctx.getString(R.string.stock_enough_for_months_days, (int) (days / 7), (days % 7));
            }
        } else {
            response = ctx.getString(R.string.stock_enough_for_upper_limit);
        }
        return response;

    }

    public static LocalDate getEstimatedStockEnd(Medicine m) {
        return getEstimatedStockEnd(DB.schedules().findByMedicine(m), m.stock());
    }


    public static Long getEstimatedStockDays(Medicine m) {
        LocalDate estimatedEnd = getEstimatedStockEnd(m);
        if (estimatedEnd != null) {
            return new Duration(DateTime.now().withTimeAtStartOfDay(), estimatedEnd.toDateTimeAtStartOfDay()).getStandardDays();
        }
        return null;
    }


    public static LocalDate getEstimatedStockEnd(List<Schedule> schedules, float stock) {
        LocalDate current = LocalDate.now();
        LocalDate end = LocalDate.now().plusMonths(3);
        Duration duration = new Duration(current.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay());

        float virtualStock = stock;

        Log.d(TAG, "getEstimatedStockEnd: virtual stock " + virtualStock);

        for (int i = 0; i < duration.getStandardDays(); i++) {
            for (Schedule s : schedules) {
                if (s.enabledForDate(current)) {
                    if (s.repeatsHourly()) {
                        virtualStock = virtualStock - (s.hourlyItemsAt(current.toDateTimeAtStartOfDay()).size() * s.dose());
                    } else {
                        List<ScheduleItem> items = s.items();
                        for (ScheduleItem item : items) {
                            virtualStock = virtualStock - item.dose();
                        }
                    }
                }
                Log.d(TAG, "getEstimatedStockEnd: virtual stock " + virtualStock + " on " + current.toString("dd/MM"));
            }
            if (virtualStock < 0) {
                return current;
            }
            current = current.plusDays(1);
        }
        return null;
    }

    public static void showStockRunningOutDialog(final Context context, final Medicine m, Long days) {

        String msg = "Quedan " + m.stock().intValue() + " " + m.presentation().units(context.getResources(), m.stock()) + " de " + m.name() + ", y ";
        msg += "se acabarán en " + days + " días con la pauta actual.";


        new MaterialStyledDialog.Builder(context)
                .setTitle("Se están acabando las existencias de " + m.name())
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(context, m.presentation().icon(), R.color.white, 48))
                .setHeaderColor(R.color.android_orange_dark)
                .withDialogAnimation(true)
                .setDescription(msg)
                .setPositiveText("Gestionar stock")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent i = new Intent(context, MedicinesActivity.class);
                        i.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, m.getId());
                        context.startActivity(i);
                    }
                })
                .setNeutralText("Entendido")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }


}
