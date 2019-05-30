package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.helpers.CheckConnection;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.models.TypeObject;
import miles.identigate.soja.R;

public class RecordExit extends SojaActivity {
    private static final String TAG = "RecordExit";
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    Button record;
    Spinner mode;
    ArrayList<TypeObject> modes = new ArrayList<>();

    TextView name;
    TextView idNumber;
    TextView car;
    TextView entry;
    LinearLayout lin_id,lin_nm;
    //EditText comment;
    DatabaseHandler handler;
    String type;
    static String ID;
    Preferences preferences;

    String entity_name = "destination";
    String entity_owner = "visitor";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_exit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Record Exit");
        handler=new DatabaseHandler(this);
        record = findViewById(R.id.commentRecord);
        mode = findViewById(R.id.mode);
        name = findViewById(R.id.name);
        idNumber = findViewById(R.id.idNumber);
        car = findViewById(R.id.car);
        entry = findViewById(R.id.entry);
        lin_id=findViewById(R.id.lin_id);
        lin_nm=findViewById(R.id.lin_nm);
        //comment=(EditText)findViewById(R.id.comment);


        if (preferences.getBaseURL().contains("casuals")) {
            String entity_name = "event";
            String entity_owner = "supervisor";

        }


        if (getIntent() != null) {
           type=getIntent().getStringExtra("TYPE");
            if(!getIntent().getStringExtra("NAME").equals("null")){
                lin_nm.setVisibility(View.GONE);
                name.setText(getIntent().getStringExtra("NAME"));
            }
            if(!getIntent().getStringExtra("ID").equals("null")){
                lin_id.setVisibility(View.GONE);
                idNumber.setText(getIntent().getStringExtra("ID"));
            }
            ID=getIntent().getStringExtra("ID");

            Log.d(TAG, "onCreate: " + getIntent().getStringExtra("ENTRY"));

            entry.setText(convertDateFormat(getIntent().getStringExtra("ENTRY")));
            if(type.equals("DRIVE")){
                car.setText(getIntent().getStringExtra("CAR"));
            }else if(type.equals("WALK")){
                car.setVisibility(View.GONE);
            }else if(type.equals("RESIDENT")){
                car.setText("HOUSE:"+ getIntent().getStringExtra("HOUSE"));
            }else if(type.equals("PROVIDER")){
                car.setText(getIntent().getStringExtra("PROVIDERNAME"));
            }
        }else{
            finish();
        }

        modes.add(new TypeObject("1", "Foot"));
        modes.add(new TypeObject("2", "Drive"));

        TypeAdapter modeData = new TypeAdapter(this, R.layout.tv, modes);

        mode.setAdapter(modeData);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new CheckConnection().check(RecordExit.this)){
                    String idN=getIntent().getStringExtra("ID");
                    String urlParameters =null;
                    try {
                        urlParameters = "idNumber=" + URLEncoder.encode(idN, "UTF-8") +
                                "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                                "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");

                        Log.d(TAG, "URL Parameters: "+urlParameters);
                        new ExitAsync().execute(preferences.getBaseURL()+"record-visitor-exit", urlParameters);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else{
                    exitLocal();
                }
            }
        });
    }

//    Converts String Date into More Readable
    public String convertDateFormat(String dateString){


        String newDate="";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        SimpleDateFormat newDateFormat = new SimpleDateFormat("EEE dd, MMM YYYY hh:mm:aa");

        Date convertedDate=new Date();
        try{
            convertedDate=dateFormat.parse(dateString);
            newDate=newDateFormat.format(convertedDate);

            return newDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }
    public void exitLocal(){
        handler.updateExitTime(type,Constants.getCurrentTimeStamp(), ID);
        if(new CheckConnection().check(this)){
            return;
        }else {
            new MaterialDialog.Builder(RecordExit.this)
                    .title("SUCCESS")
                    .content(name.getText().toString() + " removed successfully.")
                    .positiveText("OK")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .show();
        }
    }
    private class ExitAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder=new MaterialDialog.Builder(RecordExit.this)
                .title("Exit")
                .content("Removing " + entity_owner + "...")
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
                try {
                    if(result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
                            exitLocal();
                            new MaterialDialog.Builder(RecordExit.this)
                                    .title("SUCCESS")
                                    .content(name.getText().toString() + " removed successfully.")
                                    .positiveText("OK")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            new MaterialDialog.Builder(RecordExit.this)
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
                        new MaterialDialog.Builder(RecordExit.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new MaterialDialog.Builder(RecordExit.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }

}
