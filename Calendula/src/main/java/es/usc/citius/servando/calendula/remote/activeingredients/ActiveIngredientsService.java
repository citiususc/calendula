package es.usc.citius.servando.calendula.remote.activeingredients;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by alvaro.brey.vilas on 4/11/16.
 */

public interface ActiveIngredientsService {

    @GET("principiosActivos")
    public Call<List<ActiveIngredient>> findActiveIngredientsByName(@Query("query") String query);
}
