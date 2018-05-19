package se.yawnmedia.fairsplit;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;

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

    public static JSONObject DoRequest(String method, String uri, JSONObject data, String apiKey, Context context) throws IOException, JSONException {
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
        if (urlConnection.getRequestMethod().equals("POST") || urlConnection.getRequestMethod().equals("PUT")) {
            urlConnection.setDoOutput(true);
        }
        urlConnection.connect();

        if ((urlConnection.getRequestMethod().equals("POST") || urlConnection.getRequestMethod().equals("PUT")) && data != null) {
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
        JSONObject responseObject = new JSONObject(jsonString);

        if (responseObject.getInt("errorCode") == 100 && context != null) {
            if (!context.getClass().equals(LoginActivity.class)) {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.getApplicationContext().startActivity(intent);
            }
        }

        return responseObject;
    }

    public static JSONObject GET(String uri, String apiKey, Context context) throws IOException, JSONException {
        return DoRequest("GET", uri, null, apiKey, context);
    }

    public static JSONObject POST(String uri, JSONObject data, String apiKey, Context context) throws IOException, JSONException {
        return DoRequest("POST", uri, data, apiKey, context);
    }

    public static JSONObject PUT(String uri, JSONObject data, String apiKey, Context context) throws IOException, JSONException {
        return DoRequest("PUT", uri, data, apiKey, context);
    }

    public static JSONObject DELETE(String uri, JSONObject data, String apiKey, Context context) throws IOException, JSONException {
        return DoRequest("DELETE", uri, data, apiKey, context);
    }

    public static String loginUser(FairSplit app, String login, String password, Context context) throws IOException, JSONException {
        String errorMessage = null;
        JSONObject loginData = new JSONObject();

        loginData.put("userName", login);
        loginData.put("password", password);

        // Try to login user
        JSONObject response = RESTHelper.POST("/login", loginData, null, context);

        if (response.has("errorCode") && (int) response.get("errorCode") != 0) {
            errorMessage = response.get("message").toString();
            return errorMessage;
        }

        JSONObject responseUser = response.getJSONArray("data").getJSONObject(0);
        User user = new User(responseUser, context);
        errorMessage = populateOtherData(app, user, context);

        return errorMessage;
    }

    public static String loginUserByAPIKey(FairSplit app, Context context) throws IOException, JSONException {
        String errorMessage = null;

        // Try to login user
        JSONObject response = RESTHelper.GET("/user/" + app.getUserID(), app.getAPIKey(), context);

        if (response.has("errorCode") && (int) response.get("errorCode") != 0) {
            errorMessage = response.get("message").toString();
            return errorMessage;
        }

        JSONObject responseUser = response.getJSONArray("data").getJSONObject(0);
        User user = new User(responseUser, context);
        errorMessage = populateOtherData(app, user, context);

        return errorMessage;
    }

    private static String populateOtherData(FairSplit app, User user, Context context) throws IOException, JSONException {
        app.setCurrentUser(user);
        app.addToAllUsers(user);
        app.setAPIKey(user.apiKey);
        app.setUserID(user.userID);

        String errorMessage = null;
        // Gather all info
        if (user.groups.size() > 0) {
            for (int i = 0; i < user.groups.size(); i++) {
                JSONObject response = RESTHelper.GET("/group/" + user.groups.get(i), user.apiKey, context);
                if (response.has("errorCode") && (int) response.get("errorCode") != 0) {
                    errorMessage = response.get("message").toString();
                    return errorMessage;
                }
                JSONObject responseGroup = response.getJSONArray("data").getJSONObject(0);
                Group groupToAdd = new Group(responseGroup);
                app.addToAllGroups(groupToAdd);

                // Also fetch all users for the group
                for (int userID : groupToAdd.members) {
                    // Only fetch user if not already fetched
                    if (!app.userInList(userID)) {
                        response = RESTHelper.GET("/user/" + userID, user.apiKey, context);
                        JSONObject responseAdditionalUser = response.getJSONArray("data").getJSONObject(0);
                        User userToAdd = new User(responseAdditionalUser, context);
                        app.addToAllUsers(userToAdd);
                    }
                }
            }
            app.setCurrentGroup(app.getAllGroups().get(0));
        }
        return errorMessage;
    }
}
