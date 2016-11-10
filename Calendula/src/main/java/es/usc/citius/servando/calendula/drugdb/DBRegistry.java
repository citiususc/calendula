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
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.drugdb;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import es.usc.citius.servando.calendula.R;

/**
 * Created by joseangel.pineiro on 9/4/15.
 */
public class DBRegistry {

    private static DBRegistry instance;

    private Map<String, PrescriptionDBMgr> databases;

    private PrescriptionDBMgr defaultDBMgr;


    public  static DBRegistry instance(){
        if(instance == null){
            throw  new RuntimeException("DBRegistry is not initialized!");
        }
        return instance;
    }

    public  static DBRegistry init(Context ctx){
        if(instance==null) {
            instance = new DBRegistry();
            instance.databases = new HashMap<>();

            PrescriptionDBMgr aemps = new AEMPSPrescriptionDBMgr();
            aemps.setId(ctx.getString(R.string.database_aemps_id));
            aemps.setDisplayName(ctx.getString(R.string.database_aemps_display));
            aemps.setDescription(ctx.getString(R.string.database_aemps_desc));

//            PrescriptionDBMgr uk = new USPrescriptionDBMgr();
//            uk.setId(ctx.getString(R.string.database_uk_id));
//            uk.setDisplayName(ctx.getString(R.string.database_uk_display));
//            uk.setDescription(ctx.getString(R.string.database_uk_display));

            PrescriptionDBMgr us = new USPrescriptionDBMgr();
            us.setId(ctx.getString(R.string.database_us_id));
            us.setDisplayName(ctx.getString(R.string.database_us_display));
            us.setDescription(ctx.getString(R.string.database_us_display));

            instance.databases.put(aemps.id(), aemps);
//            instance.databases.put(uk.id(),uk);
            instance.databases.put(us.id(),us);

            instance.defaultDBMgr = aemps;
        }
        return instance;
    }

    public PrescriptionDBMgr db(String key){
        return databases.get(key);
    }

    public PrescriptionDBMgr current(Context ctx){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        String none = ctx.getString(R.string.database_none_id);
        String settingUp= ctx.getString(R.string.database_setting_up);
        String key = settings.getString("prescriptions_database",none);
        Log.d("DBRegistry", "Key: " + key);
        return (key!=null && !key.equals(none) && !key.equals(settingUp)) ? databases.get(key) :  defaultDBMgr;
    }

    public PrescriptionDBMgr defaultDBMgr(){
        return  defaultDBMgr;
    }

}
