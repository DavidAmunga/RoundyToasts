package miles.identigate.soja.Helpers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

import miles.identigate.soja.R;

/**
 * Created by myles on 9/26/15.
 */
public class CheckConnection {
     public static  boolean check(Activity activity){
        ConnectivityManager connectivityManager=(ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isConnectedOrConnecting()){
            return true;
        }else{
            return false;
        }
    }
    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
    public void show_dialog(Context context){
        new MaterialDialog.Builder(context)
                .title("Notice")
                .content("No active internet connection.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .widgetColorRes(R.color.colorPrimary)
                .titleColorRes(R.color.ColorPrimary)
                .positiveColorRes(R.color.colorPrimaryDark)
                .show();
    }
}
