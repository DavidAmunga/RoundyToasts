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
    public String getBaseURL(){
        String baseUrl = settings.getString("base_url", "https://soja.co.ke/soja-rest/index.php/api/visits/");
        Constants.URL = baseUrl;
        return baseUrl;
    }
    public void setBaseURL(String url){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("base_url", url);
        editor.commit();
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
    public String getPremiseZoneId() {
        return settings.getString("premiseZoneId", "");
    }
    public void setPremiseZoneId(String premiseId) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("premiseZoneId", premiseId);
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
    public boolean canPrint(){
        return settings.getBoolean("canPrint", false);
    }
    public void setCanPrint(boolean status){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("canPrint", status);
        editor.commit();
    }
    public String getPremiseName(){

        return settings.getString("premise_name", "SOJA");
    }
    public void setPremiseName(String premise_name){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("premise_name", premise_name);
        editor.commit();
    }
    public boolean isPhoneNumberEnabled(){
        return settings.getBoolean("record_phone_number", true);
    }
    public void setPhoneNumberEnabled(boolean isEnabled){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("record_phone_number", isEnabled);
        editor.commit();
    }
    public boolean isCompanyNameEnabled(){
        return settings.getBoolean("record_company_name", false);
    }
    public void setCompanyNameEnabled(boolean isEnabled){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("record_company_name", isEnabled);
        editor.commit();
    }

}
