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

package es.usc.citius.servando.calendula.util;


import android.content.Context;
import android.graphics.Typeface;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.ITypeface;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class PresentationsTypeface implements ITypeface {
    private static final String TTF_FILE = "presentation_icons.ttf";

    private static Typeface typeface = null;

    private static HashMap<String, Character> mChars;

    @Override
    public IIcon getIcon(String key) {
        return Icon.valueOf(key);
    }

    @Override
    public HashMap<String, Character> getCharacters() {
        if (mChars == null) {
            HashMap<String, Character> aChars = new HashMap<>();
            for (Icon v : Icon.values()) {
                aChars.put(v.name(), v.character);
            }
            mChars = aChars;
        }

        return mChars;
    }

    @Override
    public String getMappingPrefix() {
        return "med-presentation";
    }

    @Override
    public String getFontName() {
        return "Calendula medicine Icons";
    }

    @Override
    public String getVersion() {
        return "4.4.0";
    }

    @Override
    public int getIconCount() {
        return mChars.size();
    }

    @Override
    public Collection<String> getIcons() {
        Collection<String> icons = new LinkedList<>();

        for (Icon value : Icon.values()) {
            icons.add(value.name());
        }

        return icons;
    }

    @Override
    public String getAuthor() {
        return "Calendula Team";
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLicense() {
        return "";
    }

    @Override
    public String getLicenseUrl() {
        return "";
    }

    @Override
    public Typeface getTypeface(Context context) {
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + TTF_FILE);
            } catch (Exception e) {
                return null;
            }
        }
        return typeface;
    }

    public enum Icon implements IIcon {

        ic_drops('\ue901'),
        ic_effervescent('\ue902'),
        ic_inhaler('\ue903'),
        ic_injection('\ue904'),
        ic_capsule('\ue905'),
        ic_pill('\ue906'),
        ic_spray('\ue90d'),
        ic_syrup('\ue908'),
        ic_patch('\ue909'),
        ic_powder('\ue91f'),
        ic_cream('\ue90c'),
        ic_pomade('\ue900'),
        ic_powder_patch('\ue90e'),
        ic_circle('\ue92f'),
        ic_triangle('\ue92e'),
        ic_square('\ue930'),
        ic_pentagon('\ue933'),
        ic_hexagon('\ue932'),
        ic_diamond('\ue931'),
        ic_unknown('\ue907');

        // remember the typeface so we can use it later
        private static ITypeface typeface;
        char character;

        Icon(char character) {
            this.character = character;
        }

        public String getFormattedName() {
            return "{" + name() + "}";
        }

        public char getCharacter() {
            return character;
        }

        public String getName() {
            return name();
        }

        public ITypeface getTypeface() {
            if (typeface == null) {
                typeface = new PresentationsTypeface();
            }
            return typeface;
        }
    }
}
