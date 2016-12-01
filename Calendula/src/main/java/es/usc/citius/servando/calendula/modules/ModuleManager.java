package es.usc.citius.servando.calendula.modules;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.addAll;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public class ModuleManager {

    private static ModuleManager theInstance = null;
    private Map<String, CalendulaModule> modules;

    private final static String TAG = "ModuleManager";

    private ModuleManager() {
        modules = new ArrayMap<>();
    }

    private ModuleManager(Collection<CalendulaModule> modules) {
        this();
        addAll(modules);
    }

    public static ModuleManager getInstance() {
        if (theInstance == null) {
            Log.d(TAG, "getInstance: ModuleManager initialized");
            theInstance = new ModuleManager();
        }
        return theInstance;
    }


    public void run(CalendulaModule module, Context ctx) {
        try {
            modules.put(module.getId(), module);
            module.onApplicationStartup(ctx);
            Log.v(TAG, "Module ran: " + module.getId());
        } catch (Exception e) {
            Log.e(TAG, "run: Error when enabling module \"" + module.getId() + "\": ", e);
        }
    }

    public void run(Collection<CalendulaModule> modules, Context ctx) {
        for (CalendulaModule module : modules) {
            run(module, ctx);
        }
    }

    public void run(CalendulaModule[] modules, Context ctx) {
        for (CalendulaModule module : modules) {
            run(module, ctx);
        }
    }

    public void runDefaultModules(Context ctx) {
        final List<CalendulaModule> modules = ModuleRegistry.getDefaultModules();
        run(modules, ctx);
    }

    public boolean isEnabled(String moduleID) {
        return modules.keySet().contains(moduleID);
    }

}
