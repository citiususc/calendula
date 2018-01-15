/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.modules;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.util.LogUtil;

import static java.util.Collections.addAll;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public class ModuleManager {

    private static final String TAG = "ModuleManager";
    private static ModuleManager theInstance = null;
    private Map<String, CalendulaModule> modules;

    private ModuleManager() {
        modules = new ArrayMap<>();
    }

    private ModuleManager(Collection<CalendulaModule> modules) {
        this();
        addAll(modules);
    }

    public static ModuleManager getInstance() {
        if (theInstance == null) {
            LogUtil.d(TAG, "getInstance: ModuleManager initialized");
            theInstance = new ModuleManager();
        }
        return theInstance;
    }

    public static boolean isEnabled(String moduleID) {
        return getInstance().isModuleEnabled(moduleID);
    }

    public void run(CalendulaModule module, Context ctx) {
        try {
            modules.put(module.getId(), module);
            module.onApplicationStartup(ctx);
            LogUtil.v(TAG, "Module ran: " + module.getId());
        } catch (Exception e) {
            LogUtil.e(TAG, "run: Error when enabling module \"" + module.getId() + "\": ", e);
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
        LogUtil.d(TAG, "runModules: Loading default module configuration");
        run(modules, ctx);
    }

    public void runModules(final String configName, Context ctx) {
        final List<CalendulaModule> modules = ModuleRegistry.getModulesForConfig(configName);
        LogUtil.d(TAG, "runModules: Loading module configuration: " + configName);
        if (modules == null)
            throw new IllegalArgumentException("Module config " + configName + "does not exist or is empty");
        run(modules, ctx);
    }

    public boolean isModuleEnabled(String moduleID) {
        return modules.keySet().contains(moduleID);
    }
}
