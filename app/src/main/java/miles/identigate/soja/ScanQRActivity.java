package miles.identigate.soja;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.regula.documentreader.api.enums.eVisualFieldType;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.ZxingHelperActivity;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.UserInterface.RecordWalkIn;


public class ScanQRActivity extends AppCompatActivity {
    Dialog dialog;
    private static String token;
    Preferences preferences;

    private static final String TAG = "ScanQRActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R.id.scan_icon)
    ImageView scanIcon;
    @BindView(R.id.driveInLayout)
    RelativeLayout driveInLayout;
    @BindView(R.id.scan_icon_2)
    ImageView scanIcon2;
    @BindView(R.id.walkInLayout)
    RelativeLayout walkInLayout;


    ArrayList<TypeObject> houses;
    DatabaseHandler handler;


    String selectedMode = "";

    String qr_contents = "";

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
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        getSupportActionBar().setTitle("Residents");

        handler = new DatabaseHandler(this);


        dialog = Constants.showProgressDialog(this, "QR Check In", "Checking In...");



        driveInLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanQRActivity.this)
                        .addExtra("mode", "Drive In")
                        .addExtra("type", "checkIn")
                        .setPrompt("Scan Resident QR Code ")
                        .setBeepEnabled(true)
                        .setCaptureActivity(ZxingHelperActivity.class).initiateScan();
                selectedMode = "driveIn";
            }
        });

        walkInLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanQRActivity.this)
                        .addExtra("mode", "Walk In")
                        .addExtra("type", "checkIn")
                        .setPrompt("Scan Resident QR Code ")
                        .setBeepEnabled(true)
                        .setCaptureActivity(ZxingHelperActivity.class).initiateScan();
                selectedMode = "walkIn";
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                Log.v("QR", result.getContents());
                token = result.getContents();

//                Log.d(TAG, "onActivityResult: Token"+token);
                qr_contents = result.getContents();
                try {
                    String urlParameters = "deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                            "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                            "&qr=" + result.getContents();


                    if (selectedMode.equals("walkIn")) {
                        Log.d(TAG, "onActivityResult: " + preferences.getResidentsURL() + "qr_checkin?" + urlParameters);
                        new RecordQRCheckInWalk().execute(preferences.getResidentsURL() + "qr_checkin", urlParameters);
                    } else {
                        new GetQRDriveDetails().execute(preferences.getBaseURL() + "qr_data/" + result.getContents());
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);


        }
    }

    private class RecordQRCheckInWalk extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }
        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
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

                        Log.d(TAG, "onPostExecute: " + resultText);

                        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
                            Log.d(TAG, "onPostExecute: Success");

                            MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                    finish();
                                }
                            };
                            dialog = Constants.showDialog(ScanQRActivity.this, "SUCCESS", "Resident recorded successfully.", "OK", singleButtonCallback);

                        } else {
                            if (resultText.contains("still in")) {
                                new MaterialDialog.Builder(ScanQRActivity.this)
                                        .title("Soja")
                                        .content(Constants.sentenceCaseForText(resultText))
                                        .positiveText("OK")
                                        .negativeText("Check out")
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                dialog.dismiss();
//                                                Checkout QR
                                                String urlParameters = null;
                                                try {
                                                    urlParameters = "qr=" + qr_contents +
                                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                                            "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                                            "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");


                                                    Log.d(TAG, "onNegative: " + preferences.getResidentsURL() + "qr_checkout?" + urlParameters);
                                                    new RecordQRCheckOut().execute(preferences.getResidentsURL() + "qr_checkout", urlParameters);
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                        .show();
                            } else {
                                new MaterialDialog.Builder(ScanQRActivity.this)
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class RecordQRCheckOut extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(ScanQRActivity.this)
                .title("Exit")
                .content("Removing Resident...")
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();

        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            builder.dismiss();
            if (result != null) {
                try {
                    if (result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        if (resultText.equals("OK") && resultContent.equals("success")) {
//                            CheckIn Again QR
                            try {

                                String urlParameters = "deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                        "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                        "&qr=" + qr_contents;
                                Log.d(TAG, "onActivityResult: " + preferences.getResidentsURL() + "qr_checkin?" + urlParameters);
                                new RecordQRCheckInWalk().execute(preferences.getResidentsURL() + "qr_checkin", urlParameters);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }


                        } else {
                            new MaterialDialog.Builder(ScanQRActivity.this)
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
                            Log.d(TAG, "Error: " + result);
                        }
                    } else {
                        new MaterialDialog.Builder(ScanQRActivity.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class GetQRDriveDetails extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (dialog != null && !dialog.isShowing())
                dialog.show();
        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().GET(params[0]);
        }

        protected void onPostExecute(String result) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (result != null) {
                //Log.e("QR",result);
                //Log.e("SCAN",result);
                Object json = null;
                try {
                    json = new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject) {
                        Intent intent = new Intent(getApplicationContext(), RecordResidentVehicleActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("token", token);
                        JSONObject object = new JSONObject(result);
                        int result_code = object.getInt("result_code");
                        if (result_code == 0) {
                            bundle.putInt("Type", 0);


                            JSONObject content = object.getJSONObject("result_content");
                            String registration = content.getString("registration");
                            String model = content.getString("model");
                            String type = content.getString("type");
                            String name = content.getString("name");
                            String house = content.getString("house");
                            Log.d(TAG, "onPostExecute: " + house);

                            String houseID = "";

                            houses = handler.getTypes("houses", null);

                            for (TypeObject houseObj : houses) {
                                if (houseObj.getName().equals(house)) {
                                    houseID = houseObj.getId();
                                }
                            }

                            Log.d(TAG, "onPostExecute: " + houseID);

//
//
//                            bundle.putString("registration", registration);
//                            bundle.putString("model", model);
//                            bundle.putString("type", type);
//                            bundle.putString("name", name);
//                            bundle.putString("house", house);
//                            intent.putExtras(bundle);
//                            startActivity(intent);


                            if (result_code == 0) {
                                String urlParameters = null;
                                try {
                                    urlParameters = "token=" + token +
                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                            "&premiseZoneId=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                                            "&entry_time=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");
                                    new RecordQRCheckInDrive().execute(preferences.getBaseURL() + "qr_checkin", urlParameters);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                String urlParameters = null;
                                try {
                                    urlParameters = "token=" + token +
                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                            "&premiseID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8") +
                                            "&regNo=" + URLEncoder.encode(registration, "UTF-8") +
                                            "&houseID=" + URLEncoder.encode(houseID, "UTF-8");
                                    new RecordQRCheckInDrive().execute(preferences.getBaseURL() + "qr_register", urlParameters);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }


                        } else {
                            bundle.putInt("Type", result_code);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class RecordQRCheckInDrive extends AsyncTask<String, Void, String> {
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
                            new MaterialDialog.Builder(ScanQRActivity.this)
                                    .title("SUCCESS")
                                    .content("Resident recorded successfully.")
                                    .positiveText("OK")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
                                            startActivity(new Intent(ScanQRActivity.this, Dashboard.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            new MaterialDialog.Builder(ScanQRActivity.this)
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
