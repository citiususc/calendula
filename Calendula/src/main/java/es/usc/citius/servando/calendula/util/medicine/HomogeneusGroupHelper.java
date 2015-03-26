package es.usc.citius.servando.calendula.util.medicine;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;

/**
 * Created by joseangel.pineiro on 3/23/15.
 */
public class HomogeneusGroupHelper {

    public static String URI = "http://tec.citius.usc.es/calendula/groups/##ID##";

    public static HomogeneusGroup queryGroup(String id) {
        try {
            String queryUri = URI.replace("##ID##", id);

            Log.d("HomogeneusGroupHelper", "Query " + queryUri);

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(queryUri));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                Result result = new Gson().fromJson(responseString, Result.class);

                if (result != null && result.group != null) {
                    return result.group;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private class Result {
        public HomogeneusGroup group;
    }

}
