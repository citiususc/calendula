package es.usc.citius.servando.calendula.database;


import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

public class DatabaseManager<H extends OrmLiteSqliteOpenHelper> {

    private H helper;

    @SuppressWarnings("unchecked")
    public H getHelper(Context context, Class<H> helperCls) {
        if (helper == null) {
            helper = (H) OpenHelperManager.getHelper(context, helperCls);
        }
        return helper;
    }

    public void releaseHelper(H helper) {
        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }
    }
}
