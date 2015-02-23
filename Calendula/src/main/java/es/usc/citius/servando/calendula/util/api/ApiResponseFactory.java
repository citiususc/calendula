package es.usc.citius.servando.calendula.util.api;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by joseangel.pineiro on 7/3/14.
 */
public class ApiResponseFactory {

    private static Gson gson = new Gson();

    public static <E> E createFrom(HttpResponse response, Class<?> clazz) {
        try {

            String content = EntityUtils.toString(response.getEntity());

            Log.d("APIResponseFactory", content);

            return (E) gson.fromJson(content, clazz);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
