package miles.identigate.soja.UserInterface;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.documentreader.api.enums.eVisualFieldType;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

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
import miles.identigate.soja.SmsCheckInActivity;

public class RecordWalkIn extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "RecordWalkIn";
    Spinner visitor_type;
    DatabaseHandler handler;
    Button record;
    Spinner spinnerDestination, spinnerHost;
    TypeObject selectedDestination, selectedHost, selectedType;
    ArrayList<TypeObject> houses, visitorTypes, hosts;
    Preferences preferences;

    LinearLayout phoneNumberLayout, companyNameLayout, hostLayout;
    EditText phoneNumberEdittext, companyNameEdittext;

    String firstName, lastName, idNumber;
    String result_slip = "";
    int visit_id = 0;
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
        setContentView(R.layout.activity_record_walk_in);
        if (Constants.documentReaderResults == null)
            finish();
        preferences=new Preferences(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);

        handler=new DatabaseHandler(this);

        visitor_type = findViewById(R.id.visitor_type);
        record = findViewById(R.id.record);
        spinnerDestination = findViewById(R.id.spinnerDestination);

        phoneNumberLayout = findViewById(R.id.phoneNumberLayout);
        phoneNumberEdittext = findViewById(R.id.phoneNumberEdittext);
        companyNameLayout = findViewById(R.id.companyNameLayout);
        companyNameEdittext = findViewById(R.id.companyNameEdittext);
        hostLayout = findViewById(R.id.hostLayout);
        spinnerHost = findViewById(R.id.spinnerHost);

        phoneNumberLayout.setVisibility(preferences.isPhoneNumberEnabled()?View.VISIBLE:View.GONE);
        companyNameLayout.setVisibility(preferences.isCompanyNameEnabled()?View.VISIBLE:View.GONE);
        hostLayout.setVisibility(preferences.isSelectHostsEnabled()?View.VISIBLE:View.GONE);

        houses = handler.getTypes("houses", null);
        Log.d(TAG, "Houses: " + houses);
        visitorTypes = handler.getTypes("visitors", null);
        record.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if(CheckConnection.check(RecordWalkIn.this)){
                   recordInternet();
               }else{
                 recordOffline();
               }
           }
       });
        TypeAdapter adapter =new TypeAdapter(RecordWalkIn.this,R.layout.tv,visitorTypes);
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
//        DESTINATION
        TypeAdapter housesAdapter =new TypeAdapter(RecordWalkIn.this,R.layout.tv,houses);
        spinnerDestination.setAdapter(housesAdapter);
        spinnerDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object=(TypeObject)parent.getSelectedItem();
                selectedDestination = object;


                hosts = handler.getTypes("hosts", selectedDestination.getId());
                hostLayout.setVisibility(preferences.isSelectHostsEnabled() && hosts.size() > 0 ? View.VISIBLE : View.GONE);

                TypeAdapter hostsAdapter = new TypeAdapter(RecordWalkIn.this, R.layout.tv, hosts);
                spinnerHost.setAdapter(hostsAdapter);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        HOSTS
        spinnerHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedHost = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
    public void recordInternet(){
        String urlParameters = null;
        try {
            String idN="000000000";
            String scan_id_type = "ID";
            String classCode=Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
            if(classCode.equals("ID")){
                scan_id_type = "ID";
                idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");
                idNumber = idN.substring(2, idN.length()-1);
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
                    "&company=" + URLEncoder.encode(companyNameEdittext.getText().toString(), "UTF-8") +
                    "&scan_id_type=" + URLEncoder.encode(scan_id_type, "UTF-8") +
                    "&visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8")+
                    "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8")+
                            (preferences.isSelectHostsEnabled() ? ("&hostID=" + URLEncoder.encode(selectedHost.getHostId())) : "") +

                            "&houseID=" + URLEncoder.encode(selectedDestination.getId(), "UTF-8") +
                    "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8")+
                    "&birthDate=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"), "UTF-8")+
                    "&genderID=" + URLEncoder.encode(gender, "UTF-8")+
                    "&firstName=" + URLEncoder.encode(firstName, "UTF-8")+
                    "&lastName=" + URLEncoder.encode(lastName, "UTF-8")+
                    "&idType=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8")+
                    "&nationality=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n"), "UTF-8")+
                    "&nationCode=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n"), "UTF-8");

            new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
        driveIn.setVisitorType(selectedType.getId());
        driveIn.setCarNumber("NULL");
        driveIn.setImage("NULL");
        driveIn.setRecordType("WALK");
        driveIn.setEntryTime(Constants.getCurrentTimeStamp());
        driveIn.setStatus("IN");
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
            new MaterialDialog.Builder(RecordWalkIn.this)
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
    private class DriveinAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder=new MaterialDialog.Builder(RecordWalkIn.this)
                .title("Walk in")
                .content("Recording...")
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();
        }
        protected String  doInBackground(String... params) {
            return NetworkHandler.executePost(params[0],params[1]);
        }
        protected void onPostExecute(String result) {
            builder.dismiss();
            if(result !=null){

                if(result.contains("result_code")) {
                    try {
                        Object json=new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject){
                            resultHandler(result);
                        }else {
                            //TODO remove this.Temporary workaround
                            recordOffline();
                            parseResult();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                   recordOffline();
                }

            }else{
                recordOffline();
            }

        }
    }
    private void parseResult(){
       if (preferences.canPrint()){
           Intent intent = new Intent(getApplicationContext(), SlipActivity.class);
           intent.putExtra("title", "RECORD WALK IN");
           intent.putExtra("house", selectedDestination.getName());
           intent.putExtra("firstName", firstName);
           intent.putExtra("lastName", lastName);
           intent.putExtra("idNumber", idNumber);
           intent.putExtra("result_slip", result_slip);
           intent.putExtra("visit_id", visit_id);
           startActivity(intent);
       }else{
           new MaterialDialog.Builder(RecordWalkIn.this)
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
        //Log.d("WALKIN", result);
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");
        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
            result_slip = obj.getString("result_slip");
            visit_id = obj.getInt("visit_id");
            recordOffline();
            parseResult();
        } else {
            if (resultText.contains("still in")) {
                new MaterialDialog.Builder(RecordWalkIn.this)
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
                                String idN="000000000";
                                String classCode=Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
                                if(classCode.equals("ID")){
                                    idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");
                                }else if (classCode.equals("P")){
                                    idN= Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                                }
                                String urlParameters = null;
                                try {
                                    urlParameters = "idNumber=" + URLEncoder.encode(idN.substring(2, idN.length() - 1), "UTF-8") +
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
                new MaterialDialog.Builder(RecordWalkIn.this)
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
            }
        }
    }
    private class ExitAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder=new MaterialDialog.Builder(RecordWalkIn.this)
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
        protected String  doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }
        protected void onPostExecute(String result) {
            builder.dismiss();
            if(result !=null){
                try {
                    if(result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            recordInternet();
                        } else {
                            new MaterialDialog.Builder(RecordWalkIn.this)
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
                    }else {
                        new MaterialDialog.Builder(RecordWalkIn.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new MaterialDialog.Builder(RecordWalkIn.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }
    private class FetchHostsService extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }
        protected void onPostExecute(String result) {
            //TODo: Parse result

        }
    }
}
