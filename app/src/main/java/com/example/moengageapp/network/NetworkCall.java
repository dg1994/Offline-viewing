package com.example.moengageapp.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/*
Gets data from an api
 */
public class NetworkCall {

    public String getContentFromUrl(String urlString) throws IOException {
        StringBuilder content = new StringBuilder();

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } else {
            throw new IOException(conn.getResponseMessage());
        }

        return content.toString();
    }
}
