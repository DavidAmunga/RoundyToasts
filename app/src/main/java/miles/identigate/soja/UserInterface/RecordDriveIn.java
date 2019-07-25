package miles.identigate.soja.UserInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.documentreader.api.enums.eVisualFieldType;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.SlipActivity;
import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.EditTextRegular;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.CheckConnection;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.models.DriveInPassenger;
import miles.identigate.soja.models.TypeObject;

public class RecordDriveIn extends SojaActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    Spinner visitor_type;
    DatabaseHandler handler;
    Button record;
    String vehicleNo;
    EditText vehicleRegNo;
    EditText numberOfPeople;
    TypeObject selectedType, selectedDestination, selectedHost, selectedGender;
    ArrayList<TypeObject> houses, visitorTypes, hosts, genderTypes;
    Preferences preferences;
    MaterialDialog progressDialog;
    MaterialDialog dialog;
    LinearLayout phoneNumberLayout, hostLayout, companyNameLayout;
    EditText phoneNumberEdittext, companyNameEdittext;
    @BindView(R.id.numberOfPeopleLayout)
    LinearLayout numberOfPeopleLayout;
    @BindView(R.id.txt_passengers_no)
    EditTextRegular txtPassengersNo;
    @BindView(R.id.lin_passenger_no)
    LinearLayout linPassengerNo;

    private Boolean doubleBackToExitPressedOnce = true;


    TextView spinnerDestination, spinnerHost;


    String firstName, lastName, idNumber;
    String result_slip = "";
    String visit_id;
    int driverPassengers = 1;


    private static final String TAG = "RecordDriveIn";
    @BindView(R.id.txt_first_name)
    EditTextRegular txtFirstName;
    @BindView(R.id.txt_last_name)
    EditTextRegular txtLastName;
    @BindView(R.id.nameLayout)
    LinearLayout nameLayout;
    @BindView(R.id.txtID)
    EditTextRegular txtID;
    @BindView(R.id.idLayout)
    LinearLayout idLayout;
    @BindView(R.id.typeLabel)
    TextViewBold typeLabel;
    @BindView(R.id.gender_type)
    Spinner genderType;
    @BindView(R.id.genderLayout)
    LinearLayout genderLayout;

    boolean manualEdit = false;


    DriveInPassenger driveInPassenger;

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
    private List<DriveInPassenger> passengersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_drive_in);
        ButterKnife.bind(this);
        if (Constants.documentReaderResults == null)
            finish();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Record Drive In");
        Paper.init(this);

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);

        handler = new DatabaseHandler(this);
        vehicleRegNo = findViewById(R.id.car_number);
        vehicleRegNo.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        visitor_type = findViewById(R.id.visitor_type);
        record = findViewById(R.id.record);
        spinnerDestination = findViewById(R.id.spinnerDestination);
        spinnerHost = findViewById(R.id.spinnerHost);
        numberOfPeople = findViewById(R.id.numberOfPeople);
        phoneNumberLayout = findViewById(R.id.phoneNumberLayout);
        phoneNumberEdittext = findViewById(R.id.phoneNumberEdittext);
        companyNameLayout = findViewById(R.id.companyNameLayout);
        hostLayout = findViewById(R.id.hostLayout);
        companyNameEdittext = findViewById(R.id.companyNameEdittext);

        phoneNumberLayout.setVisibility(preferences.isPhoneNumberEnabled() ? View.VISIBLE : View.GONE);
        companyNameLayout.setVisibility(preferences.isCompanyNameEnabled() ? View.VISIBLE : View.GONE);


        if (preferences.isRecordPassengerDetails()) {
            linPassengerNo.setVisibility(View.GONE);
        } else {
            linPassengerNo.setVisibility(View.VISIBLE);
        }

//        Initialize Passenger
        driveInPassenger = new DriveInPassenger();


        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.check(RecordDriveIn.this)) {
                    if (selectedDestination == null || vehicleRegNo.getText().toString().equals(null) || vehicleRegNo.getText().toString().equals("")) {
                        Snackbar.make(v, "All fields are required.", Snackbar.LENGTH_SHORT).setActionTextColor(getResources().getColor(R.color.white)).show();
                    } else {
                        recordInternt();
                    }
                } else {
                    if (vehicleRegNo.getText().toString().equals(null) || vehicleRegNo.getText().toString().equals("")) {
                        Snackbar.make(v, "All fields are required.", Snackbar.LENGTH_SHORT).setActionTextColor(getResources().getColor(R.color.white)).show();
                    }
                }
            }
        });


//        Check if manual
        if (

                getIntent() != null) {
            Bundle bundle = getIntent().getExtras();

//            if (bundle.getBoolean("manual")) {
//                manualEdit = true;
//                updateOptions();
//            }
        }


//        VISITOR TYPES
        visitorTypes = handler.getTypes("visitors", null);
        TypeAdapter adapter = new TypeAdapter(RecordDriveIn.this, R.layout.tv, visitorTypes);
        visitor_type.setAdapter(adapter);
        visitor_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedType = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//      DESTINATION / HOUSE

        houses = handler.getTypes("houses", null);

        spinnerDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                new SimpleSearchDialogCompat(RecordDriveIn.this,
                        "Search for Destination...", "What destination is the visitor heading...?", null, houses, new SearchResultListener<TypeObject>() {
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


//                        Toast.makeText(RecordDriveIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                        spinnerDestination.setText(o.getName());
                        baseSearchDialogCompat.dismiss();

                    }
                }).show();
            }
        });

//        HOST
        spinnerHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                if (selectedDestination == null) {
                    Toast.makeText(RecordDriveIn.this, "Select Destination First", Toast.LENGTH_SHORT).show();
                } else {
                    new SimpleSearchDialogCompat(RecordDriveIn.this,
                            "Search for Host...", "Who is the visitor seeing in " + selectedDestination.getName(), null, hosts, new SearchResultListener<TypeObject>() {
                        @Override
                        public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                            TypeObject object = o;
                            selectedHost = object;

//                            Toast.makeText(RecordDriveIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                            spinnerHost.setText(o.getName());
                            baseSearchDialogCompat.dismiss();

                        }
                    }).show();
                }
            }
        });


//        CHECK IF DRIVER PASS EXISTS
        if (Paper.book().

                read(Common.PREF_CURRENT_DRIVER_PASS) != null) {
            driverPassengers = Paper.book().read(Common.PREF_CURRENT_DRIVER_PASS);
            Log.d(TAG, "onCreate: Same Driver");

            if (Paper.book().read(Common.PREF_CURRENT_PASSENGERS_LIST) != null) {
                passengersList = Paper.book().read(Common.PREF_CURRENT_PASSENGERS_LIST);
                Log.d(TAG, "onCreate: Same Driver ++");


                DriveInPassenger lastPassenger = passengersList.get(passengersList.size() - 1);
                DriveInPassenger firstPassenger = passengersList.get(0);

                for (TypeObject house : houses) {
                    if (house.getId().equals(lastPassenger.getHouseID())) {
                        selectedDestination = house;
                        spinnerDestination.setText(selectedDestination.getName());
                    }
                }

                if (preferences.isSelectHostsEnabled()) {

                    if (selectedDestination != null) {

                        hosts = handler.getTypes("hosts", selectedDestination.getId());


                        hostLayout.setVisibility(preferences.isSelectHostsEnabled() && hosts.size() > 0 ? View.VISIBLE : View.GONE);

                        if (hosts.size() == 0) {
                            selectedHost = null;
                        }

                        for (TypeObject host : hosts) {
                            if (host.getId().equals(lastPassenger.getHostID())) {
                                selectedHost = host;
                                spinnerHost.setText(selectedHost.getName());
                            }
                        }

                    }


                }


                vehicleRegNo.setText(firstPassenger.getVehicleRegNO());
                companyNameEdittext.setText(lastPassenger.getCompanyName());

                numberOfPeopleLayout.setVisibility(View.GONE);

            }
        } else {
            Log.d(TAG, "onCreate: New Driver");
        }
    }

    private void updateOptions() {
        if (manualEdit) {
            nameLayout.setVisibility(View.VISIBLE);
            idLayout.setVisibility(View.VISIBLE);
            genderLayout.setVisibility(View.VISIBLE);

            genderTypes = handler.getTypes("genders", null);


            TypeAdapter adapter = new TypeAdapter(RecordDriveIn.this, R.layout.tv, genderTypes);


            genderType.setAdapter(adapter);
            genderType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TypeObject object = (TypeObject) parent.getSelectedItem();
                    selectedGender = object;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        } else {
            nameLayout.setVisibility(View.GONE);
            genderLayout.setVisibility(View.GONE);
            idLayout.setVisibility(View.GONE);
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

    public void recordInternt() {
        //Insert to local and online database.

        String urlParameters = null;
        try {
            String idN = "000000000";
            String scan_id_type = "ID";
            String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
            if (classCode.equals("ID")) {
                scan_id_type = "ID";
                if (Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER) == null) {
                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
                } else {
                    idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");

                }
                idNumber = idN.substring(2, idN.length() - 1);
                Log.d(TAG, "ID Number: " + idNumber);
            } else if (classCode.equals("P")) {
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


            String gender = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n").contains("M") ? "0" : "1";
            String mrzLines = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);


            driveInPassenger.setPhone(phoneNumberEdittext.getText().toString());
            driveInPassenger.setCompanyName(companyNameEdittext.getText().toString());
            driveInPassenger.setScan_id_type(scan_id_type);
            driveInPassenger.setVisitType("drive-in");
            driveInPassenger.setDeviceID(preferences.getDeviceId());
            driveInPassenger.setPremiseZoneID(preferences.getPremiseZoneId());
            driveInPassenger.setVisitorTypeID(selectedType.getId());
            driveInPassenger.setHouseID(selectedDestination.getId());
            driveInPassenger.setHostID(selectedHost != null ? selectedHost.getHostId() : "");
            driveInPassenger.setPaxinvehicle(1);
            driveInPassenger.setEntryTime(Constants.getCurrentTimeStamp());
            driveInPassenger.setVehicleRegNO(vehicleRegNo.getText().toString());
            driveInPassenger.setBirthDate(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"));
            driveInPassenger.setGenderID(gender);
            driveInPassenger.setFirstName(firstName);
            driveInPassenger.setLastName(lastName);
            driveInPassenger.setIdType(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"));
            driveInPassenger.setIdNumber(idNumber);
            driveInPassenger.setNationality(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n"));
            driveInPassenger.setNationCode(Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n"));

            int passengers = 0;

            if (!preferences.isRecordPassengerDetails()) {
                passengers = Integer.parseInt(txtPassengersNo.getText().toString());
            }

            urlParameters =
                    "mrz=" + URLEncoder.encode(mrzLines, "UTF-8") +
                            "&phone=" + URLEncoder.encode(driveInPassenger.getPhone(), "UTF-8") +
                            (preferences.isCompanyNameEnabled() && !driveInPassenger.getCompanyName().equals("") ?
                                    ("&company=" + URLEncoder.encode(driveInPassenger.getCompanyName(), "UTF-8")) : "") +
                            //Passengers Check   //
                            (preferences.isRecordPassengerDetails() && Paper.book().read(Common.PREF_CURRENT_VISIT_ID) != null ?
                                    ("&driverVisitID=" + Paper.book().read(Common.PREF_CURRENT_VISIT_ID)) : "") +
//
                            "&scan_id_type=" + URLEncoder.encode(driveInPassenger.getScan_id_type(), "UTF-8") +
                            "&visitType=" + URLEncoder.encode(driveInPassenger.getVisitType(), "UTF-8") +
                            "&deviceID=" + URLEncoder.encode(driveInPassenger.getDeviceID(), "UTF-8") +
                            "&premiseZoneID=" + URLEncoder.encode(driveInPassenger.getPremiseZoneID(), "UTF-8") +
                            "&visitorTypeID=" + URLEncoder.encode(driveInPassenger.getVisitorTypeID(), "UTF-8") +
                            "&houseID=" + URLEncoder.encode(driveInPassenger.getHouseID(), "UTF-8") +
                            (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(driveInPassenger.getHostID())) : "") +
                            (!preferences.isRecordPassengerDetails() ?
                                    "&paxinvehicle=" + (passengers) :
                                    "&paxinvehicle=" + (driveInPassenger.getPaxinvehicle() >= 1 ? driveInPassenger.getPaxinvehicle() : 1)

                            ) +
                            "&entryTime=" + URLEncoder.encode(driveInPassenger.getEntryTime(), "UTF-8") +
                            "&vehicleRegNO=" + URLEncoder.encode(driveInPassenger.getVehicleRegNO(), "UTF-8") +
                            "&birthDate=" + URLEncoder.encode(driveInPassenger.getBirthDate(), "UTF-8") +
                            "&genderID=" + URLEncoder.encode(driveInPassenger.getGenderID(), "UTF-8") +
                            "&firstName=" + URLEncoder.encode(driveInPassenger.getFirstName(), "UTF-8") +
                            "&lastName=" + URLEncoder.encode(driveInPassenger.getLastName(), "UTF-8") +
                            "&idType=" + URLEncoder.encode(driveInPassenger.getIdType(), "UTF-8") +
                            "&idNumber=" + URLEncoder.encode(driveInPassenger.getIdNumber(), "UTF-8") +
                            "&nationality=" + URLEncoder.encode(driveInPassenger.getNationality(), "UTF-8") +

                            "&nationCode=" + URLEncoder.encode(driveInPassenger.getNationCode(), "UTF-8");
            Log.d(TAG, "URL Param: " + urlParameters);
            new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(

            );
        }
    }


    private class DriveinAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Constants.showProgressDialog(RecordDriveIn.this, "Drive in", "Checking in...");
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result != null) {
                try {
                    Object json = new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject) {
                        resultHandler(result);
                    } else {
                        parseResult();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
            }

        }
    }

    private void parseResult() {
        if (preferences.canPrint()) {
            Intent intent = new Intent(getApplicationContext(), SlipActivity.class);
            intent.putExtra("title", "RECORD DRIVE IN");
            intent.putExtra("house", selectedDestination.getName());
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("idNumber", idNumber);
            intent.putExtra("result_slip", result_slip);
            intent.putExtra("visit_id", visit_id);
            intent.putExtra("vehicleNo", vehicleRegNo.getText().toString());
            intent.putExtra("checkInType", "drive");
            intent.putExtra("checkInMode", "ID No");

            if (!preferences.isRecordPassengerDetails()) {
                intent.putExtra("peopleNo", txtPassengersNo.getText().toString());

            }


            if (preferences.isSelectHostsEnabled() && selectedHost != null) {
                intent.putExtra("host", selectedHost.getName());
            }

            startActivity(intent);
        } else {

            if (preferences.isRecordPassengerDetails()) {
                new MaterialDialog.Builder(RecordDriveIn.this)
                        .title("SUCCESS")
                        .content("Visitor recorded successfully. " +
                                "Do you want to record another passenger for Car "
                                + (passengersList.size() > 0 ? passengersList.get(0).getVehicleRegNO() : vehicleRegNo.getText().toString()) +
                                " under " +
                                (passengersList.size() > 0 ? passengersList.get(0).getFirstName() : firstName)
                                + "?"
                        )
                        .positiveText("YES")
                        .negativeText("NO")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
//                            Go to next Passenger
                                driverPassengers++;
                                passengersList.add(driveInPassenger);
                                Paper.book().write(Common.PREF_CURRENT_DRIVER_PASS, driverPassengers);

                                Log.d(TAG, "onPositive: Visit ID " + String.valueOf(visit_id));

                                Paper.book().write(Common.PREF_CURRENT_PASSENGERS_LIST, passengersList);

                                dialog.dismiss();
                                Intent intent = new Intent(RecordDriveIn.this, ScanActivity.class);
                                Bundle extras = new Bundle();

                                extras.putInt("TargetActivity", Common.DRIVE_IN);
                                intent.putExtras(extras);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                Paper.book().write(Common.PREF_CURRENT_DRIVER_PASS, 1);
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                finish();
                            }

                        })
                        .show();
            } else {
                new MaterialDialog.Builder(RecordDriveIn.this)
                        .title("SUCCESS")
                        .content("Visitor recorded successfully"
                        )
                        .positiveText("OK")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
//                            Go to next Passenger
                                Paper.book().write(Common.PREF_CURRENT_DRIVER_PASS, 1);
                                dialog.dismiss();
                                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                finish();
                            }


                        })
                        .show();

            }


            // Get instance of Vibrator from current Context
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
            v.vibrate(400);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, "Pressing Back again will Exit Recording Passengers for the same car", Toast.LENGTH_SHORT).show();
            doubleBackToExitPressedOnce = false;
        } else {
            super.onBackPressed();

        }

    }

    private void resultHandler(String result) throws JSONException {
        JSONObject obj = new JSONObject(result);
        int resultCode = obj.getInt("result_code");
        String resultText = obj.getString("result_text");
        String resultContent = obj.getString("result_content");
        Log.d(TAG, "resultHandler: " + obj.toString());
        if (resultText.equals("OK") && resultContent.equals("success")) {
            result_slip = obj.getString("result_slip");
            visit_id = obj.getString("id");

            Paper.book().write(Common.PREF_CURRENT_VISIT_ID, visit_id);

            parseResult();
        } else {
            if (resultText.contains("still in")) {
                MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        String urlParameters = null;
                        String idN = "000000000";
                        String idNumber = "";

                        String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");
                        if (classCode.equals("ID")) {
                            idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_IDENTITY_CARD_NUMBER).replace("^", "\n");
                            idNumber = idN.substring(2, idN.length() - 1);
                        } else if (classCode.equals("P")) {
                            idN = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_NUMBER).replace("^", "\n");
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
                        try {
                            urlParameters = "deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                                    "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8") +
                                    "&exitTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8");


                            Log.d(TAG, "URL Param : " + urlParameters);

                            new ExitAsync().execute(preferences.getBaseURL() + "record-visitor-exit", urlParameters);

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                };
                dialog = Constants.showDialog(this, "Soja", resultText, "Check out", singleButtonCallback);
                dialog.show();
            } else {
                MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                };
                dialog = Constants.showDialog(this, "Soja", resultText, "OK", singleButtonCallback);
                dialog.show();
            }
        }
    }

    private class ExitAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Constants.showProgressDialog(RecordDriveIn.this, "Exit", "Removing visitor...");
            progressDialog.show();
        }

        protected String doInBackground(String... params) {
            return new NetworkHandler().executePost(params[0], params[1]);
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            if (result != null) {

                try {
                    if (result.contains("result_code")) {
                        JSONObject obj = new JSONObject(result);
                        int resultCode = obj.getInt("result_code");
                        String resultText = obj.getString("result_text");
                        String resultContent = obj.getString("result_content");
                        Log.d(TAG, "onPostExecute: " + result);

                        if (resultText.equals("OK") && resultContent.equals("success")) {
                            recordInternt();
                        } else {
                            MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            };
                            dialog = Constants.showDialog(RecordDriveIn.this, "ERROR", resultText, "OK", singleButtonCallback);
                            dialog.show();
                        }
                    } else {
                        MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        };
                        dialog = Constants.showDialog(RecordDriveIn.this, "Result", "Poor internet connection", "OK", singleButtonCallback);
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                };
                dialog = Constants.showDialog(RecordDriveIn.this, "Result", "Poor internet connection", "OK", singleButtonCallback);
                dialog.show();
            }
        }
    }
}
