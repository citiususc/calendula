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

package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;

import java.util.Comparator;

import es.usc.citius.servando.calendula.persistence.typeSerializers.LocalDatePersister;


/**
 * Created by joseangel.pineiro on 4/9/15.
 */
@DatabaseTable(tableName = "Pickups")
public class PickupInfo {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FROM = "From";
    public static final String COLUMN_TO = "To";
    public static final String COLUMN_TAKEN = "Taken";
    public static final String COLUMN_MEDICINE = "Medicine";


    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_FROM, persisterClass = LocalDatePersister.class)
    private LocalDate from;

    @DatabaseField(columnName = COLUMN_TO, persisterClass = LocalDatePersister.class)
    private LocalDate to;

    @DatabaseField(columnName = COLUMN_TAKEN)
    private boolean taken;

    @DatabaseField(columnName = COLUMN_MEDICINE, foreign = true, foreignAutoRefresh = true)
    private Medicine medicine;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }


    //
    // DB QUERIES
    //


    // Comparator

    public static class PickupComparator implements Comparator<PickupInfo> {

        public static PickupComparator instance = new PickupComparator();

        @Override
        public int compare(PickupInfo a, PickupInfo b) {
            LocalDate fromA = a.getFrom();
            LocalDate fromB = b.getFrom();
            return fromA.compareTo(fromB);
        }
    }

}
