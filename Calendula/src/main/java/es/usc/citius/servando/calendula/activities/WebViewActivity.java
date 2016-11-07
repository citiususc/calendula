package es.usc.citius.servando.calendula.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
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
import android.widget.Toast;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.InputStream;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.HtmlCacheManager;
import es.usc.citius.servando.calendula.util.ScreenUtils;

public class WebViewActivity extends CalendulaActivity {

    /**
     * Request bean for WebViewActivity. Must be provided and must contain at least a URL.
     */
    public static final String PARAM_WEBVIEW_REQUEST = "webview_param_request";

    /**
     * Max cache size for AppCache
     */
    private static final Integer CACHE_MAX_SIZE = 8388608; //8mb

    private static final Integer LEAFLET_CACHE_TTL_MILLIS = 259200000;

    private static final String TAG = "WebViewActivity";

    private static final String HTTP_ERROR_REGEXP = "^.*?(404|403|[nN]ot [fF]ound).*$";

    private WebView webView;

    private String url;

    // switch to disable JavaScript in API<17
    private boolean isJavaScriptInsecure = false;

    // switch to disable caching
    private boolean errorDisableCache = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


        //check api version to see if we can use JavaScript
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isJavaScriptInsecure = true;
        }

        //check for request and URL  and finish if not present
        WebViewRequest request = getIntent().getParcelableExtra(PARAM_WEBVIEW_REQUEST);
        if (request == null || (url=request.getUrl()) == null) {
            Log.e(TAG, "onCreate: No WebViewRequest provided in intent!");
            showErrorToast();
            finish();
        } else {

            webView = (WebView) findViewById(R.id.webView1);

            //setup toolbar and statusbar
            int color = DB.patients().getActive(this).color();
            String title = request.getTitle();
            setupToolbar(title, color);
            setupStatusBar(color);

            //setup the webView
            setupWebView(request);

        }

    }

    private void setupWebView(final WebViewRequest request) {

        //enable JavaScript if it is explicitly enabled or custom css sheet must be injected
        if (request.isJavaScriptEnabled() || request.getCustomCss() != null) {
            if (!isJavaScriptInsecure) {
                Log.d(TAG, "Enabling JavaScript!");
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
        final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.title_generic_loading), loadingMessage);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

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
        if (!isJavaScriptInsecure && request.getCacheType().equals(WebViewRequest.CacheType.DOWNLOAD_CACHE)) {
            if (!isCached()) {
                webView.addJavascriptInterface(new SimpleJSCacheInterface(this), "HtmlCache");
            } else {
                cachedData = HtmlCacheManager.getInstance().get(originalUrl);
            }
        }

        final String customCssSheet = isJavaScriptInsecure ? null : request.getCustomCss();

        webView.setWebViewClient(
                new WebViewClient() {

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
                            showErrorToast();
                            finish();
                        }

                        if (customCssSheet != null) {
                            injectCSS(customCssSheet);

                            //if JavaScript is not enabled explicitly, turn it off after CSS injection
                            webView.getSettings().setJavaScriptEnabled(request.isJavaScriptEnabled());
                        }
                        Log.d(TAG, "Finished loading URL: " + url);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (!isJavaScriptInsecure && !errorDisableCache && request.cacheType.equals(WebViewRequest.CacheType.DOWNLOAD_CACHE) && !isCached()) {
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.loadUrl("javascript:window.HtmlCache.writeToCache" +
                                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                            webView.getSettings().setJavaScriptEnabled(request.isJavaScriptEnabled());
                        }

                    }

                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Log.e(TAG, "Received error when trying to load page");
                        showErrorToast();
                        finish();
                    }

                    @Override
                    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                        Log.e(TAG, "Received HTTP Error when trying to load page");
                        showErrorToast();
                        finish();
                    }

                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        Log.e(TAG, "Received SSL Error when trying to load page");
                        showErrorToast();
                        finish();
                    }
                });

        if (cachedData != null) {
            Log.d(TAG, "setupWebView: Loading page from cache");
            webView.loadData(cachedData, "text/html; charset=UTF-8", null);
        } else {
            Log.d(TAG, "setupWebView: Loading page from URL");
            webView.loadUrl(originalUrl);
        }
    }

    private boolean isCached() {
        return HtmlCacheManager.getInstance().isCached(url);
    }

    private void injectCSS(final String file) {
        try {
            // read CSS from file
            InputStream inputStream = getAssets().open(file);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            // get a reference to the active patient
            Patient p = DB.patients().getActive(this);
            // get patient color in hex format
            String hexColor = String.format("#%06X", (0xFFFFFF & p.color()));
            // get screen width
            float x = ScreenUtils.getDpSize(this).x;
            // replace screen width placeholders
            String css = new String(buffer).replaceAll("###SCREEN_WIDTH###", (int)(x*0.9)+"px");
            // replace patient color placeholders
            css = css.replaceAll("###PATIENT_COLOR###", hexColor);
            //encode CSS string in base64
            String encoded = Base64.encodeToString(css.getBytes(), Base64.NO_WRAP);
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
        webView.getSettings().setAppCachePath("/data/data/" + getPackageName() + "/cache");
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void showErrorToast() {

        errorDisableCache = false;
        final WebViewRequest request = getIntent().getParcelableExtra(PARAM_WEBVIEW_REQUEST);
        String error = request.getErrorMessage();

        if (error == null) error = getString(R.string.message_generic_pageloaderror);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        //link navigation
        if (webView.canGoBack())
            webView.goBack();
        else
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
                startActivity(Intent.createChooser(i,getString(R.string.title_share_link)));
                return true;
            case R.id.action_open_with_browser:
                Intent i1=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                startActivity(i1);
                return true;
            default:
                onBackPressed();
                return true;
        }
    }


    /**
     * Encapsulates a request for a {@link WebViewActivity}.
     * <p>
     */
    public static class WebViewRequest implements Parcelable {

        private final String url;
        private String title = null;
        private String loadingMessage = null;
        private String errorMessage = null;
        private boolean javaScriptEnabled = false;
        private boolean externalLinksEnabled = false;
        private CacheType cacheType = CacheType.NO_CACHE;
        private String customCss = null;

        public enum CacheType {
            NO_CACHE,
            APP_CACHE,
            DOWNLOAD_CACHE
        }

        public WebViewRequest(String url) {
            this.url = url;
        }

        public WebViewRequest(String url, String title, String loadingMessage, String errorMessage, boolean javaScriptEnabled, boolean externalLinksEnabled, CacheType cacheType, String customCss) {
            this.url = url;
            this.title = title;
            this.loadingMessage = loadingMessage;
            this.errorMessage = errorMessage;
            this.javaScriptEnabled = javaScriptEnabled;
            this.externalLinksEnabled = externalLinksEnabled;
            this.cacheType = cacheType;
            this.customCss = customCss;
        }

        protected WebViewRequest(Parcel in) {
            url = in.readString();
            title = in.readString();
            loadingMessage = in.readString();
            errorMessage = in.readString();
            javaScriptEnabled = in.readByte() != 0;
            externalLinksEnabled = in.readByte() != 0;
            cacheType = CacheType.valueOf(in.readString());
            customCss = in.readString();
        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(title);
            dest.writeString(loadingMessage);
            dest.writeString(errorMessage);
            dest.writeByte((byte) (javaScriptEnabled ? 1 : 0));
            dest.writeByte((byte) (externalLinksEnabled ? 1 : 0));
            dest.writeString(cacheType.toString());
            dest.writeString(customCss);
        }

        @Override
        public int describeContents() {
            return 0;
        }

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

        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Set a custom error message in case the page can't be loaded. A default message will be used if <code>null</code>.
         *
         * @param errorMessage the message
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public boolean isJavaScriptEnabled() {
            return javaScriptEnabled;
        }

        /**
         * Enable/disable JavaScript for the webpage. Default is <code>false</code>.
         *
         * @param javaScriptEnabled
         */
        public void setJavaScriptEnabled(boolean javaScriptEnabled) {
            this.javaScriptEnabled = javaScriptEnabled;
        }

        public String getCustomCss() {
            return customCss;
        }

        /**
         * Set a custom CSS sheet to be injected into the page. If <code>null</code>, no CSS will be loaded.
         * Injecting the CSS <b>requires JavaScript</b> and will override setJavaScriptEnabled during the injection.
         *
         * @param filename
         */
        public void setCustomCss(String filename) {
            this.customCss = filename;
        }

        public boolean isExternalLinksEnabled() {
            return externalLinksEnabled;
        }

        /**
         * Set if external links should be opened in the webview (<code>true</code>) or should launch an action intent (<code>false</code>).
         * Default value is <code>false</code>.
         *
         * @param externalLinksEnabled
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
         * @param cacheType
         */
        public void setCacheType(CacheType cacheType) {
            this.cacheType = cacheType;
        }
    }


    private class SimpleJSCacheInterface {
        private Context ctx;

        SimpleJSCacheInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void writeToCache(String html) {
            Log.d(TAG, "writeToCache: writing url " + url);
            HtmlCacheManager.getInstance().put(url, html, LEAFLET_CACHE_TTL_MILLIS);
        }
    }
}