package es.usc.citius.servando.calendula.remote;

import org.junit.Test;

import java.io.IOException;

import es.usc.citius.servando.calendula.remote.medicineallergiesinfo.MedicineAllergiesInfoClient;
import es.usc.citius.servando.calendula.remote.medicineallergiesinfo.datamodel.AllergiesInfo;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Created by alvaro.brey.vilas on 3/11/16.
 */
public class AllergiesInfoTest {


    @Test
    public void testAllergiesInfo() {
        final Integer id = 753665;

        try {
            AllergiesInfo p = MedicineAllergiesInfoClient.getPrescriptionAllergiesInfo(id);
            assertEquals("Id is not equal", id, p.get_id());
            assertNotNull(p.getFormasFarmaceuticas());
            assertNotNull(p.getFormasFarmaceuticas().getExcipientes());
            assertNotNull(p.getFormasFarmaceuticas().getPrincipioActivos());
        } catch (IOException | IllegalStateException e) {
            fail("An exception was thrown: "+ e.getMessage());
        }

    }
}
