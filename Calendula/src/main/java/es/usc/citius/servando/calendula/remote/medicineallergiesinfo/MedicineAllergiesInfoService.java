package es.usc.citius.servando.calendula.remote.medicineallergiesinfo;

import java.util.List;

import es.usc.citius.servando.calendula.remote.medicineallergiesinfo.datamodel.AllergiesInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by alvaro.brey.vilas on 3/11/16.
 */

public interface MedicineAllergiesInfoService {

    @GET("medicamentos?select=%22formasFarmaceuticas.excipientes,formasFarmaceuticas.principioActivos.principioActivo%22")
    public Call<List<AllergiesInfo>> getAllergiesInfo(@Query("query") String query);
}
