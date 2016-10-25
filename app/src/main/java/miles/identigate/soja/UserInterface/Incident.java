package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

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
import miles.identigate.soja.Models.IncidentModel;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;
import miles.identigate.soja.app.Common;

public class Incident extends SojaActivity {;
    Spinner incident_types;
    Spinner visitorType;
    Spinner visitor;
    EditText description;
    Button record;
    DatabaseHandler handler;
    ArrayList<TypeObject> objects;
    String type;
    Preferences preferences;
    String typeText;
    LinearLayout visitorLayout;
    ArrayList<TypeObject> visitorTypes;
    ArrayList<TypeObject> visitors=new ArrayList<>();
    private String ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handler=new DatabaseHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        incident_types=(Spinner)findViewById(R.id.incident_types);
        visitorType=(Spinner)findViewById(R.id.visitorType);
        visitor=(Spinner)findViewById(R.id.visitor);
        description=(EditText)findViewById(R.id.comment);
        record=(Button)findViewById(R.id.record);
        visitorLayout=(LinearLayout)findViewById(R.id.visitorLayout);
        preferences=new Preferences(this);
        visitorTypes=handler.getTypes("visitors");
        objects=handler.getTypes("incidents");

        TypeAdapter adapter =new TypeAdapter(Incident.this,R.layout.tv,visitorTypes);
        visitorType.setAdapter(adapter);
        visitorType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    visitorLayout.setVisibility(View.VISIBLE);
                }else{
                    visitorLayout.setVisibility(View.VISIBLE);
                    TypeObject object = (TypeObject) parent.getSelectedItem();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                visitorLayout.setVisibility(View.GONE);
            }
        });

        ArrayList<DriveIn> DRIVE_TEMP=handler.getDriveIns(0);
        ArrayList<DriveIn> WALK_TEMP=handler.getWalk(0);
        for (int i=0;i<DRIVE_TEMP.size();i++){
            TypeObject object=new TypeObject();
            object.setId(DRIVE_TEMP.get(i).getNationalId());
            object.setName(DRIVE_TEMP.get(i).getName());
            visitors.add(object);
        }
        for (int i=0;i<WALK_TEMP.size();i++){
            TypeObject object=new TypeObject();
            object.setId(WALK_TEMP.get(i).getNationalId());
            object.setName(WALK_TEMP.get(i).getName());
            visitors.add(object);
        }
        TypeAdapter Visitoradapter =new TypeAdapter(Incident.this,R.layout.tv,visitors);
        visitor.setAdapter(Visitoradapter);
        visitor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 TypeObject object = (TypeObject) parent.getSelectedItem();
                 ID=object.getId();
             }

             @Override
             public void onNothingSelected(AdapterView<?> parent) {
                    ID=preferences.getName();
             }
         });


        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( description.getText().toString().equals("")||description.getText().toString().equals(null)) {
                    Snackbar.make(v, "All fields are required.", Snackbar.LENGTH_SHORT).show();
                } else if (description.getText().toString().length() <= 5) {
                    Snackbar.make(v,"Description must be at least 5 characters long.",Snackbar.LENGTH_SHORT).show();
                } else {
                    if(new CheckConnection().check(Incident.this)){
                       recordInternt();
                    }else{
                        recordOffline();
                    }
                }
            }
        });
        TypeAdapter Incidentadapter =new TypeAdapter(Incident.this,R.layout.tv,objects);
        incident_types.setAdapter(Incidentadapter);
        incident_types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object=(TypeObject)parent.getItemAtPosition(position);
                type=object.getId();
                typeText=object.getName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type="1";
            }
        });
    }
    public void recordInternt(){
        String urlParameters = null;
        try {
            urlParameters =
                    "incidentDescription=" + URLEncoder.encode(description.getText().toString(), "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(ID, "UTF-8")+
                    "&incidentTypeID=" + URLEncoder.encode(type, "UTF-8");
            new IncidentAsync().execute(Constants.BASE_URL + "record-incident", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void recordOffline(){
        //Insert to local database
        IncidentModel model=new IncidentModel();
        model.setCategory(typeText);
        model.setDescription(description.getText().toString());
        model.setDate(new Constants().getCurrentTimeStamp());
        model.setNationalId(ID);
        model.setDob("NULL");
        model.setSex("NULL");
        model.setName("NULL");
        model.setOtherNames("NULL");
        model.setIdType("NULL");

        handler.insertIncident(model);
        if(new CheckConnection().check(this)){
            return;
        }else {
            new MaterialDialog.Builder(Incident.this)
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
    private class IncidentAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder=new MaterialDialog.Builder(Incident.this)
                .title("Soja")
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
                try {
                    Object json=new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject){
                        resultHandler(result);
                    }else {
                        recordOffline();
                        new MaterialDialog.Builder(Incident.this)
                                .title("SUCCESS")
                                .content("This incident has been recorded successfully.")
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
                new MaterialDialog.Builder(Incident.this)
                        .title("Poor connection")
                        .content("You have a poor internet connection.Please check your network and try again.")
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
            new MaterialDialog.Builder(Incident.this)
                    .title("SUCCESS")
                    .content("This incident has been recorded successfully.")
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
            new MaterialDialog.Builder(Incident.this)
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

    }
}
