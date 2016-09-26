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

package es.usc.citius.servando.calendula.util.view;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;

/**
 * Created by joseangel.pineiro on 11/12/15.
 */
public class FABBehavior extends FloatingActionButton.Behavior {

    public FABBehavior() {
    }

    public FABBehavior(Context context, AttributeSet attributeSet) {
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if(dependency instanceof Snackbar.SnackbarLayout) {
            return super.onDependentViewChanged(parent, child, dependency);
        } else if(dependency instanceof AppBarLayout) {
            this.updateFabVisibility(parent, (AppBarLayout)dependency, child);
        }

        return false;
    }

    private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingActionButton child) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
        if(lp.getAnchorId() != appBarLayout.getId()) {
            return false;
        } else {

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            int point = child.getTop() - params.topMargin;
            try {
                Method method = AppBarLayout.class.getDeclaredMethod("getMinimumHeightForVisibleOverlappingContent");
                method.setAccessible(true);
                if(point <= (int) method.invoke(appBarLayout)) {
                    child.hide();
                } else {
                    child.show();
                }
                return true;
            } catch (Exception e) {
                return true;
            }
        }
    }
}
