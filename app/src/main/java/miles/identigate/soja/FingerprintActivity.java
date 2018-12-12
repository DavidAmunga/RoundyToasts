package miles.identigate.soja;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import miles.identigate.soja.Fragments.FingerprintRegistrationFragment;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.Visitor;

public class FingerprintActivity extends SojaActivity implements FingerprintRegistrationFragment.OnFragmentInteractionListener {

    public static final boolean mbUsbExternalUSBManager = false;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager mUsbManager = null;
    private PendingIntent mPermissionIntent= null;

    public static final int REQUEST_WRITE_PERMISSION = 786;

    ImageView fingerprint;
    TextView place_finger;
    Button ok_button;
    Button submit_button;
    LinearLayout info;
    TextView name;
    TextView idNUmber;

    byte[] scannedFingerprint = null;
    int scannedLen = 0;

    Visitor matchedVisitor = null;

    MaterialDialog progressDialog;
    MaterialDialog dialog;

    private static BioMiniFactory mBioMiniFactory = null;
    public IBioMiniDevice mCurrentDevice = null;

    private ArrayList<Visitor> visitors = new ArrayList<>();
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
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fingerprint = (ImageView)findViewById(R.id.fingerprint);
        place_finger = (TextView)findViewById(R.id.place_finger);
        ok_button = (Button)findViewById(R.id.ok_button);
        submit_button = (Button)findViewById(R.id.submit_button);
        info = (LinearLayout)findViewById(R.id.info);
        name = (TextView)findViewById(R.id.name);
        idNUmber = (TextView)findViewById(R.id.idNUmber);

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

                for (Visitor visitor :
                        visitors) {
                    if (mCurrentDevice.verify(scannedFingerprint, scannedLen, visitor.getFingerprint(), visitor.getFingerprint().length)){
                        isMatched = true;
                        matchedVisitor = visitor;
                        break;
                    }
                }

                dialog.dismiss();

                if (isMatched && matchedVisitor != null){
                    Constants.showDialog(FingerprintActivity.this, "Match Found", "A match has been found for " + matchedVisitor.getName() + ". Tap OK to record", "OK", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            //TODO: Check in on server
                        }
                    });
                }else{
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
                    });
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onFragmentInteraction(Visitor visitor) {
        //TODO: Update UI
        //Send to server
        matchedVisitor = visitor;
        info.setVisibility(View.VISIBLE);
        name.setText(visitor.getName());
        idNUmber.setText(visitor.getNational_id());
        ok_button.setText("SUBMIT");
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Send register to server and check in automatically
                Toast.makeText(FingerprintActivity.this, "Registered", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
