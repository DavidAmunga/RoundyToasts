package miles.identigate.soja.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.internal.LinkedTreeMap;
import com.hbb20.CountryCodePicker;
import com.regula.documentreader.api.enums.eVisualFieldType;

import java.util.ArrayList;

import HPRTAndroidSDK.HPRTPrinterHelper;
import HPRTAndroidSDK.PublicFunction;
import butterknife.BindView;
import butterknife.ButterKnife;
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.Printer.DeviceListActivity;
import miles.identigate.soja.Printer.PrinterProperty;
import miles.identigate.soja.Printer.PublicAction;
import miles.identigate.soja.R;
import miles.identigate.soja.adapters.TypeAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.EditTextRegular;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.models.TypeObject;
import miles.identigate.soja.service.network.api.APIClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterGuest extends AppCompatActivity {
    private static final String TAG = "RegisterGuest";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtPhoneNo)
    EditTextRegular txtPhoneNo;
    @BindView(R.id.phoneNumberLayout)
    LinearLayout phoneNumberLayout;
    @BindView(R.id.txtEmail)
    EditTextRegular txtEmail;
    @BindView(R.id.emailLayout)
    LinearLayout emailLayout;
    @BindView(R.id.companyNameLayout)
    LinearLayout companyNameLayout;
    @BindView(R.id.record)
    Button record;

    Preferences preferences;

    @BindView(R.id.nameLayout)
    LinearLayout nameLayout;
    @BindView(R.id.txtCompany)
    EditTextRegular txtCompany;
    @BindView(R.id.hostLabel)
    TextViewBold hostLabel;
    @BindView(R.id.spinnerEvent)
    TextViewBold spinnerEvent;

    ArrayList<TypeObject> events, visitorTypes, genderTypes, documentTypes;
    TypeObject selectedEvent, selectedType, selectedGender, selectedDocument;

    ProgressDialog progressDialog;


    String nationality, nationalityCode;
    String companyName = "";
    @BindView(R.id.ccp)
    CountryCodePicker ccp;
    @BindView(R.id.countryLayout)
    LinearLayout countryLayout;
    @BindView(R.id.document_type)
    Spinner documentType;
    @BindView(R.id.documentLayout)
    LinearLayout documentLayout;
    private String PrinterName = "";
    @BindView(R.id.typeLabel)
    TextViewBold typeLabel;
    @BindView(R.id.visitor_type)
    Spinner visitorType;

    boolean manualEdit = false;

    @BindView(R.id.txt_first_name)
    EditTextRegular txtFirstName;
    @BindView(R.id.txt_last_name)
    EditTextRegular txtLastName;
    @BindView(R.id.gender_type)
    Spinner genderType;
    @BindView(R.id.genderLayout)
    LinearLayout genderLayout;
    @BindView(R.id.txt_designation)
    EditTextRegular txtDesignation;
    @BindView(R.id.designationLayout)
    LinearLayout designationLayout;

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


    boolean isPrinted = false;

    DatabaseHandler handler;


    String gender, firstName, lastName, idNumber, email, phoneNo, designation;

    private static final int REQUEST_ENABLE_BT = 200;
    private static final int REQUEST_ENABLE_LOCATION = 300;


    private static HPRTPrinterHelper HPRTPrinter = new HPRTPrinterHelper();
    private BluetoothAdapter mBluetoothAdapter;
    private PublicFunction PFun = null;
    private PublicAction PAct = null;
    MaterialDialog dialog;
    private String qrCode = null;
    private ArrayAdapter arrPrinterList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }


        setContentView(R.layout.activity_register_guest);
        ButterKnife.bind(this);
//        if (Constants.documentReaderResults == null)
//            finish();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register Guest");


//        Confirm Manual

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);


        handler = new DatabaseHandler(this);

        progressDialog = new ProgressDialog(this);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                if (bundle.getBoolean("manual")) {
                    manualEdit = true;
                    updateOptions();
                }
            }

        }


//        PFun = new PublicFunction(GuestList.this);
//        PAct = new PublicAction(GuestList.this);
//


//        VISITOR TYPES
        visitorTypes = handler.getTypes("visitors", null);
        TypeAdapter adapter = new TypeAdapter(RegisterGuest.this, R.layout.tv, visitorTypes);


        visitorType.setAdapter(adapter);
        visitorType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TypeObject object = (TypeObject) parent.getSelectedItem();
                selectedType = object;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


//        HOTFIX Select Visitor Type
//        selectedType = visi.get(0);
//        spinnerEvent.setText(events.get(0).getName());

//        DESTINATION

        events = handler.getTypes("houses", null);


        spinnerEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clicked");
                new SimpleSearchDialogCompat(RegisterGuest.this,
                        "Search for event", "What event are you going", null, events, new SearchResultListener<TypeObject>() {
                    @Override
                    public void onSelected(BaseSearchDialogCompat baseSearchDialogCompat, TypeObject o, int i) {

                        TypeObject object = o;
                        selectedEvent = object;

                        Log.d(TAG, "Selected Destination: " + selectedEvent.getId());

//                        Toast.makeText(RecordWalkIn.this, o.getName(), Toast.LENGTH_SHORT).show();
                        spinnerEvent.setText(o.getName());
                        baseSearchDialogCompat.dismiss();

                    }
                }).show();
            }
        });


//        HOTFIX EVENT
        selectedEvent = events.get(0);
        spinnerEvent.setText(events.get(0).getName());


        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateDetails()) {
                    if (manualEdit) {
                        Log.d(TAG, "onClick: Manual");
                        recordCheckInManual();
                    } else {
                        recordCheckIn();
                    }
                }

            }
        });

//        Dialog Setup
        dialog = Constants.showProgressDialog(RegisterGuest.this, "Printing", "Printing slip...");
        dialog.setCancelable(true);

//Printing
        PFun = new PublicFunction(RegisterGuest.this);
        PAct = new PublicAction(RegisterGuest.this);


        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                Log.d(TAG, "onCountrySelected: " +
                        ccp.getSelectedCountryCode()

                );
            }
        });


    }

    private boolean validateDetails() {
        if (selectedType == null) {
            Toast.makeText(this, "Please fill Visitor type", Toast.LENGTH_SHORT).show();
            return false;
        } else if (selectedEvent == null) {
            Toast.makeText(this, "Please fill Event type", Toast.LENGTH_SHORT).show();
            return false;
        } else if (manualEdit && TextUtils.isEmpty(txtFirstName.getText().toString())) {
            Toast.makeText(this, "Please fill First Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void recordCheckIn() {
        progressDialog.setMessage("Checking in...");
        progressDialog.setCancelable(false);
        progressDialog.show();


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
        companyName = txtCompany.getText().toString();

        String gender = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n").contains("M") ? "0" : "1";
        String mrzLines = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);

        phoneNo = txtPhoneNo.getText().toString();
        email = txtEmail.getText().toString();
        designation = txtDesignation.getText().toString();


//
//            Start Request
        APIClient.getClient(preferences, "").recordGuest(
                selectedEvent.getId(),
                Constants.getCurrentTimeStamp(),
                Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n"),
                gender,
                firstName,
                TextUtils.isEmpty(lastName) ? null : lastName,
                TextUtils.isEmpty(email) ? null : email,
                TextUtils.isEmpty(phoneNo) ? null : phoneNo,
                TextUtils.isEmpty(designation) ? null : designation,
                idNumber,
                Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n"),
                Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n"),
                Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n"),
                TextUtils.isEmpty(companyName) ? null : companyName,
                "walk-in",
                preferences.getDeviceId(),
                preferences.getPremiseZoneId(),
                selectedType.getId()
        ).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode().equals(0) && queryResponse.getResultText().equals("OK")) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onResponse: " + queryResponse.getResultContent().toString());


                            Log.d(TAG, "onResponse: Result Content " + queryResponse.getResultContent());

                            LinkedTreeMap treeMap = (LinkedTreeMap) queryResponse.getResultContent();


                            Log.d(TAG, "onResponse: " + treeMap.get("qr_token"));

                            qrCode = treeMap.get("qr_token").toString();


                            new MaterialDialog.Builder(RegisterGuest.this)
                                    .title("SUCCESS")
                                    .content("Registered successfully.")
                                    .positiveText("OK")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
                                            if (!TextUtils.isEmpty(qrCode)) {
                                                doPrint();
                                            } else {
                                                Toast.makeText(RegisterGuest.this, "QR Code not available", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .show();

                        } else {
//
                            progressDialog.dismiss();

                            if (queryResponse.getResultText().contains("still in")) {
//                                Visitor Is Still In

                                new MaterialDialog.Builder(RegisterGuest.this)
                                        .title("Notice")
                                        .content(queryResponse.getResultText())
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

                            } else {

                                new MaterialDialog.Builder(RegisterGuest.this)
                                        .title("Notice")
                                        .content(queryResponse.getResultText())
                                        .positiveText("OK")
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                dialog.dismiss();
                                                //finish();
                                            }
                                        })
                                        .show();
                                Log.d(TAG, "resultHandler: Error" + queryResponse.getResultText());
                            }


//                            Log.d(TAG, "onResponse: " + queryResponse.getResultContent().toString());


                        }

                    } else {
                        progressDialog.dismiss();
                        Log.d(TAG, "onResponse: " + response.toString());
                    }
                } else {

                    progressDialog.dismiss();
                    new MaterialDialog.Builder(RegisterGuest.this)
                            .title("Notice")
                            .content("Please try again")
                            .positiveText("OK")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .show();


                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(RegisterGuest.this, "Timed out. Please try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });


    }

    private void recordCheckInManual() {
        progressDialog.setMessage("Checking in...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        String scan_id_type = selectedDocument.getId();
        idNumber = Common.randomString(11);


        firstName = txtFirstName.getText().toString().trim();
        lastName = txtLastName.getText().toString().trim();
        companyName = txtCompany.getText().toString();
        phoneNo = txtPhoneNo.getText().toString();
        email = txtEmail.getText().toString();
        designation = txtDesignation.getText().toString();

        nationality = ccp.getSelectedCountryEnglishName();
        nationalityCode = ccp.getSelectedCountryCode();

        if (nationalityCode.length() <= 2) {
            nationalityCode = String.format("%03d", Integer.parseInt(nationalityCode));
        }


        gender = selectedGender.getId();

//
//            Start Request
        APIClient.getClient(preferences, "").recordGuest(
                selectedEvent.getId(),
                Constants.getCurrentTimeStamp(),
                Constants.getCurrentTimeStamp(),
                gender,
                firstName,
                TextUtils.isEmpty(lastName) ? null : lastName,
                TextUtils.isEmpty(email) ? null : email,
                TextUtils.isEmpty(phoneNo) ? null : phoneNo,
                TextUtils.isEmpty(designation) ? null : designation,
                idNumber,
                selectedDocument.getName(),
                nationality,
                nationalityCode,
                TextUtils.isEmpty(companyName) ? null : companyName,
                "walk-in",
                preferences.getDeviceId(),
                preferences.getPremiseZoneId(),
                selectedType.getId()
        ).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode().equals(0) && queryResponse.getResultText().equals("OK")) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onResponse: " + queryResponse.getResultContent().toString());


                            Log.d(TAG, "onResponse: Result Content " + queryResponse.getResultContent());

                            LinkedTreeMap treeMap = (LinkedTreeMap) queryResponse.getResultContent();


                            Log.d(TAG, "onResponse: " + treeMap.get("qr_token"));

                            qrCode = treeMap.get("qr_token").toString();


                            new MaterialDialog.Builder(RegisterGuest.this)
                                    .title("SUCCESS")
                                    .content("Registered successfully.")
                                    .positiveText("OK")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            dialog.dismiss();
                                            if (!TextUtils.isEmpty(qrCode)) {
                                                doPrint();
                                            } else {
                                                Toast.makeText(RegisterGuest.this, "QR Code not available", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .show();

                        } else {
//
                            progressDialog.dismiss();

                            if (queryResponse.getResultText().contains("still in")) {
//                                Visitor Is Still In

                                new MaterialDialog.Builder(RegisterGuest.this)
                                        .title("Notice")
                                        .content(queryResponse.getResultText())
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

                            } else {

                                new MaterialDialog.Builder(RegisterGuest.this)
                                        .title("Notice")
                                        .content(queryResponse.getResultText())
                                        .positiveText("OK")
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                dialog.dismiss();
                                                //finish();
                                            }
                                        })
                                        .show();
                                Log.d(TAG, "resultHandler: Error" + queryResponse.getResultText());
                            }


//                            Log.d(TAG, "onResponse: " + queryResponse.getResultContent().toString());


                        }

                    } else {
                        progressDialog.dismiss();
                        Log.d(TAG, "onResponse: " + response.toString());
                    }
                } else {

                    progressDialog.dismiss();
                    new MaterialDialog.Builder(RegisterGuest.this)
                            .title("Notice")
                            .content("Please try again")
                            .positiveText("OK")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .show();


                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(RegisterGuest.this, "Timed out. Please try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });


    }


    private void recordCheckOut() {
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage("Checking Out");
            progressDialog.show();
        }
        Log.d(TAG, "recordCheckOut: " + idNumber + "," + preferences.getDeviceId());
        APIClient.getClient(preferences, "").recordVisitExit(
                idNumber,
                preferences.getDeviceId(),
                Constants.getCurrentTimeStamp()
        ).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode().equals(0) && queryResponse.getResultText().equals("OK")) {
                            progressDialog.dismiss();

                            recordCheckIn();
                        } else {
                            new MaterialDialog.Builder(RegisterGuest.this)
                                    .title("Notice")
                                    .content(queryResponse.getResultText())
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
                        progressDialog.dismiss();
                        new MaterialDialog.Builder(RegisterGuest.this)
                                .title("Notice")
                                .content("Please try again")
                                .positiveText("OK")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .show();
                    }

                } else {
                    progressDialog.dismiss();
                    new MaterialDialog.Builder(RegisterGuest.this)
                            .title("Notice")
                            .content("Please try again")
                            .positiveText("OK")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(RegisterGuest.this, "Timed out. Please try again", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_manual:
                manualEdit = true;
                updateOptions();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void updateOptions() {
        if (manualEdit) {
            nameLayout.setVisibility(View.VISIBLE);
            genderLayout.setVisibility(View.VISIBLE);
//            documentLayout.setVisibility(View.VISIBLE);
            countryLayout.setVisibility(View.VISIBLE);


            genderTypes = handler.getTypes("genders", null);


            TypeAdapter adapter = new TypeAdapter(RegisterGuest.this, R.layout.tv, genderTypes);


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


            documentTypes = new ArrayList<>();


            documentTypes = handler.getTypes("idTypes", "");


            for (TypeObject document : documentTypes) {
                if (document.getName().toLowerCase().contains("auto")) {
                    selectedDocument = document;
                }
            }


            TypeAdapter documentAdapter = new TypeAdapter(RegisterGuest.this, R.layout.tv, documentTypes);

            documentType.setAdapter(documentAdapter);

            documentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TypeObject object = (TypeObject) parent.getSelectedItem();
                    selectedDocument = object;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


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
            countryLayout.setVisibility(View.GONE);
//            documentLayout.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String strIsConnected;
        if (data == null || data.getExtras() == null)
            return;
        switch (resultCode) {
            case HPRTPrinterHelper.ACTIVITY_CONNECT_BT:
                String strBTAddress = "";

                strIsConnected = data.getExtras().getString("is_connected");
                if (strIsConnected.equals("NO")) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    doPrint();

                } else {

                    String PrinterName = "MPT-II";
                    HPRTPrinter = new HPRTPrinterHelper(RegisterGuest.this, PrinterName);
                    CapturePrinterFunction();
                    GetPrinterProperty();
                    PrintSlip();
                }
                break;
            case REQUEST_ENABLE_LOCATION:
                doPrint();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    private void doPrint() {
        setupBT();
        if (!dialog.isShowing())
            dialog.show();
        String PrinterName = "MPT-II";
        HPRTPrinter = new HPRTPrinterHelper(RegisterGuest.this, PrinterName);
        CapturePrinterFunction();
        GetPrinterProperty();
        PrintSlip();
    }

    private void setupBT() {
        if (ContextCompat.checkSelfPermission(RegisterGuest.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            dialog.dismiss();
            ActivityCompat.requestPermissions(RegisterGuest.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ENABLE_LOCATION);
        } else {
            Intent serverIntent = new Intent(RegisterGuest.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
            return;
        }
    }


    private void PrintSlip() {
        try {
            byte[] data = new byte[]{0x1b, 0x40};
            HPRTPrinterHelper.WriteData(data);
            PAct.LanguageEncode();
            PAct.BeforePrintAction();
            HPRTPrinterHelper.PrintText(
                    Common.centerString(18, Common.formatString(firstName.toLowerCase()))
                    , 32, 2, 16);
            String msg = Common.centerString(18, companyName != null ? companyName + "\n" : "");


            HPRTPrinterHelper.PrintText(msg, 32, 0, 16);
//            if(!idNumber.equals("") && idNumber!=null){
//                Log.d(TAG, "PrintSlip: ID No");


            HPRTPrinterHelper.PrintQRCode(qrCode, 7, (3 + 0x30), 1);


//            HPRTPrinterHelper.PrintText("\n" + Common.centerString(16, "Powered By soja.co.ke"), 0, 1, 0);
            HPRTPrinterHelper.PrintText("\n" + ">>>> Powered By soja.co.ke <<<<", 0, 0, 0);


            HPRTPrinterHelper.PrintText("\n", 0, 1, 0);

            PAct.AfterPrintAction();
            Log.d(TAG, "PrintSlip: Done");
            if (dialog.isShowing())
                dialog.dismiss();
            showSuccess();

//            startActivity(new Intent(getApplicationContext(), Dashboard.class));
//            finish();
        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }

    void showSuccess() {
        dialog.dismiss();
        LocalBroadcastManager.getInstance(RegisterGuest.this).sendBroadcast(new Intent(Constants.RECORDED_VISITOR));
        dialog = new MaterialDialog.Builder(this)
                .title("PRINTED")
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
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
//                        finish();
                    }
                })
                .build();
        View view = dialog.getCustomView();
        TextView messageText = (TextView) view.findViewById(R.id.message);
        messageText.setText("Guest has been recorded");
        dialog.show();
    }

    private void CapturePrinterFunction() {
        try {
            int[] propType = new int[1];
            byte[] Value = new byte[500];
            int[] DataLen = new int[1];
            String strValue = "";
            boolean isCheck = false;

            int iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BEEP, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Buzzer = (Value[0] == 0 ? false : true);

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Cut = (Value[0] == 0 ? false : true);
            //btnCut.setVisibility((PrinterProperty.Cut?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_DRAWER, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Cashdrawer = (Value[0] == 0 ? false : true);
            //btnOpenCashDrawer.setVisibility((PrinterProperty.Cashdrawer?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BARCODE, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Barcode = new String(Value);
            isCheck = PrinterProperty.Barcode.replace("QRCODE", "").replace("PDF417", "").replace(",,", ",").replace(",,", ",").length() > 0;
            //btn1DBarcodes.setVisibility((isCheck?View.VISIBLE:View.GONE));
            isCheck = PrinterProperty.Barcode.contains("QRCODE");
            //btnQRCode.setVisibility((isCheck?View.VISIBLE:View.GONE));
            //btnPDF417.setVisibility((isCheck?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Pagemode = (Value[0] == 0 ? false : true);

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_GET_REMAINING_POWER, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.GetRemainingPower = (Value[0] == 0 ? false : true);
            //btnGetRemainingPower.setVisibility((PrinterProperty.GetRemainingPower?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_CONNECT_TYPE, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.ConnectType = (Value[1] << 8) + Value[0];

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PRINT_RECEIPT, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.SampleReceipt = (Value[0] == 0 ? false : true);
            //btnSampleReceipt.setVisibility((PrinterProperty.SampleReceipt?View.VISIBLE:View.GONE));
        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
        }
    }

    private void GetPrinterProperty() {
        try {
            int[] propType = new int[1];
            byte[] Value = new byte[500];
            int[] DataLen = new int[1];
            String strValue = "";
            int iRtn = 0;

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_STATUS_MODEL, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.StatusMode = Value[0];

            if (PrinterProperty.Cut) {
                iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT_SPACING, propType, Value, DataLen);
                if (iRtn != 0)
                    return;
                PrinterProperty.CutSpacing = Value[0];
            } else {
                iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_TEAR_SPACING, propType, Value, DataLen);
                if (iRtn != 0)
                    return;
                PrinterProperty.TearSpacing = Value[0];
            }

            if (PrinterProperty.Pagemode) {
                iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE_AREA, propType, Value, DataLen);
                if (iRtn != 0)
                    return;
                PrinterProperty.PagemodeArea = new String(Value).trim();
            }
            Value = new byte[500];
            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_WIDTH, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.PrintableWidth = (int) (Value[0] & 0xFF | ((Value[1] & 0xFF) << 8));
        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
        }
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, receiveFilter);
        super.onResume();
    }


    private void PrintSampleReceipt() {
        try {
            byte[] data = new byte[]{0x1b, 0x40};
            HPRTPrinterHelper.WriteData(data);
//            PAct.LanguageEncode();
            PAct.BeforePrintAction();
//            HPRTPrinterHelper.PrintText("\t " + preferences.getPremiseName() + "\n");
            HPRTPrinterHelper.PrintText(Common.formatString(firstName), 32, 2, 16);
            String msg = (companyName != null ? companyName + "\n" : "");


            HPRTPrinterHelper.PrintText(msg, 32, 0, 16);
//            if(!idNumber.equals("") && idNumber!=null){
//                Log.d(TAG, "PrintSlip: ID No");


            HPRTPrinterHelper.PrintQRCode(qrCode, 7, (3 + 0x30), 1);


            HPRTPrinterHelper.PrintText("\n" + ">>>> Powered By soja.co.ke <<<<", 0, 0, 0);


            HPRTPrinterHelper.PrintText("\n", 0, 1, 0);

            PAct.AfterPrintAction();
            Log.d(TAG, "PrintSlip: Done");
            if (dialog.isShowing())
                dialog.dismiss();
            showSuccess();
//            startActivity(new Intent(getApplicationContext(), Dashboard.class));
//            finish();

        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }


}

