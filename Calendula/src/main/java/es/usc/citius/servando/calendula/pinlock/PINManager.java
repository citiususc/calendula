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

import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;


public class PINManager {

    private static final String TAG = "PINManager";
    private static final String SALT_PATTERN = "%1$s%2$s";
    private static Random random;

    /**
     * Checks if the PIN number matches the currently stored one.
     *
     * @param pin the PIN number
     * @return <code>true</code> if the PIN matches, <code>false</code> otherwise
     * @throws IllegalStateException if there's no PIN stored currently.
     */
    public static boolean checkPIN(final String pin) throws IllegalStateException {

        final String salt = PreferenceUtils.getString(PreferenceKeys.UNLOCK_PIN_SALT, null);
        if (salt != null) {
            final String salted = String.format(SALT_PATTERN, salt, pin);
            final String hash = calculateHash(salted);
            if (hash != null) {
                final String storedHash = PreferenceUtils.getString(PreferenceKeys.UNLOCK_PIN_HASH, null);
                return storedHash != null && storedHash.equals(hash);
            } else {
                throw new RuntimeException("Failed to check PIN number");
            }
        } else {
            throw new IllegalStateException("No PIN currently stored!");
        }
    }

    /**
     * Checks if PIN is set
     *
     * @return <code>true</code> if PIN is set, <code>false</code> otherwise
     */
    public static boolean isPINSet() {
        return PreferenceUtils.getString(PreferenceKeys.UNLOCK_PIN_HASH, null) != null;
    }

    /**
     * Clears PIN info from prefs.
     */
    public static void clearPIN() {
        SharedPreferences.Editor edit = PreferenceUtils.edit();
        edit.remove(PreferenceKeys.UNLOCK_PIN_HASH.key());
        edit.remove(PreferenceKeys.UNLOCK_PIN_SALT.key());
        edit.remove(PreferenceKeys.FINGERPRINT_ENABLED.key());
        edit.apply();
    }

    /**
     * Stores the PIN number, overwriting the current one if present.
     *
     * @param pin the PIN number
     * @return <code>true</code> if saved correctly, <code>false</code> otherwise.
     */
    public static boolean savePIN(final String pin) {

        byte[] saltBytes = new byte[20];
        getRandom().nextBytes(saltBytes);
        final String salt = new String(saltBytes);

        final String salted = String.format(SALT_PATTERN, salt, pin);

        final String hash = calculateHash(salted);
        if (hash != null) {
            SharedPreferences.Editor edit = PreferenceUtils.edit();
            edit.putString(PreferenceKeys.UNLOCK_PIN_HASH.key(), hash);
            edit.putString(PreferenceKeys.UNLOCK_PIN_SALT.key(), salt);
            edit.apply();
            return true;
        } else {
            return false;
        }
    }

    private static Random getRandom() {
        if (random == null) {
            random = new SecureRandom();
        }
        return random;
    }

    private static String calculateHash(String message) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(message.getBytes());
            byte byteData[] = md.digest();
            return new String(byteData);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.e(TAG, "calculateHash: ", e);
            return null;
        }
    }

}
