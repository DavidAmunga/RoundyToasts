package miles.identigate.soja.UserInterface;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.regula.documentreader.api.enums.eVisualFieldType;

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
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanQRActivity;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.ButtonRegular;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.font.TextViewRegular;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.TypeObject;

public class AssignQR extends AppCompatActivity {

    private static final String TAG = "AssignQR";

    @BindView(R.id.car_number)
    TextViewRegular carNumber;
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
    TextViewRegular carHouse;
    @BindView(R.id.car_house_edit)
    EditText carHouseEdit;
    @BindView(R.id.openSansBold)
    TextViewBold openSansBold;
    @BindView(R.id.car_owner)
    EditText carOwner;
    @BindView(R.id.ownerLayout)
    LinearLayout ownerLayout;
    @BindView(R.id.car_profile)
    LinearLayout carProfile;
    @BindView(R.id.btn_save)
    ButtonRegular btnSave;

    Preferences preferences;

    DatabaseHandler handler;

    ProgressDialog progressDialog;

    ArrayList<TypeObject> houses;
    ArrayList<TypeObject> licenses = new ArrayList<>();

    TypeObject selectedLicense, selectedHouse;
    @BindView(R.id.qr_scanner)
    DecoratedBarcodeView qrScanner;
    @BindView(R.id.options_layout)
    LinearLayout optionsLayout;
    @BindView(R.id.title)
    TextViewBold title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private BeepManager beepManager;

    private String qr_code;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(qr_code)) {
                // Prevent duplicate scans
                return;
            }


            qr_code = result.getText();
            if (qr_code != null && !TextUtils.isEmpty(qr_code)) {
                changeButtonState(Common.STATE_ENABLED);
            } else {
                changeButtonState(Common.STATE_DISABLED);
            }
//            barcodeView.setStatusText(result.getText());


            Toast.makeText(AssignQR.this, "QR Captured", Toast.LENGTH_SHORT).show();

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
        setContentView(R.layout.activity_assign_qr);
        ButterKnife.bind(this);

        handler = new DatabaseHandler(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        title.setText("Assign QR Sticker");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        progressDialog = new ProgressDialog(this);
        changeButtonState(Common.STATE_DISABLED);


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


        try {
            licenses = new FetchDriverRegNo().execute().get();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        licenses = new ArrayList<>();


        carNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                if (licenses.size() == 0) {
                    Toast.makeText(AssignQR.this, "No Unassigned Driver License available", Toast.LENGTH_SHORT).show();
                } else {
                    new SimpleSearchDialogCompat(AssignQR.this,
                            "Search for Driver License No...", "What driver license do you want to attach this qr", null, licenses, new SearchResultListener<TypeObject>() {
                        @Override
                        public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                            TypeObject object = o;


                            selectedLicense = object;

                            carNumber.setText(o.getName());

                            baseSearchDialogCompat.dismiss();

                        }
                    }).show();
                }
            }
        });


        houses = handler.getTypes("houses", null);


        carHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");

                new SimpleSearchDialogCompat(AssignQR.this,
                        "Search for Owners House...", "Which House does the Owner of the License belong to", null, houses, new SearchResultListener<TypeObject>() {
                    @Override
                    public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                        TypeObject object = o;
                        selectedHouse = object;

                        carHouse.setText(o.getName());

                        baseSearchDialogCompat.dismiss();

                    }
                }).show();
            }

        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!btnSave.isEnabled()) {
                    Toast.makeText(AssignQR.this, "Please scan a QR Sticker First!", Toast.LENGTH_SHORT).show();
                } else {
                    registerQRDrive(qr_code, carNumber.getText().toString());
                }
            }
        });


    }


    private void registerQRDrive(String qr_code, String carNumber) {

        String urlParameters = null;
        try {
            urlParameters = "token=" + qr_code +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                    "&premiseID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8") +
                    "&regNo=" + URLEncoder.encode(carNumber, "UTF-8") +
                    "&houseID=" + URLEncoder.encode(selectedHouse.getId(), "UTF-8");
            new RecordTask().execute(preferences.getBaseURL() + "qr_register", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private class RecordTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Assigning QR Sticker....");
            progressDialog.setCancelable(false);
            progressDialog.show();


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
                            progressDialog.dismiss();
                            Toast.makeText(AssignQR.this, "QR Sticker Assigned", Toast.LENGTH_SHORT).show();

                            checkInResident(qr_code);

                        } else {
                            progressDialog.dismiss();

                            Toast.makeText(AssignQR.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();


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

    private class RecordQRCheckIn extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {


            progressDialog.setMessage("Checking In...");
            progressDialog.setCancelable(false);
            progressDialog.show();

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

                            progressDialog.dismiss();
                            Toast.makeText(AssignQR.this, "Checked In!", Toast.LENGTH_SHORT).show();


                            finish();

                        } else {
                            Log.d(TAG, "onPostExecute: Still in");

                            if (resultText.contains("still in")) {
                                Log.d(TAG, "onPostExecute: " + resultText);
                                checkOutResident(qr_code);

                            } else {

                                Constants.showDialog(AssignQR.this, "Error", resultText, "OK", new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();

                                    }
                                }).show();


                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private class FetchDriverRegNo extends AsyncTask<Void, String, ArrayList<TypeObject>> {

        MaterialDialog builder = new MaterialDialog.Builder(AssignQR.this)
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


            String licensesResult = NetworkHandler.GET(preferences.getResidentsURL() + "get_vehicles?" + "premise_id=" + preferences.getPremise());

            ArrayList<TypeObject> licensesList = new ArrayList<>();
            Log.d(TAG, "doInBackground: " + licensesList.size());

            if (licensesResult != null) {
                try {
                    JSONObject obj = new JSONObject(licensesResult);
                    int resultCode = obj.getInt("result_code");
                    String resultText = obj.getString("result_text");
                    Log.d(TAG, "doInBackground: " + obj.toString());
                    if (resultCode == 0 && resultText.equals("OK")) {
                        JSONArray licensesArray = obj.getJSONArray("result_content");

                        for (int i = 0; i < licensesArray.length(); i++) {
                            JSONObject license = licensesArray.getJSONObject(i);
                            licenses.add(new TypeObject(license.getString("id"), license.getString("license_plate")));
                        }

                        builder.dismiss();

                        return licenses;
                    } else {

                        builder.dismiss();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                builder.dismiss();


            }
            return houses;
        }

        @Override
        protected void onPostExecute(ArrayList<TypeObject> typeObjects) {
            super.onPostExecute(typeObjects);

            licenses = typeObjects;
            builder.dismiss();
        }
    }

    private void changeButtonState(String option) {
        switch (option) {
            case Common
                    .STATE_ENABLED:
                btnSave.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btnSave.setEnabled(true);
                break;
            case Common
                    .STATE_DISABLED:
                btnSave.setBackgroundColor(getResources().getColor(R.color.grey));
                btnSave.setEnabled(false);
                break;
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


    private class RecordQRCheckOut extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Checking Out(Still In)...");
            progressDialog.setCancelable(false);
            progressDialog.show();


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
                            progressDialog.dismiss();
//                            CheckIn Again QR
                            checkInResident(qr_code);
                        } else {

                            Constants.showDialog(AssignQR.this, "Error", resultText, "OK", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();

                                }
                            }).show();

                        }
                    } else {

                        Constants.showDialog(AssignQR.this, "Error", "Poor Internet Connection", "OK", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                            }
                        }).show();


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
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

}
