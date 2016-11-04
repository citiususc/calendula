package es.usc.citius.servando.calendula.remote;

import org.junit.Test;

import java.util.List;

import es.usc.citius.servando.calendula.remote.excipients.Excipient;
import es.usc.citius.servando.calendula.remote.excipients.ExcipientsClient;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Created by alvaro.brey.vilas on 3/11/16.
 */
public class ExcipientsTest {


    @Test
    public void testActiveIngredients() {
//        {
//            "_id": 2362,
//                "nombre": "Citrato de sodio (E-331)"
//        }
        final String name = "e-331";

        try {
            List<Excipient> excipients = ExcipientsClient.findExcipientsByName(name);
            assertEquals("List size is different", 1, excipients.size());
            assertEquals("ID is different", (Integer) 2362,excipients.get(0).get_id());
            assertEquals("Name is different", "Citrato de sodio (E-331)", excipients.get(0).getNombre());
        } catch (Exception e) {
            fail("Exception caught: " + e.getMessage());
        }
    }
}
