package miles.identigate.soja;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import miles.identigate.soja.Adapters.TypeAdapter;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.TypeObject;

public class RecordResidentVehicleActivity extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    TextView title;
    EditText car_number;

    Spinner car_house;
    EditText car_owner;
    EditText car_house_edit;
    Button record;
    String selectedHouse;
    ArrayList<TypeObject> houses;
    DatabaseHandler handler;
    MaterialDialog dialog;
    private String token;
    Preferences preferences;
    private int type=0;
    LinearLayout ownerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_resident_vehicle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferences=new Preferences(this);
        handler=new DatabaseHandler(this);
        dialog=new MaterialDialog.Builder(RecordResidentVehicleActivity.this)
                .title("Resident")
                .content("Checking in...")
                .progress(true,0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();
        title=(TextView)findViewById(R.id.title);
        car_number=(EditText)findViewById(R.id.car_number);
        ownerLayout=(LinearLayout)findViewById(R.id.ownerLayout);

        houses=handler.getTypes("houses");
        car_house=(Spinner)findViewById(R.id.car_house);
        car_owner=(EditText)findViewById(R.id.car_owner);
        car_house_edit=(EditText)findViewById(R.id.car_house_edit);
        record=(Button)findViewById(R.id.record);
        if (getIntent().getExtras()== null){
            finish();
        }else {
            Bundle extras=getIntent().getExtras();
            type=extras.getInt("Type");
            token=extras.getString("token");
            if (type == 0){
                disableUI();
                title.setText("RECORD RESIDENT");
                car_number.setText(extras.getString("registration"));
                car_house_edit.setText(extras.getString("house"));
                car_owner.setText(extras.getString("name"));
            }else {
                //New vehicle ;Register
                title.setText("REGISTER VEHICLE");
                ownerLayout.setVisibility(View.GONE);
            }
        }
        TypeAdapter housesAdapter =new TypeAdapter(RecordResidentVehicleActivity.this,R.layout.tv,houses);
        car_house.setAdapter(housesAdapter);
        car_house.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (car_number.getText().toString()==null){
                    car_number.setError("Required.");
                }else{
                    if (type==0){
                        String urlParameters =null;
                        try {
                            urlParameters = "token=" + token +
                                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                    "&premiseZoneId=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8")+
                                    "&entry_time=" + URLEncoder.encode(selectedHouse, "UTF-8");
                            new RecordTask().execute(preferences.getBaseURL()+"qr_checkin", urlParameters);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }else {
                        String urlParameters =null;
                        try {
                            urlParameters = "token=" + token +
                                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                    "&premiseID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8")+
                                    "&regNo=" + URLEncoder.encode(car_number.getText().toString(), "UTF-8")+
                                    "&houseID=" + URLEncoder.encode(selectedHouse, "UTF-8");
                            new RecordTask().execute(preferences.getBaseURL()+"qr_register", urlParameters);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
    }
    private void disableUI(){
        disableEditText(car_number);
        disableEditText(car_house_edit);
        disableEditText(car_owner);


    }
    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }
    private class RecordTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (dialog != null && !dialog.isShowing())
                dialog.show();
        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0],params[1]);
        }

        protected void onPostExecute(String result) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (result != null) {
                //Log.e("RESIDENT",result);
                //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
                Object json = null;
                try {
                    json = new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject object = new JSONObject(result);
                        int result_code = object.getInt("result_code");
                        if (result_code == 0) {
                            new MaterialDialog.Builder(RecordResidentVehicleActivity.this)
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
                        }else{
                            new MaterialDialog.Builder(RecordResidentVehicleActivity.this)
                                    .title("Soja")
                                    .content("Invalid QR code.")
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                //Log.e("RESIDENT","null");
            }
        }
    }
}
