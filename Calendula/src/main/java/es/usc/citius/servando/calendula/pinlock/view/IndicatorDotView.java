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

package es.usc.citius.servando.calendula.pinlock.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.IconUtils;


public class IndicatorDotView extends LinearLayout {

    private static final int DOT_SIZE = 26;

    private final IconicsDrawable unmarkedIcon = IconUtils.icon(getContext(), GoogleMaterial.Icon.gmd_circle, R.color.white_50, DOT_SIZE, 4);
    private final IconicsDrawable markedIcon = IconUtils.icon(getContext(), GoogleMaterial.Icon.gmd_circle, R.color.white, DOT_SIZE, 4);
    private final IconicsDrawable errorIcon = IconUtils.icon(getContext(), GoogleMaterial.Icon.gmd_circle, R.color.white, DOT_SIZE, 4);
    private Animation shakeAnim;

    private List<IconicsImageView> theDots;
    private int index = 0;
    private int size;


    public IndicatorDotView(Context context) {
        super(context);
        initView();
    }

    public IndicatorDotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public IndicatorDotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setSize(final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        this.size = size;
        if (theDots != null) {
            theDots.clear();
            removeAllViews();
        } else {
            theDots = new ArrayList<>();
        }
        for (int i = 0; i < this.size; i++) {
            IconicsImageView iv = new IconicsImageView(getContext());
            iv.setImageDrawable(unmarkedIcon);
            theDots.add(iv);
            addView(iv);
        }
    }

    public void markOne() {
        if (index < (size - 1)) {
            markCurrent();
            index++;
        }
    }

    public void setMarked(int marked) {
        if (marked > index) {
            while (index < marked) {
                markCurrent();
                index++;
            }
        } else if (index > marked) {
            while (index > marked) {
                index--;
                unmarkCurrent();
            }
        }
        if (index < 0)
            index = 0;
    }


    public void clearOne() {
        if (index > 0) {
            index--;
            unmarkCurrent();
        }
    }

    public void clearAll() {
        if (index > 0) {
            while (index > 0) {
                index--;
                unmarkCurrent();
            }
        }
        index = 0;
    }

    public void error() {
        for (int i = 0; i < size; i++) {
            final IconicsImageView currentDot = theDots.get(i);
            currentDot.setIcon(errorIcon);
        }
        this.startAnimation(shakeAnim);
    }

    private void unmarkCurrent() {
        final IconicsImageView currentDot = theDots.get(index);
        currentDot.setIcon(unmarkedIcon);
    }

    private void markCurrent() {
        final IconicsImageView currentDot = theDots.get(index);
        currentDot.setIcon(markedIcon);
    }

    private void initView() {
        this.setOrientation(HORIZONTAL);
        shakeAnim = new TranslateAnimation(0, 10, 0, 0);
        shakeAnim.setDuration(800);
        shakeAnim.setInterpolator(new CycleInterpolator(7));
        shakeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                index = size;
                clearAll();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
