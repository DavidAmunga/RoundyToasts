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
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;

public class RecordResident extends SojaActivity {
    Spinner host;
    Button record;
    DatabaseHandler handler;
    String selectedType;
    ArrayList<TypeObject> houses;
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_resident);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        host=(Spinner)findViewById(R.id.host);
        record=(Button)findViewById(R.id.record);
        handler=new DatabaseHandler(this);
        preferences=new Preferences(this);
        houses=handler.getTypes("houses");
       record.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(new CheckConnection().check(RecordResident.this)){
                   recordOnline();
               }else{
                   recordOffline();
               }
           }
       });
        TypeAdapter housesAdapter =new TypeAdapter(RecordResident.this,R.layout.tv,houses);
        host.setAdapter(housesAdapter);
        host.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object=(TypeObject)parent.getSelectedItem();
                selectedType=object.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedType="1";
            }
        });
    }
    public void recordOnline(){
        String idN="000000000";
        String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
        if(classCode.equals("ID")){
            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
        }else if (classCode.equals("P")){
            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
        }
        String gender=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n").contains("M")?"0":"1";
        String urlParameters = null;
        try {
            urlParameters = "visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8")+
                    "&visitorTypeID=" + URLEncoder.encode(selectedType, "UTF-8")+
                    "&houseID=" + URLEncoder.encode(selectedType, "UTF-8")+
                    "&entryTime=" + URLEncoder.encode(new Constants().getCurrentTimeStamp(), "UTF-8")+
                    "&birthDate=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Date_of_Birth).bufText.replace("^", "\n"), "UTF-8")+
                    "&genderID=" + URLEncoder.encode(gender, "UTF-8")+
                    "&firstName=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"), "UTF-8")+
                    "&lastName=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"), "UTF-8")+
                    "&idType=" + URLEncoder.encode(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n"), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(idN.substring(2, idN.length()-1), "UTF-8");
            new DriveinAsync().execute(Constants.BASE_URL + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void recordOffline(){
        String idN="000000000";
        String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
        if(classCode.equals("ID")){
            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
        }else if (classCode.equals("P")){
            idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
        }
        Resident item=new Resident();
        item.setEntryTime(new Constants().getCurrentTimeStamp());
        item.setExitTime("NULL");
        item.setHouse(selectedType);
        item.setImage("NULL");
        item.setNationalId(idN);
        item.setDob(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Date_of_Birth).bufText.replace("^", "\n"));
        item.setSex(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n"));
        item.setName(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"));
        item.setOtherNames(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"));
        item.setIdType(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n"));


        handler.insertResident(item);
        if(new CheckConnection().check(this)){
            return;
        }else {
            new MaterialDialog.Builder(RecordResident.this)
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
        MaterialDialog builder=new MaterialDialog.Builder(RecordResident.this)
                .title("Resident")
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
            builder.hide();
            if(result !=null){
                try {
                    Object json=new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject){
                        resultHandler(result);
                    }else {
                        recordOffline();
                        new MaterialDialog.Builder(RecordResident.this)
                                .title("SUCCESS")
                                .content("Resident recorded successfully.")
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new MaterialDialog.Builder(RecordResident.this)
                        .title("Result")
                        .content("An error occurred.")
                        .positiveText("Ok")
                        .show();
            }

        }
    }
    private void resultHandler(String result) throws JSONException {
        JSONObject obj=new JSONObject(result);
        int resultCode=obj.getInt("result_code");
        String resultText=obj.getString("result_text");
        String resultContent=obj.getString("result_content");
        if(resultCode==0&&resultText.equals("OK")&&resultContent.equals("success")){
            recordOffline();
            new MaterialDialog.Builder(RecordResident.this)
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
        }else {
            if(resultText.contains("still in")){
                new MaterialDialog.Builder(RecordResident.this)
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
                            public void onNegative(MaterialDialog dialog){
                                String idN="000000000";
                                String classCode=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n");
                                if(classCode.equals("ID")){
                                    idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
                                }else if (classCode.equals("P")){
                                    idN= DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Number).bufText.replace("^", "\n");
                                }
                                String urlParameters =null;
                                try {
                                    urlParameters = "idNumber=" + URLEncoder.encode(idN.substring(2, idN.length()-1), "UTF-8") +
                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                                            "&exitTime=" + URLEncoder.encode(new Constants().getCurrentTimeStamp(), "UTF-8");
                                    new ExitAsync().execute(Constants.BASE_URL+"record-visitor-exit", urlParameters);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
            }else {
                new MaterialDialog.Builder(RecordResident.this)
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
        MaterialDialog builder=new MaterialDialog.Builder(RecordResident.this)
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
            return new NetworkHandler().excutePost(params[0], params[1]);
        }
        protected void onPostExecute(String result) {
            builder.hide();
            if(result !=null){
                try {
                    if(result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            recordOnline();
                        } else {
                            new MaterialDialog.Builder(RecordResident.this)
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
                        new MaterialDialog.Builder(RecordResident.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new MaterialDialog.Builder(RecordResident.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }
}
