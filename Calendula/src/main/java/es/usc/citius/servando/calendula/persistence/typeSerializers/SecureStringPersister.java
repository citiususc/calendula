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

package es.usc.citius.servando.calendula.persistence.typeSerializers;

import android.util.Base64;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.nio.charset.Charset;
import java.sql.SQLException;

import es.usc.citius.servando.calendula.util.security.SecuredVault;

public class SecureStringPersister extends BaseDataType {

    private static final SecureStringPersister singleton = new SecureStringPersister();
    private static Charset UTF8 = Charset.forName("UTF-8");

    public SecureStringPersister() {
        super(SqlType.STRING, new Class<?>[]{String.class});
    }

    public static SecureStringPersister getSingleton() {
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
        String v = new String(Base64.decode((String) sqlArg, Base64.DEFAULT), UTF8);
        return SecuredVault.instance().decrypt(v);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        String encrypted = SecuredVault.instance().encrypt((String) javaObject);
        return Base64.encodeToString(encrypted.getBytes(UTF8), Base64.DEFAULT);
    }
}
