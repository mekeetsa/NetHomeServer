package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class HttpResponse {
    public final String systemId;
    public final String body;
    public final String[] headers;
    public final String challenge;
    public final String sessionToken;

    public HttpResponse(String systemId, String body, String[] headers, String challenge) {
        this(systemId, body, headers, challenge, "");
    }

    public HttpResponse(String systemId, String body, String[] headers, String challenge, String sessionToken) {
        this.systemId = systemId;
        this.body = body;
        this.headers = headers;
        this.challenge = challenge;
        this.sessionToken = sessionToken;
    }

    //public HttpResponse(JSONObject json) {
    //    body = json.getString("body");
    //}

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("system", systemId);
        jsonObject.put("body", body);
        jsonObject.put("headers", Arrays.asList(headers));
        jsonObject.put("challenge", challenge);
        jsonObject.put("sessionToken", sessionToken);
        return jsonObject;
    }
}
