package se.yawnmedia.fairsplit;

import android.app.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public final class RESTHelper {
    public static String baseURL = Settings.BaseURL + "/api";
    public static String transactionEndpoint = "/transaction";

    public static JSONObject DoRequest(String method, String uri, JSONObject data, String apiKey) throws IOException, JSONException {
        URL url = new URL(baseURL + uri);
        HttpURLConnection urlConnection = null;
        if (baseURL.startsWith("https")) {
            urlConnection = (HttpsURLConnection) url.openConnection();
        } else {
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        urlConnection.setRequestMethod(method);
        if (apiKey != null) {
            urlConnection.setRequestProperty("fairsplit-apikey", apiKey);
        }
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        if (urlConnection.getRequestMethod().equals("POST")) {
            urlConnection.setDoOutput(true);
        }
        urlConnection.connect();

        if (urlConnection.getRequestMethod().equals("POST") && data != null) {
            OutputStream os = urlConnection.getOutputStream();
            os.write(data.toString().getBytes());
            os.flush();
        }

        InputStream is;
        if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            is = urlConnection.getInputStream();
        } else {
            is = urlConnection.getErrorStream();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        System.out.println("JSON: " + jsonString);

        return new JSONObject(jsonString);
    }

    public static JSONObject GET(String uri, String apiKey) throws IOException, JSONException {
        return DoRequest("GET", uri, null, apiKey);
    }

    public static JSONObject POST(String uri, JSONObject data, String apiKey) throws IOException, JSONException {
        return DoRequest("POST", uri, data, apiKey);
    }

    public static String loginUser(FairSplit app, String login, String password) throws IOException, JSONException {
        String errorMessage = null;
        JSONObject loginData = new JSONObject();

        loginData.put("userName", login);
        loginData.put("password", password);

        // Try to login user
        JSONObject response = RESTHelper.POST("/login", loginData, null);

        if (response.has("errorCode") && (int) response.get("errorCode") != 0) {
            errorMessage = response.get("message").toString();
            return errorMessage;
        }

        JSONObject responseUser = response.getJSONArray("data").getJSONObject(0);
        User user = new User(responseUser);
        app.setCurrentUser(user);
        app.addToAllUsers(user);

        // Get groups info
        if (user.groups.size() > 0) {
            for (int i = 0; i < user.groups.size(); i++) {
                response = RESTHelper.GET("/group/" + user.groups.get(i), user.apiKey);
                if (response.has("errorCode") && (int) response.get("errorCode") != 0) {
                    errorMessage = response.get("message").toString();
                    return errorMessage;
                }
                JSONObject responseGroup = response.getJSONArray("data").getJSONObject(0);
                app.addToAllGroups(new Group(responseGroup));
            }
            app.setCurrentGroup(app.getAllGroups().get(0));
        }

        return errorMessage;
    }
}
