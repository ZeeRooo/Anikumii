package com.zeerooo.anikumii.anikumiiparts;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AnikumiiConnection {

    public String getStringResponse(String request, String url, String params) throws IOException {
        final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setRequestMethod(request);

        if (request.equals("POST")) {
            httpURLConnection.setDoOutput(true);
        } else {
            httpURLConnection.setDoInput(true);
        }

        httpURLConnection.connect();

        if (params != null) {
            final DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(params);
            dataOutputStream.close();
        }

        final InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8");
        final StringBuilder response = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String responseLine;

            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine/*.trim()*/);
            }

            inputStreamReader.close();
            bufferedReader.close();
            return response.toString();
        }
    }

}