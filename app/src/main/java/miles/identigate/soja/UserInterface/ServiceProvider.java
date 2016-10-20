package miles.identigate.soja.UserInterface;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;
import miles.identigate.soja.app.Common;

public class ServiceProvider extends SojaActivity {
    Spinner company;
    Button record;
    TextView companyLabel;
    ImageView image;
    Spinner host;
    TextView hostLabel;
    DatabaseHandler handler;
    ArrayList<TypeObject> visitorTypes;
    String providerId;
    String selectedHouse;
    ArrayList<TypeObject> houses;
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        company=(Spinner)findViewById(R.id.company);
        record=(Button)findViewById(R.id.record);
        companyLabel=(TextView)findViewById(R.id.companyLabel);
        image=(ImageView)findViewById(R.id.image);
        hostLabel=(TextView)findViewById(R.id.hostLabel);
        host=(Spinner)findViewById(R.id.host);
        handler=new DatabaseHandler(this);
        visitorTypes=handler.getTypes("service_providers");
        preferences=new Preferences(this);
        houses=handler.getTypes("houses");
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new CheckConnection().check(ServiceProvider.this)){
                    recordOnline();
                }else{
                  recordOffline();
                }
            }
        });
        TypeAdapter adapter =new TypeAdapter(ServiceProvider.this,R.layout.tv,visitorTypes);
        company.setAdapter(adapter);
        company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getItemAtPosition(position);
                providerId = object.getName();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        TypeAdapter housesAdapter =new TypeAdapter(ServiceProvider.this,R.layout.tv,houses);
        host.setAdapter(housesAdapter);
        host.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object=(TypeObject)parent.getSelectedItem();
                selectedHouse=object.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHouse="1";
            }
        });
    }
    public void recordOnline(){
        String idN=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
        String gender=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n").contains("M")?"0":"1";
        String urlParameters = null;
        try {
            urlParameters = "visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8")+
                    "&visitorTypeID=" + URLEncoder.encode("3", "UTF-8")+
                    "&houseID=" + URLEncoder.encode(selectedHouse, "UTF-8")+
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
        ServiceProviderModel item=new ServiceProviderModel();
        item.setEntryTime(new Constants().getCurrentTimeStamp());
        item.setExitTime("NULL");
        item.setCompanyName(providerId);
        item.setProviderName(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"));
        item.setProviderImage("NULL");
        item.setNationalId(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n"));
        item.setDob(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Date_of_Birth).bufText.replace("^", "\n"));
        item.setSex(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Sex).bufText.replace("^", "\n"));
        item.setOtherNames(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Surname_And_Given_Names).bufText.replace("^", "\n"));
        item.setIdType(DocumentReader.getTextFieldByType(eVisualFieldType.ft_Document_Class_Code).bufText.replace("^", "\n"));


        handler.insertServiceProvider(item);
        if(new CheckConnection().check(this)){
            return;
        }else {
            new MaterialDialog.Builder(ServiceProvider.this)
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
        MaterialDialog builder=new MaterialDialog.Builder(ServiceProvider.this)
                .title("Service provider")
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
            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            if(result !=null){
                Log.d("RESULT",result);
                try {
                    Object json=new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject){
                        resultHandler(result);
                    }else {
                        recordOffline();
                        new MaterialDialog.Builder(ServiceProvider.this)
                                .title("SUCCESS")
                                .content("Service Provider recorded successfully.")
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
                new MaterialDialog.Builder(ServiceProvider.this)
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
            new MaterialDialog.Builder(ServiceProvider.this)
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
                new MaterialDialog.Builder(ServiceProvider.this)
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
                                String idN=DocumentReader.getTextFieldByType(eVisualFieldType.ft_Identity_Card_Number).bufText.replace("^", "\n");
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
                new MaterialDialog.Builder(ServiceProvider.this)
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
        MaterialDialog builder=new MaterialDialog.Builder(ServiceProvider.this)
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
            builder.dismiss();
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
                            new MaterialDialog.Builder(ServiceProvider.this)
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
                        new MaterialDialog.Builder(ServiceProvider.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new MaterialDialog.Builder(ServiceProvider.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }
}
