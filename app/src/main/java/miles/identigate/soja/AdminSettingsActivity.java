package miles.identigate.soja;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.UserInterface.Login;

public class AdminSettingsActivity extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    EditText custom_ip;
    Button set;
    Preferences preferences;

    RadioButton custom;
    RadioButton main;
    DatabaseHandler handler;

    Switch printerSwitch;
    Switch phoneSwitch;
    Switch companySwitch;
    Switch host;
    Switch fingerprints;
    Switch sms;
    Switch scan_photo;

    boolean refresh = false;


    TextView versionCode;

    boolean customServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title);
        title.setText("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        preferences = new Preferences(this);
        handler = new DatabaseHandler(this);

        custom_ip = findViewById(R.id.custom_ip);
        set = findViewById(R.id.set);
        custom = findViewById(R.id.custom);
        main = findViewById(R.id.main);

        printerSwitch = findViewById(R.id.printer);
        phoneSwitch = findViewById(R.id.phone);
        scan_photo = findViewById(R.id.scan_photo);
        companySwitch = findViewById(R.id.company);
        host = findViewById(R.id.host);
        fingerprints = findViewById(R.id.fingerprints);
        sms = findViewById(R.id.sms);
        versionCode = findViewById(R.id.version_number);

        printerSwitch.setChecked(preferences.canPrint());
        phoneSwitch.setChecked(preferences.isPhoneNumberEnabled());
        companySwitch.setChecked(preferences.isCompanyNameEnabled());
        host.setChecked(preferences.isSelectHostsEnabled());
        fingerprints.setChecked(preferences.isFingerprintsEnabled());
        sms.setChecked(preferences.isSMSCheckInEnabled());
        scan_photo.setChecked(preferences.isScanPicture());

        fingerprints.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (getDeviceName().contains("Ruggbo")) {
                    } else {
                        Toast.makeText(AdminSettingsActivity.this, "Not a Fingerprint Device", Toast.LENGTH_SHORT).show();
                        fingerprints.setChecked(false);
                    }
                }
            }
        });


//        Get Version Name
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;

            versionCode.setText("Version Name: " + versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        if (preferences.getBaseURL().equals("https://soja.co.ke/soja-rest/index.php/api/visits/")) {
            main.setChecked(true);
            customServer = false;
        } else {
            customServer = true;
            custom.setChecked(true);
            String s = preferences.getBaseURL();
            custom_ip.setText(s.substring(0, s.length() - 31));
        }

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.setCanPrint(printerSwitch.isChecked());
                preferences.setPhoneNumberEnabled(phoneSwitch.isChecked());
                preferences.setCompanyNameEnabled(companySwitch.isChecked());
                preferences.setSelectHostsEnabled(host.isChecked());
                preferences.setFingerprintsEnabled(fingerprints.isChecked());
                preferences.setSMSCheckInEnabled(sms.isChecked());
                preferences.setScanPicture(scan_photo.isChecked());
                if (customServer) {
                    String ip = custom_ip.getText().toString().trim();
                    if (ip.isEmpty()) {
                        custom_ip.setError("Invalid IP");
                        return;
                    }
                    if ((!ip.startsWith("http://")) && (!ip.startsWith("https://"))) {
                        custom_ip.setError("IP must start with http:// or https://");
                        return;
                    }
                    if (!ip.endsWith("/")) {
                        ip += "/";
                    }
                    ip += "soja-rest/index.php/api/visits/";
                    Log.d("IP", ip);
                    preferences.setBaseURL(ip);
                } else {
                    preferences.setBaseURL("https://soja.co.ke/soja-rest/index.php/api/visits/");
                }
                Toast.makeText(getApplicationContext(), "Settings updated", Toast.LENGTH_SHORT).show();
                if (!refresh) {
                    startActivity(new Intent(AdminSettingsActivity.this, Dashboard.class));
                    finish();
                } else {
                    startActivity(new Intent(AdminSettingsActivity.this, Login.class));
                    finish();
                }

            }
        });

    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.main:
                if (checked)
                    customServer = false;
                refresh = true;
                break;
            case R.id.custom:
                if (checked) {
                    customServer = true;
                    if (custom_ip.getVisibility() != View.VISIBLE)
                        custom_ip.setVisibility(View.VISIBLE);
                    refresh = true;
                }

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

}


