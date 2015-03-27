//package es.usc.citius.servando.calendula.persistence.typeSerializers;
//
//import com.activeandroid.serializer.TypeSerializer;
//
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//
///**
// * Created by joseangel.pineiro on 10/9/14.
// */
//public class DateTimeSerializer extends TypeSerializer {
//
//    @Override
//    public Class<?> getDeserializedType() {
//        return DateTime.class;
//    }
//
//    @Override
//    public Class<?> getSerializedType() {
//        return String.class;
//    }
//
//    @Override
//    public String serialize(Object data) {
//        if (data == null) {
//            return null;
//        }
//        return ((DateTime) data).toString("yy/MM/dd kk:mm");
//    }
//
//    @Override
//    public DateTime deserialize(Object data) {
//        if (data == null) {
//            return null;
//        }
//        return DateTime.parse((String) data, DateTimeFormat.forPattern("yy/MM/dd kk:mm"));
//    }
//}
