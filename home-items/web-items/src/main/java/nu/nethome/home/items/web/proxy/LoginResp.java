package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class LoginResp {
    public final String Id;
    public final String account;
    public final int accountId;
    public final int server;

    public LoginResp(String Id, String account, int accountId, int server) {
        this.Id = Id;
        this.account = account;
        this.accountId = accountId;
        this.server = server;
    }

    public LoginResp(JSONObject json) {
        Id = json.getString("id");
        account = json.getString("account");
        accountId = json.getInt("accountId");
        server = json.getInt("server");
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", Id);
        jsonObject.put("account", account);
        jsonObject.put("accountId", accountId);
        jsonObject.put("server", server);
        return jsonObject;
    }
}
