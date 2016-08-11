package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class LoginResp {
    public final String sesssionId;

    public LoginResp(String sesssionId) {
        this.sesssionId = sesssionId;
    }

    public LoginResp(JSONObject json) {
        sesssionId = json.getString("sessionId");
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("sesssionId", sesssionId);
        return jsonObject;
    }
}
