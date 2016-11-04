package es.usc.citius.servando.calendula.remote.excipients;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import es.usc.citius.servando.calendula.remote.RemoteConfig;
import es.usc.citius.servando.calendula.remote.RemoteServiceCreator;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by alvaro.brey.vilas on 4/11/16.
 */

public class ExcipientsClient {


    private static ExcipientsService service = null;

    private final static String TAG = "ExcipientsClient";

    private ExcipientsClient() {
    }

    private static ExcipientsService getService() {
        if (service == null) {
            Log.d(TAG, "getService: Instantiating new service");
            service = RemoteServiceCreator.createService(ExcipientsService.class, RemoteConfig.BASE_URL);
        }
        return service;
    }

    /**
     * Searches for excipients based on their name.
     *
     * @param name name (or part of the name) of the excipient
     * @return list of {@link Excipient} with the given name (can be empty):
     * @throws IOException           if an error occurs when fetching
     * @throws IllegalStateException if no API url is available
     */
    public static List<Excipient> findExcipientsByName(final String name) throws IOException, IllegalStateException {

        if (RemoteConfig.BASE_URL == null) {
            throw new IllegalStateException("Base URL is not set. Cannot call API.");
        }

        final String query = "{\"nombre\":\"~(" + name + ")\"}";


        Log.d(TAG, "findExcipientsByName: querying service with: " + query);
        Log.d(TAG, "findExcipientsByName: base URL is: " + RemoteConfig.BASE_URL);

        Call<List<Excipient>> call = getService().findExcipientsByName(query);
        Response<List<Excipient>> response = call.execute();

        return response.body();
    }
}
