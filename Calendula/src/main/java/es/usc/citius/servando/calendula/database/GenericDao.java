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

package es.usc.citius.servando.calendula.database;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.ObjectFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by joseangel.pineiro
 */
public abstract class GenericDao<T extends Object, I> implements Dao<T, I> {

    protected Dao<T, I> dao;
    DatabaseHelper dbHelper;

    public GenericDao(DatabaseHelper db) {
        dbHelper = db;
        this.dao = getConcreteDao();
    }

    public abstract Dao<T, I> getConcreteDao();

    /**
     * Fires an event indication changes in this collection
     * To be implemented by subclasses if necessary
     */
    public void fireEvent() {
        // default implementation: do nothing
    }

    public void save(T model) {
        try {
            dao.createOrUpdate(model);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving model", e);
        }
    }

    public void saveAndFireEvent(T model) {
        save(model);
        fireEvent();
    }


    public List<T> findAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding models", e);
        }
    }

    public T findById(I id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public T findOneBy(String columnName, Object value) {
        try {
            return dao.queryBuilder().where().eq(columnName, value).queryForFirst();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public List<T> findBy(String columnName, Object value) {
        try {
            return dao.queryBuilder()
                    .where().eq(columnName, value)
                    .query();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public List<T> like(String columnName, Object value, Long limit) {
        try {
            QueryBuilder<T, I> qb = dao.queryBuilder();
            qb.where().like(columnName, value);
            qb.orderBy(columnName, false);
            qb.limit(limit);
            return qb.query();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding model", e);
        }
    }

    public int count() {
        try {
            return (int) countOf();
        } catch (SQLException e) {
            throw new RuntimeException("Error in coutOf", e);
        }

    }

    public void remove(T model) {
        try {
            delete(model);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting model", e);
        }

    }


    // DELEGATE METHODS

    public T queryForId(I i) throws SQLException {
        return dao.queryForId(i);
    }

    public T queryForFirst(PreparedQuery<T> preparedQuery) throws SQLException {
        return dao.queryForFirst(preparedQuery);
    }

    @Override
    public List<T> queryForAll() throws SQLException {
        return dao.queryForAll();
    }

    @Override
    public List<T> queryForEq(String fieldName, Object value) throws SQLException {
        return dao.queryForEq(fieldName, value);
    }

    public List<T> queryForMatching(T matchObj) throws SQLException {
        return dao.queryForMatching(matchObj);
    }

    public List<T> queryForMatchingArgs(T matchObj) throws SQLException {
        return dao.queryForMatchingArgs(matchObj);
    }

    @Override
    public List<T> queryForFieldValues(Map<String, Object> fieldValues) throws SQLException {
        return dao.queryForFieldValues(fieldValues);
    }

    @Override
    public List<T> queryForFieldValuesArgs(Map<String, Object> fieldValues) throws SQLException {
        return dao.queryForFieldValuesArgs(fieldValues);
    }

    public T queryForSameId(T data) throws SQLException {
        return dao.queryForSameId(data);
    }

    @Override
    public QueryBuilder<T, I> queryBuilder() {
        return dao.queryBuilder();
    }

    @Override
    public UpdateBuilder<T, I> updateBuilder() {
        return dao.updateBuilder();
    }

    @Override
    public DeleteBuilder<T, I> deleteBuilder() {
        return dao.deleteBuilder();
    }

    public List<T> query(PreparedQuery<T> preparedQuery) throws SQLException {
        return dao.query(preparedQuery);
    }

    public int create(T data) throws SQLException {
        return dao.create(data);
    }

    public T createIfNotExists(T data) throws SQLException {
        return dao.createIfNotExists(data);
    }

    public CreateOrUpdateStatus createOrUpdate(T data) throws SQLException {
        return dao.createOrUpdate(data);
    }

    public int update(T data) throws SQLException {
        return dao.update(data);
    }

    public int updateId(T data, I newId) throws SQLException {
        return dao.updateId(data, newId);
    }

    public int update(PreparedUpdate<T> preparedUpdate) throws SQLException {
        return dao.update(preparedUpdate);
    }

    public int refresh(T data) throws SQLException {
        return dao.refresh(data);
    }

    public int delete(T data) throws SQLException {
        return dao.delete(data);
    }

    public int deleteById(I i) throws SQLException {
        return dao.deleteById(i);
    }

    public int delete(Collection<T> datas) throws SQLException {
        return dao.delete(datas);
    }

    public int deleteIds(Collection<I> is) throws SQLException {
        return dao.deleteIds(is);
    }

    public int delete(PreparedDelete<T> preparedDelete) throws SQLException {
        return dao.delete(preparedDelete);
    }

    @Override
    public CloseableIterator<T> iterator() {
        return dao.iterator();
    }

    @Override
    public CloseableIterator<T> iterator(int resultFlags) {
        return dao.iterator(resultFlags);
    }

    public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery) throws SQLException {
        return dao.iterator(preparedQuery);
    }

    public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery, int resultFlags) throws SQLException {
        return dao.iterator(preparedQuery, resultFlags);
    }

    @Override
    public CloseableWrappedIterable<T> getWrappedIterable() {
        return dao.getWrappedIterable();
    }

    public CloseableWrappedIterable<T> getWrappedIterable(PreparedQuery<T> preparedQuery) {
        return dao.getWrappedIterable(preparedQuery);
    }

    @Override
    public void closeLastIterator() throws SQLException {
        dao.closeLastIterator();
    }

    @Override
    public GenericRawResults<String[]> queryRaw(String query, String... arguments) throws SQLException {
        return dao.queryRaw(query, arguments);
    }

    @Override
    public <UO> GenericRawResults<UO> queryRaw(String query, RawRowMapper<UO> mapper, String... arguments) throws SQLException {
        return dao.queryRaw(query, mapper, arguments);
    }

    @Override
    public GenericRawResults<Object[]> queryRaw(String query, DataType[] columnTypes, String... arguments) throws SQLException {
        return dao.queryRaw(query, columnTypes, arguments);
    }

    @Override
    public long queryRawValue(String query, String... arguments) throws SQLException {
        return dao.queryRawValue(query, arguments);
    }

    @Override
    public int executeRaw(String statement, String... arguments) throws SQLException {
        return dao.executeRaw(statement, arguments);
    }

    @Override
    public int executeRawNoArgs(String statement) throws SQLException {
        return dao.executeRawNoArgs(statement);
    }

    @Override
    public int updateRaw(String statement, String... arguments) throws SQLException {
        return dao.updateRaw(statement, arguments);
    }

    @Override
    public <CT> CT callBatchTasks(Callable<CT> callable) throws Exception {
        return dao.callBatchTasks(callable);
    }

    public String objectToString(T data) {
        return dao.objectToString(data);
    }

    public boolean objectsEqual(T data1, T data2) throws SQLException {
        return dao.objectsEqual(data1, data2);
    }

    public I extractId(T data) throws SQLException {
        return dao.extractId(data);
    }

    @Override
    public Class<T> getDataClass() {
        return dao.getDataClass();
    }

    @Override
    public FieldType findForeignFieldType(Class<?> clazz) {
        return dao.findForeignFieldType(clazz);
    }

    @Override
    public boolean isUpdatable() {
        return dao.isUpdatable();
    }

    @Override
    public boolean isTableExists() throws SQLException {
        return dao.isTableExists();
    }

    @Override
    public long countOf() throws SQLException {
        return dao.countOf();
    }

    public long countOf(PreparedQuery<T> preparedQuery) throws SQLException {
        return dao.countOf(preparedQuery);
    }

    public void assignEmptyForeignCollection(T parent, String fieldName) throws SQLException {
        dao.assignEmptyForeignCollection(parent, fieldName);
    }

    @Override
    public <FT> ForeignCollection<FT> getEmptyForeignCollection(String fieldName) throws SQLException {
        return dao.getEmptyForeignCollection(fieldName);
    }

    @Override
    public void setObjectCache(boolean enabled) throws SQLException {
        dao.setObjectCache(enabled);
    }

    @Override
    public ObjectCache getObjectCache() {
        return dao.getObjectCache();
    }

    @Override
    public void setObjectCache(ObjectCache objectCache) throws SQLException {
        dao.setObjectCache(objectCache);
    }

    @Override
    public void clearObjectCache() {
        dao.clearObjectCache();
    }

    @Override
    public T mapSelectStarRow(DatabaseResults results) throws SQLException {
        return dao.mapSelectStarRow(results);
    }

    @Override
    public GenericRowMapper<T> getSelectStarRowMapper() throws SQLException {
        return dao.getSelectStarRowMapper();
    }

    @Override
    public RawRowMapper<T> getRawRowMapper() {
        return dao.getRawRowMapper();
    }

    public boolean idExists(I i) throws SQLException {
        return dao.idExists(i);
    }

    @Override
    public DatabaseConnection startThreadConnection() throws SQLException {
        return dao.startThreadConnection();
    }

    @Override
    public void endThreadConnection(DatabaseConnection connection) throws SQLException {
        dao.endThreadConnection(connection);
    }

    @Override
    public void setAutoCommit(DatabaseConnection connection, boolean autoCommit) throws SQLException {
        dao.setAutoCommit(connection, autoCommit);
    }

    @Override
    @Deprecated
    public boolean isAutoCommit() throws SQLException {
        return dao.isAutoCommit();
    }

    @Override
    @Deprecated
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        dao.setAutoCommit(autoCommit);
    }

    @Override
    public boolean isAutoCommit(DatabaseConnection connection) throws SQLException {
        return dao.isAutoCommit(connection);
    }

    @Override
    public void commit(DatabaseConnection connection) throws SQLException {
        dao.commit(connection);
    }

    @Override
    public void rollBack(DatabaseConnection connection) throws SQLException {
        dao.rollBack(connection);
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return dao.getConnectionSource();
    }

    public void setObjectFactory(ObjectFactory<T> objectFactory) {
        dao.setObjectFactory(objectFactory);
    }

    @Override
    public CloseableIterator<T> closeableIterator() {
        return dao.closeableIterator();
    }



}
