package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class LoginReq {
    public final String account;
    public final String password;

    public LoginReq(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("account", account);
        jsonObject.put("password", password);
        return jsonObject;
    }
}
