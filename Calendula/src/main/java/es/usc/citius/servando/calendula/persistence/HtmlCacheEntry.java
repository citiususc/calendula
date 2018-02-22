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

package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "HtmlCache")
public class HtmlCacheEntry {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HASHCODE = "HashCode";
    public static final String COLUMN_TIMESTAMP = "Timestamp";
    public static final String COLUMN_DATA = "Data";
    public static final String COLUMN_TTL = "ttl";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;
    @DatabaseField(columnName = COLUMN_HASHCODE, unique = true)
    private Integer hashCode;

    @DatabaseField(dataType = DataType.DATE, columnName = COLUMN_TIMESTAMP)
    private Date timestamp;

    @DatabaseField(columnName = COLUMN_DATA)
    private String data;

    @DatabaseField(columnName = COLUMN_TTL)
    private Long ttl;

    public HtmlCacheEntry() {
    }

    public HtmlCacheEntry(Integer hashCode, Date timestamp, String data, Long ttl) {
        this.hashCode = hashCode;
        this.timestamp = timestamp;
        this.data = data;
        this.ttl = ttl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.hashCode = hashCode;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "HtmlCacheEntry{" +
                "hashCode=" + hashCode +
                ", timestamp=" + timestamp +
                ", ttl=" + ttl +
                '}';
    }
}
