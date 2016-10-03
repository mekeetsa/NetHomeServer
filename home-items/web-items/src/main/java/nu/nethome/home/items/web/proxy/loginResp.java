package nu.nethome.home.items.web.proxy;

import org.json.JSONObject;

public class loginResp {
    public final String Id;
    public final String account;
    public final int accountId;
    public final int server;

    public loginResp(String Id, String account, int accountId, int server) {
        this.Id = Id;
        this.account = account;
        this.accountId = accountId;
        this.server = server;
    }

    public loginResp(JSONObject json) {
        Id = json.getString("id");
        account = json.getString("account");
        accountId = json.getInt("accountId");
        server = json.getInt("server");
    }

    public JSONObject toJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", Id);
        return jsonObject;
    }
}
