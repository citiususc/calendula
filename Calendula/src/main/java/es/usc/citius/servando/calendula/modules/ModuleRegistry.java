package es.usc.citius.servando.calendula.modules;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public class ModuleRegistry {

    public static final String TAG = "ModuleRegistry";

    public static List<CalendulaModule> getDefaultModules() {
        return getModulesForConfig(ModuleConfig.DEFAULT);
    }

    public static List<CalendulaModule> getModulesForConfig(String configName) {
        return getModulesForConfig(ModuleConfig.valueOf(configName));
    }

    public static List<CalendulaModule> getModulesForConfig(ModuleConfig config) {
        List<CalendulaModule> modules = new ArrayList<>();
        for (Class<?> moduleClass : config.modList) {
            try {
                modules.add((CalendulaModule) moduleClass.newInstance());
            } catch (Exception e) {
                Log.e(TAG, "getModulesForConfig: An error occurred when trying to instantiate module", e);
                throw new RuntimeException(e);
            }
        }
        Log.d(TAG, "getModulesForConfig: " + modules.size() + " modules instantiated successfully");
        return modules;
    }

    public enum ModuleConfig {
        DEFAULT(ModuleLists.DEFAULT_MODULES);

        private Class<?>[] modList;

        ModuleConfig(Class<?>[] modules) {
            this.modList = modules;
        }
    }

    private static class ModuleLists {
        private static final Class<?>[] DEFAULT_MODULES = new Class<?>[]{
                BaseModule.class // Base module is required. Do not remove!
        };
    }
}
