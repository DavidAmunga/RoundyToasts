package miles.identigate.soja.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import miles.identigate.soja.R;
import miles.identigate.soja.activities.CheckInGuest;
import miles.identigate.soja.adapters.ServiceAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.services.DataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanEventTicket extends Fragment {
    private static final String TAG = "ScanEventTicket";
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
    Unbinder unbinder;

    private BeepManager beepManager;
    DataService mResidentsService;
    DataService mEventsService;
    String lastText;
    Animation scale_up;


    Preferences preferences;

    ServiceAdapter serviceAdapter;
    String eventID, eventName, serviceID, serviceName;

    Boolean direct = false;


    public ScanEventTicket() {
        // Required empty public constructor
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
//            barcodeView.setStatusText(result.getText());


            if (!direct) {
                checkInTicketManagedService(result.getText());
            } else {
                checkInTicket(result.getText());

            }

//            Toast.makeText(ScanTicket.this, "QR is " + result.getText(), Toast.LENGTH_SHORT).show();

            beepManager.playBeepSoundAndVibrate();

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_event_ticket, container, false);
        unbinder = ButterKnife.bind(this, view);

        preferences = new Preferences(getActivity());

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        qrScanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        qrScanner.initializeFromIntent(getActivity().getIntent());
        qrScanner.decodeContinuous(callback);

        beepManager = new BeepManager(getActivity());
        qrScanner.setStatusText("");

        Bundle bundle = getArguments();


        if (bundle.getString("eventID") != null) {
            eventID = bundle.getString("eventID");
            eventName = bundle.getString("eventName");
            serviceID = bundle.getString("serviceID");
            serviceName = bundle.getString("serviceName");

            ((CheckInGuest) getActivity()).getSupportActionBar().setTitle("Scan " + serviceName + " Ticket");
        }

        triggerScan();

        if (bundle.getBoolean("direct")) {
            direct = bundle.getBoolean("direct");
        }


        optionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerScan();
            }
        });

        mResidentsService = Common.getResidentsDataService(getActivity());
        mEventsService = Common.getEventsDataService(getActivity());


        if (preferences.isDarkModeOn()) {
            qrScanner.setTorchOn();
        }


        return view;
    }

    private void checkInTicketManagedService(String text) {
        changeUIState(Common.STATE_LOADING, "Checking In");

        mEventsService.checkInQRManagedService(
                preferences.getCurrentUser().getDeviceId(),
                preferences.getCurrentUser().getPremiseZoneId(),
                text,
                serviceID,
                eventID
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().toString());
                        int result_code = object.getInt("result_code");
                        String result_text = object.getString("result_text");

                        if (result_code == 0) {

                            changeUIState(Common.STATE_SUCCESS, "Success. Ticket Checked In");
                        } else {
                            if (result_text.contains("still in")) {
                                changeUIState(Common.STATE_INFO, "Ticket already used");

                                checkOutTicket();
                            } else {
                                changeUIState(Common.STATE_INFO, result_text);

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    Log.d(TAG, "onResponse: " + response.toString());
                    Log.d(TAG, "onResponse: " + "No Internet Connection");
                    changeUIState(Common.STATE_FAILURE, "There was a problem");

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                changeUIState(Common.STATE_FAILURE, "No Internet Connection");


            }
        });

    }


    private void checkInTicket(String text) {
        changeUIState(Common.STATE_LOADING, "Checking In");

        mResidentsService.checkInQR(
                preferences.getCurrentUser().getDeviceId(),
                preferences.getCurrentUser().getPremiseZoneId(),
                Constants.getCurrentTimeStamp(),
                text
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().toString());
                        int result_code = object.getInt("result_code");
                        String result_text = object.getString("result_text");

                        if (result_code == 0) {
                            changeUIState(Common.STATE_SUCCESS, "Success. Ticket Checked In");
                        } else {
                            if (result_text.contains("still in")) {
                                changeUIState(Common.STATE_INFO, "Ticket already used");

                                checkOutTicket();
                            } else {
                                changeUIState(Common.STATE_INFO, result_text);

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    Log.d(TAG, "onResponse: " + response.toString());
                    Log.d(TAG, "onResponse: " + "No Internet Connection");
                    changeUIState(Common.STATE_FAILURE, "There was a problem");

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                changeUIState(Common.STATE_FAILURE, "No Internet Connection");


            }
        });

    }


    private void checkOutTicket() {
        changeUIState(Common.STATE_LOADING, "Checking Out");

        mResidentsService.checkOutQR(
                preferences.getCurrentUser().getDeviceId(),
                preferences.getCurrentUser().getPremiseZoneId(),
                Constants.getCurrentTimeStamp(),
                lastText
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().toString());
                        int result_code = object.getInt("result_code");
                        String resultText = object.getString("result_text");
                        String resultContent = object.getString("result_content");


                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            checkInTicket(lastText);
                        } else {
                            changeUIState(Common.STATE_INFO, resultText);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {

                    changeUIState(Common.STATE_FAILURE, "There was a problem");

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                changeUIState(Common.STATE_FAILURE, "No Internet Connection");


            }
        });
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

                infoHelp.setText("Place your ticket within the square to scan");
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
        lastText = "";
        qrScanner.decodeSingle(callback);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return qrScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
