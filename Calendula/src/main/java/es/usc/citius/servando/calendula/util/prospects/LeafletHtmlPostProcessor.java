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

package es.usc.citius.servando.calendula.util.prospects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import es.usc.citius.servando.calendula.activities.WebViewActivity;

/**
 * Give AEMPS leaflets a better look for WebView
 */
public class LeafletHtmlPostProcessor implements WebViewActivity.HtmlPostprocessor {

    @Override
    public String process(String html) {
        // Parse str into a Document
        Document doc = Jsoup.parseBodyFragment(html);
        doc.select("nav").remove();
        doc.select("div#pdfurl").remove();

        // white list to clean html
        Whitelist wl = Whitelist.relaxed();
        wl.addTags("div", "span", "p", "h1", "h2", "h3", "ul", "ol", "li", "a", "img");
        wl.preserveRelativeLinks(true);
        wl.addAttributes("img", "src");
        wl.addAttributes("a", "href");

        // perform cleaning
        Document cleaned = new Cleaner(wl).clean(doc);
        cleaned.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

        // Remove empty elements
        Set<String> removable = new HashSet<>(Arrays.asList("div", "span", "strong", "p", "h1", "h2", "h3", "ul", "ol", "li", "a"));
        cleaned.select("p:matchesOwn((?is) )").remove();
        // For each element in the cleaned document
        for (Element el : cleaned.getAllElements()) {
            if (el.children().isEmpty() && (!el.hasText() || el.text().replaceAll("\u00a0", "").trim().equals(""))) {
                // Element is empty, check if should be removed
                if (removable.contains(el.tagName())) el.remove();
            }
        }
        // return html for  display
        return cleaned.html();
    }
}

