package miles.identigate.soja.UserInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
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
import miles.identigate.soja.SlipActivity;

public class RecordWalkIn extends SojaActivity {

    Spinner visitor_type;
    DatabaseHandler handler;
    Button record;
    Spinner host;
    TypeObject selectedType;
    TypeObject selectedHouse;
    ArrayList<TypeObject> houses;
    ArrayList<TypeObject> visitorTypes;
    Preferences preferences;

    String firstName;
    String lastName;
    String idNumber;
    String result_slip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_walk_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler=new DatabaseHandler(this);
        visitor_type = (Spinner) findViewById(R.id.visitor_type);
        record=(Button)findViewById(R.id.record);
        host=(Spinner)findViewById(R.id.host);
        preferences=new Preferences(this);
        houses=handler.getTypes("houses");
        visitorTypes=handler.getTypes("visitors");
       record.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(new CheckConnection().check(RecordWalkIn.this)){
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
        TypeAdapter housesAdapter =new TypeAdapter(RecordWalkIn.this,R.layout.tv,houses);
        host.setAdapter(housesAdapter);
        host.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object=(TypeObject)parent.getSelectedItem();
                selectedHouse=object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void recordInternet(){
        String urlParameters = null;
        try {
            String idN="000000000";
            String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
            if(classCode.equals("ID")){
                idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
            }else if (classCode.equals("P")){
                idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
            }
          /* if (DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number) != null){
                idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
            }else {
                if (DocumentReader.getTextFieldByType(eVisualFieldType.ft_Passport_Number) != null){
                    idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Passport_Number).bufText.replace("^", "\n");
                }else {
                    if (DocumentReader.getTextFieldByType(eVisualFieldType.ft_Visa_Number) != null){
                        idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Visa_Number).bufText.replace("^", "\n");
                    }
                }
            }*/
            firstName = DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n");
            lastName = DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n");
            idNumber = idN.substring(2, idN.length()-1);
            String gender=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n").contains("M")?"0":"1";
            urlParameters = "visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8")+
                    "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8")+
                    "&houseID=" + URLEncoder.encode(selectedHouse.getId(), "UTF-8")+
                    "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8")+
                    "&birthDate=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Date_of_Birth).bufText.replace("^", "\n"), "UTF-8")+
                    "&genderID=" + URLEncoder.encode(gender, "UTF-8")+
                    "&firstName=" + URLEncoder.encode(firstName, "UTF-8")+
                    "&lastName=" + URLEncoder.encode(lastName, "UTF-8")+
                    "&idType=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n"), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8");

            new DriveinAsync().execute(Constants.BASE_URL + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
        driveIn.setVisitorType(selectedType.getId());
        driveIn.setCarNumber("NULL");
        driveIn.setImage("NULL");
        driveIn.setRecordType("WALK");
        driveIn.setEntryTime(new Constants().getCurrentTimeStamp());
        driveIn.setStatus("IN");
        driveIn.setExitTime("NULL");
        driveIn.setHouseID(selectedHouse.getId());
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
            return new NetworkHandler().excutePost(params[0],params[1]);
        }
        protected void onPostExecute(String result) {
            builder.dismiss();
            if(result !=null){
                //Log.e("WALK",result);
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
           intent.putExtra("house", selectedHouse.getName());
           intent.putExtra("firstName", firstName);
           intent.putExtra("lastName", lastName);
           intent.putExtra("idNumber", idNumber);
           intent.putExtra("result_slip", result_slip);
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
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");
        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
            result_slip = obj.getString("result_slip");
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
                                String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
                                if(classCode.equals("ID")){
                                    idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
                                }else if (classCode.equals("P")){
                                    idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
                                }
                                String urlParameters = null;
                                try {
                                    urlParameters = "idNumber=" + URLEncoder.encode(idN.substring(2, idN.length() - 1), "UTF-8") +
                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                            "&exitTime=" + URLEncoder.encode(new Constants().getCurrentTimeStamp(), "UTF-8");
                                    new ExitAsync().execute(Constants.BASE_URL + "record-visitor-exit", urlParameters);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
            } else {
                new MaterialDialog.Builder(RecordWalkIn.this)
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
            return NetworkHandler.excutePost(params[0], params[1]);
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
}
