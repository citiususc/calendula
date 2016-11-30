package es.usc.citius.servando.calendula.modules;

import android.content.Context;

/**
 * Created by alvaro.brey.vilas on 30/11/16.
 */

public abstract class CalendulaModule {

    public abstract String getId();

    abstract void onApplicationStartup(Context ctx);

}
