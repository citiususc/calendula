package es.usc.citius.servando.calendula.remote;

import org.junit.Test;

import java.util.List;

import es.usc.citius.servando.calendula.remote.activeingredients.ActiveIngredient;
import es.usc.citius.servando.calendula.remote.activeingredients.ActiveIngredientsClient;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Created by alvaro.brey.vilas on 3/11/16.
 */
public class ActiveIngredientsTest {


    @Test
    public void testActiveIngredients() {
//        [
//        {
//                "_id": 4104,
//                "codigo": "3060A",
//                "nombre": "Sulfametizol"
//        }
//        ]
        final String name = "sulfametizol";

        try {
            List<ActiveIngredient> activeIngredients = ActiveIngredientsClient.findActiveIngredientsByName(name);
            assertEquals("List size is different", 1, activeIngredients.size());
            assertEquals("ID is different", (Integer) 4104,activeIngredients.get(0).get_id());
            assertEquals("Code is different", "3060A", activeIngredients.get(0).getCodigo());
            assertEquals("Name is different", "Sulfametizol", activeIngredients.get(0).getNombre());
        } catch (Exception e) {
            fail("Exception caught: " + e.getMessage());
        }
    }
}
