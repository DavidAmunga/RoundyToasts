package miles.identigate.soja;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import miles.identigate.soja.adapters.SimpleListAdapter;
import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.TypeObject;

public class SmsCheckInActivity extends AppCompatActivity {

    private static final String TAG = "SmsCheckInActivity";

    Button btnRecord, btnConfirm;
    LinearLayout lin_walk_confirm, lin_drive_confirm, lin_checkin, lin_spinner, lin_host;
    EditText edtPhoneNo, edtHost, edtCode, edtPeopleNo, edtCarNo, companyNameEditTexxt;
    Preferences preferences;
    Spinner spinnerTypeOfVisit, spinnerVisitType;
    ArrayList<String> visitTypes = new ArrayList<>();
    ArrayList<TypeObject> houses, hosts, visitorTypes;
    DatabaseHandler handler;
    TypeObject selectedDestination, selectedType, selectedHost;
    TextView spinnerDestination, spinnerHost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_check_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("SMS CheckIn");

        handler = new DatabaseHandler(this);


        spinnerTypeOfVisit = findViewById(R.id.spinnerTypeOfVisit);
        spinnerVisitType = findViewById(R.id.spinnerVisitType);
        spinnerHost = findViewById(R.id.spinnerHost);
        btnRecord = findViewById(R.id.btnRecord);
        btnConfirm = findViewById(R.id.btnConfirm);
        lin_walk_confirm = findViewById(R.id.lin_walk_confirm);
        lin_drive_confirm = findViewById(R.id.lin_drive_confirm);
        lin_checkin = findViewById(R.id.lin_checkin);
        lin_spinner = findViewById(R.id.lin_spinner);
        lin_host = findViewById(R.id.lin_host);
        edtPhoneNo = findViewById(R.id.edtPhoneNo);
        companyNameEditTexxt = findViewById(R.id.companyNameEdittext);
        edtHost = findViewById(R.id.edtHost);
        edtCode = findViewById(R.id.edtCode);
        edtPeopleNo = findViewById(R.id.edtPeopleNo);
        edtCarNo = findViewById(R.id.edtCarNo);
        spinnerDestination = findViewById(R.id.spinnerDestination);


//        ADD VISIT TYPES
        visitTypes.add("Walk In");
        visitTypes.add("Drive In");

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
                if (selectedDestination != null) {
                    Log.d(TAG, "onClick: Start Record");
                    if (spinnerTypeOfVisit.getSelectedItem().toString().equals("Walk In")) {
                        Log.d(TAG, "onClick: Walk");

                        recordSMSCheckInWalk();
                    }
                    if (spinnerTypeOfVisit.getSelectedItem().toString().equals("Drive In")) {
                        Log.d(TAG, "onClick: Drive");

                        recordSMSCheckInDrive();
                    }
                } else if (selectedHost != null) {
                    Toast.makeText(SmsCheckInActivity.this, "Please enter destination", Toast.LENGTH_SHORT).show();
                }


            }
        });


//        HOUSE Select


        houses = handler.getTypes("houses", null);


        spinnerDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                new SimpleSearchDialogCompat(SmsCheckInActivity.this,
                        "Search for Destination...", "What destination is the visitor heading...?", null, houses, new SearchResultListener<TypeObject>() {
                    @Override
                    public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                        TypeObject object = o;
                        selectedDestination = object;

                        Log.d(TAG, "Selected Destination: " + selectedDestination.getId());

                        hosts = handler.getTypes("hosts", selectedDestination.getId());


                        lin_host.setVisibility(preferences.isSelectHostsEnabled() && hosts.size() > 0 ? View.VISIBLE : View.GONE);

                        if (hosts.size() == 0) {
                            selectedHost = null;
                        }

                        Log.d(TAG, "Host ID " + selectedDestination.getId());
                        Log.d(TAG, "Hosts: " + hosts);


//                        Toast.makeText(SmsCheckInActivity.this, o.getName(), Toast.LENGTH_SHORT).show();
                        spinnerDestination.setText(o.getName());
                        baseSearchDialogCompat.dismiss();

                    }
                }).show();
            }
        });

//        HOST Select
//        if(selectedDestination!=null){

        Log.d(TAG, "All Hosts " + handler.getTypes("hosts", null));

        spinnerHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                if (selectedDestination == null) {
                    Toast.makeText(SmsCheckInActivity.this, "Select Destination First", Toast.LENGTH_SHORT).show();
                } else {
                    new SimpleSearchDialogCompat(SmsCheckInActivity.this,
                            "Search for Host...", "Who is the visitor seeing in " + selectedDestination.getName(), null, hosts, new SearchResultListener<TypeObject>() {
                        @Override
                        public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                            TypeObject object = o;
                            selectedHost = object;

//                            Toast.makeText(RecordDriveIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                            spinnerHost.setText(o.getName());
                            baseSearchDialogCompat.dismiss();

                        }
                    }).show();
                }
            }
        });

//
//        spinnerHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                TypeObject object = (TypeObject) parent.getSelectedItem();
//                selectedHost = object;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });


//        }


//        VISIT TYPE SELECT
        visitorTypes = handler.getTypes("visitors", null);
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

    //    Send Verification Code
    public void sendConfirmationCode() {
        String urlParameters = null;
        String phoneNo = edtPhoneNo.getText().toString();
        if (!phoneNo.equals("")) {
            try {
                urlParameters =
                        "phone=" + URLEncoder.encode(phoneNo, "UTF-8")+
                "&organisationID=" + URLEncoder.encode(preferences.getOrganizationId(), "UTF-8")
                ;

                Log.d(TAG, "sendConfirmationCode: "+urlParameters);


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
                                (preferences.isCompanyNameEnabled() && !companyNameEditTexxt.getText().toString().equals("") ?
                                        ("&company=" + URLEncoder.encode(companyNameEditTexxt.getText().toString(), "UTF-8")) : "") +
                                "&scan_id_type=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                "&code=" + URLEncoder.encode(edtCode.getText().toString(), "UTF-8") +
                                (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(selectedHost.getHostId())) : "") +
                                "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8") +
                                "&houseID=" + URLEncoder.encode(selectedDestination.getId(), "UTF-8") +
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
        int peopleNo = Integer.parseInt(edtPeopleNo.getText().toString());


        Log.d(TAG, "recordSMSCheckInDrive: Start");


        if (!phoneNo.equals("")) {
            try {
                urlParameters =
                        "company=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&scan_id_type=" + URLEncoder.encode("PHONE", "UTF-8") +
                                "&visitType=" + URLEncoder.encode("drive-in", "UTF-8") +
                                "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                "&paxinvehicle=" + (peopleNo >= 1 ? peopleNo : 1) +
                                (preferences.isCompanyNameEnabled() && !companyNameEditTexxt.getText().toString().equals("") ?
                                        ("&company=" + URLEncoder.encode(companyNameEditTexxt.getText().toString(), "UTF-8")) : "") +
                                (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(selectedHost.getHostId())) : "") +
                                "&code=" + URLEncoder.encode(edtCode.getText().toString(), "UTF-8") +
                                "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8") +
                                "&houseID=" + URLEncoder.encode(selectedDestination.getId(), "UTF-8") +
                                "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                                "&vehicleRegNO=" + URLEncoder.encode(carNo, "UTF-8");

                Log.d(TAG, "URL Parameters " + urlParameters);

                new SMSRecordAsync().execute(preferences.getBaseURL() + "record_visit", urlParameters);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    private class SMSRecordAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(SmsCheckInActivity.this)
                .title(spinnerTypeOfVisit.getSelectedItem().toString())
                .content("Checking In...")
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
                            btnConfirm.setText("SEND VERIFICATION CODE");

                            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            btnRecord.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        } else {
                            Toast.makeText(SmsCheckInActivity.this, "Error" + result, Toast.LENGTH_SHORT).show();
                            //TODO remove this.Temporary workaround
//                            recordOffline();
//                            parseResult();
                            btnRecord.setEnabled(true);
                            btnRecord.setText("RECORD");
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("SEND VERIFICATION CODE");

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
                            Log.d(TAG, "onPostExecute: " + result);
                        } else {
                            Toast.makeText(SmsCheckInActivity.this, "Ooops! Please try again!", Toast.LENGTH_SHORT).show();

                            btnRecord.setEnabled(true);
                            btnRecord.setText("RECORD");
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("SEND VERIFICATION CODE");

//                            Toast.makeText(SmsCheckInActivity.this, resultText, Toast.LENGTH_SHORT).show();
                            //TODO remove this.Temporary workaround
//                            recordOffline();
//                            parseResult();

                            Log.d(TAG, "onPostExecute: " + result);

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
            edtCode.requestFocus();
//            Restore Button State
            btnConfirm.setEnabled(true);
            btnConfirm.setText("SEND CONFIRMATION CODE");
            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            Log.d(TAG, "confirmResultHandler: " + result);
        } else {
            Toast.makeText(SmsCheckInActivity.this, "Error" + resultText, Toast.LENGTH_SHORT).show();

        }
    }

    private void parseResult() {
        if (preferences.canPrint()) {
            Intent intent = new Intent(getApplicationContext(), SlipActivity.class);
            intent.putExtra("title", "SMS");
            intent.putExtra("house", selectedDestination.getName());

            if (!edtCarNo.getText().toString().equals("")) {
                intent.putExtra("vehicleNo", edtCarNo.getText().toString());
            }
            if (preferences.isSelectHostsEnabled() && selectedHost != null) {
                intent.putExtra("host", selectedHost.getName());
            }
            if (selectedType.equals("Drive In")) {
                intent.putExtra("checkInType", "drive");
            } else {
                intent.putExtra("checkInType", "walk");
            }
            if (!edtPeopleNo.getText().toString().equals("")) {
                intent.putExtra("peopleNo", edtPeopleNo.getText().toString());
            }
            intent.putExtra("phoneNo", edtPhoneNo.getText().toString());
            intent.putExtra("checkInMode", "SMS");

            startActivity(intent);
        }
//        else{
//            new MaterialDialog.Builder(SmsCheckInActivity.this)
//                    .title("SUCCESS")
//                    .content("Visitor recorded successfully.")
//                    .positiveText("OK")
//                    .callback(new MaterialDialog.ButtonCallback() {
//                        @Override
//                        public void onPositive(MaterialDialog dialog) {
//                            dialog.dismiss();
//                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
//                            finish();
//                        }
//                    })
//                    .show();
//        }
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
//            parseResult();

            if (preferences.canPrint()) {
                parseResult();
            } else {
                startActivity(new Intent(this, Dashboard.class));
            }

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
                Log.d(TAG, "recordResultHandler: "+result);

            } else {
                new MaterialDialog.Builder(this)
                        .title("Soja")
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
                Log.d(TAG, "recordResultHandler: "+result);
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
                            Log.d(TAG, "onPostExecute: "+result.toString());
                            Toast.makeText(SmsCheckInActivity.this, result, Toast.LENGTH_SHORT).show();
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

