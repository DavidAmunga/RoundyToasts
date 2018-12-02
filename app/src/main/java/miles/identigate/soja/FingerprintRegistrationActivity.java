package miles.identigate.soja;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suprema.BioMiniFactory;
import com.suprema.CaptureResponder;
import com.suprema.IBioMiniDevice;
import com.suprema.IUsbEventHandler;

import java.util.ArrayList;
import java.util.Arrays;

import miles.identigate.soja.Fragments.FingerprintRegistrationFragment;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.Visitor;

public class FingerprintRegistrationActivity extends SojaActivity implements FingerprintRegistrationFragment.OnFragmentInteractionListener {
    ImageView fingerprint;
    TextView place_finger;
    Button ok_button;
    LinearLayout info;
    TextView name;
    TextView idNUmber;

    private static BioMiniFactory mBioMiniFactory = null;
    public IBioMiniDevice mCurrentDevice = null;
    class UserData {
        String name;
        byte[] template;
        public UserData(String name, byte[] data, int len) {
            this.name = name;
            this.template = Arrays.copyOf(data, len);
        }
    }
    private ArrayList<UserData> mUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fingerprint = (ImageView)findViewById(R.id.fingerprint);
        place_finger = (TextView)findViewById(R.id.place_finger);
        ok_button = (Button)findViewById(R.id.ok_button);
        info = (LinearLayout)findViewById(R.id.info);
        name = (TextView)findViewById(R.id.name);
        idNUmber = (TextView)findViewById(R.id.idNUmber);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Check fingerprint and show search dialog

                FragmentManager fragmentManager = getSupportFragmentManager();
                Bundle args = new Bundle();
                FingerprintRegistrationFragment fingerprintRegistrationFragment = FingerprintRegistrationFragment.newInstance();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(android.R.id.content, fingerprintRegistrationFragment).addToBackStack(null).commit();
            }
        });
        // allocate SDK instance
        mBioMiniFactory = new BioMiniFactory(FingerprintRegistrationActivity.this) {
            @Override
            public void onDeviceChange(DeviceChangeEvent deviceChangeEvent, Object o) {
                if (deviceChangeEvent == DeviceChangeEvent.DEVICE_ATTACHED && mCurrentDevice == null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBioMiniFactory != null){
                                mCurrentDevice = mBioMiniFactory.getDevice(0);
                                if (mCurrentDevice != null) {
                                    Log.d(" DeviceName  " , mCurrentDevice.getDeviceInfo().deviceName);
                                    Log.d("         SN  " , mCurrentDevice.getDeviceInfo().deviceSN);
                                    Log.d("SDK version " , mCurrentDevice.getDeviceInfo().versionSDK);
                                    capture();
                                }
                            }
                        }
                    }).start();
                } else if (mCurrentDevice != null && deviceChangeEvent == IUsbEventHandler.DeviceChangeEvent.DEVICE_DETACHED && mCurrentDevice.isEqual(o)) {
                    Log.d("Soja", "mCurrentDevice removed : " + mCurrentDevice);
                    mCurrentDevice = null;
                }
            }
        };

    }
    private  void capture(){
        fingerprint.setImageBitmap(null);
        IBioMiniDevice.CaptureOption option = new IBioMiniDevice.CaptureOption();
        option.captureTemplate = true;
        //Capture fingerprint
        mCurrentDevice.captureSingle(option, new CaptureResponder() {
            @Override
            public boolean onCaptureEx(Object o,final Bitmap capturedImage, IBioMiniDevice.TemplateData capturedTemplate, IBioMiniDevice.FingerState fingerState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(capturedImage != null) {
                            if(fingerprint != null) {
                                fingerprint.setImageBitmap(capturedImage);
                            }

                        }
                    }
                });
                if(capturedTemplate != null) {
                    mUsers.add(new UserData(" ", capturedTemplate.data,
                            capturedTemplate.data.length));
                    Log.d("Soja", "User captured");
                    ok_button.setVisibility(View.VISIBLE);
                }
                return super.onCaptureEx(o, capturedImage, capturedTemplate, fingerState);
            }
            @Override
            public void onCaptureError(Object context, int errorCode, String error) {
                Log.d("onCaptureError" ,error);
                //TODO: show error message
            }
        }, true);

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
        info.setVisibility(View.VISIBLE);
        name.setText(visitor.getName());
        idNUmber.setText(visitor.getNational_id());
        ok_button.setText("SUBMIT");
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Send to server to store
                Toast.makeText(FingerprintRegistrationActivity.this, "Registered", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
