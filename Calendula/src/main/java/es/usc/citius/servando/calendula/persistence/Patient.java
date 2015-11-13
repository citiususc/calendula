package es.usc.citius.servando.calendula.persistence;

import android.graphics.Color;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import es.usc.citius.servando.calendula.util.AvatarMgr;

/**
 * Models an user
 */
@DatabaseTable(tableName = "Patients")
public class Patient {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_DEFAULT = "Default";
    public static final String COLUMN_AVATAR = "Avatar";
    public static final String COLUMN_COLOR = "Color";


    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = COLUMN_DEFAULT)
    private boolean isDefault;

    @DatabaseField(columnName = COLUMN_AVATAR)
    private String avatar = AvatarMgr.DEFAULT_AVATAR;


    @DatabaseField(columnName = COLUMN_COLOR)
    private int color = Color.parseColor("#1976d2"); // material blue 700

    public Long id() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String avatar() {
        return avatar;
    }


    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int color(){
        return color;
    }
}
