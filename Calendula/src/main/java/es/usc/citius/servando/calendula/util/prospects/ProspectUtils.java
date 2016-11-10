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

package es.usc.citius.servando.calendula.util.prospects;

import android.app.Activity;
import android.content.Intent;

import org.joda.time.Duration;

import java.util.HashMap;
import java.util.Map;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.WebViewActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Prescription;
import es.usc.citius.servando.calendula.util.ScreenUtils;

/**
 * Utility functions to simplify dealing with prospects
 */
public class ProspectUtils {

    public static final Duration PROSPECT_TTL = Duration.standardDays(30);

    public static void openProspect(Prescription p, final Activity activity, boolean enableCache) {
        PrescriptionDBMgr dbMgr = DBRegistry.instance().current(activity);
        final String url = dbMgr.getProspectURL(p);

        Intent i = new Intent(activity, WebViewActivity.class);

        final Patient patient = DB.patients().getActive(activity);
        Map<String, String> overrides = new HashMap<String, String>() {{
            put("###SCREEN_WIDTH###", (int) (ScreenUtils.getDpSize(activity).x * 0.9) + "px");
            put("###PATIENT_COLOR###", String.format("#%06X", (0xFFFFFF & patient.color())));
        }};

        WebViewActivity.WebViewRequest request = new WebViewActivity.WebViewRequest(url);
        request.setCustomCss("prospectView.css", overrides);
        request.setConnectionErrorMessage(activity.getString(R.string.message_prospect_connection_error));
        request.setNotFoundErrorMessage(activity.getString(R.string.message_prospect_not_found_error));
        request.setLoadingMessage(activity.getString(R.string.message_prospect_loading));
        request.setTitle(activity.getString(R.string.title_prospect_webview));
        request.setPostProcessorClassname(LeafletHtmlPostProcessor.class.getCanonicalName());
        if (enableCache)
            request.setCacheType(WebViewActivity.WebViewRequest.CacheType.DOWNLOAD_CACHE);
        request.setJavaScriptEnabled(true);
        request.setCacheTTL(PROSPECT_TTL);
        i.putExtra(WebViewActivity.PARAM_WEBVIEW_REQUEST, request);
        activity.startActivity(i);
    }

}
