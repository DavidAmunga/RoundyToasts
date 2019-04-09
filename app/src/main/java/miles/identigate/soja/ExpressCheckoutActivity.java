package miles.identigate.soja;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
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

import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.ZxingHelperActivity;

public class ExpressCheckoutActivity extends AppCompatActivity {
    String visit_id;
    MaterialDialog dialog;
    Preferences preferences;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);

        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_express_checkout);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("Express Checkout");
        ImageView scan_icon = findViewById(R.id.scan_icon);
        scan_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ExpressCheckoutActivity.this).setCaptureActivity(ZxingHelperActivity.class).initiateScan();
            }
        });
//        new IntentIntegrator(ExpressCheckoutActivity.this).setCaptureActivity(ZxingHelperActivity.class).initiateScan();
        dialog = new MaterialDialog.Builder(ExpressCheckoutActivity.this)
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
                visit_id = result.getContents();


                String urlParameters = null;
                try {
                    urlParameters = "idNumber=" + URLEncoder.encode(visit_id, "UTF-8") +
                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                            "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");

                    new CheckoutService().execute(preferences.getBaseURL() + "record-visitor-exit", urlParameters);
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


                            dialog = Constants.showDialog(ExpressCheckoutActivity.this, "Attention", "Visitor Already Checked Out or Unknown Visitor", "OK", callback);
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
                .title("CHECKED OUT")
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
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        finish();
                    }
                })
                .build();
        View view = dialog.getCustomView();
        TextView messageText = view.findViewById(R.id.message);
        messageText.setText("Visitor successfully checked out.");
        dialog.show();
    }
}
