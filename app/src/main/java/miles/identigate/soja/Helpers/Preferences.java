package miles.identigate.soja.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by miles on 2/2/16.
 */
public class Preferences {
    private Context _context;
    public static final String PREFS_NAME = "PREFERENCES";
    SharedPreferences settings;
    public Preferences(Context context) {
        _context = context;
        settings = context.getSharedPreferences(PREFS_NAME, 0);
    }
    public String getToken() {
        return settings.getString("token", "");
    }

    public void setToken(String token) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);
        editor.commit();
    }
    public boolean isLoggedin(){
        return settings.getBoolean("isLoggedin", false);
    }
    public void setIsLoggedin(boolean status){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isLoggedin", status);
        editor.commit();
    }
    public String getName() {
        return settings.getString("name", "");
    }

    public void setName(String name) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("name", name);
        editor.commit();
    }
    public String getId() {
        return settings.getString("id", "");
    }

    public void setId(String id) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("id", id);
        editor.commit();
    }
    public String getPremise() {
        return settings.getString("premise", "");
    }

    public void setPremise(String premise) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("premise", premise);
        editor.commit();
    }
    public String getDeviceId() {
        return settings.getString("device", "");
    }

    public void setDeviceId(String device) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("device", device);
        editor.commit();
    }
}
