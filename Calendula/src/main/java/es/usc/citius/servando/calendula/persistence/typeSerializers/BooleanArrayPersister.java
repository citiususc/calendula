package es.usc.citius.servando.calendula.persistence.typeSerializers;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
public class BooleanArrayPersister extends BaseDataType {

    private static final BooleanArrayPersister singleton = new BooleanArrayPersister();
    String format = "kk:mm";

    public BooleanArrayPersister() {
        super(SqlType.STRING, new Class<?>[]{boolean[].class});
    }

    public static BooleanArrayPersister getSingleton() {
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
        if (sqlArg == null) {
            return null;
        }
        return toArray((String) sqlArg);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        if (javaObject == null) {
            return null;
        }
        return toString((boolean[]) javaObject);
    }

    private boolean[] toArray(String value) {
        String[] values = value.split(",");
        boolean[] result = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Boolean.parseBoolean(values[i]);
        }
        return result;
    }

    private String toString(boolean[] values) {
        String result = "";
        for (int i = 0; i < values.length; i++) {
            result += String.valueOf(values[i]);
            if (i < values.length - 1) {
                result += ",";
            }
        }
        return result;
    }
}
