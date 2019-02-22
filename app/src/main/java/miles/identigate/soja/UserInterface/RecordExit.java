package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import miles.identigate.soja.Dashboard;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.R;

public class RecordExit extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    Button record;
    Spinner mode;
    String[] Mode={
            "Foot","Drive out","Other(Specify)"
    };
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        preferences=new Preferences(this);



        if(getIntent() !=null){
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
        ArrayAdapter<CharSequence> modeData = new ArrayAdapter(this,android.R.layout.simple_spinner_item,Mode);
        modeData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        SimpleDateFormat newDateFormat = new SimpleDateFormat("EEE, MMM d yyyy hh:mm aaa");

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
                                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
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
