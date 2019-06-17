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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import io.paperdb.Paper;
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
    @BindView(R.id.scan_icon)
    ImageView scanIcon;
    @BindView(R.id.scan_id_text)
    TextViewBold scanIdText;


    String qr_token;
    ProgressDialog progressDialog;
    @BindView(R.id.btnList)
    Button btnList;
    @BindView(R.id.root_layout)
    CoordinatorLayout rootLayout;
    @BindView(R.id.rel1)
    RelativeLayout rel1;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.frame1)
    FrameLayout frame1;


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
    DatabaseReference mDatabase;
    private boolean offline = false;


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


        Paper.init(this);


        scanIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanTicket.this).setCaptureActivity(ZxingHelperActivity.class).addExtra("PROMPT_MESSAGE", "Place QR Ticket Here to scan it").initiateScan();
            }
        });

        progressDialog = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference(Common.TICKETS);
        mDatabase.keepSynced(true);

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
                startActivity(new Intent(ScanTicket.this, TicketList.class));

            }
        });


    }


    private void addTickets() {

        Log.d(TAG, "addTickets: Start");
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

                Log.d(TAG, "onActivityResult: Here " + qr_token);

                String[] tokenString = qr_token.split("-");

                String premiseZoneID = tokenString[1];
                String qr = tokenString[0];

                if (validatePremise(premiseZoneID)) {

                    if (new CheckConnection().check(this)) {

                        checkInFB(qr_token);
                    } else {
//                    showSuccess("offline");

                        checkInAsync(qr_token);
                    }


                } else {
                    Toast.makeText(this, "This Ticket is not valid for this queue", Toast.LENGTH_SHORT).show();
                }


            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean validatePremise(String premiseZoneID) {

        if (premiseZoneID.equals(preferences.getCurrentUser().getPremiseZoneId())) {
            return true;
        }

        return false;
    }


    private void checkInAsync(String token) {
        Log.d(TAG, "checkInAsync: Start");


        mDatabase.orderByKey().equalTo(token).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.hasChild("ticketId")) {
//                      Add Entry Time
                        if (postSnapshot.hasChild("entryTime")) {
                            Snackbar snackbar = Snackbar.make(frame1,
                                    "Already Checked In"
                                    , Snackbar.LENGTH_LONG);


                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();
//                            Toast.makeText(ScanTicket.this, "Already Checked In", Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(ScanTicket.this, "Currently Offline. Will Check In later", Toast.LENGTH_SHORT).show();

//                        New Checked In User
                            Map<String, Object> updateInfo = new HashMap<>();
                            updateInfo.put("entryTime", Constants.getCurrentTimeStamp());

                            FirebaseDatabase.getInstance().getReference(Common.TICKETS).child(token).updateChildren(updateInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        String[] tokenString = token.split("-");

                                        String qr = tokenString[0];


                                        recordCheckInAsync(qr);
//                                    Toast.makeText(ScanTicket.this, "Checked In", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        progressDialog.dismiss();

                        Toast.makeText(ScanTicket.this, "Ticket Not Available", Toast.LENGTH_SHORT).show();
                    }

                    if (dataSnapshot.getChildrenCount() == 0) {
                        Toast.makeText(ScanTicket.this, "Incorrect QR", Toast.LENGTH_SHORT).show();

                    }
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void checkInFB(String token) {
        Log.d(TAG, "checkInFB: " + token);
        mDatabase.orderByKey().equalTo(token).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    if (postSnapshot.hasChild("ticketId")) {
//                      Add Entry Time
                        if (postSnapshot.hasChild("entryTime")) {
                            progressDialog.dismiss();

                            Snackbar snackbar = Snackbar.make(frame1,
                                    "Already Checked In"
                                    , Snackbar.LENGTH_LONG);


                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

//                            Toast.makeText(ScanTicket.this, "Already Checked In", Toast.LENGTH_SHORT).show();

                        } else {
//                        New Checked In User
                            Map<String, Object> updateInfo = new HashMap<>();
                            updateInfo.put("entryTime", Constants.getCurrentTimeStamp());

                            FirebaseDatabase.getInstance().getReference(Common.TICKETS).child(token).updateChildren(updateInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();


                                        String[] tokenString = token.split("-");

                                        String qr = tokenString[0];


                                        recordCheckIn(qr);
//                                    Toast.makeText(ScanTicket.this, "Checked In", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        progressDialog.dismiss();

                        Toast.makeText(ScanTicket.this, "Ticket Not Available", Toast.LENGTH_SHORT).show();
                    }

                    if (dataSnapshot.getChildrenCount() == 0) {
                        Toast.makeText(ScanTicket.this, "Incorrect QR", Toast.LENGTH_SHORT).show();

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
        getMenuInflater().inflate(R.menu.menu_scan_ticket, menu);
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
        } else if (id == R.id.ticket_list) {
            addTickets();
            startActivity(new Intent(getApplicationContext(), TicketList.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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


    public void recordCheckInAsync(String qr_token) {
        String urlParameters = null;
        try {
            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getCurrentUser().getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getCurrentUser().getPremiseZoneId(), "UTF-8") +
                    "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                    "&qr=" + URLEncoder.encode(qr_token, "UTF-8");

            Log.d(TAG, "onActivityResult: " + preferences.getCurrentUser().getPremiseZoneId());


            new RecordQRCheckinAsync().execute(preferences.getBaseURL().replace("visits", "residents") + "qr_checkin", urlParameters);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class RecordQRCheckin extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {

            if (dialog != null && !dialog.isShowing() && !offline)
                dialog.show();
        }

        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {

            Log.d(TAG, "onPostExecute: Result" + result.toString());
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

                            showSuccess("online");


                            Log.d(TAG, "onPostExecute: SUCCESS");


                        } else {

                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }

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


    private class RecordQRCheckinAsync extends AsyncTask<String, Void, String> {


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

                            Snackbar snackbar = Snackbar.make(frame1,
                                    "Ticket Checked In "
                                    , Snackbar.LENGTH_LONG);

                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                            Toast.makeText(ScanTicket.this, "Offline Ticket Checked In", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onPostExecute: SUCCESS");


                        } else {

                            Snackbar snackbar = Snackbar.make(frame1,
                                    "Ticket already Checked in "
                                    , Snackbar.LENGTH_LONG);


                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                            Toast.makeText(ScanTicket.this, "Ticket already Checked In", Toast.LENGTH_SHORT).show();

//                            new MaterialDialog.Builder(ScanTicket.this)
//                                    .title("Notice")
//                                    .content(result_text)
//                                    .positiveText("Ok")
////                                    .negativeText("Check Out")
//                                    .callback(new MaterialDialog.ButtonCallback() {
//                                        @Override
//                                        public void onPositive(MaterialDialog dialog) {
//                                            dialog.dismiss();
////                                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
////                                            finish();
//                                        }
//
////                                        @Override
////                                        public void onNegative(MaterialDialog dialog) {
////                                            dialog.dismiss();
////
////
////                                            recordCheckOut();
////
////                                        }
//                                    })
//                                    .show();
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


    private void showSuccess(String option) {
        Log.d(TAG, "showSuccess: Yes");

        if (dialog.isShowing() && dialog != null) {
            dialog.dismiss();
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (option.equals("offline")) {
            Toast.makeText(this, "Currently Offline. Will Check In later", Toast.LENGTH_SHORT).show();

//            snackbar = Snackbar.make(frame1,
//                    "Ticket Checked In Offline "
//                    , Snackbar.LENGTH_LONG);
        }

        Snackbar snackbar = Snackbar.make(frame1,
                "Ticket Checked In "
                , Snackbar.LENGTH_LONG);


        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.white));
        snackbar.show();

    }


}
