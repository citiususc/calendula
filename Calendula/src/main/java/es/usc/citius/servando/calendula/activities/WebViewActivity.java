package es.usc.citius.servando.calendula.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;

public class WebViewActivity extends CalendulaActivity {

    public static final String PARAM_URL = "webview_param_url";
    public static final String PARAM_TITLE = "webview_param_title";
    public static final String PARAM_LOADING_MESSAGE = "webview_param_loading_message";
    public static final String PARAM_ERROR_MESSAGE = "webview_param_error";

    private static final String TAG = "WebViewActivity";

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = (WebView) findViewById(R.id.webView1);
        // webView.getSettings().setJavaScriptEnabled(true);

        int color = DB.patients().getActive(this).color();

        String title = getIntent().getStringExtra(PARAM_TITLE);
        setupToolbar(title, color);


        setupStatusBar(color);

        String url = getIntent().getStringExtra(PARAM_URL);
        if (url == null) {
            Log.e(TAG, "onCreate: URL not provided in intent");
            finish();
        }
        Log.d(TAG, "Opening URL:" + url);
        webView.loadUrl(url);

        String message = getIntent().getStringExtra(PARAM_LOADING_MESSAGE);
        if (message == null) message = getString(R.string.message_generic_pleasewait);

        final ProgressDialog progressBar = ProgressDialog.show(this, getString(R.string.title_generic_loading), message);
        progressBar.setCancelable(true);
        progressBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });

        webView.setWebViewClient(
                new WebViewClient() {

                    public void onPageFinished(WebView view, String url) {
                        Log.d(TAG, "Finished loading URL: " + url);
                        if (progressBar.isShowing()) {
                            progressBar.dismiss();
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

    }

    private void showErrorToast() {
        String error = getIntent().getStringExtra(PARAM_ERROR_MESSAGE);
        if (error == null) error = getString(R.string.message_generic_pageloaderror);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

}