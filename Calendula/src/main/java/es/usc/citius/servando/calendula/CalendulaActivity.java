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

package es.usc.citius.servando.calendula;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.EventBus;

import es.usc.citius.servando.calendula.util.ScreenUtils;


/**
 * Created by joseangel.pineiro on 10/30/15.
 */
@SuppressLint("Registered")
public abstract class CalendulaActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected CalendulaActivity setupToolbar(@Nullable String title, @ColorInt int color, @ColorInt int iconColor) {
        // set up the toolbar
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(color);
        toolbar.setNavigationIcon(getNavigationIcon(iconColor));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (title == null) {
            //set the back arrow in the toolbar
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);
        }
        return this;
    }

    protected CalendulaActivity setupToolbar(@Nullable String title, @ColorInt int color) {
        return setupToolbar(title, color, Color.WHITE);
    }

    protected CalendulaActivity setupStatusBar(@ColorInt int color) {
        ScreenUtils.setStatusBarColor(this, color);
        return this;
    }

    protected CalendulaActivity subscribeToEvents() {
        CalendulaApp.eventBus().register(this);
        return this;
    }

    protected CalendulaActivity unsubscribeFromEvents() {
        final EventBus eventBus = CalendulaApp.eventBus();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
        return this;
    }

    protected Drawable getNavigationIcon(@ColorInt int iconColor) {
        return new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back)
                .color(iconColor)
                .actionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeFromEvents();
    }
}
