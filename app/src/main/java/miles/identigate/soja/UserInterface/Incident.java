package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
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
import miles.identigate.soja.Models.IncidentModel;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;

public class Incident extends SojaActivity {;
    Spinner incident_types;
    Spinner visitorInvolved;
    Spinner visitor;
    EditText description;
    Button record;
    DatabaseHandler handler;
    ArrayList<TypeObject> objects;
    String type;
    Preferences preferences;
    String typeText;
    LinearLayout visitorLayout;
    ArrayList<TypeObject> visitorTypes = new ArrayList<>();
    ArrayList<TypeObject> visitors=new ArrayList<>();
    ArrayList<TypeObject> visitorFromAPI=new ArrayList<>();
    ArrayList<TypeObject> houses=new ArrayList<>();
    private String ID;

    TypeAdapter Visitoradapter;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handler=new DatabaseHandler(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        incident_types = findViewById(R.id.incident_types);
        visitorInvolved = findViewById(R.id.visitorInvolved);
        visitor = findViewById(R.id.visitor);
        description = findViewById(R.id.comment);
        record = findViewById(R.id.record);
        visitorLayout = findViewById(R.id.visitorLayout);
        preferences=new Preferences(this);

        //visitorTypes=handler.getTypes("visitors");
        visitorTypes = new ArrayList<>();
        houses= handler.getTypes("houses");
        objects=handler.getTypes("incidents");
        visitorTypes.add(new TypeObject("1","Visitor"));
        visitorTypes.add(new TypeObject("2","Resident"));
        visitorTypes.add(new TypeObject("3","None"));

        TypeAdapter adapter =new TypeAdapter(Incident.this,R.layout.tv,visitorTypes);
        visitorInvolved.setAdapter(adapter);
        visitorInvolved.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject typeObject = (TypeObject)parent.getItemAtPosition(position);
                switch (Integer.valueOf(typeObject.getId())){
                    case 1:
                        ID = null;
                        visitors.clear();
                        visitors.addAll(visitorFromAPI);
                        Visitoradapter.notifyDataSetChanged();
                        break;
                    case 2:
                        ID = null;
                        visitors.clear();
                        visitors.addAll(houses);
                        Visitoradapter.notifyDataSetChanged();
                        break;
                    case 3:
                        visitors.clear();
                        Visitoradapter.notifyDataSetChanged();
                        ID = preferences.getId();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //visitorLayout.setVisibility(View.GONE);
                visitors.clear();
                Visitoradapter.notifyDataSetChanged();
            }
        });

        if (new CheckConnection().check(this)){
            //recordOffline();
            String s = preferences.getBaseURL();
            String url = s.substring(0,s.length()-11);
            new GetActiveVisitors().execute(url+"api/visitors/visitors_in/"+preferences.getPremise());
        }else {
            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
        }


        Visitoradapter =new TypeAdapter(Incident.this,R.layout.tv,visitors);
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
                    recordOffline();
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void recordInternet(){
        String urlParameters = null;
        try {
            urlParameters =
                    "incidentDescription=" + URLEncoder.encode(description.getText().toString(), "UTF-8") +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&idNumber=" + URLEncoder.encode(ID, "UTF-8")+
                    "&incidentTypeID=" + URLEncoder.encode(type, "UTF-8");
            new IncidentAsync().execute(preferences.getBaseURL() + "record-incident", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void recordOffline(){
        //Insert to local database
        IncidentModel model=new IncidentModel();
        model.setCategory(typeText);
        model.setDescription(description.getText().toString());
        model.setDate(Constants.getCurrentTimeStamp());
        model.setNationalId(ID);
        model.setDob("NULL");
        model.setSex("NULL");
        model.setName("NULL");
        model.setOtherNames("NULL");
        model.setIdType("NULL");

        handler.insertIncident(model);
        if(new CheckConnection().check(this)){
            new MaterialDialog.Builder(Incident.this)
                    .title("SUCCESS")
                    .content("Incident recorded successfully.")
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
                    .title("SUCCESS")
                    .content("Incident recorded successfully.")
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
            //Log.e("REQUEST",params[1]);
            return NetworkHandler.executePost(params[0],params[1]);
        }
        protected void onPostExecute(String result) {
            builder.dismiss();
            if(result !=null){
                //Log.e("RESULT",result);
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
    private class GetActiveVisitors extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }
        @Override
        public void onPostExecute(String s){
            visitorFromAPI.clear();
            if (s != null){
                Log.e("Result",s);
                Object json=null;
                try {
                    json=new JSONTokener(s).nextValue();
                    if (json instanceof JSONObject){
                        JSONObject object=new JSONObject(s);
                        JSONArray array=object.getJSONArray("result_content");
                        if (array.length() >0 ){
                            for (int i=0;i<array.length();i++){
                                JSONObject item=array.getJSONObject(i);
                                visitorFromAPI.add(new TypeObject(item.getString("id_number"),item.getString("fullname")));
                            }

                        }
                        visitors.clear();
                        visitors.addAll(visitorFromAPI);
                        Visitoradapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
