/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class JsonRestClient {

    private static final int READ_TIMEOUT = 60000;

    public JSONResponse get(String baseUrl, String resource, JSONObject argument) throws IOException {
        return performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "GET", "");
    }

    public JSONResponse put(String baseUrl, String resource, JSONObject argument) throws IOException {
        return performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "PUT", "");
    }

    public JSONResponse post(String baseUrl, String resource, JSONObject argument, String sessionId) throws IOException {
        return performRequest(baseUrl, resource, argument != null ?argument.toString() : "", "POST", sessionId);
    }

    private JSONResponse performRequest(String baseUrl, String resource, String body, String method, String sessionId) throws IOException {
        HttpURLConnection connection = null;
        DataOutputStream wr = null;
        BufferedReader rd = null;
        StringBuilder sb = new StringBuilder();
        String line;
        URL serverAddress;

        try {
            serverAddress = new URL(baseUrl + resource);

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/json");
            if (!sessionId.isEmpty()) {
                connection.setRequestProperty("id", sessionId);
            }
            connection.setUseCaches(false);
            if (body.length() > 0) {
                byte[] data = body.getBytes("UTF8");
                connection.setRequestProperty("Content-Length", Integer.toString(data.length));
                wr = new DataOutputStream(connection.getOutputStream());
                wr.write(data);
                wr.flush();
                wr.close();
                wr = null;
            }
            connection.connect();
            if (connection.getResponseCode() <= 299) {
                rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            while ((line = rd.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

        } finally {
            if (rd != null) {
                rd.close();
            }
            if (wr != null) {
                wr.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONResponse(sb.toString(), connection.getResponseCode());
    }
}
