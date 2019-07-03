package miles.identigate.soja;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;

public class ExpressCheckoutActivity extends AppCompatActivity {
    String visit_id;
    MaterialDialog dialog;
    Preferences preferences;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "ExpressCheckoutActivity";


    String option = "";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_title)
    TextViewBold toolbarTitle;

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

    String lastText;
    Animation scale_up;

    String[] filterItems = {"Visitor", "Resident"};


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
//            barcodeView.setStatusText(result.getText());

            checkOut(lastText);


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
        setContentView(R.layout.activity_express_checkout);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        qrScanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        qrScanner.initializeFromIntent(getIntent());
        qrScanner.decodeContinuous(callback);

        beepManager = new BeepManager(this);
        qrScanner.setStatusText("");


        toolbarTitle.setText("Express Checkout");


        optionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastText = "";
                triggerScan();
            }
        });


        if (preferences.isDarkModeOn()) {
            qrScanner.setTorchOn();
        }

    }


    private void checkOut(String qr_code) {

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

            changeUIState(Common.STATE_LOADING, "Checking Out");


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
                        JSONObject resultContent = obj.optJSONObject("result_content");

                        if (resultText.equals("OK") && resultCode == 0) {

                            String name = resultContent.getString("name");


                            changeUIState(Common.STATE_SUCCESS, name + " checked Out");

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
