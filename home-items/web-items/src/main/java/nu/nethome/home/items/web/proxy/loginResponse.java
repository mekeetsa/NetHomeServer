package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class LoginResponse {
    public final String sesssionId;

    public LoginResponse(String sesssionId) {
        this.sesssionId = sesssionId;
    }

    public LoginResponse(JSONObject json) {
        sesssionId = json.getString("sessionId");
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("sesssionId", sesssionId);
        return jsonObject;
    }
}
