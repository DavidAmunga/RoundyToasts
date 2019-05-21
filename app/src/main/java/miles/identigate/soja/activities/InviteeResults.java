package miles.identigate.soja.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.regula.documentreader.api.enums.eVisualFieldType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.SlipActivity;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.adapters.InviteeAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.Invitee;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.services.DataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteeResults extends AppCompatActivity {

    private static final String TAG = "InviteeResults";

    @BindView(R.id.title)
    TextViewBold title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txt_invitee_name)
    TextViewBold txtInviteeName;
    @BindView(R.id.txt_id_no)
    TextViewBold txtIdNo;
    @BindView(R.id.txt_id_type)
    TextViewBold txtIdType;
    @BindView(R.id.txt_gender)
    TextViewBold txtGender;
    @BindView(R.id.txt_birth_date)
    TextViewBold txtBirthDate;
    @BindView(R.id.txt_phone)
    TextViewBold txtPhone;
    @BindView(R.id.lin_phone)
    LinearLayout linPhone;
    @BindView(R.id.txt_email)
    TextViewBold txtEmail;
    @BindView(R.id.lin_email)
    LinearLayout linEmail;
    @BindView(R.id.card_invitee_details)
    CardView cardInviteeDetails;
    @BindView(R.id.txt_destination)
    TextViewBold txtDestination;
    @BindView(R.id.txt_resident_name)
    TextViewBold txtResidentName;
    @BindView(R.id.card_resident_details)
    CardView cardResidentDetails;
    @BindView(R.id.btnAction)
    Button btnAction;
    Invitee invitee;

    int visit_id = 0;


    int targetAction = 0;
    private String scan_id_type = "";
    private String idNumber = "";
    private String inviteeName = "";
    private String gender = "";
    private String dateOfBirth = "";
    private String classCode = "";
    private String nationality = "";
    private String nationCode = "";
    private String mrzLines = "";

    ProgressDialog progressDialog;


    Preferences preferences;
    private String lastName, firstName;
    String result_slip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitee_results);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        preferences = new Preferences(this);

//        mService = Common.getDataService(this);

        getSupportActionBar().setTitle("");
        title.setText("Confirm Invitee");

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();

            invitee = bundle.getParcelable("invitee");
            targetAction = bundle.getInt("TargetAction", 0);


            displayResults();

        }


        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recordInvitee();
            }
        });


    }

    private void recordInvitee() {
        String urlParameters = "";

        try {
            urlParameters =
                    "mrz=" + URLEncoder.encode(mrzLines, "UTF-8") +
                            "&scan_id_type=" + URLEncoder.encode(scan_id_type, "UTF-8") +
                            "&visitType=" + URLEncoder.encode("walk-in", "UTF-8") +
                            "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8") +
                            "&premiseZoneID=" + URLEncoder.encode(preferences.getPremiseZoneId(), "UTF-8") +
                            "&visitorTypeID=" + URLEncoder.encode("4", "UTF-8") + ///TODO: Add Visitor Type ID
//                            (preferences.isSelectHostsEnabled() && selectedHost != null ? ("&hostID=" + URLEncoder.encode(invitee.gethos)) : "") +
                            "&houseID=" + URLEncoder.encode(invitee.getHouseId(), "UTF-8") +
                            "&entryTime=" + URLEncoder.encode(Constants.getCurrentTimeStamp(), "UTF-8") +
                            "&birthDate=" + URLEncoder.encode(dateOfBirth, "UTF-8") +
                            "&genderID=" + URLEncoder.encode(gender.contains("M") ? "0" : "1", "UTF-8") +
                            "&firstName=" + URLEncoder.encode(inviteeName, "UTF-8") +
                            "&lastName=" + URLEncoder.encode(inviteeName, "UTF-8") +
                            "&idType=" + URLEncoder.encode(classCode, "UTF-8") +
                            "&idNumber=" + URLEncoder.encode(idNumber, "UTF-8") +
                            "&nationality=" + URLEncoder.encode(nationality, "UTF-8") +
                            "&bookingID=" + URLEncoder.encode(invitee.getBookingId(), "UTF-8") +
                            "&nationCode=" + URLEncoder.encode(nationCode, "UTF-8");

            Log.d(TAG, "recordInternet: " + preferences.getBaseURL() + "record-visit/" + urlParameters);

            new DriveinAsync().execute(preferences.getBaseURL() + "record-visit", urlParameters);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    private class DriveinAsync extends AsyncTask<String, Void, String> {
        MaterialDialog builder = new MaterialDialog.Builder(InviteeResults.this)
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
                            parseResult();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onPostExecute: Record Offline" + result);
                }

            } else {
            }

        }
    }


    private void parseResult() {
        if (preferences.canPrint()) {
            Intent intent = new Intent(getApplicationContext(), SlipActivity.class);
            intent.putExtra("title", "RECORD WALK IN");
            intent.putExtra("house", txtDestination.getText().toString());
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("idNumber", idNumber);
            intent.putExtra("result_slip", result_slip);
            intent.putExtra("visit_id", visit_id);
            intent.putExtra("checkInType", "walk");
            intent.putExtra("checkInMode", "ID No");
            if (preferences.isSelectHostsEnabled()) {
                intent.putExtra("host", txtResidentName.getText().toString());
            }

            startActivity(intent);
        } else {
            new MaterialDialog.Builder(InviteeResults.this)
                    .title("SUCCESS")
                    .content("Recorded successfully. Do you want to record another visit?")
                    .positiveText("YES")
                    .negativeText("NO")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                            Intent intent = new Intent(InviteeResults.this, ScanActivity.class);
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

            pushNotificationToHost();

            // Get instance of Vibrator from current Context
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// Vibrate for 400 milliseconds
            v.vibrate(400);
        }
    }

    private void pushNotificationToHost() {

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
            parseResult();
        } else {
            Log.d(TAG, "resultHandler: " + result);
            if (resultText.contains("still in")) {
                Log.d(TAG, "resultHandler: Still In");
                new MaterialDialog.Builder(InviteeResults.this)
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
                new MaterialDialog.Builder(InviteeResults.this)
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
        MaterialDialog builder = new MaterialDialog.Builder(InviteeResults.this)
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
                            recordInvitee();
                        } else {
                            new MaterialDialog.Builder(InviteeResults.this)
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
                        new MaterialDialog.Builder(InviteeResults.this)
                                .title("Result")
                                .content("Poor internet connection.")
                                .positiveText("Ok")
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                new MaterialDialog.Builder(InviteeResults.this)
                        .title("Result")
                        .content("Poor internet connection.")
                        .positiveText("Ok")
                        .show();
            }
        }
    }


    private void displayResults() {
        String idN = "000000000";
        scan_id_type = "ID";
        classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");


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

        mrzLines = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);


        inviteeName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");

        gender = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n");
        dateOfBirth = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n");
        nationality = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n");
        nationCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n");


        txtIdNo.setText(idNumber);
        txtIdType.setText(scan_id_type);
        txtBirthDate.setText(formatDate(dateOfBirth));
        txtGender.setText(gender.equals("M") ? "Male" : "Female");
        txtDestination.setText(invitee.getDestination());
        txtInviteeName.setText(invitee.getFirstName() + " " + (invitee.getLastName() != null ? invitee.getLastName() : ""));
        txtResidentName.setText(invitee.getHostFirstName() + " " + (invitee.getHostLastName() != null ? invitee.getHostLastName() : ""));


        if (!TextUtils.isEmpty(invitee.getEmail())) {
            linEmail.setVisibility(View.VISIBLE);
        } else {
            linEmail.setVisibility(View.GONE);
            txtEmail.setText(invitee.getEmail());

        }


        if (!TextUtils.isEmpty(invitee.getPhone())) {
            linPhone.setVisibility(View.VISIBLE);
        } else {
            linPhone.setVisibility(View.GONE);
            txtPhone.setText(invitee.getPhone());

        }

    }

    public String formatDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("mm/dd/yy");

        try {
            Date oldDate = format.parse(date);
            String newDate;
            newDate = new SimpleDateFormat("EEE dd, MMM YYYY").format(oldDate);
            return newDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Date";

    }


}
