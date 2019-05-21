package com.zeerooo.anikumii2.anikumiiparts;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class AnikumiiConnection {

    public String getStringResponse(String request, String url, String params) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setRequestMethod(request);

        if (request.equals("POST")) {
            httpURLConnection.setDoOutput(true);
        } else {
            httpURLConnection.setDoInput(true);
        }

        httpURLConnection.connect();

        if (params != null) {
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(params);
            dataOutputStream.close();
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
            }

            bufferedReader.close();
            return response.toString();
        }
    }

}