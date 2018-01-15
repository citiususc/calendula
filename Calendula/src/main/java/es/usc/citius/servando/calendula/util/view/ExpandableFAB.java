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

package es.usc.citius.servando.calendula.util.view;

import android.animation.Animator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by alvaro.brey.vilas on 15/12/17.
 */

public class ExpandableFAB extends FloatingActionButton {

    private static final String TAG = "ExpandableFAB";
    private static final int ROTATION_DEGREES = 135;

    private boolean isExpanded = false;
    private List<View> subViews = new ArrayList<>();

    public ExpandableFAB(Context context) {
        super(context);
    }

    public ExpandableFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addSubView(final View view) {
        if (!subViews.contains(view)) {
            subViews.add(view);
        } else {
            LogUtil.w(TAG, "addSubView: view already added, ignoring");
        }
    }

    public void expand() {
        if (!isExpanded) {
            this.animate().rotationBy(-ROTATION_DEGREES);
            for (int i = 0; i < subViews.size(); i++) {
                View subView = subViews.get(i);
                subView.setVisibility(VISIBLE);
                subView.animate().translationY(-(getResources().getDimension(R.dimen.fab_expand_base_margin)
                        + (i + 1) * getResources().getDimension(R.dimen.fab_expand_additive_margin)))
                .alpha(1);
            }
            isExpanded = true;
        } else {
            LogUtil.w(TAG, "expand: already expanded");
        }
    }

    public void collapse() {
        if (isExpanded) {
            this.animate().rotationBy(ROTATION_DEGREES);
            for (int i = subViews.size() - 1; i >= 0; i--) {
                final View subView = subViews.get(i);
                subView.animate().translationY(0).alpha(0).setListener(new CollapseListener(subView));
            }
            isExpanded = false;
        } else {
            LogUtil.w(TAG, "collapse: already collapsed");
        }
    }

    public void toggle() {
        if (isExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public List<View> getSubViews() {
        return subViews;
    }

    public void setSubViews(List<View> subViews) {
        this.subViews = subViews;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    private static class CollapseListener implements Animator.AnimatorListener {

        final View view;

        public CollapseListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            view.setVisibility(GONE);
            view.animate().setListener(null);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
