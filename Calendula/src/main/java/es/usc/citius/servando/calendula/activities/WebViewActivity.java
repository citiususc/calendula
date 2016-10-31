package es.usc.citius.servando.calendula.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.InputStream;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;

public class WebViewActivity extends CalendulaActivity {

    /**
     * Request bean for WebViewActivity. Must be provided and must contain at least a URL.
     */
    public static final String PARAM_WEBVIEW_REQUEST = "webview_param_request";

    private static final String TAG = "WebViewActivity";

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


        //check for request and URL  and finish if not present
        WebViewRequest request = getIntent().getParcelableExtra(PARAM_WEBVIEW_REQUEST);
        if (request == null || request.getUrl() == null) {
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
            Log.d(TAG, "Enabling JavaScript!");
            webView.getSettings().setJavaScriptEnabled(true);
        }


        String url = request.getUrl();
        Log.d(TAG, "Opening URL:" + url);

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
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);


        final String customCssSheet = request.getCustomCss();

        webView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (customCssSheet != null) {
                            injectCSS(customCssSheet);

                            //if JavaScript is not enabled explicitly, turn it off after CSS injection
                            webView.getSettings().setJavaScriptEnabled(request.isJavaScriptEnabled());
                        }
                        Log.d(TAG, "Finished loading URL: " + url);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
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

        webView.loadUrl(url);
    }

    private void injectCSS(final String file) {
        try {
            // read CSS from file
            InputStream inputStream = getAssets().open(file);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
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

    private void showErrorToast() {

        final WebViewRequest request = getIntent().getParcelableExtra(PARAM_WEBVIEW_REQUEST);
        String error = request.getErrorMessage();

        if (error == null) error = getString(R.string.message_generic_pageloaderror);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
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
        private String customCss = null;


        public WebViewRequest(String url) {
            this.url = url;
        }

        public WebViewRequest(String url, String title, String loadingMessage, String errorMessage, boolean javaScriptEnabled, String customCss) {
            this.url = url;
            this.title = title;
            this.loadingMessage = loadingMessage;
            this.errorMessage = errorMessage;
            this.javaScriptEnabled = javaScriptEnabled;
            this.customCss = customCss;
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
         * Injecting the CSS <b>requires JavaScript</b> and will override setJavaScriptEnabled.
         * @param filename
         */
        public void setCustomCss(String filename) {
            this.customCss = filename;
        }

        protected WebViewRequest(Parcel in) {
            url = in.readString();
            title = in.readString();
            loadingMessage = in.readString();
            errorMessage = in.readString();
            javaScriptEnabled = in.readByte() != 0;
            customCss = in.readString();
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(title);
            dest.writeString(loadingMessage);
            dest.writeString(errorMessage);
            dest.writeByte((byte) (javaScriptEnabled ? 1 : 0));
            dest.writeString(customCss);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WebViewRequest that = (WebViewRequest) o;

            if (javaScriptEnabled != that.javaScriptEnabled) return false;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;
            if (title != null ? !title.equals(that.title) : that.title != null) return false;
            if (loadingMessage != null ? !loadingMessage.equals(that.loadingMessage) : that.loadingMessage != null)
                return false;
            if (errorMessage != null ? !errorMessage.equals(that.errorMessage) : that.errorMessage != null)
                return false;
            return customCss != null ? customCss.equals(that.customCss) : that.customCss == null;

        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            result = 31 * result + (loadingMessage != null ? loadingMessage.hashCode() : 0);
            result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
            result = 31 * result + (javaScriptEnabled ? 1 : 0);
            result = 31 * result + (customCss != null ? customCss.hashCode() : 0);
            return result;
        }
    }
}