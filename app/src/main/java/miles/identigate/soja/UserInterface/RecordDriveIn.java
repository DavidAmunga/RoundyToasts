package miles.identigate.soja.UserInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.documentreader.api.enums.eVisualFieldType;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import miles.identigate.soja.Adapters.TypeAdapter;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;
import miles.identigate.soja.SlipActivity;

public class RecordDriveIn extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    Spinner visitor_type;
    DatabaseHandler handler;
    Button record;
    String vehicleNo;
    EditText vehicleRegNo;
    EditText numberOfPeople;
    TypeObject selectedType, selectedDestination, selectedHost;
    ArrayList<TypeObject> houses, visitorTypes, hosts;
    Preferences preferences;
    MaterialDialog progressDialog;
    MaterialDialog dialog;

    LinearLayout phoneNumberLayout, hostLayout, companyNameLayout;
    EditText phoneNumberEdittext, companyNameEdittext;

    TextView spinnerDestination, spinnerHost;


    String firstName, lastName, idNumber;
    String result_slip = "";
    int visit_id = 0;

    private static final String TAG = "RecordDriveIn";

    private IntentFilter receiveFilter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.RECORDED_VISITOR)){
                finish();
            }else if(action.equals(Constants.LOGOUT_BROADCAST)){
                finish();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_drive_in);
        if (Constants.documentReaderResults == null)
            finish();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Record Drive In");

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);

        handler=new DatabaseHandler(this);
        preferences=new Preferences(this);
        vehicleRegNo = findViewById(R.id.car_number);
        vehicleRegNo.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        visitor_type = findViewById(R.id.visitor_type);
        record = findViewById(R.id.record);
        spinnerDestination = findViewById(R.id.spinnerDestination);
        spinnerHost = findViewById(R.id.spinnerHost);
        numberOfPeople = findViewById(R.id.numberOfPeople);
        phoneNumberLayout = findViewById(R.id.phoneNumberLayout);
        phoneNumberEdittext = findViewById(R.id.phoneNumberEdittext);
        companyNameLayout = findViewById(R.id.companyNameLayout);
        hostLayout = findViewById(R.id.hostLayout);
        companyNameEdittext = findViewById(R.id.companyNameEdittext);

        phoneNumberLayout.setVisibility(preferences.isPhoneNumberEnabled()?View.VISIBLE:View.GONE);
        companyNameLayout.setVisibility(preferences.isCompanyNameEnabled()?View.VISIBLE:View.GONE);



        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckConnection.check(RecordDriveIn.this)){
                    if(vehicleRegNo.getText().toString().equals(null)||vehicleRegNo.getText().toString().equals("")||numberOfPeople.getText().toString().equals(null)|| numberOfPeople.getText().toString().equals("")) {
                        Snackbar.make(v,"All fields are required.",Snackbar.LENGTH_SHORT).show();
                    }else {
                        recordInternt();
                    }
                }else {
                    if(vehicleRegNo.getText().toString().equals(null)||vehicleRegNo.getText().toString().equals("")||numberOfPeople.getText().toString().equals(null)|| numberOfPeople.getText().toString().equals("")) {
                        Snackbar.make(v,"All fields are required.",Snackbar.LENGTH_SHORT).show();
                    }else {
                        recordOffline();
                    }
                }
            }
        });


//        VISITOR TYPES
        visitorTypes = handler.getTypes("visitors", null);
        TypeAdapter adapter =new TypeAdapter(RecordDriveIn.this,R.layout.tv,visitorTypes);
        visitor_type.setAdapter(adapter);
        visitor_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedType = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//      DESTINATION / HOUSE

        houses = handler.getTypes("houses", null);

        spinnerDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                new SimpleSearchDialogCompat(RecordDriveIn.this,
                        "Search for Destination...", "What destination is the visitor heading...?", null, houses, new SearchResultListener<TypeObject>() {
                    @Override
                    public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                        TypeObject object = o;
                        selectedDestination = object;

                        Log.d(TAG, "Selected Destination: " + selectedDestination.getId());

                        hosts = handler.getTypes("hosts", selectedDestination.getId());


                        hostLayout.setVisibility(preferences.isSelectHostsEnabled() && hosts.size() > 0 ? View.VISIBLE : View.GONE);

                        if (hosts.size() == 0) {
                            selectedHost = null;
                        }

                        Log.d(TAG, "Host ID " + selectedDestination.getId());
                        Log.d(TAG, "Hosts: " + hosts);


//                        Toast.makeText(RecordDriveIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                        spinnerDestination.setText(o.getName());
                        baseSearchDialogCompat.dismiss();

                    }
                }).show();
            }
        });

//        HOST
        spinnerHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                if(selectedDestination==null){
                    Toast.makeText(RecordDriveIn.this, "Select Destination First", Toast.LENGTH_SHORT).show();
                }
                else{
                    new SimpleSearchDialogCompat(RecordDriveIn.this,
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
    }
    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, receiveFilter);
        super.onResume();
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onPause();
    }
    public void recordInternt() {
        //Insert to local and online database.

        String urlParameters = null;
        try {
            String idN="000000000";
            String scan_id_type = "ID";
            String classCode=Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
            if(classCode.equals("ID")){
                scan_id_type = "ID";
                idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");
                idNumber = idN.substring(2, idN.length()-1);
                Log.d(TAG, "ID Number: "+idNumber);
            }else if (classCode.equals("P")){
                scan_id_type = "P";
                idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                idNumber = idN;
            }
            firstName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");
            lastName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");

            String gender=Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n").contains("M")?"0":"1";
            String mrzLines =  Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);
            urlParameters =
                    "mrz=" + URLEncoder.encode(mrzLines, "UTF-8")+
                    "&phone=" + URLEncoder.encode(phoneNumberEdittext.getText().toString(), "UTF-8")+
                            (preferences.isCompanyNameEnabled() && companyNameEdittext.getText().toString().equals("")?
                                    ("&company=" + URLEncoder.encode(companyNameEdittext.getText().toString(), "UTF-8")):"") +
                    "&scan_id_type=" + URLEncoder.encode(scan_id_type, "UTF-8") +
                    "&visitType=" + URLEncoder.encode("drive-in", "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8")+
                    "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8")+
                            "&houseID=" + URLEncoder.encode(selectedDestination.getId(), "UTF-8") +
                            (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(selectedHost.getHostId())) : "") +

                            "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                    "&vehicleRegNO=" + URLEncoder.encode(vehicleRegNo.getText().toString(), "UTF-8")+
                    "&birthDate=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"), "UTF-8")+
                    "&genderID=" + URLEncoder.encode(gender, "UTF-8")+
                    "&firstName=" + URLEncoder.encode(firstName, "UTF-8")+
                    "&lastName=" + URLEncoder.encode(lastName, "UTF-8")+
                    "&idType=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8")+
                    "&nationality=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n"), "UTF-8")+
                    "&nationCode=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n"), "UTF-8");
            Log.d(TAG, "URL Param: "+urlParameters);
            new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(

            );
        }
    }

    public void recordOffline(){
        //Insert to local database
        String idN="000000000";
        String classCode=Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
        if(classCode.equals("ID")){
            idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");
        }else if (classCode.equals("P")){
            idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
        }
        DriveIn driveIn=new DriveIn();
        vehicleNo=vehicleRegNo.getText().toString();
        driveIn.setVisitorType(selectedType.getId());
        driveIn.setCarNumber(vehicleRegNo.getText().toString());
        driveIn.setImage("NULL");
        driveIn.setEntryTime(Constants.getCurrentTimeStamp());
        driveIn.setStatus("IN");
        driveIn.setRecordType("DRIVE");
        driveIn.setExitTime("NULL");
        driveIn.setHouseID(selectedDestination.getId());
        driveIn.setNationalId(idN);
        driveIn.setDob(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"));
        driveIn.setSex(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n"));
        driveIn.setName(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n"));
        driveIn.setOtherNames(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n"));
        driveIn.setIdType(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"));
        if(CheckConnection.check(this)){
            driveIn.setSynced(true);
            handler.insertDriveIn(driveIn);
            return;
        }else {
            driveIn.setSynced(false);
            handler.insertDriveIn(driveIn);
            MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
                    finish();
                }
            };
            dialog = Constants.showDialog(this, "SUCCESS","Visitor recorded successfully.","OK" ,singleButtonCallback);
            dialog.show();
        }
    }
    private class DriveinAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Constants.showProgressDialog(RecordDriveIn.this,"Drive in", "Recording...");
            progressDialog.show();
        }
        protected String  doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0],params[1]);
        }
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if(result !=null){
                try {
                    Object json=new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject){
                        resultHandler(result);
                    }else {
                        recordOffline();
                        parseResult();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                recordOffline();
            }

        }
    }
    private void parseResult(){
        if (preferences.canPrint()){
            Intent intent = new Intent(getApplicationContext(), SlipActivity.class);
            intent.putExtra("title", "RECORD DRIVE IN");
            intent.putExtra("house", selectedDestination.getName());
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("idNumber", idNumber);
            intent.putExtra("result_slip", result_slip);
            intent.putExtra("visit_id", visit_id);
            intent.putExtra("vehicleNo", vehicleRegNo.getText().toString());
            intent.putExtra("checkInType","drive");
            intent.putExtra("checkInMode","ID No");
            intent.putExtra("peopleNo",numberOfPeople.getText().toString());
            if(preferences.isSelectHostsEnabled() && selectedHost!=null){
                intent.putExtra("host",selectedHost.getName());
            }



            startActivity(intent);
        }else{
            new MaterialDialog.Builder(RecordDriveIn.this)
                    .title("SUCCESS")
                    .content("Visitor recorded successfully.")
                    .positiveText("OK")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            finish();
                        }
                    })
                    .show();
        }
    }
    private void resultHandler(String result) throws JSONException {
        JSONObject obj=new JSONObject(result);
        int resultCode=obj.getInt("result_code");
        String resultText=obj.getString("result_text");
        String resultContent=obj.getString("result_content");
        if(resultText.equals("OK")&&resultContent.equals("success")){
            result_slip = obj.getString("result_slip");
            visit_id = obj.getInt("visit_id");
            recordOffline();
            parseResult();
        }else {
            if(resultText.contains("still in")){
                MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        String urlParameters =null;
                        String idN="000000000";
                        String idNumber="";

                        String classCode=Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
                        if(classCode.equals("ID")){
                            idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");
                            idNumber=idN.substring(2,idN.length()-1);
                        }else if (classCode.equals("P")){
                            idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                            idNumber=idN;
                        }
                        try {
                            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                                    "&idNumber="+ URLEncoder.encode(idNumber,"UTF-8") +
                                    "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");


                            Log.d(TAG, "URL Param : "+urlParameters);

                            new ExitAsync().execute(preferences.getBaseURL()+"record-visitor-exit", urlParameters);

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                };
                dialog = Constants.showDialog(this, "Soja", resultText,"Check out", singleButtonCallback);
                dialog.show();
            }else {
                MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                };
                dialog = Constants.showDialog(this, "Soja", resultText,"OK", singleButtonCallback);
                dialog.show();
            }
        }
    }
    private class ExitAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Constants.showProgressDialog(RecordDriveIn.this,"Exit", "Removing visitor...");
            progressDialog.show();
        }
        protected String  doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if(result !=null){

                try {
                    if(result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        Log.d(TAG, "onPostExecute: "+result);

                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            recordInternt();
                        } else {
                            MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            };
                            dialog = Constants.showDialog(RecordDriveIn.this, "ERROR", resultText,"OK", singleButtonCallback);
                            dialog.show();
                        }
                    }else {
                        MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        };
                        dialog = Constants.showDialog(RecordDriveIn.this, "Result", "Poor internet connection","OK", singleButtonCallback);
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                };
                dialog = Constants.showDialog(RecordDriveIn.this, "Result", "Poor internet connection","OK", singleButtonCallback);
                dialog.show();
            }
        }
    }
}
