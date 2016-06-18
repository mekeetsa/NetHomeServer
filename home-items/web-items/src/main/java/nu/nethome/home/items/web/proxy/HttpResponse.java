package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class HttpResponse {
    public final String body;
    public final String[] headers;

    public HttpResponse(String body, String[] headers) {
        this.body = body;
        this.headers = headers;
    }

    //public HttpResponse(JSONObject json) {
    //    body = json.getString("body");
    //}

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("body", body);
        jsonObject.put("headers", Arrays.asList(headers));
        return jsonObject;
    }
}
