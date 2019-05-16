package miles.identigate.soja.UserInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.hbb20.CountryCodePicker;
import com.regula.documentreader.api.enums.eVisualFieldType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.SlipActivity;
import miles.identigate.soja.SmsCheckInActivity;
import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.EditTextRegular;
import miles.identigate.soja.font.OpenSansBold;
import miles.identigate.soja.helpers.CheckConnection;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.TypeObject;
import miles.identigate.soja.services.IFCMService;

public class RecordWalkIn extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    IFCMService fcmService;

    private static final String TAG = "RecordWalkIn";
    Spinner visitor_type;
    DatabaseHandler handler;
    TypeObject selectedDestination, selectedHost, selectedType, selectedGender, selectedDocument;
    ArrayList<TypeObject> houses, visitorTypes, hosts, genderTypes, documentTypes;
    Preferences preferences;


    String firstName, lastName, idNumber;

    String entity_name = "destination";
    String entity_owner = "visitor";

    String nationality, nationalityCode;


    boolean manualEdit = false;


    String result_slip = "";
    int visit_id = 0;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.phoneNumberEdittext)
    EditTextRegular phoneNumberEdittext;
    @BindView(R.id.btnConfirm)
    Button btnConfirm;
    @BindView(R.id.phone_verification_layout)
    LinearLayout phoneVerificationLayout;
    @BindView(R.id.typeLabel2)
    OpenSansBold typeLabel2;
    @BindView(R.id.edtCode)
    EditTextRegular edtCode;
    @BindView(R.id.lin_verification_code)
    LinearLayout linVerificationCode;
    @BindView(R.id.typeLabel)
    OpenSansBold typeLabel;
    @BindView(R.id.visitor_type)
    Spinner visitorType;
    @BindView(R.id.hostLabel)
    OpenSansBold hostLabel;
    @BindView(R.id.spinnerDestination)
    OpenSansBold spinnerDestination;
    @BindView(R.id.spinnerHost)
    OpenSansBold spinnerHost;
    @BindView(R.id.hostLayout)
    LinearLayout hostLayout;
    @BindView(R.id.ccp)
    CountryCodePicker ccp;
    @BindView(R.id.countryLayout)
    LinearLayout countryLayout;
    @BindView(R.id.companyNameEdittext)
    EditTextRegular companyNameEdittext;
    @BindView(R.id.companyNameLayout)
    LinearLayout companyNameLayout;
    @BindView(R.id.iprn_profile)
    LinearLayout iprnProfile;
    @BindView(R.id.record)
    Button record;
    @BindView(R.id.car_profile)
    LinearLayout carProfile;
    @BindView(R.id.lin_fields)
    RelativeLayout linFields;

    private IntentFilter receiveFilter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.RECORDED_VISITOR)) {
                finish();
            } else if (action.equals(Constants.LOGOUT_BROADCAST)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_record_walk_in);
        ButterKnife.bind(this);
        if (Constants.documentReaderResults == null)
            finish();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Record Walk In");

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);

        handler = new DatabaseHandler(this);

        fcmService = Common.getFCMService();

        visitor_type = findViewById(R.id.visitor_type);
        record = findViewById(R.id.record);
        spinnerDestination = findViewById(R.id.spinnerDestination);


        if (preferences.isPhoneNumberEnabled() && preferences.isPhoneVerificationEnabled()) {
            linFields.setVisibility(View.GONE);
            phoneVerificationLayout.setVisibility(View.VISIBLE);
            linVerificationCode.setVisibility(View.VISIBLE);
        } else {
            linFields.setVisibility(View.VISIBLE);
            phoneVerificationLayout.setVisibility(View.GONE);
            linVerificationCode.setVisibility(View.GONE);

        }


        companyNameLayout.setVisibility(preferences.isCompanyNameEnabled() ? View.VISIBLE : View.GONE);
        hostLayout.setVisibility(preferences.isSelectHostsEnabled() ? View.VISIBLE : View.GONE);

        hostLabel.setText(entity_name.toUpperCase());
//        visitorLabel.setText(entity_owner.toUpperCase());

        spinnerDestination.setText("Select " + entity_name);

        if (!preferences.getBaseURL().contains("casuals")) {
            houses = handler.getTypes("houses", null);
        } else {
            Log.d(TAG, "onCreate: Casuals");
            try {
                houses = new FetchHouseDetails().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


//        Check if manual
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                if (bundle.getBoolean("manual")) {
                    manualEdit = true;
                }
            }

        }


        Log.d(TAG, "Houses: " + houses);
        visitorTypes = handler.getTypes("visitors", null);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDestination == null) {
                    Toast.makeText(RecordWalkIn.this, "Select a " + entity_name, Toast.LENGTH_SHORT).show();
                } else {
                    if (CheckConnection.check(RecordWalkIn.this)) {
                        recordInternet();
                    } else {
                        recordOffline();
                    }
                }

            }
        });
        Log.d(TAG, "onCreate: " + visitorTypes.size());
        TypeAdapter adapter = new TypeAdapter(RecordWalkIn.this, R.layout.tv, visitorTypes);


        visitor_type.setAdapter(adapter);
        visitor_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedType = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        if (preferences.getBaseURL().contains("casuals")) {
            visitor_type.setSelection(1);
            visitor_type.setSelected(true);
//            visitor_type.setEnabled(false);

            entity_name = "event";
            entity_owner = "Employee";

            hostLabel.setText(entity_name.toUpperCase());
            spinnerDestination.setText("Select " + entity_name);
//            typeLabel.setText(entity_owner.toUpperCase());
        }


//        DESTINATION
        spinnerDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                new SimpleSearchDialogCompat(RecordWalkIn.this,
                        "Search for " + entity_name, "What " + entity_name + " is the " + entity_owner + " heading...?", null, houses, new SearchResultListener<TypeObject>() {
                    @Override
                    public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                        TypeObject object = o;
                        selectedDestination = object;

                        Log.d(TAG, "Selected Destination: " + selectedDestination.getId());

                        hosts = handler.getTypes("hosts", selectedDestination.getId());


                        hostLayout.setVisibility(preferences.isSelectHostsEnabled() && hosts.size() > 0 ? View.VISIBLE : View.GONE);

                        if (hosts.size() == 0) {
                            selectedHost = null;
                        }

                        Log.d(TAG, "Host ID " + selectedDestination.getId());
                        Log.d(TAG, "Hosts: " + hosts);


//                        Toast.makeText(RecordWalkIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                        spinnerDestination.setText(o.getName());
                        baseSearchDialogCompat.dismiss();

                    }
                }).show();
            }
        });

//        HOSTS
        spinnerHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedDestination == null) {
                    Toast.makeText(RecordWalkIn.this, "Select Destination First", Toast.LENGTH_SHORT).show();
                } else {
                    new SimpleSearchDialogCompat(RecordWalkIn.this,
                            "Search for Host...", "Who is the visitor seeing in " + selectedDestination.getName(), null, hosts, new SearchResultListener<TypeObject>() {
                        @Override
                        public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                            TypeObject object = o;
                            selectedHost = object;

//                            Toast.makeText(RecordWalkIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                            spinnerHost.setText(o.getName());
                            baseSearchDialogCompat.dismiss();

                        }
                    }).show();
                }
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendConfirmationCode();

            }
        });
    }


    //    Send Verification Code
    public void sendConfirmationCode() {
        String urlParameters = null;
        String phoneNo = phoneNumberEdittext.getText().toString();
        if (!phoneNo.equals("")) {
            try {
                urlParameters =
                        "phone=" + URLEncoder.encode(phoneNo, "UTF-8") +
                                "&organisationID=" + URLEncoder.encode(preferences.getOrganizationId(), "UTF-8")
                ;

                Log.d(TAG, "sendConfirmationCode: " + urlParameters);


                new SMSCheckInAsync().execute(preferences.getBaseURL() + "send_code", urlParameters);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }


    //    Record Check In Only
    public void recordCheckIn() {
        String urlParameters = null;
        try {
            String idN = "000000000";
            String scan_id_type = "ID";
            String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");

            Log.d(TAG, "recordInternet: " + Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"));

            if (classCode.equals("ID")) {
                Log.d(TAG, "recordInternet: ID");
                scan_id_type = "ID";

                if (Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER) == null) {
                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                } else {
                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");

                }
                idNumber = idN.substring(2, idN.length() - 1);

            } else if (classCode.equals("P")) {
                Log.d(TAG, "recordInternet: Passport");

                scan_id_type = "P";
                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                idNumber = idN;
            } else if (classCode.equals("PA")) {
                Log.d(TAG, "recordInternet: Passport");

                scan_id_type = "P";
                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                idNumber = idN;
            } else if (classCode.equals("AC")) {
                Log.d(TAG, "Class Code : " + classCode);
//                TODO: Standardize Alien ID
                Log.d(TAG, "recordInternet: Alien Id");
                scan_id_type = "AID";

                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_LINE_2_OPTIONAL_DATA).replace("^", "\n");
                idNumber = idN.substring(2, idN.length() - 1);
                Log.d(TAG, "recordInternet: ID" + idNumber);


            }


            firstName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");
            lastName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");

            Log.d(TAG, "recordInternet: " + firstName + " " + lastName);

            String gender = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n").contains("M") ? "0" : "1";
            String mrzLines = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);


            Log.d(TAG, "recordInternet: " + selectedDestination.getId());

            urlParameters =
                    "mrz=" + URLEncoder.encode(mrzLines, "UTF-8") +
                            "&phone=" + URLEncoder.encode(phoneNumberEdittext.getText().toString(), "UTF-8") +
                            (preferences.isCompanyNameEnabled() && !companyNameEdittext.getText().toString().equals("") ?
                                    ("&company=" + URLEncoder.encode(companyNameEdittext.getText().toString(), "UTF-8")) : "") +
                            "&scan_id_type=" + URLEncoder.encode(scan_id_type, "UTF-8") +
                            "&visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                            "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                            "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8") +
                            (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(selectedHost.getHostId())) : "") +
                            "&houseID=" + URLEncoder.encode(selectedDestination.getId(), "UTF-8") +
                            "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                            "&birthDate=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"), "UTF-8") +
                            "&genderID=" + URLEncoder.encode(gender, "UTF-8") +
                            "&firstName=" + URLEncoder.encode(firstName, "UTF-8") +
                            "&lastName=" + URLEncoder.encode(lastName, "UTF-8") +
                            "&idType=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"), "UTF-8") +
                            "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8") +
                            "&nationality=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n"), "UTF-8") +
                            "&nationCode=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n"), "UTF-8");

            Log.d(TAG, "recordInternet: " + preferences.getBaseURL() + "record-visit/" + urlParameters);

            new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    //    Record CheckIn with Base URL changes
    public void recordInternet() {
        String urlParameters = null;
        try {
            String idN = "000000000";
            String scan_id_type = "ID";
            String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");

//            Log.d(TAG, "recordInternet: Document Number" + Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"));
//            Log.d(TAG, "recordInternet: Class Code" + classCode);


            if (classCode.equals("ID")) {
                Log.d(TAG, "recordInternet: ID");
                scan_id_type = "ID";
                if (Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER) == null) {
                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                } else {
                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");

                }
                idNumber = idN.substring(2, idN.length() - 1);

            } else if (classCode.equals("P")) {
                Log.d(TAG, "recordInternet: Passport");

                scan_id_type = "P";
                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                idNumber = idN;
            } else if (classCode.equals("PA")) {
                Log.d(TAG, "recordInternet: Passport");

                scan_id_type = "P";
                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                idNumber = idN;
            } else if (classCode.equals("AC")) {
                Log.d(TAG, "Class Code : " + classCode);
//                TODO: Standardize Alien ID
                Log.d(TAG, "recordInternet: Alien Id");
                scan_id_type = "AID";

                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_LINE_2_OPTIONAL_DATA).replace("^", "\n");
                idNumber = idN.substring(2, idN.length() - 1);
                Log.d(TAG, "recordInternet: ID" + idNumber);


            }


            firstName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");
            lastName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");

            Log.d(TAG, "recordInternet: " + firstName + " " + lastName);

            String gender = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n").contains("M") ? "0" : "1";
            String mrzLines = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);


            Log.d(TAG, "recordInternet: " + selectedDestination.getId());

            urlParameters =
                    "mrz=" + URLEncoder.encode(mrzLines, "UTF-8") +
                            "&phone=" + URLEncoder.encode(phoneNumberEdittext.getText().toString(), "UTF-8") +
                            (preferences.isCompanyNameEnabled() && !companyNameEdittext.getText().toString().equals("") ?
                                    ("&company=" + URLEncoder.encode(companyNameEdittext.getText().toString(), "UTF-8")) : "") +
                            (preferences.isPhoneNumberEnabled() && !edtCode.getText().toString().equals("") ?
                                    ("&code=" + URLEncoder.encode(edtCode.getText().toString(), "UTF-8")) : "") +
                            "&scan_id_type=" + URLEncoder.encode(scan_id_type, "UTF-8") +
                            "&visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                            "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                            "&visitorTypeID=" + URLEncoder.encode(selectedType.getId(), "UTF-8") +
                            (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(selectedHost.getHostId())) : "") +
                            "&houseID=" + URLEncoder.encode(selectedDestination.getId(), "UTF-8") +
                            "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                            "&birthDate=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"), "UTF-8") +
                            "&genderID=" + URLEncoder.encode(gender, "UTF-8") +
                            "&firstName=" + URLEncoder.encode(firstName, "UTF-8") +
                            "&lastName=" + URLEncoder.encode(lastName, "UTF-8") +
                            "&idType=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"), "UTF-8") +
                            "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8") +
                            "&nationality=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n"), "UTF-8") +
                            "&nationCode=" + URLEncoder.encode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n"), "UTF-8");

            Log.d(TAG, "recordInternet: " + preferences.getBaseURL() + "record-visit/" + urlParameters);
            if (preferences.getBaseURL().contains("casuals")) {
                checkIfLocationClose(selectedDestination);
//                new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);

            } else {
                new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void checkIfLocationClose(TypeObject selectedDestination) {
        String urlParameters = null;

        urlParameters = "houseID=" + selectedDestination.getId();


        new CheckLocationAsync().execute(preferences.getBaseURL() + "getHouseLocation", urlParameters);


    }

    public void recordOffline() {
        //Insert to local database
        String idN = "000000000";
        String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
        if (classCode.equals("ID")) {
            if (Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER) == null) {
                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
            } else {
                idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");

            }
        } else if (classCode.equals("P")) {
            idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
        } else if (classCode.equals("AC")) {
            idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_LINE_2_OPTIONAL_DATA).replace("^", "\n");


        }
        DriveIn driveIn = new DriveIn();
        driveIn.setVisitorType(selectedType.getId());
        driveIn.setCarNumber("NULL");
        driveIn.setImage("NULL");
        driveIn.setRecordType("WALK");
        driveIn.setEntryTime(Constants.getCurrentTimeStamp());
        driveIn.setStatus("IN");
        driveIn.setExitTime("NULL");
        driveIn.setHouseID(selectedDestination.getId());
        driveIn.setNationalId(idN);
        driveIn.setDob(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"));
        driveIn.setSex(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n"));
        driveIn.setName(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n"));
        driveIn.setOtherNames(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n"));
        driveIn.setIdType(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"));


        if (CheckConnection.check(this)) {
            driveIn.setSynced(true);
            handler.insertDriveIn(driveIn);
            return;
        } else {
            driveIn.setSynced(false);
            handler.insertDriveIn(driveIn);
            new MaterialDialog.Builder(RecordWalkIn.this)
                    .title("SUCCESS")
                    .content("Recorded successfully. Do you want to record another visit?")
                    .positiveText("YES")
                    .negativeText("NO")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                            Intent intent = new Intent(RecordWalkIn.this, ScanActivity.class);
                            Bundle extras = new Bundle();
                            extras.putInt("TargetActivity", Common.WALK_IN);
                            intent.putExtras(extras);
                            startActivity(intent);
                            finish();

                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            finish();
                        }
                    })
                    .show();

        }
    }

    private void pushNotificationToHost() {
//        Log.d(TAG, "pushNotificationToHost: Hre" + String.valueOf(selectedHost.getHostId()));
//        FirebaseDatabase.getInstance().getReference(Common.TOKENS).child("resident_" + selectedHost.getHostId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Token token = dataSnapshot.getValue(Token.class);
//
//                Log.d(TAG, "onDataChange: Changed");
//
//                if (token != null && token.getToken() != null) {
//                    String tokenResident = token.getToken();
//
//                    Log.d(TAG, "onDataChange: Changed" + tokenResident);
//
//                    Notification data = new Notification("Visitor Arrived", "Your visitor " + firstName + " has arrived", "All");
////                    Send to Resident app and we will deserialize it again
//                    Sender content = new Sender(data, tokenResident);
//
//                    fcmService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
//                        @Override
//                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                            if (response.body().success == 1) {
////                                Toast.makeText(RecordWalkIn.this, "Notification Sent to Resident", Toast.LENGTH_SHORT).show();
//
//                            } else {
////                                Log.d(TAG, "onResponse: " + response.toString()
////                                );
//
////                                Toast.makeText(RecordWalkIn.this, "Failed !", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<FCMResponse> call, Throwable t) {
//                            Log.e(TAG, "onFailure: Failed", t);
//                        }
//                    });
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private class CheckLocationAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(RecordWalkIn.this)
                .title("Please Wait")
                .content("Confirming Location...")
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
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            builder.dismiss();

            if (result != null) {
                if (result.contains("result_code")) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        JSONObject resultContent = obj.getJSONObject("result_content");
                        if (resultCode == 0 && resultText.equals("OK")) {

                            Double latitude = resultContent.getDouble("latitude");
                            Double longitude = resultContent.getDouble("longitude");

                            Log.d(TAG, "onPostExecute: " + latitude + "," + longitude);
                            if (Constants.mLastLocation != null) {
                                Log.d(TAG, "onPostExecute: My Location " + Constants.mLastLocation.getLatitude() + "," + Constants.mLastLocation.getLongitude());
                            }

                            if (compareLocations(new LatLng(latitude, longitude))) {

                                recordCheckIn();

                            } else {
                                builder.dismiss();
                                MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                };

                                Constants.showDialog(RecordWalkIn.this, "Failed", " You are not within the specified event location", "OK", singleButtonCallback).show();
                            }

                        } else {
                            MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            };

                            Constants.showDialog(RecordWalkIn.this, "Failed", " Sorry, Failed. Please turn on you internet connection", "OK", singleButtonCallback).show();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onPostExecute: Record Offline" + result);
                    recordOffline();
                }
                super.onPostExecute(result);

            }
        }
    }

    private boolean compareLocations(LatLng eventLocation) {
        float[] dist = new float[1];

        if (Constants.mLastLocation != null) {
            Location.distanceBetween(Constants.mLastLocation.getLatitude(), Constants.mLastLocation.getLongitude(), eventLocation.latitude, eventLocation.longitude, dist);

            if (dist[0] / 1000 <= 1) {
                return true;
            }
        }

        return false;

    }

    private class DriveinAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(RecordWalkIn.this)
                .title("Walk in")
                .content("Checking In...")
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

        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {
            builder.dismiss();
            Log.d(TAG, "onPostExecute:  Result" + result);
            if (result != null) {

                if (result.contains("result_code")) {
                    try {
                        Object json = new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject) {

                            resultHandler(result);
                        } else {
                            //TODO remove this.Temporary workaround
                            recordOffline();
                            parseResult();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onPostExecute: Record Offline" + result);
                    recordOffline();
                }

            } else {
                recordOffline();
            }

        }
    }

    private void parseResult() {
        if (preferences.canPrint()) {
            Intent intent = new Intent(getApplicationContext(), SlipActivity.class);
            intent.putExtra("title", "RECORD WALK IN");
            intent.putExtra("house", selectedDestination.getName());
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("idNumber", idNumber);
            intent.putExtra("result_slip", result_slip);
            intent.putExtra("visit_id", visit_id);
            intent.putExtra("checkInType", "walk");
            intent.putExtra("checkInMode", "ID No");
            if (preferences.isSelectHostsEnabled() && selectedHost != null) {
                intent.putExtra("host", selectedHost.getName());
            }

            startActivity(intent);
        } else {
            new MaterialDialog.Builder(RecordWalkIn.this)
                    .title("SUCCESS")
                    .content("Recorded successfully. Do you want to record another visit?")
                    .positiveText("YES")
                    .negativeText("NO")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                            Intent intent = new Intent(RecordWalkIn.this, ScanActivity.class);
                            Bundle extras = new Bundle();
                            extras.putInt("TargetActivity", Common.WALK_IN);
                            intent.putExtras(extras);
                            startActivity(intent);
                            finish();

                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), Dashboard.class));
                            finish();
                        }
                    })
                    .show();

            if (selectedHost != null) {
                Log.d(TAG, "recordOffline: Not Null");
                pushNotificationToHost();
            }

            // Get instance of Vibrator from current Context
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
            v.vibrate(400);
        }
    }

    private void resultHandler(String result) throws JSONException {
        //Log.d("WALKIN", result);
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");
        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
            result_slip = obj.getString("result_slip");
            visit_id = obj.getInt("visit_id");
            recordOffline();
            parseResult();
        } else {
            Log.d(TAG, "resultHandler: " + result);
            if (resultText.contains("still in")) {
                Log.d(TAG, "resultHandler: Still In");
                new MaterialDialog.Builder(RecordWalkIn.this)
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
                                String idN = "000000000";
                                String idNumber = "";
                                String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
                                if (classCode.equals("ID")) {
                                    if (Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER) == null) {
                                        idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                                    } else {
                                        idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");

                                    }
                                    idNumber = idN.substring(2, idN.length() - 1);
                                } else if (classCode.equals("P")) {
                                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                                    Log.d(TAG, "onNegative: " + idN);
                                    idNumber = idN;
                                } else if (classCode.equals("PA")) {
                                    Log.d(TAG, "recordInternet: Passport");

                                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                                    idNumber = idN;
                                } else if (classCode.equals("AC")) {
                                    Log.d(TAG, "Class Code : " + classCode);

                                    Log.d(TAG, "recordInternet: Alien Id");

                                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_LINE_2_OPTIONAL_DATA).replace("^", "\n");
                                    idNumber = idN.substring(2, idN.length() - 1);
                                    Log.d(TAG, "recordInternet: ID" + idNumber);


                                }
                                String urlParameters = null;
                                try {
                                    urlParameters = "idNumber=" + URLEncoder.encode(idNumber, "UTF-8") +
                                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                            "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");
                                    new ExitAsync().execute(preferences.getBaseURL() + "record-visitor-exit", urlParameters);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .show();
            } else {
                new MaterialDialog.Builder(RecordWalkIn.this)
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
                Log.d(TAG, "resultHandler: Error" + result);
            }
        }
    }

    private class ExitAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(RecordWalkIn.this)
                .title("Exit")
                .content("Removing visitor...")
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

        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);
        }

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
                            recordInternet();
                        } else {
                            new MaterialDialog.Builder(RecordWalkIn.this)
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
                        new MaterialDialog.Builder(RecordWalkIn.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                new MaterialDialog.Builder(RecordWalkIn.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }


    private class FetchHostsService extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }

        protected void onPostExecute(String result) {
            //TODo: Parse result

        }
    }


    private class FetchHouseDetails extends AsyncTask<Void, String, ArrayList<TypeObject>> {

        MaterialDialog builder = new MaterialDialog.Builder(RecordWalkIn.this)
                .title("Soja")
                .titleGravity(GravityEnum.CENTER)
                .titleColor(getResources().getColor(R.color.ColorPrimary))
                .content("Fetching Data")
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
            String houseResult = NetworkHandler.GET(preferences.getBaseURL() + "active_events");

            ArrayList<TypeObject> houses = new ArrayList<>();
            Log.d(TAG, "getActiveHouses: " + houseResult);
            if (houseResult != null) {
                try {
                    JSONObject housesObject = new JSONObject(houseResult);
                    if (housesObject.getInt("result_code") == 0 && housesObject.getString("result_text").equals("OK")) {
                        JSONArray housesArray = housesObject.getJSONArray("result_content");
                        for (int i = 0; i < housesArray.length(); i++) {
                            JSONObject house = housesArray.getJSONObject(i);
                            houses.add(new TypeObject(house.getString("house_id"), house.getString("house_description")));
//                        handler.insertHouse(house.getString("house_id"), house.getString("house_description"),house.getString("block_description"));
                        }
                        return houses;
                    } else {
                        Toast.makeText(RecordWalkIn.this, "Couldn't retrieve houses", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return houses;
        }

        @Override
        protected void onPostExecute(ArrayList<TypeObject> typeObjects) {
            super.onPostExecute(typeObjects);
            builder.dismiss();
        }
    }


    private class SMSCheckInAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Please wait...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btnConfirm.setBackgroundColor(getColor(R.color.colorPrimaryDark));
            } else {
                btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            }

        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkHandler.executePost(params[0], params[1]);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                if (result.contains("result_code")) {
                    try {
                        Object json = new JSONTokener(result).nextValue();
                        if (json instanceof JSONObject) {
                            confirmResultHandler(result);
                            Log.d(TAG, "onPostExecute: " + result);
                        } else {
                            Toast.makeText(RecordWalkIn.this, "Ooops! Please try again!", Toast.LENGTH_SHORT).show();
//
//                            linFields.setVisibility(View.VISIBLE);
//                            phoneVerificationLayout.setVisibility(View.GONE);
//                            linVerificationCode.setVisibility(View.GONE);

                            linFields.setVisibility(View.GONE);
                            phoneVerificationLayout.setVisibility(View.VISIBLE);
                            linVerificationCode.setVisibility(View.VISIBLE);


                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("SEND VERIFICATION CODE");

//                            Toast.makeText(SmsCheckInActivity.this, resultText, Toast.LENGTH_SHORT).show();
                            //TODO remove this.Temporary workaround
//                            recordOffline();
//                            parseResult();

                            Log.d(TAG, "onPostExecute: " + result);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
//                    recordOffline();
                }

            }
        }

    }

    //    Confirm Code
    private void confirmResultHandler(String result) throws JSONException {
        //Log.d("WALKIN", result);
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");
        if (resultCode == 0 && resultText.equals("OK") && resultContent.equals("success")) {
            Toast.makeText(this, "Enter Verification Code", Toast.LENGTH_SHORT).show();

//            Set Visibility of Selected Item

            linFields.setVisibility(View.VISIBLE);
            phoneVerificationLayout.setVisibility(View.GONE);
            linVerificationCode.setVisibility(View.VISIBLE);

            edtCode.requestFocus();
//            Restore Button State
            btnConfirm.setEnabled(true);
            btnConfirm.setText("SEND CONFIRMATION CODE");
            btnConfirm.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            Log.d(TAG, "confirmResultHandler: " + result);
        } else {
            Toast.makeText(RecordWalkIn.this, "Error" + resultText, Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, receiveFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onPause();
    }

}


