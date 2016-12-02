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

package es.usc.citius.servando.calendula.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.joda.time.Duration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.util.HtmlCacheManager;
import es.usc.citius.servando.calendula.util.IconUtils;

public class WebViewActivity extends CalendulaActivity {

    /**
     * Request bean for WebViewActivity. Must be provided and must contain at least a URL.
     */
    public static final String PARAM_WEBVIEW_REQUEST = "webview_param_request";

    /**
     * Max cache size for AppCache
     */
    private static final Integer CACHE_MAX_SIZE = 8388608; //8mb

    private static final String TAG = "WebViewActivity";

    private static final String HTTP_ERROR_REGEXP = "^.*?(404|403|[nN]ot [fF]ound).*$";
    // reference to the request params
    WebViewRequest request;
    // handler to access activity methods from javascript interface
    Handler handler;
    MaterialStyledDialog loadingDialog;
    View toolbarSahdow;
    int color;

    //ProgressDialog progressDialog;
    private WebView webView;
    private String url;
    // switch to disable JavaScript in API<17
    private boolean isJavaScriptInsecure = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        handler = new Handler();
        //check api version to see if we can use JavaScript
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isJavaScriptInsecure = true;
        }

        //check for request and URL  and finish if not present
        request = getIntent().getParcelableExtra(PARAM_WEBVIEW_REQUEST);
        if (request == null || (url = request.getUrl()) == null) {
            Log.e(TAG, "onCreate: No WebViewRequest provided in intent!");
            showErrorToast(null);
            finish();
        } else {

            webView = (WebView) findViewById(R.id.webView1);
            toolbarSahdow = findViewById(R.id.tabs_shadow);

            //setup toolbar and statusbar
            color = DB.patients().getActive(this).color();
            String title = request.getTitle();
            setupToolbar(title, color);
            setupStatusBar(color);

            //setup the webView
            setupWebView(request);

        }

    }


    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        Log.d(TAG, "onActionModeStarted");
        if (toolbar != null) {
            toolbarSahdow.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
            setupStatusBar(getResources().getColor(R.color.dark_grey_home));
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        Log.d(TAG, "onActionModeFinished");
        if (toolbar != null) {
            setupStatusBar(color);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbar.setAlpha(0);
                    toolbarSahdow.setAlpha(0);
                    toolbar.setVisibility(View.VISIBLE);
                    toolbarSahdow.setVisibility(View.VISIBLE);
                    toolbar.animate().alpha(1).start();
                    toolbarSahdow.animate().alpha(1).start();
                }
            }, 300);


        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);

        IconicsDrawable icon = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_share_variant)
                .sizeDp(48)
                .paddingDp(6)
                .color(Color.WHITE);

        menu.getItem(0).setIcon(icon);

        IconicsDrawable icon2 = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_web)
                .sizeDp(48)
                .paddingDp(6)
                .color(Color.WHITE);

        menu.getItem(1).setIcon(icon2);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_link:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT,
                        url);
                i.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
                i.setType("text/plain");
                startActivity(Intent.createChooser(i, getString(R.string.title_share_link)));
                return true;
            case R.id.action_open_with_browser:
                Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i1);
                return true;
            default:
                onBackPressed();
                return true;
        }
    }

    /**
     * Whether a request needs html access after loading, that is, whether is must be
     * cached or processed and it has not been cached yet
     *
     * @param request the request
     */
    public boolean needsHtmlAccess(WebViewRequest request) {

        if (isCached()) {
            return false;
        } else if (request.needsPostprocessing()) {
            return true;
        }
        return !isJavaScriptInsecure &&
                request.cacheType.equals(WebViewRequest.CacheType.DOWNLOAD_CACHE);
    }

    private void setupWebView(final WebViewRequest request) {

        //enable JavaScript if it is explicitly enabled or custom css sheet must be injected
        if (request.isJavaScriptEnabled() || request.getCustomCss() != null) {
            if (!isJavaScriptInsecure) {
                Log.d(TAG, "Enabling JavaScript!");
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setJavaScriptEnabled(true);
            } else {
                Log.w(TAG, "Javascript cannot be enabled with API version < 17 due to security reasons, disabling.");
            }
        }


        final String originalUrl = url;
        Log.d(TAG, "Opening URL: " + originalUrl);

        //setup progressDialog
        String loadingMessage = request.getLoadingMessage();
        if (loadingMessage == null) loadingMessage = getString(R.string.message_generic_pleasewait);
        webView.setVisibility(View.INVISIBLE);
        showProgressDialog(loadingMessage);
        //misc webView settings
        //set single column layout
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //enable pinch to zoom
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setEnableSmoothTransition(true);

        //enable AppCache if requested
        if (request.getCacheType().equals(WebViewRequest.CacheType.APP_CACHE))
            enableAppCache();

        //enable download cache if requested
        String cachedData = null;
        if (needsHtmlAccess(request)) {
            webView.addJavascriptInterface(new SimpleJSCacheInterface(this), "HtmlCache");
        } else {
            cachedData = HtmlCacheManager.getInstance().get(originalUrl);
        }

        webView.setWebViewClient(new CustomWebViewClient(request));

        if (cachedData != null) {
            Log.d(TAG, "setupWebView: Loading page from cache");
            webView.loadData(cachedData, "text/html; charset=UTF-8", null);
        } else {
            Log.d(TAG, "setupWebView: Loading page from URL");
            webView.loadUrl(originalUrl);
        }
    }

    private void hideLoading() {
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    private void showProgressDialog(String loadingMsg) {
        final MaterialStyledDialog.Builder builder = new MaterialStyledDialog.Builder(this)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_file_document, R.color.white, 100))
                .setHeaderColor(R.color.android_blue)
                .withIconAnimation(false)
                .withDialogAnimation(false)
                .setCancelable(false)
                .setNegativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        webView.stopLoading();
                        loadingDialog.dismiss();
                        finish();
                    }
                });

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_loading_view, null);
        TextView customText = (TextView) customView.findViewById(R.id.loading_description);
        customText.setText(loadingMsg);

        builder.setCustomView(customView);

        loadingDialog = builder.show();
    }

    private boolean isCached() {
        return HtmlCacheManager.getInstance().isCached(url);
    }

    private void injectCSS(final String file, Map<String, String> overrides) {
        try {
            // read CSS from file
            InputStream inputStream = getAssets().open(file);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            // perform css replacements if any
            if (overrides != null && overrides.size() > 0) {
                String css = new String(buffer);
                for (Map.Entry<String, String> entry : overrides.entrySet()) {
                    css = css.replaceAll(entry.getKey(), entry.getValue());
                }
                buffer = css.getBytes();
            }
            //encode CSS string in base64
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            //inject CSS into the webpage <head> element
            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            Log.w(TAG, "injectCSS:" + file);
        }
    }

    private void enableAppCache() {
        Log.d(TAG, "Enabling cache with max size " + CACHE_MAX_SIZE + " bytes");
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(CACHE_MAX_SIZE);
        webView.getSettings().setAppCachePath(getFilesDir().getPath() + "data/" + getPackageName() + "/cache");
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void showErrorToast(String error) {
        if (error == null) error = getString(R.string.message_generic_pageloaderror);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Interface that must be implemented in order to access the page html
     * and make changes before it is displayed
     */
    public interface HtmlPostprocessor {
        String process(String html);
    }

    /**
     * Encapsulates a request for a {@link WebViewActivity}.
     * <p>
     */
    public static class WebViewRequest implements Parcelable {

        public static final Creator<WebViewRequest> CREATOR = new Creator<WebViewRequest>() {
            @Override
            public WebViewRequest createFromParcel(Parcel in) {
                return new WebViewRequest(in);
            }

            @Override
            public WebViewRequest[] newArray(int size) {
                return new WebViewRequest[size];
            }
        };
        private final String url;
        private String title = null;
        private String loadingMessage = null;
        private String connectionErrorMessage = null;
        private String notFoundErrorMessage = null;
        private String postProcessorClassname = null;
        private boolean javaScriptEnabled = false;
        private boolean externalLinksEnabled = false;
        private CacheType cacheType = CacheType.NO_CACHE;
        private String customCss = null;
        private Map<String, String> customCssOverrides = null;
        private Duration cacheTTL;

        public WebViewRequest(String url) {
            this.url = url;
        }

        public WebViewRequest(String url, String title, String loadingMessage,
                              String connectionErrorMessage, String notFoundErrorMessage,
                              String postProcessorClassname, boolean javaScriptEnabled,
                              boolean externalLinksEnabled, CacheType cacheType, String customCss,
                              Map<String, String> customCssOverrides, Duration cacheTTL) {
            this.url = url;
            this.title = title;
            this.loadingMessage = loadingMessage;
            this.connectionErrorMessage = connectionErrorMessage;
            this.notFoundErrorMessage = notFoundErrorMessage;
            this.postProcessorClassname = postProcessorClassname;
            this.javaScriptEnabled = javaScriptEnabled;
            this.externalLinksEnabled = externalLinksEnabled;
            this.cacheType = cacheType;
            this.customCss = customCss;
            this.customCssOverrides = customCssOverrides;
            this.cacheTTL = cacheTTL;
        }

        protected WebViewRequest(Parcel in) {
            url = in.readString();
            title = in.readString();
            loadingMessage = in.readString();
            connectionErrorMessage = in.readString();
            notFoundErrorMessage = in.readString();
            postProcessorClassname = in.readString();
            javaScriptEnabled = in.readByte() != 0;
            externalLinksEnabled = in.readByte() != 0;
            cacheType = CacheType.valueOf(in.readString());
            customCss = in.readString();
            cacheTTL = Duration.parse(in.readString());
            // read overrides map
            customCssOverrides = new HashMap<>();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                String value = in.readString();
                customCssOverrides.put(key, value);
            }
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(title);
            dest.writeString(loadingMessage);
            dest.writeString(connectionErrorMessage);
            dest.writeString(notFoundErrorMessage);
            dest.writeString(postProcessorClassname);
            dest.writeByte((byte) (javaScriptEnabled ? 1 : 0));
            dest.writeByte((byte) (externalLinksEnabled ? 1 : 0));
            dest.writeString(cacheType.toString());
            dest.writeString(customCss);
            dest.writeString(cacheTTL.toString());
            // write overrides map
            dest.writeInt(customCssOverrides.size());
            for (Map.Entry<String, String> entry : customCssOverrides.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        /**
         * Set the title for the webview title bar. Can be <code>null</code>, no title will be displayed if so.
         *
         * @param title the title
         */
        public void setTitle(String title) {
            this.title = title;
        }

        public String getLoadingMessage() {
            return loadingMessage;
        }

        /**
         * Set a custom loading message. A default message will be used if <code>null</code>.
         *
         * @param loadingMessage the message
         */
        public void setLoadingMessage(String loadingMessage) {
            this.loadingMessage = loadingMessage;
        }

        public String getConnectionErrorMessage() {
            return connectionErrorMessage;
        }

        /**
         * Set a custom error message in case the page can't be loaded for connection reasons. A default message will be used if <code>null</code>.
         *
         * @param errorMessage the message
         */
        public void setConnectionErrorMessage(String errorMessage) {
            this.connectionErrorMessage = errorMessage;
        }

        public String getNotFoundErrorMessage() {
            return notFoundErrorMessage;
        }

        /**
         * Set a custom error message in case the page does not exists. A default message will be used if <code>null</code>.
         *
         * @param errorMessage the message
         */
        public void setNotFoundErrorMessage(String errorMessage) {
            this.notFoundErrorMessage = errorMessage;
        }

        public boolean isJavaScriptEnabled() {
            return javaScriptEnabled;
        }

        /**
         * Enable/disable JavaScript for the webpage. Default is <code>false</code>.
         *
         * @param javaScriptEnabled <code>true</code> if Javascript should be enabled
         */
        public void setJavaScriptEnabled(boolean javaScriptEnabled) {
            this.javaScriptEnabled = javaScriptEnabled;
        }

        public boolean needsPostprocessing() {
            return this.postProcessorClassname != null;
        }

        /**
         * Name of a class that implements the HtmlPostprocessor interface, for accessing and modifying the
         * html before it is displayed. Default is <code>false</code>.
         *
         * @param processor the processor
         */
        public void setPostProcessorClassname(String processor) {
            this.postProcessorClassname = processor;
        }

        public String getCustomCss() {
            return customCss;
        }

        public Map<String, String> getCustomCssOverrides() {
            return customCssOverrides;
        }

        /**
         * Set a custom CSS sheet to be injected into the page. If <code>null</code>, no CSS will be loaded.
         * Injecting the CSS <b>requires JavaScript</b> and will override setJavaScriptEnabled during the injection.
         *
         * @param filename the filename
         */
        public void setCustomCss(String filename, Map<String, String> overrides) {
            this.customCss = filename;
            this.customCssOverrides = overrides;
        }

        public boolean isExternalLinksEnabled() {
            return externalLinksEnabled;
        }

        /**
         * Set if external links should be opened in the webview (<code>true</code>) or should launch an action intent (<code>false</code>).
         * Default value is <code>false</code>.
         *
         * @param externalLinksEnabled if external links should be enabled
         */
        public void setExternalLinksEnabled(boolean externalLinksEnabled) {
            this.externalLinksEnabled = externalLinksEnabled;
        }

        public CacheType getCacheType() {
            return cacheType;
        }

        /**
         * Set cache type for the webview:
         * - <code>NO_CACHE</code>: deactivate caching. Default value.
         * - <code>APP_CACHE</code>: HTML5 App Cache
         * - <code>DOWNLOAD_CACHE</code>: Cache full HTML document in database. Warning! Locally linked resources will be lost.
         *
         * @param cacheType the cache type
         */
        public void setCacheType(CacheType cacheType) {
            this.cacheType = cacheType;
        }

        public Duration getCacheTTL() {
            return cacheTTL;
        }

        /**
         * Set TTL for download cache. Only works if CacheType.DOWNLOAD_CACHE is set.
         * If <code>null</code>, a default TTL will be used.
         *
         * @param cacheTTL the ttl
         */
        public void setCacheTTL(Duration cacheTTL) {
            this.cacheTTL = cacheTTL;
        }

        public enum CacheType {
            NO_CACHE,
            APP_CACHE,
            DOWNLOAD_CACHE
        }
    }

    private class CustomWebViewClient extends WebViewClient {

        protected final WebViewRequest request;
        protected final String originalUrl;
        protected final String customCssSheet;

        public CustomWebViewClient(WebViewRequest request) {
            this.request = request;
            this.originalUrl = request.getUrl();
            this.customCssSheet = isJavaScriptInsecure ? null : request.getCustomCss();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //use webview only for the requested URL or suburls, unless external links are enabled
            if (url.contains(originalUrl) || request.isExternalLinksEnabled()) {
                return super.shouldOverrideUrlLoading(view, url);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            if (view.getTitle().matches(HTTP_ERROR_REGEXP)) {
                Log.e(TAG, "Received HTTP error, page title is: " + view.getTitle());
                showErrorToast(request.getNotFoundErrorMessage());
                hideLoading();
                WebViewActivity.this.finish();
            } else {
                // setup javascript interface if the request needs access to html
                if (needsHtmlAccess(request)) {
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl("javascript:window.HtmlCache.writeToCache" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                } else {
                    if (customCssSheet != null) {
                        webView.getSettings().setJavaScriptEnabled(true);
                        injectCSS(customCssSheet, request.getCustomCssOverrides());
                        //if JavaScript is not enabled explicitly, turn it off after CSS injection
                        webView.getSettings().setJavaScriptEnabled(request.isJavaScriptEnabled());
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            webView.setVisibility(View.VISIBLE);
                            hideLoading();
                        }
                    }, 200);
                    Log.d(TAG, "Finished loading URL: " + url);
                }
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e(TAG, "Received error when trying to load page");
            showErrorToast(request.getConnectionErrorMessage());
            hideLoading();
            finish();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest r, WebResourceResponse errorResponse) {
            Log.e(TAG, "Received HTTP Error when trying to load page");
            showErrorToast(request.getNotFoundErrorMessage());
            hideLoading();
            finish();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e(TAG, "Received SSL Error when trying to load page");
            showErrorToast(request.getConnectionErrorMessage());
            hideLoading();
            finish();
        }
    }

    private class SimpleJSCacheInterface {
        private Context ctx;

        SimpleJSCacheInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void writeToCache(String html) {

            final Duration ttl = request.getCacheTTL(); //can  be null
            String processed = null;
            // if there is a postprocessor enabled
            if (request.needsPostprocessing()) {
                // instantiate postprocessor
                try {
                    HtmlPostprocessor processor = (HtmlPostprocessor) Class.forName(request.postProcessorClassname).newInstance();
                    // get processed html
                    processed = processor.process(html);
                    // save it to cache if needed
                    if (request.cacheType.equals(WebViewRequest.CacheType.DOWNLOAD_CACHE)) {
                        HtmlCacheManager.getInstance().put(url, processed, ttl);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error trying to post process content", e);
                }
            }
            // in other case, simply write html content to cache
            else if (request.cacheType.equals(WebViewRequest.CacheType.DOWNLOAD_CACHE)) {
                HtmlCacheManager.getInstance().put(url, html, ttl);
            }

            final String finalHtml = processed != null ? processed : html;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    webView.setWebViewClient(new CustomWebViewClient(request) {
                                                 @Override
                                                 public void onPageFinished(WebView view, String url) {
                                                     if (customCssSheet != null) {
                                                         webView.getSettings().setJavaScriptEnabled(true);
                                                         injectCSS(customCssSheet, request.getCustomCssOverrides());
                                                         //if JavaScript is not enabled explicitly, turn it off after CSS injection
                                                         webView.getSettings().setJavaScriptEnabled(request.isJavaScriptEnabled());
                                                     }
                                                     handler.postDelayed(new Runnable() {
                                                         @Override
                                                         public void run() {
                                                             webView.setVisibility(View.VISIBLE);
                                                             webView.getSettings().setJavaScriptEnabled(request.isJavaScriptEnabled());
                                                             hideLoading();
                                                         }
                                                     }, 200);
                                                 }
                                             }
                    );
                    webView.loadData(finalHtml, "text/html; charset=UTF-8", null);
                    webView.clearHistory();
                }
            });


            // dismiss the loading dialog

        }
    }
}