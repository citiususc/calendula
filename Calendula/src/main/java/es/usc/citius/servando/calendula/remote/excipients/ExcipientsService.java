package es.usc.citius.servando.calendula.remote.excipients;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by alvaro.brey.vilas on 4/11/16.
 */

public interface ExcipientsService {

    @GET("excipientes")
    public Call<List<Excipient>> findExcipientsByName(@Query("query") String query);
}
