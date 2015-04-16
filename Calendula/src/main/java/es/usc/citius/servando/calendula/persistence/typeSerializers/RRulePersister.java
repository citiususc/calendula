package es.usc.citius.servando.calendula.persistence.typeSerializers;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.persistence.RepetitionRule;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
public class RRulePersister extends BaseDataType {

    public RRulePersister() {
        super(SqlType.STRING, new Class<?>[]{RepetitionRule.class});
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
        return new RepetitionRule((String) sqlArg);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        return ((RepetitionRule) javaObject).toIcal();
    }


    private static final RRulePersister singleton = new RRulePersister();

    public static RRulePersister getSingleton() {
        return singleton;
    }
}
