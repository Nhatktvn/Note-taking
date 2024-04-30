package hcmute.edu.vn.note_taking.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import hcmute.edu.vn.note_taking.models.Note;


public class NetworkUtils {
    private static final String BOUNDARY = "*****";

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static JSONObject uploadFile(Context context, String fileName, String serverUrl) {
        JSONObject response = new JSONObject();
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);

            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set connection properties
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

            // Get output stream of connection
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

            // Write file data to output stream
            outputStream.writeBytes("--" + BOUNDARY + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"\r\n\r\n");

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.writeBytes("\r\n");
            outputStream.writeBytes("--" + BOUNDARY + "--\r\n");

            // Close streams
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            // Get response from server
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder jsonResponse = new StringBuilder();
                while (scanner.hasNextLine()) {
                    jsonResponse.append(scanner.nextLine());
                }
                scanner.close();
                Log.e("NetworkUtils", jsonResponse.toString());
                response = new JSONObject(jsonResponse.toString());
            } else {
                response.put("msg", "fail");
            }

            // Disconnect the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.put("msg", "fail");
            } catch (JSONException ex) {
                Log.e("NetworkUtilsError", "Error creating JSON object", ex);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

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
                url = new URL(urlString + "?" + queryString);
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

    public static JSONObject sendNoteToServer(Context context, Note newLocalNote, String email) {
        try {
            String urlString = Constants.getHOST() + "/api/notes";
            Map<String, String> params = new HashMap<>();
            params.put("author_email", email);
            params.put("title", newLocalNote.getTitle());
            params.put("text_content", newLocalNote.getText_content());

            JSONArray jsonArray = new JSONArray(newLocalNote.getListImages());
            ArrayList<String> uploadedImageArray = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String image = jsonArray.getString(i);
                JSONObject response = uploadFile(context, image, Constants.getHOST() + "/api/upload");
                if (response.getString("status").equals("failed")) {
                    return null;
                } else {
                    uploadedImageArray.add(response.getString("url"));
                }
            }
            params.put("list_image", new JSONArray(uploadedImageArray).toString());

            String encoded_voice = AudioUtils.encodeAndSaveAudio(context, Uri.parse(newLocalNote.getVoice()));

            JSONObject response = uploadFile(context, encoded_voice, Constants.getHOST() + "/api/upload");
            String voice_url = null;
            if (response.getString("status").equals("failed")) {
                return null;
            } else {
                voice_url = response.getString("url");
            }

            params.put("voice", voice_url);
            params.put("created_at", newLocalNote.getCreated_at());
            params.put("status", String.valueOf(newLocalNote.getStatus()));
            params.put("local_id", String.valueOf(newLocalNote.getId()));

            JSONObject jsonResponse = sendHttpRequest(urlString, "POST", params);

            try {
                if (jsonResponse != null && jsonResponse.getString("status").equals("success")) {
                    return jsonResponse;
                }
            } catch (JSONException e) {
                Log.e("NetworkUtilsError", "Error parsing JSON response", e);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}