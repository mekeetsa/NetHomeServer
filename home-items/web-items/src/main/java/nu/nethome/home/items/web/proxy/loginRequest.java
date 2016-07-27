package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginRequest {
    public final String systemId;
    public final String password;

    public LoginRequest(String systemId, String password) {
        this.systemId = systemId;
        this.password = password;
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("system", systemId);
        jsonObject.put("password", password);
        return jsonObject;
    }
}
