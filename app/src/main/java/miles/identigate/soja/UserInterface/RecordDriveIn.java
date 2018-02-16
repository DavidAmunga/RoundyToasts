package miles.identigate.soja.UserInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.eVisualFieldType;

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

public class RecordDriveIn extends SojaActivity {
    Spinner visitor_type;
    DatabaseHandler handler;
    Button record;
    String vehicleNo;
    EditText vehicleRegNo;
    EditText numberOfPeople;
    Spinner host;
    String selectedType;
    ArrayList<TypeObject> visitorTypes;
    ArrayList<TypeObject>houses;
    Preferences preferences;
    String selectedHouse;
    MaterialDialog progressDialog;
    MaterialDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_drive_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler=new DatabaseHandler(this);
        preferences=new Preferences(this);
        vehicleRegNo=(EditText)findViewById(R.id.car_number);
        vehicleRegNo.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        visitor_type = (Spinner) findViewById(R.id.visitor_type);
        record=(Button)findViewById(R.id.record);
        host=(Spinner)findViewById(R.id.host);
        numberOfPeople=(EditText)findViewById(R.id.numberOfPeople);
        visitorTypes=handler.getTypes("visitors");
        houses=handler.getTypes("houses");
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new CheckConnection().check(RecordDriveIn.this)){
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
        TypeAdapter adapter =new TypeAdapter(RecordDriveIn.this,R.layout.tv,visitorTypes);
        visitor_type.setAdapter(adapter);
        visitor_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedType = object.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedType = "1";
            }
        });
        TypeAdapter housesAdapter =new TypeAdapter(RecordDriveIn.this,R.layout.tv,houses);
        host.setAdapter(housesAdapter);
        host.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object=(TypeObject)parent.getSelectedItem();
                selectedHouse=object.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHouse = "1";
            }
        });

    }
    public void recordInternt() {
        //Insert to local and online database.

        String urlParameters = null;
        try {
            String idN="000000000";
            String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
            if(classCode.equals("ID")){
                idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
            }else if (classCode.equals("P")){
                idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
            }
            String gender=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n").contains("M")?"0":"1";
            urlParameters =
                    "visitType=" + URLEncoder.encode("drive-in", "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8")+
                    "&visitorTypeID=" + URLEncoder.encode(selectedType, "UTF-8")+
                    "&houseID=" + URLEncoder.encode(selectedHouse, "UTF-8")+
                    "&entryTime=" + URLEncoder.encode(new Constants().getCurrentTimeStamp(), "UTF-8")+
                    "&vehicleRegNO=" + URLEncoder.encode(vehicleRegNo.getText().toString(), "UTF-8")+
                    "&birthDate=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Date_of_Birth).bufText.replace("^", "\n"), "UTF-8")+
                    "&genderID=" + URLEncoder.encode(gender, "UTF-8")+
                    "&firstName=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"), "UTF-8")+
                    "&lastName=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"), "UTF-8")+
                    "&idType=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n"), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(idN.substring(2, idN.length()-1), "UTF-8");
            new DriveinAsync().execute(Constants.BASE_URL + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(

            );
        }
    }

    public void recordOffline(){
        //Insert to local database
        String idN="000000000";
        String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
        if(classCode.equals("ID")){
            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
        }else if (classCode.equals("P")){
            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
        }
        DriveIn driveIn=new DriveIn();
        vehicleNo=vehicleRegNo.getText().toString();
        driveIn.setVisitorType(selectedType);
        driveIn.setCarNumber(vehicleRegNo.getText().toString());
        driveIn.setImage("NULL");
        driveIn.setEntryTime(new Constants().getCurrentTimeStamp());
        driveIn.setStatus("IN");
        driveIn.setRecordType("DRIVE");
        driveIn.setExitTime("NULL");
        driveIn.setHouseID(selectedHouse);
        driveIn.setNationalId(idN);
        driveIn.setDob(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Date_of_Birth).bufText.replace("^", "\n"));
        driveIn.setSex(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n"));
        driveIn.setName(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"));
        driveIn.setOtherNames(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"));
        driveIn.setIdType(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n"));
        if(new CheckConnection().check(this)){
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
            return new NetworkHandler().excutePost(params[0],params[1]);
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
                        MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                finish();
                            }
                        };
                        dialog = Constants.showDialog(RecordDriveIn.this, "SUCCESS", "Visitor recorded successfully.","OK", singleButtonCallback);
                        dialog.show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                recordOffline();
            }

        }
    }
    private void resultHandler(String result) throws JSONException {
        JSONObject obj=new JSONObject(result);
        int resultCode=obj.getInt("result_code");
        String resultText=obj.getString("result_text");
        String resultContent=obj.getString("result_content");
        if(resultText.equals("OK")&&resultContent.equals("success")){
            recordOffline();
            MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
                    finish();
                }
            };
            dialog = Constants.showDialog(this, "SUCCESS", "Visitor recorded successfully.","OK", singleButtonCallback);
            dialog.show();
        }else {
            if(resultText.contains("still in")){
                MaterialDialog.SingleButtonCallback singleButtonCallback=new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        String urlParameters =null;
                        String idN="000000000";
                        String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
                        if(classCode.equals("ID")){
                            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
                        }else if (classCode.equals("P")){
                            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
                        }
                        try {
                            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                                    "&idNumber=" + URLEncoder.encode(idN.substring(2, idN.length()-1), "UTF-8") +
                                    "&exitTime=" + URLEncoder.encode(new Constants().getCurrentTimeStamp(), "UTF-8");
                            new ExitAsync().execute(Constants.BASE_URL+"record-visitor-exit", urlParameters);
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
            return new NetworkHandler().excutePost(params[0], params[1]);
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
