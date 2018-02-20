package es.usc.citius.servando.calendula.activities;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates a request for a {@link WebViewActivity}.
 * <p>
 */
public class WebViewRequest implements Parcelable {

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
    private List<String> customCss = null;
    private Map<String, String> customCssOverrides = null;
    private Duration cacheTTL;

    public WebViewRequest(String url) {
        this.url = url;
    }


    private WebViewRequest(Parcel in) {
        url = in.readString();
        title = in.readString();
        loadingMessage = in.readString();
        connectionErrorMessage = in.readString();
        notFoundErrorMessage = in.readString();
        postProcessorClassname = in.readString();
        javaScriptEnabled = in.readByte() != 0;
        externalLinksEnabled = in.readByte() != 0;
        cacheType = CacheType.valueOf(in.readString());
        int cssNumber = in.readInt();
        String[] cssFiles = new String[cssNumber];
        in.readStringArray(cssFiles);
        customCss = new ArrayList<>();
        customCss.addAll(Arrays.asList(cssFiles));
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
        dest.writeInt(customCss.size());
        dest.writeStringArray(customCss.toArray(new String[customCss.size()]));
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
     * Set the title for the WebView title bar. Can be <code>null</code>, no title will be displayed if so.
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
     * Enable/disable JavaScript for the web page. Default is <code>false</code>.
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

    public List<String> getCustomCss() {
        return customCss;
    }

    public void setCustomCss(List<String> customCss) {
        this.customCss = customCss;
    }

    public Map<String, String> getCustomCssOverrides() {
        return customCssOverrides;
    }


    public boolean isExternalLinksEnabled() {
        return externalLinksEnabled;
    }

    /**
     * Set if external links should be opened in the WebView (<code>true</code>) or should launch an action intent (<code>false</code>).
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
     * Set cache type for the WebView:
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


    public String getPostProcessorClassname() {
        return postProcessorClassname;
    }


    public void setCustomCssOverrides(Map<String, String> customCssOverrides) {
        this.customCssOverrides = customCssOverrides;
    }

    public enum CacheType {
        NO_CACHE,
        APP_CACHE,
        DOWNLOAD_CACHE
    }
}
