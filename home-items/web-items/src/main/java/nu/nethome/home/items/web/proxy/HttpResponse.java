package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class HttpResponse {
    private static final HttpResponse EMPTY = new HttpResponse();
    public final String body;
    public final String[] headers;
    public final String challenge;
    public final String sessionToken;

    public HttpResponse(String body, String[] headers, String challenge) {
        this(body, headers, challenge, null);
    }

    public HttpResponse(String body, String[] headers, String challenge, String sessionToken) {
        this.body = body;
        this.headers = headers;
        this.challenge = challenge;
        this.sessionToken = sessionToken;
    }

    private HttpResponse() {
        this.body = "";
        this.headers = new String[0];
        this.challenge = "";
        this.sessionToken = "";
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("body", body);
        jsonObject.put("headers", Arrays.asList(headers));
        jsonObject.put("challenge", challenge);
        if (sessionToken != null) {
            jsonObject.put("sessionToken", sessionToken);
        }
        return jsonObject;
    }

    public static HttpResponse empty() {
        return EMPTY;
    }

    public static HttpResponse loginFailed(String challenge) {
        return new HttpResponse("", new String[0], challenge, "");
    }

    public static HttpResponse loginSucceeded(String challenge, String sessionId) {
        return new HttpResponse("", new String[0], challenge, sessionId);
    }
}
