
/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2017 CITIUS - USC
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

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;

import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.database.GenericDao;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ATCCode;


public class ATCCodeDAO extends GenericDao<ATCCode, Long> {

    public static final String TAG = "ATCCodeDAO";
    public static final String CONCAT_SEPARATOR = "/";

    private Dao<ATCCode, Long> daoInstance = null;

    public ATCCodeDAO(DatabaseHelper db) {
        super(db);
    }

    @Override
    public Dao<ATCCode, Long> getConcreteDao() {
        try {
            if (daoInstance == null)
                daoInstance = dbHelper.getDao(ATCCode.class);
            return daoInstance;
        } catch (SQLException e) {
            Log.e(TAG, "Error creating ATCCode DAO", e);
            throw new RuntimeException("Error creating ATCCode DAO", e);
        }
    }

    public List<ATCCode> searchByTagGroupByTag(final String search) {
        try {
            Log.d(TAG, "searchByTagGroupByTag: Searching \"" + search + "\"");
            QueryBuilder<ATCCode, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            w.like(ATCCode.COLUMN_TAG, "%" + search + "%");
            qb.groupBy(ATCCode.COLUMN_TAG);
            qb.orderBy(ATCCode.COLUMN_TAG, true);
            qb.setWhere(w);
            return qb.query();
        } catch (SQLException e) {
            Log.e(TAG, "searchByTagGroupByTag: ", e);
            throw new RuntimeException(e);
        }
    }

    public List<ATCCode> searchByTagOrCodeGroupByTag(final String search) {
        try {
            Log.d(TAG, "searchByTagGroupByTag: Searching \"" + search + "\"");
            QueryBuilder<ATCCode, Long> qb = dao.queryBuilder();
            Where w = qb.where();
            final String wildcard = "%" + search + "%";
            w.like(ATCCode.COLUMN_TAG, wildcard);
            w.or().like(ATCCode.COLUMN_CODE, wildcard);
            qb.groupBy(ATCCode.COLUMN_TAG);
            qb.orderBy(ATCCode.COLUMN_TAG, true);
            qb.setWhere(w);
            return qb.query();
        } catch (SQLException e) {
            Log.e(TAG, "searchByTagGroupByTag: ", e);
            throw new RuntimeException(e);
        }
    }

    public List<ATCCode> searchByTagOrCodeGroupByTagConcat(final String search) {
        Log.d(TAG, "searchByTagOrCodeGroupByTagConcat() called with: search = [" + search + "]");
        final String sq = String.format("%%%s%%", search);
        try {
            final GenericRawResults<ATCCode> atcCodes = dao.queryRaw("select Tag, group_concat(Code, '/') from ATCCode where Tag like ? or Tag like ? group by Tag;", new RawRowMapper<ATCCode>() {
                @Override
                public ATCCode mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return new ATCCode(resultColumns[1], resultColumns[0]);
                }
            }, sq, sq);
            return atcCodes.getResults();
        } catch (SQLException e) {
            Log.e(TAG, "searchByTagOrCodeGroupByTagConcat: ", e);
            throw new RuntimeException(e);
        }
    }


}