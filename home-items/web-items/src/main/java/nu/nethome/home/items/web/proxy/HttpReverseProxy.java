/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.web.proxy;

import nu.nethome.home.item.HomeItem;
import nu.nethome.home.item.HomeItemAdapter;
import nu.nethome.home.item.HomeItemType;
import nu.nethome.util.plugin.Plugin;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@SuppressWarnings("UnusedDeclaration")
@Plugin
@HomeItemType("Ports")
public class HttpReverseProxy extends HomeItemAdapter implements Runnable, HomeItem {

    private static final String MODEL = ("<?xml version = \"1.0\"?> \n"
            + "<HomeItem Class=\"HttpReverseProxy\" Category=\"Ports\" >"
            + "  <Attribute Name=\"serviceURL\" Type=\"String\" Get=\"getServiceURL\" Set=\"setServiceURL\" Default=\"true\" />"
            + "  <Attribute Name=\"localURL\" Type=\"String\" Get=\"getLocalURL\" Set=\"setLocalURL\" />"
            + "  <Attribute Name=\"systemId\" Type=\"String\" Get=\"getSystemId\" Set=\"setSystemId\" />"
            + "  <Attribute Name=\"MessageCount\" Type=\"String\" Get=\"getMessageCount\" />"
            + "</HomeItem> ");

    protected String serviceURL = "http://127.0.0.1:8080/poll";
    protected String localURL = "http://127.0.0.1:8020/";
    protected String systemId = "0";
    protected int messageCount = 0;

    /*
     * Internal attributes
     */
    private static Logger logger = Logger.getLogger(HttpReverseProxy.class.getName());
    protected Thread listenThread;
    protected boolean isRunning = false;
    private JsonRestClient jsonRestClient;


    public HttpReverseProxy() {
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
        HttpResponse noResponse = new HttpResponse(systemId, "", new String[0], "challenge");
        try {
            HttpResponse httpResponse = noResponse;
            while (isRunning) {
                try {
                    final HttpRequest request = postResponseAndFetchNewRequest(httpResponse);
                    if (request.url.isEmpty()) {
                        httpResponse = noResponse;
                    } else {
                        httpResponse = performLocalRequest(request);
                        messageCount++;
                    }
                } catch (Exception e) {
                    if (isRunning) {
                        logger.warning("Failed reading from proxy " + e);
                        httpResponse = noResponse;
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Failed creating socket in UDPListener " + e);
        }
    }

    private HttpRequest postResponseAndFetchNewRequest(HttpResponse httpResponse) throws IOException {
        final JSONData result = jsonRestClient.post(serviceURL, "", httpResponse.toJson());
        return new HttpRequest(result.getObject());
    }

    private HttpResponse performLocalRequest(HttpRequest request) throws IOException {
        HttpResponse httpResponse;
        URLConnection connection = new URL(localURL + request.url).openConnection();
        //connection.setRequestProperty("Accept-Charset", charset);
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
        }

        Map<String, List<String>> map = connection.getHeaderFields();
        String headers[] = new String[map.size()];
        int i = 0;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() +
                    " ,Value : " + entry.getValue());
            headers[i++] = entry.getKey() + ":" + entry.getValue().get(0);
        }
        httpResponse = new HttpResponse(systemId, new String(Base64.encodeBase64(baf.toByteArray())), headers, "challenge");
        return httpResponse;
    }

    public String getMessageCount() {
        return String.valueOf(messageCount);
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
}

