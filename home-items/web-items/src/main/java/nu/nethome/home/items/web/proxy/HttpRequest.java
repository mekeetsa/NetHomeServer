package nu.nethome.home.items.web.proxy;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequest {
    public final String url;
    public final String[] headers;
    public final String loginCredential;

    public HttpRequest(JSONObject json) {
        url = json.getString("url");
        final JSONArray headers1 = json.getJSONArray("headers");
        String[] h = new String[headers1.length()];
        for (int i = 0; i < h.length; i++) {
            h[i] = headers1.getString(i);
        }
        headers = h;
        loginCredential = json.getString("loginCredential");
    }

    private HttpRequest() {
        url = "";
        headers = new String[0];
        loginCredential = "";
    }

    public static HttpRequest empty() {
        return new HttpRequest();
    }
}
