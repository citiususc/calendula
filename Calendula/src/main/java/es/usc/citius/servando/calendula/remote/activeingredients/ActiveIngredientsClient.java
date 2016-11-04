package es.usc.citius.servando.calendula.remote.activeingredients;

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

public class ActiveIngredientsClient {


    private static ActiveIngredientsService service = null;

    private final static String TAG = "ActiveIngredientsClient";

    private ActiveIngredientsClient() {
    }

    private static ActiveIngredientsService getService() {
        if (service == null) {
            Log.d(TAG, "getService: Instantiating new service");
            service = RemoteServiceCreator.createService(ActiveIngredientsService.class, RemoteConfig.BASE_URL);
        }
        return service;
    }

    /**
     * Searches for active ingredients based on their name.
     *
     * @param name name (or part of the name) of the active ingredient
     * @return list of {@link ActiveIngredient} with the given name (can be empty):
     * @throws IOException           if an error occurs when fetching
     * @throws IllegalStateException if no API url is available
     */
    public static List<ActiveIngredient> findActiveIngredientsByName(final String name) throws IOException, IllegalStateException {

        if (RemoteConfig.BASE_URL == null) {
            throw new IllegalStateException("Base URL is not set. Cannot call API.");
        }

        final String query = "{\"nombre\":\"~(" + name + ")\"}";


        Log.d(TAG, "findActiveIngredientsByName: querying service with: " + query);
        Log.d(TAG, "findActiveIngredientsByName: base URL is: " + RemoteConfig.BASE_URL);

        Call<List<ActiveIngredient>> call = getService().findActiveIngredientsByName(query);
        Response<List<ActiveIngredient>> response = call.execute();

        return response.body();
    }
}
