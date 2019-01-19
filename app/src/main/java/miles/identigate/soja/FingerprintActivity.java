package miles.identigate.soja;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.common.pos.api.util.posutil.PosUtil;
import com.example.ftransisdk.FrigerprintControl;
import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;

import android.util.Base64;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import miles.identigate.soja.Fragments.FingerprintRegistrationFragment;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.PremiseResident;

public class FingerprintActivity extends SojaActivity implements FingerprintRegistrationFragment.OnFragmentInteractionListener {

    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;

    public static final int REQUEST_WRITE_PERMISSION = 786;

    DatabaseHandler handler;

    ImageView fingerprint;
    TextView place_finger;
    TextView record_type;
    Button ok_button;
    Button submit_button;
    LinearLayout info;
    TextView name;
    TextView idNUmber;

    boolean isCheckout = false;

    byte[] scannedFingerprint = null;
    int scannedLen = 0;

    PremiseResident matchedPremiseResident = null;

    MaterialDialog progressDialog;
    MaterialDialog dialog;
    Preferences preferences;

    private static BioMiniFactory mBioMiniFactory = null;
    public IBioMiniDevice mCurrentDevice = null;

    private IBioMiniDevice.CaptureOption mCaptureOptionDefault = new IBioMiniDevice.CaptureOption();
    private CaptureResponder mCaptureResponseDefault = new CaptureResponder() {
        @Override
        public boolean onCaptureEx(final Object context, final Bitmap capturedImage,
                                   final IBioMiniDevice.TemplateData capturedTemplate,
                                   final IBioMiniDevice.FingerState fingerState) {

            Log.d(FingerprintActivity.class.getName(), "onCapture : Capture successful!");
            Log.d(FingerprintActivity.class.getName(), ((IBioMiniDevice)context).popPerformanceLog());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(capturedImage != null) {
                        if(fingerprint != null) {
                            fingerprint.setImageBitmap(capturedImage);
                        }
                    }

                    if (ok_button.getVisibility() != View.VISIBLE)
                        ok_button.setVisibility(View.VISIBLE);
                }
            });
            if (capturedTemplate != null) {
                scannedFingerprint = Arrays.copyOf(capturedTemplate.data, capturedTemplate.data.length);
                scannedLen = capturedTemplate.data.length;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        place_finger.setText("Fingerprint captured.");
                    }
                });
            }

            return true;
        }

        @Override
        public void onCaptureError(Object contest, int errorCode, final String error) {
            Log.d(FingerprintActivity.class.getName(), "onCaptureError : " + error);
            if( errorCode != IBioMiniDevice.ErrorCode.OK.value()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        place_finger.setText(getResources().getText(R.string.capture_single_fail) + "("+error+")");
                    }
                });
            }


        }
    };
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(ACTION_USB_PERMISSION.equals(action)){
                synchronized(this){
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        if(device != null){
                            if( mBioMiniFactory == null) return;
                            mBioMiniFactory.addDevice(device);
                            Log.d(FingerprintActivity.class.getName(),String.format(Locale.ENGLISH ,"Initialized device count- BioMiniFactory (%d)" , mBioMiniFactory.getDeviceCount() ));
                        }
                    }
                    else{
                        Log.d(FingerprintActivity.class.getName(),"permission denied for device"+ device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        preferences = new Preferences(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isCheckout = getIntent().getBooleanExtra("CHECKOUT", false);

        fingerprint = findViewById(R.id.fingerprint);
        place_finger = findViewById(R.id.place_finger);
        ok_button = findViewById(R.id.ok_button);
        submit_button = findViewById(R.id.submit_button);
        info = findViewById(R.id.info);
        name = findViewById(R.id.name);
        idNUmber = findViewById(R.id.idNUmber);
        record_type = findViewById(R.id.record_type);

        if (isCheckout) {
            record_type.setText("BIOMETRIC CHECKOUT");
            ok_button.setText("CHECK OUT");
            ok_button.setVisibility(View.VISIBLE);
        }

        handler = new DatabaseHandler(FingerprintActivity.this);

        mCaptureOptionDefault.captureImage = true;
        mCaptureOptionDefault.captureTemplate = true;
        mCaptureOptionDefault.captureTimeout = 0;

        mCaptureOptionDefault.frameRate = IBioMiniDevice.FrameRate.SHIGH;

        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Match fingerprint
                if (scannedFingerprint == null || scannedLen == 0){
                    Toast.makeText(FingerprintActivity.this, "No Fingerprint scanned", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = Constants.showProgressDialog(FingerprintActivity.this,"Check In", "Verifying fingerprint...");
                progressDialog.show();

                boolean isMatched = false;

                for (PremiseResident premiseResident :
                        handler.getPremiseResidents()) {
                    if (premiseResident.getFingerPrint() == null || premiseResident.getFingerPrint().isEmpty() || premiseResident.getFingerPrint().equals("null")){
                        continue;
                    }

                    byte[] decoded_data = Base64.decode(premiseResident.getFingerPrint(), Base64.DEFAULT);
                    if (mCurrentDevice.verify(scannedFingerprint, scannedLen, decoded_data, decoded_data.length)){
                        isMatched = true;
                        matchedPremiseResident = premiseResident;
                        break;
                    }
                }

                progressDialog.dismiss();

                if (isCheckout){
                    if (isMatched && matchedPremiseResident != null) {
                        Constants.showDialog(FingerprintActivity.this, matchedPremiseResident.getFirstName() + " " + matchedPremiseResident.getLastName(), "A match has been found for " + matchedPremiseResident.getFirstName() + " " + matchedPremiseResident.getLastName() + ". Tap CHECKOUT to check out", "CHECKOUT", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                                recordCheckout();

                            }
                        }).show();
                    } else {
                        Constants.showDialog(FingerprintActivity.this, "Match Not Found", "No user found with that fingerprint. Please try again", "OK", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
                    }
                }else {
                    if (isMatched && matchedPremiseResident != null) {
                        Constants.showDialog(FingerprintActivity.this, matchedPremiseResident.getFirstName() + " " + matchedPremiseResident.getLastName(), "A match has been found for " + matchedPremiseResident.getFirstName() + " " + matchedPremiseResident.getLastName() + ". Tap OK to record", "OK", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                recordCheckIn();
                            }
                        }).show();
                    } else {
                        Constants.showDialog(FingerprintActivity.this, "Match Not Found", "No user found with that fingerprint.", "REGISTER", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                Bundle args = new Bundle();
                                FingerprintRegistrationFragment fingerprintRegistrationFragment = FingerprintRegistrationFragment.newInstance();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                transaction.replace(android.R.id.content, fingerprintRegistrationFragment).addToBackStack(null).commit();
                            }
                        }).show();
                    }
                }

            }
        });
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ImageView) findViewById(R.id.fingerprint)).setImageBitmap(null);
                scannedFingerprint = null;
                scannedLen = 0;
                ok_button.setVisibility(View.GONE);
                place_finger.setText("Place finger on the fingerprint reader");

                if(mCurrentDevice != null) {
                    mCurrentDevice.captureSingle(
                            mCaptureOptionDefault,
                            mCaptureResponseDefault,
                            true);
                }
            }
        });
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        restartBioMini();

    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},  REQUEST_WRITE_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(FingerprintActivity.class.getName(),"permission granted");
        }
    }
    @Override
    public void onPostCreate(Bundle savedInstanceState){
        requestPermission();
        super.onPostCreate(savedInstanceState);
    }
    void restartBioMini() {
        if(mBioMiniFactory != null) {
            mBioMiniFactory.close();
        }
        if(!android.os.Build.MODEL.contains("Biowolf LE") || !android.os.Build.MODEL.contains("BioWolf 8n")) {
            PosUtil.setFingerPrintPower(1);

            FrigerprintControl.frigerprint_power_on();
        }

        if( mbUsbExternalUSBManager )
        {
            mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
            mBioMiniFactory = new BioMiniFactory(FingerprintActivity.this, mUsbManager){
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    Log.d(FingerprintActivity.class.getName(),"----------------------------------------");
                    Log.d(FingerprintActivity.class.getName(),"onDeviceChange : " + event + " using external usb-manager");
                    Log.d(FingerprintActivity.class.getName(),"----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
            //
            mPermissionIntent = PendingIntent.getBroadcast(this,0,new Intent(ACTION_USB_PERMISSION),0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);
            checkDevice();
        }
        else {
            mBioMiniFactory = new BioMiniFactory(FingerprintActivity.this) {
                @Override
                public void onDeviceChange(DeviceChangeEvent event, Object dev) {
                    Log.d(FingerprintActivity.class.getName(),"----------------------------------------");
                    Log.d(FingerprintActivity.class.getName(),"onDeviceChange : " + event);
                    Log.d(FingerprintActivity.class.getName(),"----------------------------------------");
                    handleDevChange(event, dev);
                }
            };
        }
    }
    void handleDevChange(IUsbEventHandler.DeviceChangeEvent event, Object dev) {
        if (event == IUsbEventHandler.DeviceChangeEvent.DEVICE_ATTACHED && mCurrentDevice == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    while (mBioMiniFactory == null && cnt < 20) {
                        SystemClock.sleep(1000);
                        cnt++;
                    }
                    if (mBioMiniFactory != null) {
                        mCurrentDevice = mBioMiniFactory.getDevice(0);
                        Log.d(FingerprintActivity.class.getName(), "mCurrentDevice attached : " + mCurrentDevice);
                        if (mCurrentDevice != null) {
                            Log.d(FingerprintActivity.class.getName()," DeviceName : " + mCurrentDevice.getDeviceInfo().deviceName);
                            Log.d(FingerprintActivity.class.getName(),"         SN : " + mCurrentDevice.getDeviceInfo().deviceSN);
                            Log.d(FingerprintActivity.class.getName(),"SDK version : " + mCurrentDevice.getDeviceInfo().versionSDK);
                        }
                    }
                }
            }).start();
        } else if (mCurrentDevice != null && event == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && mCurrentDevice.isEqual(dev)) {
            Log.d(FingerprintActivity.class.getName(), "mCurrentDevice removed : " + mCurrentDevice);
            mCurrentDevice = null;
        }
    }
    public void checkDevice(){
        if(mUsbManager == null) return;
        Log.d(FingerprintActivity.class.getName(),"checkDevice");
        HashMap<String , UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIter = deviceList.values().iterator();
        while(deviceIter.hasNext()){
            UsbDevice _device = deviceIter.next();
            if( _device.getVendorId() ==0x16d1 ){
                //Suprema vendor ID
                mUsbManager.requestPermission(_device , mPermissionIntent);
            }else{
            }
        }

    }
    @Override
    protected void onDestroy() {
        if (mBioMiniFactory != null) {
            mBioMiniFactory.close();
            mBioMiniFactory = null;
        }
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onFragmentInteraction(PremiseResident visitor) {
        //Store locally and Send to server

        final String fingerprintData = Base64.encodeToString(scannedFingerprint, Base64.DEFAULT);

        matchedPremiseResident = visitor;
        matchedPremiseResident.setFingerPrint(fingerprintData);
        matchedPremiseResident.setFingerPrintLen(scannedLen);
        handler.updatePremiseResident(matchedPremiseResident);

        info.setVisibility(View.VISIBLE);
        ok_button.setVisibility(View.VISIBLE);
        name.setText(visitor.getFirstName() + " " + visitor.getLastName());
        idNUmber.setText(visitor.getIdNumber());
        ok_button.setText("REGISTER");
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send registration data to server and check in automatically
                //Toast.makeText(FingerprintActivity.this, "Registered", Toast.LENGTH_SHORT).show();
                String urlParameters = null;
                try {
                    urlParameters = "id=" + URLEncoder.encode(matchedPremiseResident.getId(), "UTF-8")+
                            "&template=" + URLEncoder.encode(fingerprintData, "UTF-8") +
                            "&length=" + URLEncoder.encode(scannedLen+"", "UTF-8");
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                new RegisterFingerPrint().execute(preferences.getBaseURL() + "record_fingerprint", urlParameters);
            }
        });
    }

    private class RegisterFingerPrint extends AsyncTask<String, Void, String>{

        protected void onPreExecute(){
            progressDialog = Constants.showProgressDialog(FingerprintActivity.this,"Registering Fingerprint", "Registering fingerprint...");
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.executePost(strings[0], strings[1]);
        }
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if(result !=null){
                if(result.contains("result_code")) {
                    try {
                        Object json=new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject){
                            //
                            JSONObject obj = new JSONObject(result);
                            int resultCode = obj.getInt("result_code");
                            String resultText = obj.getString("result_text");
                            if (resultCode == 0 && resultText.equals("OK")) {
                                recordCheckIn();
                            }else{
                                new MaterialDialog.Builder(FingerprintActivity.this)
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
                        } else {
                            // Failed
                            new MaterialDialog.Builder(FingerprintActivity.this)
                                    .title("Unable to record fingerprint")
                                    .content("An error occurred while recording fingerprint. Please try again.")
                                    .positiveText("Ok")
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Failed
                    }
                }else {
                    //Failed
                    new MaterialDialog.Builder(FingerprintActivity.this)
                            .title("Unable to record fingerprint")
                            .content("An error occurred while recording fingerprint. Please try again.")
                            .positiveText("Ok")
                            .show();
                }

            }else{
                //Network issues
                new MaterialDialog.Builder(FingerprintActivity.this)
                        .title("Unable to record fingerprint")
                        .content("An error occurred while recording fingerprint. Please try again.")
                        .positiveText("Ok")
                        .show();
            }

        }
    }
    private void recordCheckIn(){
        String urlParameters = null;
        try {
            urlParameters = "peoplerecord_id=" + URLEncoder.encode(matchedPremiseResident.getId(), "UTF-8")+
                    "&host_id=" + URLEncoder.encode(matchedPremiseResident.getHostId(), "UTF-8") +
                    "&house_id=" + URLEncoder.encode(matchedPremiseResident.getHouse(), "UTF-8") +
                    "&premise_zone_id=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                    "&device_id=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        new ExpressCheckIn().execute(preferences.getBaseURL() + "express_checkin", urlParameters);
    }
    private void  recordCheckout(){
        String urlParameters = null;
        try {
            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                    "&fp_checkout=" + URLEncoder.encode("1", "UTF-8") +
                    "&peoplerecord_id=" + URLEncoder.encode(matchedPremiseResident.getId(), "UTF-8") +
                    "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new ExitAsync().execute(preferences.getBaseURL() + "express_checkout", urlParameters);
    }
    private class  ExpressCheckIn extends AsyncTask<String, Void, String>{
        protected void onPreExecute(){
            progressDialog = Constants.showProgressDialog(FingerprintActivity.this,"Check in", "Checking in resident....");
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.executePost(strings[0], strings[1]);
        }
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                JSONObject obj = new JSONObject(result);
                int resultCode = obj.getInt("result_code");
                String resultText = obj.getString("result_text");
                String resultContent = obj.getString("result_content");
                if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
                    //Successful check in
                    new MaterialDialog.Builder(FingerprintActivity.this)
                            .title("SUCCESS")
                            .content("Visitor recorded successfully.")
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
                    if (resultText.contains("still in")) {
                        new MaterialDialog.Builder(FingerprintActivity.this)
                                .title("Soja")
                                .content(resultText)
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
                                        //TODO: check out
                                        recordCheckout();
                                    }
                                })
                                .show();
                    } else {
                        new MaterialDialog.Builder(FingerprintActivity.this)
                                .title("Soja")
                                .content(result)
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
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
    }
    private class ExitAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder=new MaterialDialog.Builder(FingerprintActivity.this)
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
            return NetworkHandler.executePost(params[0], params[1]);
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
                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            recordCheckIn();
                        } else {
                            new MaterialDialog.Builder(FingerprintActivity.this)
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
                        new MaterialDialog.Builder(FingerprintActivity.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
                new MaterialDialog.Builder(FingerprintActivity.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }

}
