package es.usc.citius.servando.calendula;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import es.usc.citius.servando.calendula.util.ScreenUtils;


/**
 * Created by joseangel.pineiro on 10/30/15.
 */
public class CalendulaActivity extends AppCompatActivity{

    protected Toolbar toolbar;

    protected CalendulaActivity setupToolbar(String title, int color){
        // set up the toolbar
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(color);
        toolbar.setNavigationIcon(getNavigationIcon());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

        if (title == null) {
            //set the back arrow in the toolbar
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } else {
            getSupportActionBar().setTitle(title);
        }
        return this;
    }

    protected CalendulaActivity setupStatusBar(int color){
        ScreenUtils.setStatusBarColor(this, color);
        return this;
    }

    protected CalendulaActivity subscribeToEvents() {
        CalendulaApp.eventBus().register(this);
        return this;
    }

    protected CalendulaActivity unsubscribeFromEvents() {
        CalendulaApp.eventBus().unregister(this);
        return this;
    }

    protected Drawable getNavigationIcon(){
        return new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back)
                .sizeDp(24)
                .paddingDp(2)
                .colorRes(R.color.white);
    }






}
