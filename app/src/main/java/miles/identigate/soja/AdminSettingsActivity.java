package miles.identigate.soja;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.UserInterface.Login;
import miles.identigate.soja.UserInterface.Visitors;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.font.TextViewRegular;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.models.PremiseResident;

public class AdminSettingsActivity extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "AdminSettingsActivity";

    EditText custom_ip;
    Button set;
    Preferences preferences;

    RadioButton custom;
    RadioButton main;
    DatabaseHandler handler;

    LinearLayout linServer;
    Switch printerSwitch;
    Switch phoneSwitch;
    Switch companySwitch;
    Switch host;
    Switch fingerprints;
    Switch sms;
    Switch darkMode;

    TextView serverName;


    boolean refresh = false;


    TextView versionCode;

    boolean customServer = false;
    @BindView(R.id.sentryName)
    TextViewBold sentryName;
    @BindView(R.id.premiseName)
    TextViewRegular premiseName;
    @BindView(R.id.phone_verify)
    Switch phoneVerify;
    @BindView(R.id.lin_phone_verify)
    LinearLayout linPhoneVerify;
    @BindView(R.id.record_invitees)
    Switch recordInvitees;
    @BindView(R.id.lin_record_invitees)
    LinearLayout linRecordInvitees;
    @BindView(R.id.residents_list)
    TextViewBold residentsList;
    @BindView(R.id.lin_filter_residents)
    LinearLayout linFilterResidents;

    ArrayList<String> hostTypes = new ArrayList<>();
    ArrayList<String> selectedHostTypes = new ArrayList<>();

    CharSequence filterItems[];

    boolean[] checkedColors;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);

        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_admin_settings);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title);
        title.setText("Settings");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        handler = new DatabaseHandler(this);

        custom_ip = findViewById(R.id.custom_ip);
        set = findViewById(R.id.set);
        custom = findViewById(R.id.custom);
        main = findViewById(R.id.main);
        linServer = findViewById(R.id.lin_server);

        printerSwitch = findViewById(R.id.printer);
        phoneSwitch = findViewById(R.id.phone);
        companySwitch = findViewById(R.id.company);
        host = findViewById(R.id.host);
        fingerprints = findViewById(R.id.fingerprints);
        sms = findViewById(R.id.sms);
        versionCode = findViewById(R.id.version_number);
        darkMode = findViewById(R.id.darkMode);
        serverName = findViewById(R.id.serverName);


        printerSwitch.setChecked(preferences.canPrint());
        phoneVerify.setChecked(preferences.isPhoneVerificationEnabled());
        phoneSwitch.setChecked(preferences.isPhoneNumberEnabled());
        companySwitch.setChecked(preferences.isCompanyNameEnabled());
        host.setChecked(preferences.isSelectHostsEnabled());
        fingerprints.setChecked(preferences.isFingerprintsEnabled());
        recordInvitees.setChecked(preferences.isRecordInvitees());
        sms.setChecked(preferences.isSMSCheckInEnabled());
        darkMode.setChecked(preferences.isDarkModeOn());
        premiseName.setText(preferences.getPremiseName());
        sentryName.setText(preferences.getName());


        if (phoneSwitch.isChecked()) {
            linPhoneVerify.setVisibility(View.VISIBLE);
        } else {
            phoneVerify.setChecked(false);
            linPhoneVerify.setVisibility(View.GONE);
        }

        phoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    linPhoneVerify.setVisibility(View.VISIBLE);
                } else {
                    phoneVerify.setChecked(false);
                    linPhoneVerify.setVisibility(View.GONE);
                }
            }
        });

        darkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    preferences.setDarkModeOn(true);
                    restartApp();
                } else {
                    preferences.setDarkModeOn(false);
                    restartApp();
                }
            }
        });


//        if (preferences.getBaseURL().contains("casuals")) {
//            sms.setVisibility(View.GONE);
//        }

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

            versionCode.setText("Version: " + versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        setServerName();


        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        linFilterResidents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterResidentsDialog();
            }
        });


        linServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminSettingsActivity.this);
                builder.setTitle("Please select server");
                builder.setCancelable(true);


                View view = getLayoutInflater().inflate(R.layout.dialog_select_server, null);

                builder.setView(view);


                final RadioButton custom = view.findViewById(R.id.custom);
                final RadioButton main = view.findViewById(R.id.main);
                final EditText custom_ip = view.findViewById(R.id.custom_ip);
                final TextView okButton = view.findViewById(R.id.ok_button);
                final TextView cancelButton = view.findViewById(R.id.cancel_button);


                if (preferences.getBaseURL().equals("https://soja.co.ke/soja-rest/index.php/api/visits/")) {
                    main.setChecked(true);
                    customServer = false;
                } else {
                    customServer = true;
                    custom.setChecked(true);
                    String s = preferences.getBaseURL();
                    custom_ip.setText(s.substring(0, s.length() - 31));
                }

                final AlertDialog alert = builder.create();


                alert.show();

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: Selected");
                        if (custom.isChecked()) {
                            Log.d(TAG, "onClick: Selected");
                            String ip = custom_ip.getText().toString().trim();
                            if (ip.isEmpty()) {
                                custom_ip.setError("Invalid IP");
                                return;
                            } else if ((!ip.startsWith("http://")) && (!ip.startsWith("https://"))) {
                                custom_ip.setError("IP must start with http:// or https://");
                                return;
                            } else if ((ip.startsWith("http://") || ip.startsWith("https://")) && ip.length() <= 8) {
                                custom_ip.setError("Please complete http:// or https://");
                                return;
                            }
                            if (!ip.endsWith("/")) {
                                ip += "/";
                            }

                            ip += "soja-rest/index.php/api/visits/";
                            Log.d("IP", ip);
                            refresh = true;
                            preferences.setBaseURL(ip);
                            preferences.setResidentsURL(ip.replace("visits", "residents"));


                            setServerName();

                            alert.cancel();


                        } else if (main.isChecked()) {
                            Log.d(TAG, "onClick: Selected Main");

                            preferences.setBaseURL("https://soja.co.ke/soja-rest/index.php/api/visits/");
                            preferences.setResidentsURL("https://soja.co.ke/soja-rest/index.php/api/residents/");


                            setServerName();
                            alert.cancel();
                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setServerName();
                        alert.cancel();
                    }
                });


            }
        });


    }

    private void showFilterResidentsDialog() {

        ArrayList<PremiseResident> premiseResidents = new ArrayList<>();

        handler = new DatabaseHandler(this);


        premiseResidents.clear();

        for (int i = 0; i < handler.getPremiseResidentsWithoutFingerprint().size(); i++) {
//            Toast.makeText(context, handler.getPremiseResidentsWithoutFingerprint().get(i).getFirstName(), Toast.LENGTH_SHORT).show();
            premiseResidents.add(handler.getPremiseResidentsWithoutFingerprint().get(i));
        }


        for (PremiseResident premiseResident : premiseResidents) {
            if (premiseResident.getHostType() != null && !premiseResident.getHostType().equals("null")
            ) {

                if (!hostTypes.contains(premiseResident.getHostType())) {
                    hostTypes.add(premiseResident.getHostType());
                }

            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(AdminSettingsActivity.this);


        filterItems = new CharSequence[hostTypes.size()];
        checkedColors = new boolean[hostTypes.size()];

        for (int i = 0; i < hostTypes.size(); i++) {

            filterItems[i] = hostTypes.get(i);

        }


        builder.setMultiChoiceItems(filterItems, checkedColors, (DialogInterface.OnMultiChoiceClickListener) (dialog, which, isChecked) -> {
            checkedColors[which] = isChecked;

        });


        // Specify the dialog is not
        // cancelable
        builder.setCancelable(false);

        // Set a title for alert dialog
        builder.setTitle("Filter Residents By");

        residentsList.setText("");
        // Set the positive/yes button click listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
//                tv.setText("Your preferred colors..... \n");
                Log.d(TAG, "onClick: " + checkedColors.length);

                for (int i = 0; i < checkedColors.length; i++) {
                    boolean checked = checkedColors[i];

                    if (checked) {

                        residentsList.setText(residentsList.getText() + hostTypes.get(i) + ",");

                    }
                }
            }
        });


        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();

    }

    private ArrayList<String> getSelectedHostTypes() {
        String residentListString = residentsList.getText().toString();
        String[] parts = residentListString.split(",");
        for (int i = 0; i < parts.length; i++) {
            if (!TextUtils.isEmpty(parts[i])) {

                selectedHostTypes.add(parts[i]);
                Log.d(TAG, "getSelectedHostTypes: Host Type " + parts[i]);

            }
        }

        return selectedHostTypes;
    }

    private void saveSettings() {
        preferences.setCanPrint(printerSwitch.isChecked());
        preferences.setPhoneNumberEnabled(phoneSwitch.isChecked());
        preferences.setPhoneVerification(phoneVerify.isChecked());
        preferences.setCompanyNameEnabled(companySwitch.isChecked());
        preferences.setSelectHostsEnabled(host.isChecked());
        preferences.setFingerprintsEnabled(fingerprints.isChecked());
        preferences.setSMSCheckInEnabled(sms.isChecked());
        preferences.setRecordInvitees(recordInvitees.isChecked());
//                preferences.setScanPicture(scan_photo.isChecked());
        preferences.setDarkModeOn(preferences.isDarkModeOn());
//        preferences.setHostTypes(getSelectedHostTypes());


//                if (customServer) {
//                    String ip = custom_ip.getText().toString().trim();
//                    if (ip.isEmpty()) {
//                        custom_ip.setError("Invalid IP");
//                        return;
//                    }
//                    if ((!ip.startsWith("http://")) && (!ip.startsWith("https://"))) {
//                        custom_ip.setError("IP must start with http:// or https://");
//                        return;
//                    }
//                    if (!ip.endsWith("/")) {
//                        ip += "/";
//                    }
//                    ip += "soja-rest/index.php/api/visits/";
//                    Log.d("IP", ip);
//                    preferences.setBaseURL(ip);
//                } else {
//                    preferences.setBaseURL("https://soja.co.ke/soja-rest/index.php/api/visits/");
//                }
        Toast.makeText(getApplicationContext(), "Settings updated", Toast.LENGTH_SHORT).show();
        if (!refresh) {
            startActivity(new Intent(AdminSettingsActivity.this, Dashboard.class));
            finish();
        } else {
            startActivity(new Intent(AdminSettingsActivity.this, Login.class));
            finish();
        }
    }

    public void setServerName() {
        String server = preferences.getBaseURL();

        int count = 0;
        for (char c : server.toCharArray()) {
            if (Character.isDigit(c)) {
                count++;
            }
        }

        if (server.contains("test")) {
            serverName.setText("Test Server");
        } else if (server.contains("casuals")) {
            serverName.setText("Casuals Server");
        } else if (server.contains("sportpesa")) {
            serverName.setText("SportPesa Server");
        } else if (server.contains("events")) {
            serverName.setText("Events Server");
        } else if (count > 5) {
            serverName.setText("Custom Server");
        } else {
            serverName.setText("Main Server");

        }


    }


    public void restartApp() {
        Intent i = new Intent(getApplicationContext(), AdminSettingsActivity.class);
        i.putExtra("goDisplay", true);
        startActivity(i);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
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
//                    if (custom_ip.getVisibility() != View.VISIBLE)
//                        custom_ip.setVisibility(View.VISIBLE);
                    refresh = true;
                }

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.ic_save) {
            saveSettings();
        } else if (item.getItemId() == android.R.id.home) {
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


