package hcmute.edu.vn.note_taking.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class NetworkUtils {
    public static JSONObject sendHttpRequest(final String urlString, final String method, final Map<String, String> params) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn;
            if (method.equals("GET") && params != null && !params.isEmpty()) {
                // Append query parameters to the URL for GET requests
                StringBuilder queryString = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (queryString.length() > 0) {
                        queryString.append("&");
                    }
                    queryString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    queryString.append("=");
                    queryString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                url = new URL(urlString + "?" + queryString.toString());
                conn = (HttpURLConnection) url.openConnection();
            } else {
                // For POST requests or GET requests without parameters
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Constructing JSON string from parameters for POST requests
                if (params != null && !params.isEmpty()) {
                    JSONObject jsonParams = new JSONObject(params);
                    String jsonInputString = jsonParams.toString();
                    try (DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream())) {
                        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                        outputStream.write(input, 0, input.length);
                    }
                }
            }

            // Handle response and parse JSON
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line).append("\n");
                }
            }

            // Parse the response JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            Log.i("JSONResponse", jsonResponse.toString());
            conn.disconnect();

            return jsonResponse;
        } catch (Exception e) {
            return null;
        }
    }
}