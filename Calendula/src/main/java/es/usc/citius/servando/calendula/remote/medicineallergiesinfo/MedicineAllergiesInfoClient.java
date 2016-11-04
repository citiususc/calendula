package es.usc.citius.servando.calendula.remote.medicineallergiesinfo;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import es.usc.citius.servando.calendula.remote.RemoteConfig;
import es.usc.citius.servando.calendula.remote.RemoteServiceCreator;
import es.usc.citius.servando.calendula.remote.medicineallergiesinfo.datamodel.AllergiesInfo;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by alvaro.brey.vilas on 4/11/16.
 */

public class MedicineAllergiesInfoClient {


    private static MedicineAllergiesInfoService service = null;

    private final static String TAG = "PAllergiesInfoClient";

    private MedicineAllergiesInfoClient() {
    }

    private static MedicineAllergiesInfoService getService() {
        if (service == null) {
            Log.d(TAG, "getService: Instantiating new service");
            service = RemoteServiceCreator.createService(MedicineAllergiesInfoService.class, RemoteConfig.BASE_URL);
        }
        return service;
    }

    /**
     * Fetches active ingredients and excipients for a prescription.
     *
     * @param id id of the prescription
     * @return the {@link AllergiesInfo} object, <code>null</code> if prescription is not found
     * @throws IOException           if an error occurs when fetching
     * @throws IllegalStateException if no API url is available
     */
    public static AllergiesInfo getPrescriptionAllergiesInfo(int id) throws IOException, IllegalStateException {

        if (RemoteConfig.BASE_URL == null) {
            throw new IllegalStateException("Base URL is not set. Cannot call API.");
        }

        final String query = "{\"_id\":" + id + "}";
        MedicineAllergiesInfoService service = getService();

        Log.d(TAG, "getPrescriptionAllergiesInfo: querying service with: " + query);
        Log.d(TAG, "getPrescriptionAllergiesInfo: base URL is: " + RemoteConfig.BASE_URL);

        Call<List<AllergiesInfo>> allergiesInfo = service.getAllergiesInfo(query);
        Response<List<AllergiesInfo>> response = allergiesInfo.execute();

        List<AllergiesInfo> list = response.body();
        if (list == null || list.isEmpty()) {
            Log.d(TAG, "getPrescriptionAllergiesInfo: Null or empty response");
            return null;
        }
        return list.get(0);
    }

}
