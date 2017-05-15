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

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.modules.modules.AllergiesModule;
import es.usc.citius.servando.calendula.modules.modules.BaseModule;
import es.usc.citius.servando.calendula.modules.modules.PharmacyModule;
import es.usc.citius.servando.calendula.modules.modules.StockModule;
import es.usc.citius.servando.calendula.modules.modules.TestDataModule;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public class ModuleRegistry {

    private static final String TAG = "ModuleRegistry";

    public static List<CalendulaModule> getDefaultModules() {
        return getModulesForConfig(ModuleConfig.PRODUCT);
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
                LogUtil.e(TAG, "getModulesForConfig: An error occurred when trying to instantiate module", e);
                throw new RuntimeException(e);
            }
        }
        LogUtil.d(TAG, "getModulesForConfig: " + modules.size() + " modules instantiated successfully");
        return modules;
    }

    public enum ModuleConfig {
        PRODUCT(ModuleLists.STABLE_MODULES), CI(ModuleLists.UNSTABLE_MODULES), DEVELOP(ModuleLists.BLEEDING_MODULES);

        private Class<?>[] modList;

        ModuleConfig(Class<?>[] modules) {
            this.modList = modules;
        }
    }

    private static class ModuleLists {
        private static final Class<?>[] STABLE_MODULES = new Class<?>[]{
                BaseModule.class // Base module is required. Do not remove!
        };

        private static final Class<?>[] UNSTABLE_MODULES = ArrayUtils.addAll(
                STABLE_MODULES,
                PharmacyModule.class,
                AllergiesModule.class,
                StockModule.class);

        private static final Class<?>[] BLEEDING_MODULES = ArrayUtils.addAll(
                UNSTABLE_MODULES,
                TestDataModule.class);
    }
}
