package miles.identigate.soja;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.models.TypeObject;

public class RecordResidentVehicleActivity extends SojaActivity {
    private static final String TAG = "RecordResidentVehicleAc";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    String selectedHouse;
    ArrayList<TypeObject> houses;
    DatabaseHandler handler;
    MaterialDialog dialog;
    @BindView(R.id.title)
    TextViewBold title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.car_number)
    EditText carNumber;
    @BindView(R.id.iprn_profile)
    LinearLayout iprnProfile;
    @BindView(R.id.car_model)
    Spinner carModel;
    @BindView(R.id.car_model_edit)
    EditText carModelEdit;
    @BindView(R.id.car_type)
    Spinner carType;
    @BindView(R.id.car_type_edit)
    EditText carTypeEdit;
    @BindView(R.id.car_house)
    Spinner carHouse;
    @BindView(R.id.car_house_edit)
    EditText carHouseEdit;
    @BindView(R.id.openSansBold)
    TextViewBold openSansBold;
    @BindView(R.id.car_owner)
    EditText carOwner;
    @BindView(R.id.ownerLayout)
    LinearLayout ownerLayout;
    @BindView(R.id.record)
    Button record;
    @BindView(R.id.car_profile)
    LinearLayout carProfile;
    private String token;
    Preferences preferences;
    private int type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_resident_vehicle);
        ButterKnife.bind(this);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferences = new Preferences(this);
        handler = new DatabaseHandler(this);
        dialog = new MaterialDialog.Builder(RecordResidentVehicleActivity.this)
                .title("Resident")
                .content("Checking in...")
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();
        ownerLayout = findViewById(R.id.ownerLayout);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        houses = handler.getTypes("houses", null);
        if (getIntent().getExtras() == null) {
            finish();
        } else {
            Bundle extras = getIntent().getExtras();
            type = extras.getInt("Type");
            token = extras.getString("token");
            if (type == 0) {
                disableUI();
                title.setText("Record Resident");
                carNumber.setText(extras.getString("registration"));
                carHouseEdit.setText(extras.getString("house"));
                carOwner.setText(extras.getString("name"));
            } else {
                //New vehicle ;Register
                title.setText("Register Vehicle");
                ownerLayout.setVisibility(View.GONE);
            }
        }
        TypeAdapter housesAdapter = new TypeAdapter(RecordResidentVehicleActivity.this, R.layout.tv, houses);
        carHouse.setAdapter(housesAdapter);
        carHouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedHouse = object.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHouse = "1";
            }
        });
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carNumber.getText().toString() == null) {
                    carNumber.setError("Required.");
                } else {
                    if (type == 0) {
                        String urlParameters = null;
                        try {
                            urlParameters = "token=" + token +
                                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                    "&premiseZoneId=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                    "&entry_time=" + URLEncoder.encode(selectedHouse, "UTF-8");
                            new RecordTask().execute(preferences.getBaseURL() + "qr_checkin", urlParameters);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String urlParameters = null;
                        try {
                            urlParameters = "token=" + token +
                                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                    "&premiseID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8") +
                                    "&regNo=" + URLEncoder.encode(carNumber.getText().toString(), "UTF-8") +
                                    "&houseID=" + URLEncoder.encode(selectedHouse, "UTF-8");
                            new RecordTask().execute(preferences.getBaseURL() + "qr_register", urlParameters);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
    }

    private void disableUI() {
        disableEditText(carNumber);
        disableEditText(carHouseEdit);
        disableEditText(carOwner);


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
            return new NetworkHandler().executePost(params[0], params[1]);
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
                        String resultText = object.getString("result_text");
                        Log.d(TAG, "onPostExecute: " + resultText);
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
                        } else {
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
            } else {
                //Log.e("RESIDENT","null");
            }
        }
    }
}
