package miles.identigate.soja.helpers;


import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by myles on 9/26/15.
 */
public class NetworkHandler {

    private static final String TAG = "NetworkHandler";


    public static String executePost(String targetURL, String urlParameters) {
       /* String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag*/
        URL url;
        HttpURLConnection connection = null;
        //System.setProperty("http.keepAlive", "false");
        try {
            //Create connection
            url = new URL(targetURL.trim());

            Log.d(TAG, "executePost: URL" + url);
            Log.d(TAG, "executePost: Params" + urlParameters);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
           /* connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");*/
            //connection.setRequestProperty("Authorization",authorizationString);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            //Get Response
            InputStream is;
            if (connection.getResponseCode() / 100 == 2) {
                is = connection.getInputStream();
            } else {
                is = connection.getErrorStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            Log.d(TAG, "executePost:Response " + response.toString());
//            Log.d(TAG, "executePostURL: " + response.toString());

            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String GET(String path) {
        Log.d(TAG, "GET: path" + path);

        HttpURLConnection httpURLConnection = null;
       /* String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag*/
        try {
            URL url = new URL(path);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            //httpURLConnection.setRequestProperty("Authorization",authorizationString);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setConnectTimeout(5000);

            //Get Response
            InputStream is;
            if (httpURLConnection.getResponseCode() / 100 == 2) {
                is = httpURLConnection.getInputStream();
            } else {
                is = httpURLConnection.getErrorStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            Log.d(TAG, "executePost: " + response.toString());
            Log.d(TAG, "executePostURL: " + response.toString());


            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }
}
