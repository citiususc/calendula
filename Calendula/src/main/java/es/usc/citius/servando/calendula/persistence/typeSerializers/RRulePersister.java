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
        if (rule.start() != null) {
            ical += "$$$" + rule.start();
        }
        return ical;
    }
}
