package miles.identigate.soja;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.journeyapps.barcodescanner.Util;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import miles.identigate.soja.Fragments.CheckIn;
import miles.identigate.soja.Fragments.CheckOut;
import miles.identigate.soja.Fragments.Logs;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.PremiseResident;
import miles.identigate.soja.Services.SyncService;
import miles.identigate.soja.UserInterface.Login;
//import miles.identigate.soja.UserInterface.Login;

public class Dashboard extends SojaActivity {
    private static final String TAG = "Dashboard";
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private  static final int CAMERA_REQUEST=200;
    private  static  final int STORAGE_REQUEST = 300;
    private static final  int PHONE_STATE_REQUEST  = 400;
    private  MaterialDialog dialog;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    DatabaseHandler handler;
    String visitorResult;
    String providerResult;
    String incidentsResult;
    String houseResult;
    String premiseResidentResult;
    Preferences preferences;
    private BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString(SyncService.MESSAGE);
                int resultCode = bundle.getInt(SyncService.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(Dashboard.this, string,Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Dashboard.this,string,Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        handler=new DatabaseHandler(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences=new Preferences(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        final Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/OpenSans-Semibold.ttf");


        SmartTabLayout tabLayout = (SmartTabLayout) findViewById(R.id.tabs);
//        Custom Font Tab
        tabLayout.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                LayoutInflater inflater=LayoutInflater.from(container.getContext());
                View tab = inflater.inflate(R.layout.custom_tab, container, false);
                TextView customText =   tab.findViewById(R.id.custom_text);

                switch (position) {
                    case 0:
                        customText.setTypeface(font);
                        customText.setText(adapter.getPageTitle(position));
                        break;
                    case 1:
                        customText.setTypeface(font);
                        customText.setText(adapter.getPageTitle(position));
                        break;
                    case 2:
                        customText.setTypeface(font);
                        customText.setText(adapter.getPageTitle(position));
                        break;
                    default:
                        throw new IllegalStateException("Invalid position: " + position);
                }
                return tab;
            }
        });

        tabLayout.setViewPager(mViewPager);
        SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
        boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
        if (isFirstStart&&preferences.isLoggedin()) {
            if (new CheckConnection().check(this)) {
                new FetchDetails().execute();
                SharedPreferences.Editor e = getPrefs.edit();
                e.putBoolean("firstStart", false);
                e.apply();
            }else {
                 dialog = new MaterialDialog.Builder(Dashboard.this)
                        .title("Soja")
                        .titleGravity(GravityEnum.CENTER)
                        .titleColor(getResources().getColor(R.color.ColorPrimary))
                        .content("It seems you have a poor internet connection.Please try again later.")
                        .cancelable(true)
                        .positiveText("EXIT")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                preferences.setIsLoggedin(false);
                                SharedPreferences getPrefs = PreferenceManager
                                        .getDefaultSharedPreferences(getBaseContext());
                                SharedPreferences.Editor e = getPrefs.edit();
                                e.putBoolean("firstStart", true);
                                e.apply();
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .widgetColorRes(R.color.colorPrimary)
                        .build();
                 dialog.show();
            }
        }else if(isFirstStart&& !preferences.isLoggedin()){
           //startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }

        askPermissions();


//        Toast.makeText(this, getDeviceName(), Toast.LENGTH_SHORT).show();


    }


    private void askPermissions(){
        if (ContextCompat.checkSelfPermission(Dashboard.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this, new String[] { permission.CAMERA }, CAMERA_REQUEST);
        }
        if (ContextCompat.checkSelfPermission(Dashboard.this, permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this, new String[] { permission.WRITE_EXTERNAL_STORAGE }, STORAGE_REQUEST);
        }
        if (ContextCompat.checkSelfPermission(Dashboard.this, permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this, new String[] { permission.READ_PHONE_STATE }, PHONE_STATE_REQUEST);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
            case STORAGE_REQUEST:

                break;
            case PHONE_STATE_REQUEST:

                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            /*for (PremiseResident premiseResident: handler.getPremiseResidents()){
                if (premiseResident.getFingerPrint() != null)
                    Log.d(premiseResident.getFirstName() + premiseResident.getLastName(), premiseResident.getFingerPrint());
            }*/
            dialog = new MaterialDialog.Builder(Dashboard.this)
                    .title("Logout")
                    .content("You are about to logout of Soja.\nYou will need to login next time you use the app.Are you sure?")
                    .positiveText("Ok")
                    .negativeText("Cancel")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {

                            preferences.setIsLoggedin(false);
                            preferences.setDeviceId(null);
                            preferences.setPremiseName("");
                            preferences.setName("");
                            preferences.setId("");
                            preferences.setCanPrint(false);
                            preferences.setFingerprintsEnabled(false);


                            SQLiteDatabase db = handler.getWritableDatabase();
                            db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_VISITOR_TYPES);
                            db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_INCIDENT_TYPES);
                            db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_SERVICE_PROVIDERS_TYPES);
                            db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_HOUSES);
                            db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_PREMISE_RESIDENTS);

                            SharedPreferences getPrefs = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor e = getPrefs.edit();
                            e.putBoolean("firstStart", true);
                            e.apply();
                           startActivity(new Intent(Dashboard.this, Login.class));
                           finish();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                        }
                    }).build();
                    dialog.show();
            return true;
        }else if(id == R.id.settings){
            startActivity(new Intent(getApplicationContext(), AdminSettingsActivity.class));
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    CheckIn checkIn=new CheckIn();
                    return checkIn;
                case 1:
                    CheckOut checkOut=new CheckOut();
                    return checkOut;
                case 2:
                    Logs logs=new Logs();
                    return logs;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CHECK IN";
                case 1:
                    return "CHECK OUT";
                case 2:
                    return "LOGS";
            }
            return null;
        }
    }
    private class FetchDetails extends AsyncTask<Void, String, String> {
        MaterialDialog builder=new MaterialDialog.Builder(Dashboard.this)
                .title("Soja")
                .titleGravity(GravityEnum.CENTER)
                .titleColor(getResources().getColor(R.color.ColorPrimary))
                .content("Please wait while we initialize the application.\nThis might take time depending on your internet.")
                .progress(true, 0)
                .cancelable(true)
                .widgetColorRes(R.color.colorPrimary)
                .build();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            visitorResult=NetworkHandler.GET(preferences.getBaseURL()+"visitor-types");
            providerResult=NetworkHandler.GET(preferences.getBaseURL()+"service-providers");
            incidentsResult=NetworkHandler.GET(preferences.getBaseURL()+"incident-types");
            houseResult=NetworkHandler.GET(preferences.getBaseURL()+"houses-blocks/zone/"+preferences.getPremiseZoneId());
            premiseResidentResult =  NetworkHandler.GET(preferences.getBaseURL() + "houses-residents/?premise=" + preferences.getPremise());
            return "success";
        }

        protected void onPostExecute(String result) {
            builder.dismiss();
            getAllData();

        }
    }
    public void getAllData(){
        if(visitorResult==null||providerResult==null||incidentsResult==null||houseResult==null || premiseResidentResult == null){
            Toast.makeText(getApplicationContext(),"An error occurred",Toast.LENGTH_LONG).show();
        }else {
            try {
                JSONObject visitorObject = new JSONObject(visitorResult);
                JSONObject providerObject = new JSONObject(providerResult);
                JSONObject incidentsObject = new JSONObject(incidentsResult);
                JSONObject housesObject=new JSONObject(houseResult);
                JSONObject premiseResidentObject = new JSONObject(premiseResidentResult);

                SQLiteDatabase db = handler.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_VISITOR_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_INCIDENT_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_SERVICE_PROVIDERS_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_HOUSES);
                db.execSQL("DROP TABLE IF EXISTS " + handler.TABLE_PREMISE_RESIDENTS);

                db.execSQL(handler.CREATE_TABLE_INCIDENT_TYPES);
                db.execSQL(handler.CREATE_TABLE_VISITOR_TYPES);
                db.execSQL(handler.CREATE_TABLE_SERVICE_PROVIDERS_TYPES);
                db.execSQL(handler.CREATE_TABLE_HOUSES);
                db.execSQL(handler.CREATE_PREMISE_RESIDENTS_TABLE);

                /*db.execSQL(handler.CREATE_TABLE_DRIVE_IN);
                db.execSQL(handler.CREATE_TABLE_SERVICE_PROVIDERS);
                db.execSQL(handler.CREATE_TABLE_RESIDENTS);
                db.execSQL(handler.CREATE_TABLE_INCIDENTS);*/
                //Visitor types
                if (visitorObject.getInt("result_code") == 0 && visitorObject.getString("result_text").equals("OK")) {
                    JSONArray visitorArray = visitorObject.getJSONArray("result_content");
                    for (int i = 0; i < visitorArray.length(); i++) {
                        JSONObject visitorType = visitorArray.getJSONObject(i);
                        handler.insertVisitorType(visitorType.getString("id"), visitorType.getString("name"));
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Couldn't retrieve visitor types", Toast.LENGTH_SHORT).show();
                }
                //Provider Types
                if (providerObject.getInt("result_code") == 0 && providerObject.getString("result_text").equals("OK")) {
                    JSONArray providerArray = providerObject.getJSONArray("result_content");
                    for (int i = 0; i < providerArray.length(); i++) {
                        JSONObject provider = providerArray.getJSONObject(i);
                        handler.insertServiceProviderType(provider.getString("id"), provider.getString("name"), provider.getString("Description"));
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Couldn't retrieve service providers", Toast.LENGTH_SHORT).show();
                }
                //INCIDENTS
                if (incidentsObject.getInt("result_code") == 0 && incidentsObject.getString("result_text").equals("OK")) {
                    JSONArray incidentsArray = incidentsObject.getJSONArray("result_content");
                    for (int i = 0; i < incidentsArray.length(); i++) {
                        JSONObject incident = incidentsArray.getJSONObject(i);
                        handler.insertIncidentTypes(incident.getString("id"), incident.getString("description"));
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Couldn't retrieve incident types", Toast.LENGTH_SHORT).show();
                }

                //Houses
                if (housesObject.getInt("result_code") == 0 && housesObject.getString("result_text").equals("OK")) {
                    JSONArray housesArray = housesObject.getJSONArray("result_content");
                    for (int i = 0; i < housesArray.length(); i++) {
                        JSONObject house = housesArray.getJSONObject(i);
                        handler.insertHouse(house.getString("house_id"), house.getString("house_description"),house.getString("block_description"));
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Couldn't retrieve houses", Toast.LENGTH_SHORT).show();
                }

               if (premiseResidentObject.getInt("result_code") == 0 && premiseResidentObject.getString("result_text").equals("OK")) {
                    JSONArray residentsArray = premiseResidentObject.getJSONArray("result_content");
                    for (int i = 0; i < residentsArray.length(); i++) {
                        JSONObject resident = residentsArray.getJSONObject(i);
                        int length = 0;
                        if(resident.getString("length") != "null"){
                            length = Integer.valueOf(resident.getString("length"));
                        }
                        String fingerPrint = resident.get("fingerprint")==null?null:resident.getString("fingerprint");
                        if (fingerPrint == "0")
                            fingerPrint = null;
                        fingerPrint = fingerPrint.replaceAll("\\n", "");
                        fingerPrint = fingerPrint.replace("\\r", "");
                        handler.insertPremiseVisitor(resident.getString("id"), resident.getString("id_number"),resident.getString("firstname"), resident.getString("lastname"), fingerPrint,length, resident.getString("house_id"), resident.getString("host_id"));
                    }
                } else {
                    Toast.makeText(Dashboard.this, "Couldn't retrieve premise residents", Toast.LENGTH_SHORT).show();
                }




            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, new IntentFilter(SyncService.NOTIFICATION));

    }

}
