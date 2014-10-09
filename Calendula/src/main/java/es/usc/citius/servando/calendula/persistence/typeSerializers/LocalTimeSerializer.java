package es.usc.citius.servando.calendula.persistence.typeSerializers;

import com.activeandroid.serializer.TypeSerializer;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by joseangel.pineiro on 10/9/14.
 */
public class LocalTimeSerializer extends TypeSerializer {

    @Override
    public Class<?> getDeserializedType() {
        return LocalTime.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public String serialize(Object data) {
        if (data == null) {
            return null;
        }
        return ((LocalTime) data).toString("kk:mm");
    }

    @Override
    public LocalTime deserialize(Object data) {
        if (data == null) {
            return null;
        }
        return DateTimeFormat.forPattern("kk:mm").parseLocalTime((String) data);
    }
}
