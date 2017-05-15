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

package es.usc.citius.servando.calendula.drugdb.model.database;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * This class was generated automatically.
 * Please check its consistency and completeness carefully.
 */
public class HomogeneousGroupDAO extends GenericDao<HomogeneousGroup, Long> {

    private static final String TAG = "HomogeneousGroupDAO";

    private Dao<HomogeneousGroup, Long> daoInstance = null;

    public HomogeneousGroupDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<HomogeneousGroup, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(HomogeneousGroup.class);
            return daoInstance;
        } catch (SQLException e) {
            LogUtil.e(TAG, "Error creating HomogeneousGroup DAO", e);
            throw new RuntimeException("Error creating HomogeneousGroup DAO", e);
        }
    }

    public boolean empty() {
        return count() <= 0;
    }


}