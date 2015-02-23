package es.usc.citius.servando.calendula.util.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import es.usc.citius.servando.calendula.util.Settings;

/**
 * Created by joseangel.pineiro on 7/3/14.
 */
public class ApiRequestBuilder {

    //    private enum METHOD {POST, GET, PUT, DELETE}
    private static HttpClient client = new DefaultHttpClient();
    private String API_LOCATION;

    // Request parameters

    private String path;
    private String data;
    private Class responseClass;
    private String token;

    public ApiRequestBuilder() {
        API_LOCATION = Settings.instance().get("API_LOCATION");
    }

    public ApiRequestBuilder to(String path) {
        this.path = path;
        return this;
    }

    public ApiRequestBuilder withBody(String data) {
        this.data = data;
        return this;
    }

    public ApiRequestBuilder withBody(JSONObject data) {
        this.data = data.toString();
        return this;
    }

    public ApiRequestBuilder expect(Class responseClass) {
        this.responseClass = responseClass;
        return this;
    }

    public ApiRequestBuilder authorize(String token) {
        this.token = token;
        return this;
    }


    public <T> T get() throws Exception {
        HttpGet get = new HttpGet(apiUri(path));

        if (token != null)
            get.setHeader("auth", token);

        HttpResponse response = client.execute(get);
        if (responseClass != null)
            return (T) ApiResponseFactory.createFrom(response, responseClass);
        else
            return null;
    }

    public <T> T post() throws Exception {
        HttpPost post = new HttpPost(apiUri(path));
        post.setHeader("content-type", "application/json");

        if (token != null)
            post.setHeader("auth", token);
        if (data != null)
            post.setEntity(new StringEntity(data));

        HttpResponse response = client.execute(post);
        if (responseClass != null)
            return (T) ApiResponseFactory.createFrom(response, responseClass);
        else
            return null;
    }

    private String apiUri(String path) {
        return API_LOCATION + "/api/" + path;
    }

}
