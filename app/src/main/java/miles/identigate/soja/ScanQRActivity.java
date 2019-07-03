package miles.identigate.soja;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.UserInterface.AssignQR;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.ButtonRegular;
import miles.identigate.soja.font.EditTextRegular;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.TypeObject;


public class ScanQRActivity extends AppCompatActivity {
    private static String token;
    Preferences preferences;

    private static final String TAG = "ScanQRActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    String[] filterItems = {"Walk In", "Drive In"};


    ArrayList<TypeObject> houses, licenses;
    DatabaseHandler handler;


    String selectedMode = "";

    String selectedHouse, selectedLicense;


    String qr_contents = "";
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.qr_scanner)
    DecoratedBarcodeView qrScanner;
    @BindView(R.id.info_image)
    ImageView infoImage;
    @BindView(R.id.pb)
    ProgressBar pb;
    @BindView(R.id.info_text)
    TextView infoText;
    @BindView(R.id.info_layout)
    LinearLayout infoLayout;
    @BindView(R.id.info_help)
    TextView infoHelp;
    @BindView(R.id.options_layout)
    LinearLayout optionsLayout;


    private BeepManager beepManager;

    String qr_code;
    Animation scale_up;


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(qr_code)) {
                // Prevent duplicate scans
                return;
            }

            qr_code = result.getText();
//            barcodeView.setStatusText(result.getText());

            checkInResident(qr_code);


//            Toast.makeText(ScanTicket.this, "QR is " + result.getText(), Toast.LENGTH_SHORT).show();

            beepManager.playBeepSoundAndVibrate();

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_qr);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Resident Check In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        getSupportActionBar().setTitle("Residents");

        handler = new DatabaseHandler(this);


        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        qrScanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        qrScanner.initializeFromIntent(getIntent());
        qrScanner.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        qrScanner.setStatusText("");


        optionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qr_code = "";
                triggerScan();
            }
        });

        if (preferences.isDarkModeOn()) {
            qrScanner.setTorchOn();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan_resident, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_new_qr) {
            startActivity(new Intent(ScanQRActivity.this, AssignQR.class));
//            showNewDriverDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkInResident(String qr_code) {

        try {
            Log.d(TAG, "Device ID: " + preferences.getDeviceId());
            String urlParameters = "device_id=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                    "&entry_time=" + Constants.getCurrentTimeStamp() +
                    "&qr=" + qr_code;


            Log.d(TAG, "onActivityResult: " + preferences.getResidentsURL() + "qr_checkin?" + urlParameters);
            new RecordQRCheckIn().execute(preferences.getResidentsURL() + "qr_checkin", urlParameters);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    private void registerQRDrive(String qr_code, String carNumber) {

        String urlParameters = null;
        try {
            urlParameters = "token=" + qr_code +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                    "&premiseID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8") +
                    "&regNo=" + URLEncoder.encode(carNumber, "UTF-8") +
                    "&houseID=" + URLEncoder.encode(selectedHouse, "UTF-8");
            new RecordTask().execute(preferences.getBaseURL() + "qr_register", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void checkOutResident(String qr_code) {
        String urlParameters = null;
        try {
            urlParameters = "qr=" + qr_code +
                    "&device_id=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                    "&exit_time=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");


            Log.d(TAG, "onNegative: " + preferences.getResidentsURL() + "qr_checkout?" + urlParameters);
            new RecordQRCheckOut().execute(preferences.getResidentsURL() + "qr_checkout", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class RecordQRCheckIn extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            changeUIState(Common.STATE_LOADING, "Checking In");

        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                //Log.e("QR",result);
                //Log.e("SCAN",result);
                Log.d(TAG, "onPostExecute: " + result);
                Object json = null;
                try {
                    json = new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject) {

                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");

                        String residentName = obj.optString("visitor_name", "Resident");


                        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
                            Log.d(TAG, "onPostExecute: Success");

                            changeUIState(Common.STATE_SUCCESS, residentName + " checked in");

                        } else {
                            Log.d(TAG, "onPostExecute: Still in");

                            if (resultText.contains("still in")) {
                                Log.d(TAG, "onPostExecute: " + resultText);
                                checkOutResident(qr_code);

                            } else {
                                changeUIState(Common.STATE_INFO, resultText);

                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class RecordQRCheckOut extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeUIState(Common.STATE_LOADING, "Checking Out(Still In)...");

        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    if (result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultText.equals("OK") && resultContent.equals("success")) {
//                            CheckIn Again QR
                            checkInResident(qr_code);
                        } else {

                            changeUIState(Common.STATE_FAILURE, resultText);

                        }
                    } else {
                        changeUIState(Common.STATE_FAILURE, "Poor Internet Connection");


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showNewDriverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanQRActivity.this);

        View dialogView = getLayoutInflater().inflate(R.layout.layout_new_driver, null);


        builder.setView(dialogView);

        Dialog dialog = builder.create();

        builder.setMessage("Pair QR Sticker with License");
        builder.setTitle("Register Vehicle");


        EditTextRegular carNumber = dialogView.findViewById(R.id.car_number);
        Spinner carHouse = dialogView.findViewById(R.id.car_house);
//        Spinner carOwner = dialogView.findViewById(R.id.car_owner);
        ButtonRegular btnSave = dialogView.findViewById(R.id.btn_save);


        houses = handler.getTypes("houses", null);


        TypeAdapter housesAdapter = new TypeAdapter(ScanQRActivity.this, R.layout.tv, houses);


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


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (carNumber.getText().toString() == null) {
                    carNumber.setError("Required.");
                } else {
                    dialog.dismiss();


                    registerQRDrive(qr_code, carNumber.getText().toString());

                }

            }
        });

        dialog.show();


    }

    private class RecordQRCheckInDrive extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {

        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {

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
                        Log.d(TAG, "onPostExecute: " + result_code);
                        if (result_code == 0) {
                            changeUIState(Common.STATE_SUCCESS, "Resident Checked In");
                        } else {
                            changeUIState(Common.STATE_FAILURE, "Invalid QR code");

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

    private class RecordTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            changeUIState(Common.STATE_LOADING, "Assigning QR Sticker");

        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {

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
                            changeUIState(Common.STATE_SUCCESS, "QR Sticker Assigned");

                            checkInResident(qr_code);

                        } else {
                            changeUIState(Common.STATE_FAILURE, "Invalid QR Code");

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

    private class FetchDriverRegNo extends AsyncTask<Void, String, ArrayList<TypeObject>> {

        MaterialDialog builder = new MaterialDialog.Builder(ScanQRActivity.this)
                .title("Soja")
                .titleGravity(GravityEnum.CENTER)
                .titleColor(getResources().getColor(R.color.ColorPrimary))
                .content("Fetching Unassigned Licenses")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();
        }


        @Override
        protected ArrayList<TypeObject> doInBackground(Void... voids) {

            String licensesResult = NetworkHandler.GET(preferences.getBaseURL() + "driver_reg_no");

            ArrayList<TypeObject> licensesList = new ArrayList<>();
            Log.d(TAG, "doInBackground: " + licensesList.size());

            if (licensesResult != null) {
                try {
                    JSONObject licensesObject = new JSONObject(licensesResult);
                    if (licensesObject.getInt("result_code") == 0 && licensesObject.getString("result_text").equals("OK")) {
                        JSONArray licensesArray = licensesObject.getJSONArray("result_content");
                        for (int i = 0; i < licensesArray.length(); i++) {
                            JSONObject license = licensesArray.getJSONObject(i);
                            licenses.add(new TypeObject(license.getString("driver_reg_id"), license.getString("driver_reg_id")));
//                        handler.insertHouse(house.getString("house_id"), house.getString("house_description"),house.getString("block_description"));
                        }

                        showNewDriverDialog();

                        return licenses;
                    } else {
                        Toast.makeText(ScanQRActivity.this, "Couldn't retrieve Licenses", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return houses;
        }

        @Override
        protected void onPostExecute(ArrayList<TypeObject> typeObjects) {
            super.onPostExecute(typeObjects);
            builder.dismiss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        qrScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        qrScanner.pause();
    }

    public void triggerScan() {
        qrScanner.decodeSingle(callback);
    }


    private void changeUIState(String option, String message) {

        Log.d(TAG, "changeUIState: " + option);
        switch (option) {
            case Common.STATE_LOADING:
                infoText.setVisibility(View.VISIBLE);
                infoText.setText(message);
                pb.setVisibility(View.VISIBLE);
                infoImage.setVisibility(View.GONE);
                infoHelp.setVisibility(View.GONE);
                break;
            case Common.STATE_SUCCESS:
                infoText.setVisibility(View.VISIBLE);
                infoText.setText(message);
                pb.setVisibility(View.GONE);
                infoImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_checked));
                infoImage.setAnimation(scale_up);
                infoImage.setVisibility(View.VISIBLE);
                infoHelp.setVisibility(View.GONE);
                break;
            case Common.STATE_FAILURE:
                infoText.setVisibility(View.VISIBLE);
                infoText.setText(message);
                pb.setVisibility(View.GONE);
                infoImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel));
                infoImage.setAnimation(scale_up);
                infoImage.setVisibility(View.VISIBLE);
                infoHelp.setVisibility(View.GONE);
                break;
            case Common.STATE_INFO:
                infoText.setVisibility(View.VISIBLE);
                infoText.setText(message);
                pb.setVisibility(View.GONE);
                infoImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning));
                infoImage.setAnimation(scale_up);
                infoImage.setVisibility(View.VISIBLE);
                infoHelp.setVisibility(View.GONE);

                break;
            default:
                pb.setVisibility(View.GONE);
                infoImage.setVisibility(View.GONE);
                infoText.setText("");
                infoHelp.setVisibility(View.VISIBLE);

                infoHelp.setText("Place your Pass within the square to scan");
        }


    }


}
