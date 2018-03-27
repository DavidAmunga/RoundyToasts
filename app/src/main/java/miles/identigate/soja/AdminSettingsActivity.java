package miles.identigate.soja;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import miles.identigate.soja.Helpers.Constants;
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

    RadioButton no_printer;
    RadioButton yes_printer;
    RadioButton custom;
    RadioButton main;
    DatabaseHandler handler;

    boolean canPrint = false;
    boolean customServer  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        preferences = new Preferences(this);
        handler=new DatabaseHandler(this);

        custom_ip = (EditText)findViewById(R.id.custom_ip);
        set = (Button)findViewById(R.id.set);
        no_printer = (RadioButton)findViewById(R.id.no_printer);
        yes_printer = (RadioButton)findViewById(R.id.yes_printer);
        custom = (RadioButton)findViewById(R.id.custom);
        main = (RadioButton)findViewById(R.id.main);

        if (preferences.canPrint()){
            yes_printer.setChecked(true);
        }else {
            no_printer.setChecked(true);
        }

        if (preferences.getBaseURL().equals("https://soja.co.ke/soja-rest/index.php/api/visits/")){
            main.setChecked(true);
            customServer = false;
        }else {
            customServer = true;
            custom.setChecked(true);
            String s = preferences.getBaseURL();
            custom_ip.setText(s.substring(0, s.length() - 31));
        }

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.setCanPrint(canPrint);
                if (customServer){
                    String ip = custom_ip.getText().toString().trim();
                    if (ip.isEmpty()){
                        custom_ip.setError("Invalid IP");
                        return;
                    }
                    if ((!ip.startsWith("http://")) && (!ip.startsWith("https://"))){
                        custom_ip.setError("IP must start with http:// or https://");
                        return;
                    }
                    if (!ip.endsWith("/")){
                        ip += "/";
                    }
                    ip += "soja-rest/index.php/api/visits/";
                    Log.d("IP", ip);
                    preferences.setBaseURL(ip);
                }else {
                    preferences.setBaseURL("https://soja.co.ke/soja-rest/index.php/api/visits/");
                }
                preferences.setIsLoggedin(false);
                preferences.setDeviceId(null);
                preferences.setPremiseName("");
                preferences.setName("");
                preferences.setId("");
                SQLiteDatabase db = handler.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_VISITOR_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_INCIDENT_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_SERVICE_PROVIDERS_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_HOUSES);

                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor e = getPrefs.edit();
                e.putBoolean("firstStart", true);
                e.apply();
                Toast.makeText(getApplicationContext(), "Settings updated successfully.Please login again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

    }
    public void onRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.main:
                    if (checked)
                        customServer = false;
                break;
            case R.id.custom:
                if (checked){
                    customServer = true;
                    if (custom_ip.getVisibility() != View.VISIBLE)
                        custom_ip.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.no_printer:
                if (checked)
                    canPrint = false;
                break;
            case R.id.yes_printer:
                if (checked)
                    canPrint = true;

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
