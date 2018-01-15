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

package es.usc.citius.servando.calendula.pinlock;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Created by alvaro.brey.vilas on 8/3/17.
 * <p>
 * Handles PIN input with individual insertion/deletion of numbers.
 */
public class PinInputStateManager {

    private static final String TAG = "PinInputStateManager";

    private final int PIN_SIZE;

    private final Deque<Integer> mBuffer;

    private PinInputCompleteListener mCompleteListener;
    private PinInputChangeListener mChangeListener;

    public PinInputStateManager(final int pinSize) {
        PIN_SIZE = pinSize;
        mBuffer = new ArrayDeque<>(pinSize);
    }

    public PinInputCompleteListener getCompleteListener() {
        return mCompleteListener;
    }

    public void setPinCompleteListener(PinInputCompleteListener mListener) {
        this.mCompleteListener = mListener;
    }

    public PinInputChangeListener getPinChangeListener() {
        return mChangeListener;
    }

    public void setPinChangeListener(PinInputChangeListener mChangeListener) {
        this.mChangeListener = mChangeListener;
    }

    /**
     * Puts a number into the pin.
     * If the pin size reaches <code>PIN_SIZE</code>, the {@link PinInputCompleteListener} will be called.
     * <p>
     * If PIN is full, it will not input anything, but will call the listener again.
     *
     * @param theNumber the number
     * @return <code>true</code> if input was successful, <code>false</code> otherwise
     */
    public boolean putNumber(Integer theNumber) {
        LogUtil.v(TAG, "putNumber() called with: theNumber = [" + theNumber + "]");
        boolean inserted = false;
        if (mBuffer.size() < PIN_SIZE) {
            LogUtil.v(TAG, "putNumber: inserted");
            mBuffer.push(theNumber);
            inserted = true;
            notifyChange();
        }
        if (mBuffer.size() == PIN_SIZE && mCompleteListener != null) {
            LogUtil.v(TAG, "putNumber: PIN is full");
            mCompleteListener.onComplete(getCurrent());
        }
        return inserted;
    }

    /**
     * Deletes the last element. Does nothing if empty.
     *
     * @return <code>true</code> if something was deleted
     */
    public boolean delete() {
        LogUtil.d(TAG, "delete() called");
        boolean deleted = false;
        if (!mBuffer.isEmpty()) {
            final Integer pop = mBuffer.pop();
            LogUtil.v(TAG, "delete: deleted " + pop);
            deleted = true;
            notifyChange();
        }
        return deleted;
    }

    /**
     * Clears the stored elements. Does nothing if empty.
     */
    public void clear() {
        LogUtil.v(TAG, "clear() called");
        mBuffer.clear();
        notifyChange();
    }

    /**
     * @return the current stored value for the PIN
     */
    public String getCurrent() {
        final Iterator<Integer> iterator = mBuffer.descendingIterator();
        StringBuilder b = new StringBuilder();
        while (iterator.hasNext()) {
            b.append(iterator.next());
        }
        final String thePin = b.toString();
        LogUtil.v(TAG, "getCurrent() returned: " + thePin);
        return thePin;
    }

    private void notifyChange() {
        if (mChangeListener != null) {
            mChangeListener.onPinChange(getCurrent(), mBuffer.size());
        }
    }


    public interface PinInputCompleteListener {
        public void onComplete(String pin);
    }

    public interface PinInputChangeListener {
        public void onPinChange(String currentPin, int pinLength);
    }


}
