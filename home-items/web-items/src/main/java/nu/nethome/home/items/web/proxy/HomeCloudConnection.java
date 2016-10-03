/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
 * <p>
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 * <p>
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.web.proxy;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;


@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class HomeCloudConnection extends HomeItemAdapter implements Runnable, HomeItem {
    // TODO: Verify session id
    // TODO: Generate dynamic challenge
    // TODO: Handle POST and DELETE
    // TODO: Transfer error codes
    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HomeCloudConnection\" Category=\"Ports\" >"
            + "  <Attribute Name=\"State\" Type=\"String\" Get=\"getState\" Default=\"true\" />"
            + "  <Attribute Name=\"ServiceURL\" Type=\"String\" Get=\"getServiceURL\" Set=\"setServiceURL\" />"
            + "  <Attribute Name=\"LocalURL\" Type=\"String\" Get=\"getLocalURL\" Set=\"setLocalURL\" />"
            + "  <Attribute Name=\"Account\" Type=\"String\" Get=\"getAccount\" Set=\"setAccount\" />"
            + "  <Attribute Name=\"AccountKey\" Type=\"Password\" Get=\"getAccountKey\" Set=\"setAccountKey\" />"
            + "  <Attribute Name=\"UserPassword\" Type=\"Password\" Get=\"getPassword\" Set=\"setPassword\" />"
            + "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
            + "</HomeItem> ");

    private static final String CHALLENGE = "challenge";
    private static final int RETRY_INTERVAL_MS = 5000;
    private static final String LOGIN_RESOURCE = "api/server-sessions";
    private static final String CLOUD_POLL_RESOURCE = "api/accounts/%d/servers/%d/poll";

    protected String serviceURL = "https://cloud.opennethome.org/";
    protected String localURL = "http://127.0.0.1:8020/";
    protected String password = "";
    protected String account = "0";
    private String accountKey = "";
    protected int messageCount = 0;
    private boolean accountKeyIsBad = false;

    private boolean connected = false;
    /*
     * Internal attributes
     */
    private static Logger logger = Logger.getLogger(HomeCloudConnection.class.getName());
    protected Thread listenThread;
    protected boolean isRunning = false;
    private JsonRestClient jsonRestClient;
    private String pollResource;

    public HomeCloudConnection() {
        account = Integer.toString(new Random().nextInt(10000));
    }

    public String getModel() {
        return MODEL;
    }

    public void activate() {
        jsonRestClient = new JsonRestClient();
        isRunning = true;
        listenThread = new Thread(this, "ProxyListenThread");
        listenThread.start();
    }

    public void stop() {
        isRunning = false;
        super.stop();
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setLocalURL(String localURL) {
        this.localURL = localURL;
    }

    public String getLocalURL() {
        return localURL;
    }

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    public void run() {
        HttpResponse noResponse = new HttpResponse(account, "", new String[0], CHALLENGE);
        HttpResponse lastHttpResponse = noResponse;
        while (isRunning) {
            try {
                if (accountKeyIsBad) {
                    Thread.sleep(RETRY_INTERVAL_MS);
                    continue;
                }
                final loginResp loginResp = loginToCloud(new LoginReq(account, accountKey));
                connected = true;
                pollResource = String.format(CLOUD_POLL_RESOURCE, loginResp.accountId, loginResp.server);
                while (isRunning) {
                    lastHttpResponse = proxyHttpRequest(noResponse, lastHttpResponse, loginResp.Id);
                }
                connected = false;
            } catch (ConnectionException | IOException | InterruptedException e) {
                connected = false;
                if (isRunning) {
                    logger.fine("Failed Communicating with cloud: " + e);
                    lastHttpResponse = noResponse;
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException e1) {
                        return;
                    }
                }
            }
        }
    }

    private HttpResponse proxyHttpRequest(HttpResponse noResponse, HttpResponse previousLocalHttpResponse, String sessionId) throws IOException, ConnectionException {
        final HttpRequest request = postPreviousResponseToCloudAndFetchNewRequest(previousLocalHttpResponse, sessionId);
        HttpResponse nextLocalHttpResponse;
        if (request.url.isEmpty()) {
            final String loginCredential = request.loginCredential;
            if (!loginCredential.isEmpty()) {
                nextLocalHttpResponse = verifyLoginRequest(noResponse, loginCredential);
            } else {
                nextLocalHttpResponse = noResponse;
            }
        } else {
            nextLocalHttpResponse = performLocalRequest(request);
            messageCount++;
        }
        return nextLocalHttpResponse;
    }

    private HttpResponse verifyLoginRequest(HttpResponse noResponse, String loginCredential) throws ConnectionException {
        HttpResponse httpResponse;
        String expectedCredential = this.account + this.password + CHALLENGE;
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ConnectionException("Could not get SHA-256");
        }
        byte[] hash = digest.digest(expectedCredential.getBytes(StandardCharsets.UTF_8));
        String expectedHashString = Hex.encodeHexString(hash);
        if (expectedHashString.equals(loginCredential)) {
            String sessionId = UUID.randomUUID().toString();
            httpResponse = new HttpResponse(account, "", new String[0], CHALLENGE, sessionId);
        } else {
            httpResponse = noResponse;
        }
        return httpResponse;
    }

    private loginResp loginToCloud(LoginReq loginReq) throws IOException, ConnectionException {
        final JSONResponse result = jsonRestClient.post(serviceURL, LOGIN_RESOURCE, loginReq.toJson(), "");
        if (result.getResultCode() == HttpURLConnection.HTTP_CREATED) {
            return new loginResp(result.getObject());
        } else if (result.getResultCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            accountKeyIsBad = true;
        }
        throw new ConnectionException("Could not login to cloud server, error code: " + result.getResultCode());
    }

    private HttpRequest postPreviousResponseToCloudAndFetchNewRequest(HttpResponse httpResponse, String sessionId) throws IOException, ConnectionException {
        final JSONResponse result = jsonRestClient.post(serviceURL, pollResource, httpResponse.toJson(), sessionId);
        if (result.getResultCode() == HttpURLConnection.HTTP_CREATED) {
            return new HttpRequest(result.getObject());
        } else if (result.getResultCode() == HttpURLConnection.HTTP_NO_CONTENT) {
            return HttpRequest.empty();
        }
        throw new ConnectionException("Got unexpected return code: " + result.getResultCode() + "from cloud server");
    }

    private HttpResponse performLocalRequest(HttpRequest request) throws IOException {
        HttpResponse httpResponse;
        HttpURLConnection connection = (HttpURLConnection) new URL(localURL + request.url).openConnection();
        for (String header : request.headers) {
            String parts[] = header.split(":");
            connection.setRequestProperty(parts[0].trim(), parts[1].trim());
        }
        ByteArrayBuffer baf = new ByteArrayBuffer(50);
        try (InputStream response = connection.getInputStream()) {
            BufferedInputStream bis = new BufferedInputStream(response);
            int read;
            int bufSize = 512;
            byte[] buffer = new byte[bufSize];
            while (true) {
                read = bis.read(buffer);
                if (read == -1) {
                    break;
                }
                baf.append(buffer, 0, read);
            }
        } catch (IOException e) {
            return new HttpResponse(account, "", new String[0], CHALLENGE);
        }

        Map<String, List<String>> map = connection.getHeaderFields();
        String headers[] = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
            headers[i++] = entry.getKey() + ":" + entry.getValue().get(0);
        }
        httpResponse = new HttpResponse(account, new String(Base64.encodeBase64(baf.toByteArray())), headers, CHALLENGE);
        return httpResponse;
    }

    public String getMessageCount() {
        return String.valueOf(messageCount);
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getState() {
        return connected ? "Connected" : "Not Connected";
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
        accountKeyIsBad = false;
    }

    private class ConnectionException extends Exception {
        public ConnectionException(String message) {
            super(message);
        }
    }
}

