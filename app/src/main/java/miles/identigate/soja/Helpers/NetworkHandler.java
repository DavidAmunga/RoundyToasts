package miles.identigate.soja.Helpers;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by myles on 9/26/15.
 */
public class NetworkHandler {
    public static String  excutePost(String targetURL, String urlParameters)
    {
        //Log.v("DATA",urlParameters);
        String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag
        URL url;
        HttpURLConnection connection = null;
        //System.setProperty("http.keepAlive", "false");
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
           /* connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");*/
            connection.setRequestProperty("Authorization",authorizationString);
            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();
            //Get Response
            InputStream is ;
            if(connection.getResponseCode()/100 ==2){
                is=connection.getInputStream();
            }else{
                is=connection.getErrorStream();
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }
    public static String GET(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag
        String responseString = null;
        try {
            HttpGet httpGet=new HttpGet(url);
            httpGet.setHeader("Authorization",authorizationString);
            response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }
}
