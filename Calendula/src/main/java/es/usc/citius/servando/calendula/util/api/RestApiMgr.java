package es.usc.citius.servando.calendula.util.api;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
public class RestApiMgr {

    private enum METHOD {POST,GET,PUT,DELETE};
    private static RestApiMgr instance = null;
    private String API_LOCATION;
    private static HttpClient client;

    public <T> T post(String path, JSONObject data, Class responseClass) throws Exception {
        return post(path,data.toString(),responseClass);
    }

    public <T> T post(String path, String data, Class responseClass) throws Exception {
        HttpPost post = new HttpPost(apiUri(path));
        post.setHeader("content-type", "application/json");
        post.setEntity(new StringEntity(data));
        return (T) ApiResponseFactory.createFrom(client.execute(post),responseClass);
    }

    public <T> T get(String path, Class responseClass) throws Exception {
        HttpGet get = new HttpGet(apiUri(path));
        return (T) ApiResponseFactory.createFrom(client.execute(get),responseClass);
    }

    public String apiUri(String path){
        return API_LOCATION+ "/api/" + path;
    }


    public RequestBuilder requestBuilder(){
        return new RequestBuilder();
    }
    // Builder

    public class RequestBuilder{

        private METHOD method;
        private String path;
        private String data;
        private Class responseClass;
        private String token;

        public RequestBuilder get(){ method = METHOD.GET; return this;}
        public RequestBuilder post(){ method = METHOD.POST; return this;}
        public RequestBuilder put(){ method = METHOD.PUT; return this;}
        public RequestBuilder delete(){ method = METHOD.DELETE; return this;}

        public RequestBuilder to(String path){
            this.path=path;
            return this;
        }

        public RequestBuilder withBody(String data){
            this.data=data;
            return this;
        }

        public RequestBuilder expect(Class responseClass){
            this.responseClass=responseClass;
            return this;
        }

        public RequestBuilder authorize(String token){
            this.token=token;
            return this;
        }

        public <T> T execute() throws Exception {

            switch (method){
                case GET:
                    return executeGet();
                case POST:
                    return executePost();
                default:
                    throw new UnsupportedOperationException("Method " + method + " not implemented yet");
            }
        }


        public <T> T executeGet() throws Exception {
            HttpGet get = new HttpGet(apiUri(path));

            if(token!=null)
                get.setHeader("auth",token);

            HttpResponse response = client.execute(get);
            if(responseClass!=null)
                return (T) ApiResponseFactory.createFrom(response,responseClass);
            else
                return null;
        }

        public <T> T executePost() throws Exception {
            HttpPost post = new HttpPost(apiUri(path));
            post.setHeader("content-type", "application/json");

            if(token!=null)
                post.setHeader("auth",token);
            if(data!=null)
                post.setEntity(new StringEntity(data));

            HttpResponse response = client.execute(post);
            if(responseClass!=null)
                return (T) ApiResponseFactory.createFrom(response,responseClass);
            else
                return null;
        }

    }

    // Singleton

    private RestApiMgr(){
        API_LOCATION = Settings.instance().get("API_LOCATION");
        client = new DefaultHttpClient();
    }

    public static RestApiMgr instance(){
        if(instance==null)
            instance = new RestApiMgr();
        return instance;
    }

}
