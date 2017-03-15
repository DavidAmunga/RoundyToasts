package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.eVisualFieldType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import miles.identigate.soja.Dashboard;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.R;

public class RecordExit extends SojaActivity {
    Button record;
    Spinner mode;
    String[] Mode={
            "Drive out","Foot","Other(Specify)"
    };
    TextView name;
    TextView idNumber;
    TextView car;
    TextView entry;
    //EditText comment;
    DatabaseHandler handler;
    String type;
    static String ID;
    Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_exit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handler=new DatabaseHandler(this);
        record=(Button)findViewById(R.id.commentRecord);
        mode=(Spinner)findViewById(R.id.mode);
        name=(TextView)findViewById(R.id.name);
        idNumber=(TextView)findViewById(R.id.idNumber);
        car=(TextView)findViewById(R.id.car);
        entry=(TextView)findViewById(R.id.entry);
        //comment=(EditText)findViewById(R.id.comment);
        preferences=new Preferences(this);
        if(getIntent() !=null){
           type=getIntent().getStringExtra("TYPE");
            name.setText(getIntent().getStringExtra("NAME"));
            idNumber.setText("ID: "+getIntent().getStringExtra("ID"));
            ID=getIntent().getStringExtra("ID");
            entry.setText("ENTRY: "+getIntent().getStringExtra("ENTRY"));
            if(type.equals("DRIVE")){
                car.setText("CAR:"+ getIntent().getStringExtra("CAR"));
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
                                "&exitTime=" + URLEncoder.encode(new Constants().getCurrentTimeStamp(), "UTF-8");
                        Log.e("REQUEST",urlParameters);
                        new ExitAsync().execute(Constants.BASE_URL+"record-visitor-exit", urlParameters);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else{
                    exitLocal();
                }
            }
        });
    }
    public void exitLocal(){
        handler.updateExitTime(type,new Constants().getCurrentTimeStamp(), ID);
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
            return new NetworkHandler().excutePost(params[0],params[1]);
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
