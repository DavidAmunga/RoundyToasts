package miles.identigate.soja.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.AdminSettingsActivity;
import miles.identigate.soja.R;
import miles.identigate.soja.UserInterface.Login;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.CheckConnection;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.ZxingHelperActivity;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.models.Ticket;
import miles.identigate.soja.service.network.api.APIClient;
import miles.identigate.soja.service.storage.model.Guest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanTicket extends AppCompatActivity {
    private static final String TAG = "ScanTicket";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.record_type)
    TextViewBold recordType;
    @BindView(R.id.scan_icon)
    ImageView scanIcon;
    @BindView(R.id.scan_id_text)
    TextViewBold scanIdText;

    @BindView(R.id.rel_id)
    RelativeLayout relId;

    String qr_token;
    ProgressDialog progressDialog;
    @BindView(R.id.btnList)
    Button btnList;


    private MaterialDialog dialog;

    DatabaseHandler handler;

    Preferences preferences;

    String visitorResult;
    String genderResult;
    String providerResult;
    String incidentsResult;
    String houseResult;
    String premiseResidentResult;
    private String idTypesResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ticket);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Scan Ticket");

        handler = new DatabaseHandler(this);


        scanIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanTicket.this).setCaptureActivity(ZxingHelperActivity.class).addExtra("PROMPT_MESSAGE", "Place QR Ticket Here to scan it").initiateScan();
            }
        });

        progressDialog = new ProgressDialog(this);


        dialog = new MaterialDialog.Builder(ScanTicket.this)
                .title("QR")
                .content("Checking QR...")
                .progress(true, 0)
                .cancelable(true)
                .widgetColorRes(R.color.colorPrimary)
                .build();


        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addTickets();
                startActivity(new Intent(ScanTicket.this, TicketList.class));

            }
        });
    }

    private void addTickets() {

//        FirebaseDatabase.getInstance().getReference(Common.TICKETS).setValue(null);

        APIClient.getClient(preferences, "").getGuests(
                preferences.getCurrentUser().getPremiseId(),
                0,
                500000000,
                null
        ).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    QueryResponse queryResponse = response.body();

                    Log.d(TAG, "onResponse: " + response.body());

                    if (queryResponse.getResultText().equals("OK") && queryResponse.getResultCode() == 0 && queryResponse.getResultContent() != null) {

                        Gson gson = new Gson();
                        JsonArray resultContent = gson.toJsonTree(queryResponse.getResultContent()).getAsJsonArray();
                        Type guestType = new TypeToken<ArrayList<Guest>>() {
                        }.getType();


                        ArrayList<Guest> guests = gson.fromJson(resultContent, guestType);
                        Log.d(TAG, "onResponse: " + guests.size());

//                        JsonObject resultDetail = gson.toJsonTree(queryResponse.getResultDetail()).getAsJsonObject();


                        if (guests.size() > 0) {
                            for (int i = 0; i < guests.size(); i++) {
                                Ticket ticket = new Ticket();
                                ticket.setTicketId(guests.get(i).getQrToken());
                                Log.d(TAG, "addTickets: Ticket No" + i);
                                Log.d(TAG, "addTickets: Ticket Token" + ticket.getTicketId());


                                FirebaseDatabase.getInstance().getReference(Common.TICKETS).child(ticket.getTicketId()).setValue(ticket);

                            }


                        }

//                        boolean hasMore = resultDetail.get("hasMore").getAsBoolean();


//                        Log.d(TAG, "onResponse: " + hasMore);


                    }

                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {

                Log.d(TAG, "onFailure: ", t);

                String errorMessage;

                if (t.getMessage() == null) {
                    errorMessage = "unknown error";
                } else {
                    errorMessage = t.getMessage();
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                //Log.v("QR",result.getContents());
                qr_token = result.getContents();

                Log.d(TAG, "onActivityResult: Here ");

                checkInFB(qr_token);

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void checkInFB(String token) {
        progressDialog.setMessage("Checking in...");
        progressDialog.show();

        Log.d(TAG, "checkInFB: " + token);
        FirebaseDatabase.getInstance().getReference(Common.TICKETS).orderByKey().equalTo(token).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.hasChild("ticketId")) {
//                      Add Entry Time
                        if (postSnapshot.hasChild("entryTime")) {
                            progressDialog.dismiss();

                            Toast.makeText(ScanTicket.this, "Already Checked In", Toast.LENGTH_SHORT).show();

                        } else {
//                        New Checked In User
                            Map<String, Object> updateInfo = new HashMap<>();
                            updateInfo.put("entryTime", Constants.getCurrentTimeStamp());

                            FirebaseDatabase.getInstance().getReference(Common.TICKETS).child(token).updateChildren(updateInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();


                                        recordCheckIn(token);
//                                    Toast.makeText(ScanTicket.this, "Checked In", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        progressDialog.dismiss();

                        Toast.makeText(ScanTicket.this, "Ticket Not Available", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            dialog = new MaterialDialog.Builder(ScanTicket.this)
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
                            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_VISITOR_TYPES);
                            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_INCIDENT_TYPES);
                            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_SERVICE_PROVIDERS_TYPES);
                            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_HOUSES);
                            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_PREMISE_RESIDENTS);
                            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_ID_TYPES);

                            SharedPreferences getPrefs = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor e = getPrefs.edit();
                            e.putBoolean("firstStart", true);
                            e.apply();
                            startActivity(new Intent(ScanTicket.this, Login.class));
                            finish();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                        }
                    }).build();
            dialog.show();
            return true;
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(), AdminSettingsActivity.class));
            return true;
        } else if (id == R.id.refresh) {
            if (new CheckConnection().check(this)) {
                new FetchDetails().execute();
            } else {
                dialog = new MaterialDialog.Builder(ScanTicket.this)
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
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchDetails extends AsyncTask<Void, String, String> {
        MaterialDialog builder = new MaterialDialog.Builder(ScanTicket.this)
                .title("Soja")
                .titleGravity(GravityEnum.CENTER)
                .titleColor(getResources().getColor(R.color.ColorPrimary))
                .content("Please wait while we initialize the application.\nThis might take time depending on your internet.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            visitorResult = NetworkHandler.GET(preferences.getBaseURL() + "visitor-types");
            providerResult = NetworkHandler.GET(preferences.getBaseURL() + "service-providers");
            incidentsResult = NetworkHandler.GET(preferences.getBaseURL() + "incident-types");
            genderResult = NetworkHandler.GET(preferences.getBaseURL() + "genders");
            idTypesResult = NetworkHandler.GET(preferences.getBaseURL() + "id_types");


//            TODO : Change Endpoint
            if (preferences.getBaseURL().contains("casuals")) {
                houseResult = NetworkHandler.GET(preferences.getBaseURL() + "active_events");
            } else {
                houseResult = NetworkHandler.GET(preferences.getBaseURL() + "houses-blocks/zone/" + preferences.getPremiseZoneId());
            }

            premiseResidentResult = NetworkHandler.GET(preferences.getBaseURL() + "houses-residents/?premise=" + preferences.getPremise());
            return "success";
        }

        protected void onPostExecute(String result) {
            builder.dismiss();
            getAllData();

        }
    }


    public void recordCheckIn(String qr_token) {
        String urlParameters = null;
        try {
            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getCurrentUser().getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getCurrentUser().getPremiseZoneId(), "UTF-8") +
                    "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                    "&qr=" + URLEncoder.encode(qr_token, "UTF-8");

            Log.d(TAG, "onActivityResult: " + preferences.getCurrentUser().getPremiseZoneId());


            new RecordQRCheckin().execute(preferences.getBaseURL().replace("visits", "residents") + "qr_checkin", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class RecordQRCheckin extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (dialog != null && !dialog.isShowing())
                dialog.show();
        }

        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {

            Log.d(TAG, "onPostExecute: Result" + result);
//            if (dialog != null && dialog.isShowing())
//                dialog.dismiss();
            if (result != null) {
                //Toast.makeText(ExpressCheckoutActivity.this,result, Toast.LENGTH_LONG).show();
                Object json = null;
                try {
                    json = new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject object = new JSONObject(result);
                        int result_code = object.getInt("result_code");
                        String result_text = object.getString("result_text");
                        if (result_code == 0) {

//                            recordCheckIn(qr_token);
                            showSuccess();

                            Log.d(TAG, "onPostExecute: SUCCESS");


                        } else {

                            new MaterialDialog.Builder(ScanTicket.this)
                                    .title("Notice")
                                    .content(result_text)
                                    .positiveText("Ok")
//                                    .negativeText("Check Out")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
//                                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
//                                            finish();
                                        }

//                                        @Override
//                                        public void onNegative(MaterialDialog dialog) {
//                                            dialog.dismiss();
//
//
//                                            recordCheckOut();
//
//                                        }
                                    })
                                    .show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void recordCheckOut() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("Checking Out");
            progressDialog.show();
        }
//        Log.d(TAG, "recordCheckOut: "+idNumber+","+preferences.getDeviceId());

        String urlParameters = null;
        try {
            urlParameters = "qr=" + qr_token +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                    "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");


            Log.d(TAG, "onNegative: " + preferences.getResidentsURL() + "qr_checkout?" + urlParameters);
            new RecordQRCheckOut().execute(preferences.getResidentsURL() + "qr_checkout", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    void showSuccess() {
        Log.d(TAG, "showSuccess: Yes");
        progressDialog.dismiss();
        dialog.dismiss();


        dialog = new MaterialDialog.Builder(this)
                .title("CHECKED IN")
                .titleGravity(GravityEnum.CENTER)
                .customView(R.layout.success_dialog, true)
                .positiveText("OK")
                .negativeText(" CANCEL")
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        finish();
                    }
                })
                .build();
        View view = dialog.getCustomView();
        TextView messageText = view.findViewById(R.id.message);
        messageText.setText("Checked In");
        dialog.show();
    }

    private class RecordQRCheckOut extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(ScanTicket.this)
                .title("Exit")
                .content("Removing Guest...")
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();

        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            builder.dismiss();
            if (result != null) {
                try {
                    if (result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultText.equals("OK") && resultContent.equals("success")) {

                            recordCheckIn(qr_token);

                        } else {
                            new MaterialDialog.Builder(ScanTicket.this)
                                    .title("Notice")
                                    .content(resultText)
                                    .positiveText("OK")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
                                            //finish();
                                        }
                                    })
                                    .show();
                            Log.d(TAG, "Error: " + result);
                        }
                    } else {
                        new MaterialDialog.Builder(ScanTicket.this)
                                .title("Notice")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void getAllData() {
        if (visitorResult == null || providerResult == null || incidentsResult == null || houseResult == null || premiseResidentResult == null || genderResult == null) {
            Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
        } else {
            try {
                JSONObject visitorObject = new JSONObject(visitorResult);
                JSONObject providerObject = new JSONObject(providerResult);
                JSONObject incidentsObject = new JSONObject(incidentsResult);
                JSONObject housesObject = new JSONObject(houseResult);
                JSONObject premiseResidentObject = new JSONObject(premiseResidentResult);
                JSONObject genderObject = new JSONObject(genderResult);
                JSONObject idTypesObject = new JSONObject(idTypesResult);

                SQLiteDatabase db = handler.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_VISITOR_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_INCIDENT_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_SERVICE_PROVIDERS_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_HOUSES);
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_PREMISE_RESIDENTS);
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_GENDER_TYPES);
                db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_ID_TYPES);

                db.execSQL(handler.CREATE_TABLE_INCIDENT_TYPES);
                db.execSQL(handler.CREATE_TABLE_VISITOR_TYPES);
                db.execSQL(handler.CREATE_TABLE_SERVICE_PROVIDERS_TYPES);
                db.execSQL(handler.CREATE_TABLE_HOUSES);
                db.execSQL(handler.CREATE_PREMISE_RESIDENTS_TABLE);
                db.execSQL(handler.CREATE_GENDER_TYPES_TABLE);
                db.execSQL(handler.CREATE_ID_TYPES_TABLE);

                /*db.execSQL(handler.CREATE_TABLE_DRIVE_IN);
                db.execSQL(handler.CREATE_TABLE_SERVICE_PROVIDERS);
                db.execSQL(handler.CREATE_TABLE_RESIDENTS);
                db.execSQL(handler.CREATE_TABLE_INCIDENTS);*/

                //Gender types
                if (genderObject.getInt("result_code") == 0 && genderObject.getString("result_text").equals("OK")) {
                    JSONArray genderArray = genderObject.getJSONArray("result_content");
                    for (int i = 0; i < genderArray.length(); i++) {
                        JSONObject genderType = genderArray.getJSONObject(i);
                        Log.d(TAG, "getAllData: Gender Types" + genderType);
                        handler.insertGenderTypes(genderType.getString("id"), genderType.getString("description"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve gender types", Toast.LENGTH_SHORT).show();
                }

//                ID Types

                if (idTypesObject.getInt("result_code") == 0 && idTypesObject.getString("result_text").equals("OK")) {
                    JSONArray idTypesObjectJSONArray = idTypesObject.getJSONArray("result_content");
                    for (int i = 0; i < idTypesObjectJSONArray.length(); i++) {
                        JSONObject idType = idTypesObjectJSONArray.getJSONObject(i);
                        Log.d(TAG, "getAllData: ID Types" + idType);
                        handler.insertIDTypes(idType.getString("id"), idType.getString("description"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve ID types", Toast.LENGTH_SHORT).show();
                }
                //Visitor types
                if (visitorObject.getInt("result_code") == 0 && visitorObject.getString("result_text").equals("OK")) {
                    JSONArray visitorArray = visitorObject.getJSONArray("result_content");
                    for (int i = 0; i < visitorArray.length(); i++) {
                        JSONObject visitorType = visitorArray.getJSONObject(i);
                        Log.d(TAG, "getAllData: visitorType" + visitorType);
                        handler.insertVisitorType(visitorType.getString("id"), visitorType.getString("name"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve visitor types", Toast.LENGTH_SHORT).show();
                }
                //Provider Types
                if (providerObject.getInt("result_code") == 0 && providerObject.getString("result_text").equals("OK")) {
                    JSONArray providerArray = providerObject.getJSONArray("result_content");
                    for (int i = 0; i < providerArray.length(); i++) {
                        JSONObject provider = providerArray.getJSONObject(i);
                        handler.insertServiceProviderType(provider.getString("id"), provider.getString("name"), provider.getString("Description"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve service providers", Toast.LENGTH_SHORT).show();
                }
                //INCIDENTS
                if (incidentsObject.getInt("result_code") == 0 && incidentsObject.getString("result_text").equals("OK")) {
                    JSONArray incidentsArray = incidentsObject.getJSONArray("result_content");
                    for (int i = 0; i < incidentsArray.length(); i++) {
                        JSONObject incident = incidentsArray.getJSONObject(i);
                        handler.insertIncidentTypes(incident.getString("id"), incident.getString("description"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve incident types", Toast.LENGTH_SHORT).show();
                }

                //Houses
                if (housesObject.getInt("result_code") == 0 && housesObject.getString("result_text").equals("OK")) {
                    JSONArray housesArray = housesObject.getJSONArray("result_content");
                    for (int i = 0; i < housesArray.length(); i++) {
                        JSONObject house = housesArray.getJSONObject(i);
                        handler.insertHouse(house.getString("house_id"), house.getString("house_description"), house.getString("block_description"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve houses", Toast.LENGTH_SHORT).show();
                }

                if (premiseResidentObject.getInt("result_code") == 0 && premiseResidentObject.getString("result_text").equals("OK")) {
                    JSONArray residentsArray = premiseResidentObject.getJSONArray("result_content");
                    for (int i = 0; i < residentsArray.length(); i++) {
                        JSONObject resident = residentsArray.getJSONObject(i);
                        int length = 0;
                        if (resident.getString("length") != "null") {
                            length = Integer.valueOf(resident.getString("length"));
                        }
                        String fingerPrint = resident.get("fingerprint") == null ? null : resident.getString("fingerprint");
                        if (fingerPrint == "0")
                            fingerPrint = null;
                        fingerPrint = fingerPrint.replaceAll("\\n", "");
                        fingerPrint = fingerPrint.replace("\\r", "");
                        handler.insertPremiseVisitor(resident.getString("id"), resident.getString("id_number"), resident.getString("firstname"), resident.getString("lastname"), fingerPrint, length, resident.getString("house_id"), resident.getString("host_id"));
                    }
                } else {
                    Toast.makeText(ScanTicket.this, "Couldn't retrieve premise residents", Toast.LENGTH_SHORT).show();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
