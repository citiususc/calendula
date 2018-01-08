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

package es.usc.citius.servando.calendula.persistence.typeSerializers;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.persistence.RepetitionRule;

/**
 * Created by joseangel.pineiro
 */
public class RRulePersister extends BaseDataType {

    private static final RRulePersister singleton = new RRulePersister();

    public RRulePersister() {
        super(SqlType.STRING, new Class<?>[]{RepetitionRule.class});
    }

    public static RRulePersister getSingleton() {
        return singleton;
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return defaultStr;
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String data = (String) sqlArg;
        if (data.contains("$$$")) {
            String[] parts = data.split("$$$");
            RepetitionRule r = new RepetitionRule(parts[0]);
            r.setStart(parts[1]);
        }
        return new RepetitionRule((String) sqlArg);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        RepetitionRule rule = (RepetitionRule) javaObject;
        String ical = rule.toIcal();
        if (rule.getStart() != null) {
            ical += "$$$" + rule.getStart();
        }
        return ical;
    }
}
