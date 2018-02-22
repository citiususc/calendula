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
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;


public class NumberPadView extends FrameLayout {

    private static final String TAG = "NumberPadView";

    private NumberPadListener mListener;
    private GridLayout numpadLayout;
    private boolean enabled = true;

    public NumberPadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public NumberPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public NumberPadView(Context context) {
        super(context);
        initView();
    }


    public NumberPadListener getListener() {
        return mListener;
    }

    public void setListener(NumberPadListener listener) {
        this.mListener = listener;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    private void initView() {
        // inflate numpad
        numpadLayout = (GridLayout) inflate(getContext(), R.layout.view_num_pad, null);
        addView(numpadLayout);
        // set icon for the delete button
        final AppCompatImageButton deleteButton = (AppCompatImageButton) numpadLayout.findViewById(R.id.numpad_delete_btn);
        final IconicsDrawable deleteIcon = IconUtils.icon(getContext(), GoogleMaterial.Icon.gmd_tag_backspace, R.color.white, 20);
        deleteButton.setImageDrawable(deleteIcon);
        // add listeners
        for (int i = 0; i < numpadLayout.getChildCount(); i++) {
            numpadLayout.getChildAt(i).setOnClickListener(new NumpadButtonListener());
        }
    }


    public interface NumberPadListener {
        void onNumberClicked(int numberValue);

        void onDeleteClicked();
    }

    private class NumpadButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (isEnabled()) {
                final String tagValue = (String) v.getTag();
                numpadLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                switch (tagValue) {
                    case "0":
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                    case "8":
                    case "9":
                        onNumberClicked(tagValue);
                        break;
                    case "DEL":
                        onDelClicked();
                        break;
                    case "SPACER":
                        //noop
                        break;
                    default:
                        LogUtil.w(TAG, "onClick: invalid tag " + tagValue);
                        break;
                }
            } else {
                LogUtil.d(TAG, "onClick: This NumberPadView is disabled. Ignoring click");
            }
        }

        private void onNumberClicked(String numberValue) {
            LogUtil.v(TAG, "Number clicked: " + numberValue);
            if (mListener != null) {
                mListener.onNumberClicked(Integer.valueOf(numberValue));
            }
        }

        private void onDelClicked() {
            LogUtil.v(TAG, "Delete clicked");
            if (mListener != null) {
                mListener.onDeleteClicked();
            }
        }
    }
}
