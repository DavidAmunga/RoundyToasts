package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import miles.identigate.soja.Dashboard;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.R;
import miles.identigate.soja.app.Common;

// 909090, soja2016
public class Login extends AppCompatActivity {
    private static final String TAG = "Login";
    Toolbar toolbar;
    Button login;
    EditText username;
    EditText pin;
    DatabaseHandler handler;
    Preferences preferences;

    boolean showPassword = false;

    //My device id: 9105772e98eb39b2
    //Martin: c9d31fb651cd2601
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        toolbar = findViewById(R.id.app_bar);
        login = findViewById(R.id.login);
        username = findViewById(R.id.username);
        pin = findViewById(R.id.pin);
//        setSupportActionBar(toolbar);
        preferences = new Preferences(this);
        handler = new DatabaseHandler(this);
        /*String authorizationString = "Basic " + Base64.encodeToString(
                ("admin" + ":" + "1234").getBytes(),
                Base64.NO_WRAP); //Base64.NO_WRAP flag
        post.setHeader("Authorization", authorizationString);*/
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor e = getPrefs.edit();
        e.putBoolean("firstStart", true);
        e.apply();
        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //Log.v("Device id",android_id);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailValue = username.getText().toString();
                String passwordValue = pin.getText().toString();
                if (new CheckConnection().check(Login.this)) {
                    if (emailValue.equals("") || emailValue.equals(null)) {
                        username.setError("Field is required.");
                    } else if (passwordValue.equals("") || passwordValue.equals(null)) {
                        pin.setError("Pin is required");
                    } else {
                        /*Utils.getMACAddress("wlan0");
                        Utils.getMACAddress("eth0");
                        Utils.getIPAddress(true); // IPv4
                        Utils.getIPAddress(false); // IPv6 */
                        String s = preferences.getBaseURL();
                        String url = s.substring(0, s.length() - 11) + "api/auth/get-access-token";
                        String urlParameters = null;
                        try {
                            urlParameters = "username=" + URLEncoder.encode(emailValue, "UTF-8") +
                                    "&password=" + URLEncoder.encode(passwordValue, "UTF-8") +
                                    "&deviceCode=" + URLEncoder.encode("9105772e98eb39b2", "UTF-8");
                            new Validate().execute(url, urlParameters);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    new CheckConnection().show_dialog(Login.this);
                }
            }
        });

        pin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (Drawable drawable : pin.getCompoundDrawables())
                    if (drawable != null) {
                        if (hasFocus) {
                            drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN));
                        } else {
                            drawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN));
                        }
                    }
            }
        });


        pin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (pin.getRight() - pin.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showPassword = !showPassword;
                        if (!showPassword) {
                            // hide password
                            pin.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        } else {
                            // show password
                            pin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        }

//                        Toast.makeText(Login.this, "Clicked", Toast.LENGTH_SHORT).show();

                        return true;
                    }
                }
                return false;
            }
        });
    }

    private class Validate extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(Login.this)
                .title("Login")
                .content("Signing in...")
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();
        }

        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {
            builder.dismiss();
            if (result != null) {
                //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                //Log.e("LOGIN",result);
                if (result.contains("access_token")) {
                    try {
                        JSONObject object = new JSONObject(result);
                        String id = object.getString("id");
                        String firstname = object.getString("firstname");
                        String lastname = object.getString("lastname");
                        String deviceId = object.getString("device_id");
                        String premiseZoneId = object.getString("premise_zone_id");
                        String access_token = object.getString("access_token");
                        String premiseId = object.getString("premise_id");
//                        String organizationID = object.getString("organisationID").isEmpty() ? "" : object.getString("organizationID");
                        preferences.setPremiseName(object.getString("premise_name"));
                        preferences.setIsLoggedin(true);
                        preferences.setPremise(premiseId);
                        preferences.setName(firstname + " " + lastname);
                        preferences.setId(id);
//                        preferences.setOrganizationId(organizationID);
                        preferences.setDeviceId(deviceId);
                        preferences.setToken(access_token);
                        preferences.setPremiseZoneId(premiseZoneId);

                        Common.updateFirebaseToken(preferences);

                        //preferences.setCanPrint(false);
                        logUser();
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject object = new JSONObject(result);
                        String text = object.getString("result_text");
                        if (text.contains("login")) {
                            new MaterialDialog.Builder(Login.this)
                                    .title("Error")
                                    .content("Login unsuccessful.Invalid credentials.Try again.")
                                    .cancelable(false)
                                    .positiveText("Ok")
                                    .show();
                        } else if (text.contains("Validation")) {
                            new MaterialDialog.Builder(Login.this)
                                    .title("Invalid device")
                                    .content("This device is not authorized to access this service.\nContinued access will lead to remote wiping of the device.")
                                    .cancelable(false)
                                    .positiveText("Ok")
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } else {
                Log.d(TAG, "onPostExecute: " + result);
                new MaterialDialog.Builder(Login.this)
                        .title("Result")
                        .content("An error occurred.Check your internet connection and try again.")
                        .positiveText("Ok")
                        .cancelable(false)
                        .show();


            }

        }
    }

    private void logUser() {
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(preferences.getId());
        Crashlytics.setUserEmail(preferences.getDeviceId());
        Crashlytics.setUserName(preferences.getName());
    }

}
