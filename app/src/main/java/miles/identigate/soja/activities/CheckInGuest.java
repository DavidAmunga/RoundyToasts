package miles.identigate.soja.activities;

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
import java.lang.ref.PhantomReference;
import java.net.URLEncoder;

import miles.identigate.soja.ExpressCheckoutActivity;
import miles.identigate.soja.R;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.ZxingHelperActivity;

public class CheckInGuest extends AppCompatActivity {

    String qr_token;
    MaterialDialog dialog;
    Preferences preferences;


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
                new IntentIntegrator(CheckInGuest.this).setCaptureActivity(ZxingHelperActivity.class).initiateScan();
            }
        });

        dialog = new MaterialDialog.Builder(CheckInGuest.this)
                .title("QR")
                .content("Checking QR...")
                .progress(true, 0)
                .cancelable(true)
                .widgetColorRes(R.color.colorPrimary)
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                //Log.v("QR",result.getContents());
                qr_token = result.getContents();


                String urlParameters = null;
                try {
                    urlParameters = "deviceID=" + URLEncoder.encode(preferences.getCurrentUser().getDeviceId(), "UTF-8") +
                            "&premise_zone_id=" + URLEncoder.encode(preferences.getCurrentUser().getPremiseZoneId(), "UTF-8") +
                            "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                            "&qr=" + URLEncoder.encode(qr_token, "UTF-8");

                    Log.d(TAG, "onActivityResult: " + preferences.getCurrentUser().getPremiseZoneId());


                    new CheckoutService().execute(preferences.getBaseURL().replace("visits", "residents") + "qr_checkin", urlParameters);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class CheckoutService extends AsyncTask<String, Void, String> {
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
                            MaterialDialog.SingleButtonCallback callback = new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            };


                            dialog = Constants.showDialog(CheckInGuest.this, "Notice", result_text, "OK", callback);
                            dialog.show();
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

    void showSuccess() {
        dialog.dismiss();
        dialog = new MaterialDialog.Builder(this)
                .title("CHECKED IN")
                .titleGravity(GravityEnum.CENTER)
                .customView(R.layout.success_dialog, true)
                .positiveText("OK")
                .negativeText("CANCEL")
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


}
