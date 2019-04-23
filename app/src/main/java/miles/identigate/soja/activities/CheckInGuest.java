package miles.identigate.soja.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import miles.identigate.soja.Dashboard;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanQRActivity;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.ZxingHelperActivity;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.service.network.api.APIClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckInGuest extends AppCompatActivity {

    String qr_token;
    MaterialDialog dialog;
    Preferences preferences;

    ProgressDialog progressDialog;


    private static final String TAG = "CheckInGuest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);

        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_guest);


        ImageView scan_icon = findViewById(R.id.scan_icon);
        scan_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(CheckInGuest.this).setCaptureActivity(ZxingHelperActivity.class).addExtra("PROMPT_MESSAGE","Place QR Here to scan it").initiateScan();
            }
        });

        progressDialog = new ProgressDialog(this);


        dialog = new MaterialDialog.Builder(CheckInGuest.this)
                .title("QR")
                .content("Checking QR...")
                .progress(true, 0)
                .cancelable(true)
                .widgetColorRes(R.color.colorPrimary)
                .build();
    }


    public void recordCheckIn(String qr_token) {
        String urlParameters = null;
        try {
            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getCurrentUser().getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getCurrentUser().getPremiseZoneId(), "UTF-8") +
                    "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                    "&qr=" + URLEncoder.encode(qr_token, "UTF-8");

            Log.d(TAG, "onActivityResult: " + preferences.getCurrentUser().getPremiseZoneId());


            new RecordQRCheckin().execute(preferences.getBaseURL().replace("visits", "residents") + "qr_checkin", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                //Log.v("QR",result.getContents());
                qr_token = result.getContents();

                recordCheckIn(qr_token);

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class RecordQRCheckin extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (dialog != null && !dialog.isShowing())
                dialog.show();
        }

        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {

            Log.d(TAG, "onPostExecute: Result" + result);
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (result != null) {
                //Toast.makeText(ExpressCheckoutActivity.this,result, Toast.LENGTH_LONG).show();
                Object json = null;
                try {
                    json = new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject object = new JSONObject(result);
                        int result_code = object.getInt("result_code");
                        String result_text = object.getString("result_text");
                        if (result_code == 0) {
                            showSuccess();

                        } else {

                            new MaterialDialog.Builder(CheckInGuest.this)
                                    .title("Notice")
                                    .content(result_text)
                                    .positiveText("Ok")
                                    .negativeText("Check Out")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
                                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                            finish();
                                        }

                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            dialog.dismiss();


                                            recordCheckOut();

                                        }
                                    })
                                    .show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void recordCheckOut() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("Checking Out");
            progressDialog.show();
        }
//        Log.d(TAG, "recordCheckOut: "+idNumber+","+preferences.getDeviceId());

        String urlParameters = null;
        try {
            urlParameters = "qr=" + qr_token +
                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                    "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");


            Log.d(TAG, "onNegative: " + preferences.getResidentsURL() + "qr_checkout?" + urlParameters);
            new RecordQRCheckOut().execute(preferences.getResidentsURL() + "qr_checkout", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    void showSuccess() {
        progressDialog.dismiss();
        dialog.dismiss();
        dialog = new MaterialDialog.Builder(this)
                .title("CHECKED IN")
                .titleGravity(GravityEnum.CENTER)
                .customView(R.layout.success_dialog, true)
                .positiveText("OK")
                .negativeText(" CANCEL")
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        finish();
                    }
                })
                .build();
        View view = dialog.getCustomView();
        TextView messageText = view.findViewById(R.id.message);
        messageText.setText("Checked In");
        dialog.show();
    }

    private class RecordQRCheckOut extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(CheckInGuest.this)
                .title("Exit")
                .content("Removing Guest...")
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

                            recordCheckIn(qr_token);

                        } else {
                            new MaterialDialog.Builder(CheckInGuest.this)
                                    .title("Notice")
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
                        new MaterialDialog.Builder(CheckInGuest.this)
                                .title("Notice")
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


}
