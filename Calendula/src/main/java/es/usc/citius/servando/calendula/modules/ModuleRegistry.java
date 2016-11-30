package es.usc.citius.servando.calendula.modules;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public class ModuleRegistry {

    public static final String TAG = "ModuleRegistry";

    private static final Class<?>[] DEFAULT_MODULES = new Class<?>[]{
            BaseModule.class // Base module is required. Do not remove!
    };

    private static List<CalendulaModule> modules = null;

    public static List<CalendulaModule> getModules() {
        if (modules == null) {
            modules = new ArrayList<>();
            for (Class<?> moduleClass : DEFAULT_MODULES) {
                try {
                    modules.add((CalendulaModule) moduleClass.newInstance());
                } catch (Exception e) {
                    Log.e(TAG, "getModules: An error occurred when trying to instantiate module", e);
                    throw new RuntimeException(e);
                }
            }
            Log.d(TAG, "getModules: " + modules.size() + " modules instantiated successfully");
        }
        return modules;
    }
}
