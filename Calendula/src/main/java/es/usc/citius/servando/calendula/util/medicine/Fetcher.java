package es.usc.citius.servando.calendula.util.medicine;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joseangel.pineiro on 2/25/15.
 */
public class Fetcher {

    private static final String QUERY_URL = "http://www.aemps.gob.es/cima/rest/maestras?maestra=15&nombre=";

    public static List<String> fetchNames(String key) {
        List<String> results = new ArrayList<String>();
        if (key != null && key.length() > 4) {

            String queryUri = QUERY_URL + key;

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(new HttpGet(queryUri));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    out.close();

                    Result result = new Gson().fromJson(responseString, Result.class);

                    if (result != null && result.resultados != null) {
                        for (ResultItem r : result.resultados) {
                            results.add(r.nombre);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }


    public class Result {
        List<ResultItem> resultados;
    }

    public class ResultItem {
        String nombre;
    }


}
