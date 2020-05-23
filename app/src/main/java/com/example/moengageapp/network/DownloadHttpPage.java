package com.example.moengageapp.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
Downloads html page of a given url
 */

public class DownloadHttpPage {

    private String url;

    public DownloadHttpPage(String url) {
        this.url = url;
    }

    public String getPage() {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        String html = "";
        try {
            HttpResponse response = client.execute(request);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            html = str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }
}
