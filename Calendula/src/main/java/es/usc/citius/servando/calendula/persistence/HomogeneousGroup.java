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

package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import es.usc.citius.servando.calendula.database.DB;

/**
 * Created by joseangel.pineiro on 3/23/15.
 */
@DatabaseTable(tableName = "Groups")
public class HomogeneousGroup {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GROUP = "Group";
    public static final String COLUMN_NAME = "Name";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_GROUP)
    public String group;

    @DatabaseField(columnName = COLUMN_NAME)
    public String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static HomogeneousGroup fromCsv(String csvLine, String separator) {
        String[] values = csvLine.split(separator);
        if (values.length != 2)
            throw new RuntimeException("Invalid CSV. Input string must contain exactly 2 members. " + csvLine);

        HomogeneousGroup g = new HomogeneousGroup();
        g.group = values[0];
        g.name = values[1];
        return g;
    }


    public static int count() {
        return DB.prescriptions().count();
    }

    public static boolean empty() {
        return count() <= 0;
    }

}
