package nu.nethome.home.items.web.proxy;

import nu.nethome.home.impl.LocalHomeItemProxy;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HomeCloudConnectionTest {

    private static final String ACCOUNT = "ACCOUNT";
    private static final String KEY = "KEY";
    private static final String URL = "https://foo.fie/fum";
    private static final String SESSION_KEY = "asdfwqer";
    private static final int ACCOUNT_ID = 17;
    private static final int SERVER_ID = 5;
    private static final String POLL_URL = String.format(HomeCloudConnection.CLOUD_POLL_RESOURCE, ACCOUNT_ID, SERVER_ID);
    private HomeCloudConnection homeCloudConnection;
    private LocalHomeItemProxy proxy;
    private JsonRestClient restClient;

    @Before
    public void setUp() throws Exception {
        homeCloudConnection = spy(new HomeCloudConnection());
        doReturn(null).when(homeCloudConnection).performLocalRequest(any(HttpRequest.class));
        proxy = new LocalHomeItemProxy(homeCloudConnection);
        proxy.setAttributeValue("Account", ACCOUNT);
        proxy.setAttributeValue("AccountKey", KEY);
        proxy.setAttributeValue("ServiceURL", URL);
        restClient = mock(JsonRestClient.class);
        homeCloudConnection.jsonRestClient = restClient;
        homeCloudConnection.isRunning = true;
    }

    @Test(expected = HomeCloudConnection.ConnectionException.class)
    public void throwsExceptionWhenLoginFailes() throws Exception {
        JSONResponse response = new JSONResponse("", HttpURLConnection.HTTP_UNAUTHORIZED);
        doReturn(response).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        homeCloudConnection.connectAndProxyCloudRequests();
    }

    @Test
    public void logsInToCloudUsingSpecifiedCredentials() throws Exception {
        JSONResponse response = new JSONResponse("", HttpURLConnection.HTTP_UNAUTHORIZED);
        doReturn(response).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        try {
            homeCloudConnection.connectAndProxyCloudRequests();
        } catch (Exception e) {

        }
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), captor.capture(), eq(""));
        assertThat(captor.getValue().get("account").toString(), is(ACCOUNT));
        assertThat(captor.getValue().get("password").toString(), is(KEY));
    }

    @Test
    public void pollsForRequestWithSessionTokenAfterLogin() throws Exception {
        loginToCloud();
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient).post(eq(URL), eq(POLL_URL), captor.capture(), eq(SESSION_KEY));
    }

    private void loginToCloud() throws IOException {
        LoginResp resp = new LoginResp(SESSION_KEY , ACCOUNT, ACCOUNT_ID, SERVER_ID);
        JSONResponse response = new JSONResponse(resp.toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(response).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        try {
            homeCloudConnection.connectAndProxyCloudRequests();
        } catch (Exception e) {
            int x = 3;
        }
        verify(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
    }

    @Test
    public void rejectsPollRequestWithoutClientLogin() throws Exception {
        HttpRequest request = new HttpRequest("/foo/fum", new String[0], "");
        JSONResponse response = new JSONResponse(request.toJson().toString(), HttpURLConnection.HTTP_CREATED);
        doReturn(response).when(restClient).post(eq(URL), eq(HomeCloudConnection.LOGIN_RESOURCE), any(JSONObject.class), eq(""));
        loginToCloud();
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);
        verify(restClient).post(eq(URL), eq(POLL_URL), captor.capture(), eq(SESSION_KEY));
    }


}