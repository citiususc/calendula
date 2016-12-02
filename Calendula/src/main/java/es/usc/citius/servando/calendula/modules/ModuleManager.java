/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses>.
 */

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
        Log.d(TAG, "runModules: Loading default module configuration");
        run(modules, ctx);
    }

    public void runModules(final String configName, Context ctx) {
        final List<CalendulaModule> modules = ModuleRegistry.getModulesForConfig(configName);
        Log.d(TAG, "runModules: Loading module configuration: " + configName);
        if (modules == null)
            throw new IllegalArgumentException("Module config " + configName + "does not exist or is empty");
        run(modules, ctx);
    }

    public boolean isEnabled(String moduleID) {
        return modules.keySet().contains(moduleID);
    }

}
