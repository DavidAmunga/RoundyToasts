package miles.identigate.soja;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.documentreader.api.enums.eVisualFieldType;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import miles.identigate.soja.Adapters.SimpleListAdapter;
import miles.identigate.soja.Adapters.TypeAdapter;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.UserInterface.RecordDriveIn;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.UserInterface.SplashScreen;

public class SmsCheckInActivity extends AppCompatActivity {

    private static final String TAG = "SmsCheckInActivity";

    Button btnRecord, btnConfirm;
    LinearLayout lin_walk_confirm, lin_drive_confirm, lin_checkin, lin_spinner;
    EditText edtPhoneNo, edtHost, edtCode, edtPeopleNo, edtCarNo;
    Preferences preferences;
    Spinner spinnerTypeOfVisit, spinnerVisitType, spinnerHouse;  // spinnerTypeVisit is the Type of Visitor
    ArrayList<String> visitTypes = new ArrayList<>();
    ArrayList<TypeObject> houses;
    ArrayList<TypeObject> visitorTypes;
    DatabaseHandler handler;
    TypeObject selectedHost, selectedType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_check_in);
        getSupportActionBar().setTitle("SMS Check In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = new Preferences(this);
        handler = new DatabaseHandler(this);


        spinnerTypeOfVisit = findViewById(R.id.spinnerTypeOfVisit);
        spinnerVisitType = findViewById(R.id.spinnerVisitType);
        spinnerHouse = findViewById(R.id.spinnerHouse);
        btnRecord = findViewById(R.id.btnRecord);
        btnConfirm = findViewById(R.id.btnConfirm);
        lin_walk_confirm = findViewById(R.id.lin_walk_confirm);
        lin_drive_confirm = findViewById(R.id.lin_drive_confirm);
        lin_checkin = findViewById(R.id.lin_checkin);
        lin_spinner = findViewById(R.id.lin_spinner);
        edtPhoneNo = findViewById(R.id.edtPhoneNo);
        edtHost = findViewById(R.id.edtHost);
        edtCode = findViewById(R.id.edtCode);
        edtPeopleNo = findViewById(R.id.edtPeopleNo);
        edtCarNo = findViewById(R.id.edtCarNo);

        visitTypes.add("Drive In");
        visitTypes.add("Walk In");

        SimpleListAdapter simpleListAdapter = new SimpleListAdapter(this, visitTypes);

        spinnerTypeOfVisit.setAdapter(simpleListAdapter);


        spinnerTypeOfVisit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectVisitType = parent.getItemAtPosition(position).toString();

                Log.d(TAG, "onItemSelected: " + selectVisitType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendConfirmationCode();
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Start Record");
                if (spinnerTypeOfVisit.getSelectedItem().toString().equals("Walk In")) {
                    Log.d(TAG, "onClick: Walk");

                    recordSMSCheckInWalk();
                }
                if (spinnerTypeOfVisit.getSelectedItem().toString().equals("Drive In")) {
                    Log.d(TAG, "onClick: Drive");

                    recordSMSCheckInDrive();
                }


            }
        });


//        HOUSE Select
        houses = handler.getTypes("houses");
        TypeAdapter adapter = new TypeAdapter(this, R.layout.tv, houses);
        spinnerHouse.setAdapter(adapter);
        spinnerHouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedHost = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

//        VISIT TYPE SELECT
        visitorTypes = handler.getTypes("visitors");
        TypeAdapter visitorsAdapter = new TypeAdapter(this, R.layout.tv, visitorTypes);
        spinnerVisitType.setAdapter(visitorsAdapter);
        spinnerVisitType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedType = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    //    Send Confirmation Code
    public void sendConfirmationCode() {
        String urlParameters = null;
        String phoneNo = edtPhoneNo.getText().toString();
        if (!phoneNo.equals("")) {
            try {
                urlParameters =
                        "phone=" + URLEncoder.encode(phoneNo, "UTF-8");

                new SMSCheckInAsync().execute(preferences.getBaseURL() + "send_code", urlParameters);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }


    //    Record SMS Check IN
    public void recordSMSCheckInWalk() {
        Log.d(TAG, "recordSMSCheckInWalk: Start");
        String urlParameters = null;
        String phoneNo = edtPhoneNo.getText().toString();
        if (!phoneNo.equals("")) {
            try {
                urlParameters =

                        "scan_id_type=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                                "&company=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&scan_id_type=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                "&code=" + URLEncoder.encode(edtCode.getText().toString(), "UTF-8") +
                                "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8") +
                                "&houseID=" + URLEncoder.encode(selectedHost.getId(), "UTF-8") +
                                "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");
                new SMSRecordAsync().execute(preferences.getBaseURL() + "record_visit", urlParameters);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    public void recordSMSCheckInDrive() {
        String urlParameters = null;
        String phoneNo = edtPhoneNo.getText().toString();
        String carNo = edtCarNo.getText().toString();

        Log.d(TAG, "recordSMSCheckInDrive: Start");


        if (!phoneNo.equals("")) {
            try {
                urlParameters =
                        "company=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&scan_id_type=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&visitType=" + URLEncoder.encode("drive-in", "UTF-8") +
                                "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                "&code=" + URLEncoder.encode(edtCode.getText().toString(), "UTF-8") +
                                "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8") +
                                "&houseID=" + URLEncoder.encode(selectedHost.getId(), "UTF-8") +
                                "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                                "&vehicleRegNO=" + URLEncoder.encode(carNo, "UTF-8");

                new SMSRecordAsync().execute(preferences.getBaseURL() + "record_visit", urlParameters);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    private class SMSRecordAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(SmsCheckInActivity.this)
                .title(spinnerTypeOfVisit.getSelectedItem().toString())
                .content("Recording...")
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();

            btnRecord.setEnabled(false);
            btnRecord.setText("Please wait...");
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Please wait...");

            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            btnRecord.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));



        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            builder.dismiss();


            if (result != null) {
                if (result.contains("result_code")) {
                    try {
                        Object json = new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject) {
                            recordResultHandler(result);

                            btnRecord.setEnabled(true);
                            btnRecord.setText("RECORD");
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("SEND CONFIRMATION CODE");

                            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            btnRecord.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        } else {
                            Toast.makeText(SmsCheckInActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            //TODO remove this.Temporary workaround
//                            recordOffline();
//                            parseResult();
                            btnRecord.setEnabled(true);
                            btnRecord.setText("RECORD");
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("SEND CONFIRMATION CODE");

                            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            btnRecord.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
//                    recordOffline();
                }

            }
        }

    }


    private class SMSCheckInAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Please wait...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btnConfirm.setBackgroundColor(getColor(R.color.colorPrimaryDark));
            } else {
                btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }

        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                if (result.contains("result_code")) {
                    try {
                        Object json = new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject) {
                            confirmResultHandler(result);
                        } else {
                            Toast.makeText(SmsCheckInActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            //TODO remove this.Temporary workaround
//                            recordOffline();
//                            parseResult();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
//                    recordOffline();
                }

            }
        }

    }

    //    Confirm Code
    private void confirmResultHandler(String result) throws JSONException {
        //Log.d("WALKIN", result);
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");
        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
            Toast.makeText(this, "Enter Verification Code", Toast.LENGTH_SHORT).show();

//            Set Visibility of Selected Item
            lin_drive_confirm.setVisibility(spinnerTypeOfVisit.getSelectedItem().toString().equals("Drive In") ? View.VISIBLE : View.GONE);
            lin_walk_confirm.setVisibility(spinnerTypeOfVisit.getSelectedItem().toString().equals("Walk In") ? View.VISIBLE : View.GONE);
            lin_spinner.setVisibility(View.VISIBLE);
            btnRecord.setVisibility(View.VISIBLE);
//            Restore Button State
            btnConfirm.setEnabled(true);
            btnConfirm.setText("SEND CONFIRMATION CODE");
            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        } else {
            Toast.makeText(SmsCheckInActivity.this, "Error", Toast.LENGTH_SHORT).show();

        }
    }

    //    Handle Record
    private void recordResultHandler(String result) throws JSONException {
        Log.d("Record Result", result);
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");


        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
            Toast.makeText(this, "Success! Visitor Checked In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Dashboard.class));

        } else {
            if (resultText.contains("still in")) {

                new MaterialDialog.Builder(this)
                        .title("Soja")
                        .content(resultText)
                        .positiveText("OK")
                        .negativeText("Check out")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();

                                String urlParameters = null;
                                try {
                                    urlParameters = "idNumber=" + edtPhoneNo.getText().toString().trim() +
                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                            "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");
                                    new ExitAsync().execute(preferences.getBaseURL() + "record-visitor-exit", urlParameters);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
            } else {
                new MaterialDialog.Builder(this)
                        .title("Soja")
                        .content(result)
                        .positiveText("OK")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                                //finish();
                            }
                        })
                        .show();
            }

        }


    }

    private class ExitAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(SmsCheckInActivity.this)
                .title("Exit")
                .content("Removing visitor...")
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
                try {
                    if (result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            recordInternet();
                        } else {
                            new MaterialDialog.Builder(SmsCheckInActivity.this)
                                    .title("ERROR")
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
                        }
                    } else {
                        new MaterialDialog.Builder(SmsCheckInActivity.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                new MaterialDialog.Builder(SmsCheckInActivity.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }

    public void recordInternet() {
        if (spinnerTypeOfVisit.getSelectedItem().toString().equals("Walk In")) {
            Log.d(TAG, "onClick: Walk");

            recordSMSCheckInWalk();
        }
        if (spinnerTypeOfVisit.getSelectedItem().toString().equals("Drive In")) {
            Log.d(TAG, "onClick: Drive");

            recordSMSCheckInDrive();
        }

    }



}

