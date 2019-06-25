package miles.identigate.soja.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import io.paperdb.Paper;
import miles.identigate.soja.models.User;

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
        Paper.init(context);
    }


    public String getBaseURL() {
        String baseUrl = settings.getString("base_url", "https://soja.co.ke/soja-rest/index.php/api/visits/");
        Constants.URL = baseUrl;
        return baseUrl;
    }

    public void setBaseURL(String url) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("base_url", url);
        editor.commit();
    }

    public String getResidentsURL() {
        String residents_url = settings.getString("residents_url", "https://soja.co.ke/soja-rest/index.php/api/residents/");
        Constants.URL = residents_url;
        return residents_url;
    }

    public void setResidentsURL(String url) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("residents_url", url);
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

    public boolean isLoggedin() {
        return settings.getBoolean("isLoggedin", false);
    }

    public void setIsLoggedin(boolean status) {
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

    public String getSentryType() {
        return settings.getString("sojaType", "");
    }

    public void setSentryType(String type) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sojaType", type);
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

    public boolean canPrint() {
        return settings.getBoolean("canPrint", false);
    }

    public void setCanPrint(boolean status) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("canPrint", status);
        editor.commit();
    }

    public String getPremiseName() {

        return settings.getString("premise_name", "SOJA");
    }

    public void setPremiseName(String premise_name) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("premise_name", premise_name);
        editor.commit();
    }

    public boolean isPhoneNumberEnabled() {
        return settings.getBoolean("record_phone_number", false);
    }

    public void setPhoneNumberEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("record_phone_number", isEnabled);
        editor.commit();
    }

    public boolean isPhoneVerificationEnabled() {
        return settings.getBoolean("verify_phone_number", false);
    }

    public void setPhoneVerification(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("verify_phone_number", isEnabled);
        editor.commit();
    }

    public boolean isCompanyNameEnabled() {
        return settings.getBoolean("record_company_name", false);
    }

    public void setCompanyNameEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("record_company_name", isEnabled);
        editor.commit();
    }

    public boolean isSelectHostsEnabled() {
        return settings.getBoolean("select_hosts", false);
    }

    public void setSelectHostsEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("select_hosts", isEnabled);
        editor.commit();
    }

    public boolean isFingerprintsEnabled() {
        return settings.getBoolean("fingerprints", false);
    }

    public void setFingerprintsEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("fingerprints", isEnabled);
        editor.commit();
    }

    public boolean isSMSCheckInEnabled() {
        return settings.getBoolean("sms", false);
    }

    public void setSMSCheckInEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("sms", isEnabled);
        editor.commit();
    }

    public boolean isScanPicture() {
        return settings.getBoolean("scan_photo", false);
    }

    public void setScanPicture(boolean isEnabled) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("scan_photo", isEnabled);
        editor.commit();
    }

    public void setDarkModeOn(boolean darkModeOn) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("darkModeOn", darkModeOn);
        editor.commit();
    }

    public boolean isDarkModeOn() {
        return settings.getBoolean("darkModeOn", false);
    }


    public void setRecordInvitees(boolean recordInvitees) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("recordInvitees", recordInvitees);
        editor.commit();
    }

    public boolean isRecordInvitees() {
        return settings.getBoolean("recordInvitees", false);
    }


    public void setOrganizationId(String organizationID) {

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("organizationId", organizationID);
        editor.commit();
    }


    public User getCurrentUser() {
        User user = Paper.book().read("currentUser");
        return user;
    }

    public void setCurrentUser(User user) {
        Paper.book().write("currentUser", user);
    }

    public void clearCurrentUser() {
        Paper.book().delete("currentUser");
    }


    public String getOrganizationId() {
        return settings.getString("organizationId", "SOJA");
    }


}
