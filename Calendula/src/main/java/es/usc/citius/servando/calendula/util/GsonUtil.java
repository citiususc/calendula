package es.usc.citius.servando.calendula.util;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by joseangel.pineiro on 7/16/14.
 */
public class GsonUtil {

    private static GsonBuilder builder = null;
    private static Gson gson = null;

    public static Gson get(){

        if(builder == null){
            builder = new GsonBuilder();
            // register date and time converters
            Converters.registerAll(builder);
            gson = builder.create();
        }
        return gson;
    }


}
