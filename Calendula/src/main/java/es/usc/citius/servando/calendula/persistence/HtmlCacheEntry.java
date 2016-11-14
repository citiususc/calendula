package es.usc.citius.servando.calendula.persistence;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by alvaro.brey on 31/10/16.
 */
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
