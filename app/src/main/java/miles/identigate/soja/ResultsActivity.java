package miles.identigate.soja;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.regula.documentreader.api.enums.eGraphicFieldType;
import com.regula.documentreader.api.enums.eVisualFieldType;
import com.regula.documentreader.api.results.DocumentReaderTextField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.UserInterface.RecordDriveIn;
import miles.identigate.soja.UserInterface.RecordResident;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.UserInterface.ServiceProvider;
import miles.identigate.soja.activities.RegisterGuest;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.Preferences;

public class ResultsActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "ResultsActivity";
    @BindView(R.id.mrzImgView)
    ImageView mrzImgView;
    @BindView(R.id.txt_visitor_name)
    TextViewBold txtVisitorName;
    @BindView(R.id.txt_id_no)
    TextViewBold txtIdNo;
    @BindView(R.id.txt_id_type)
    TextViewBold txtIdType;
    @BindView(R.id.txt_gender)
    TextViewBold txtGender;
    @BindView(R.id.txt_birth_date)
    TextViewBold txtBirthDate;
    @BindView(R.id.card_visitor_details)
    CardView cardVisitorDetails;
    @BindView(R.id.mrzItemsList)
    ListView mrzItemsList;
    @BindView(R.id.empty)
    LinearLayout empty;
    @BindView(R.id.cancel)
    AppCompatButton cancel;
    @BindView(R.id.next)
    AppCompatButton next;
    @BindView(R.id.txt_nation_code)
    TextViewBold txtNationCode;
    @BindView(R.id.txt_nationality)
    TextViewBold txtNationality;

    private SimpleMrzDataAdapter mAdapter;
    private List<DocumentReaderTextField> mResultItems;
    Preferences preferences;

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
        preferences = new Preferences(this);

        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        ButterKnife.bind(this);
        if (Constants.documentReaderResults == null)
            finish();

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);


        mResultItems = new ArrayList<>();
//        mrzItemsList.setEmptyView(findViewById(R.id.empty));


//        mAdapter = new SimpleMrzDataAdapter(ResultsActivity.this, 0, mResultItems);

//        mrzItemsList.setAdapter(mAdapter);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                finish();
            }
        });

        displayResults();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = getIntent().getExtras();
                int TargetActivity = args.getInt("TargetActivity");
                switch (TargetActivity) {
                    case Common.DRIVE_IN:
                        startActivity(new Intent(getApplicationContext(), RecordDriveIn.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.WALK_IN:
                        startActivity(new Intent(getApplicationContext(), RecordWalkIn.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.SERVICE_PROVIDER:
                        startActivity(new Intent(getApplicationContext(), ServiceProvider.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.RESIDENTS:
                        startActivity(new Intent(getApplicationContext(), RecordResident.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.REGISTER_GUEST:
                        startActivity(new Intent(getApplicationContext(), RegisterGuest.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.CHECK_IN_GUEST:
                        startActivity(new Intent(getApplicationContext(), RecordResident.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.ISSUE_TICKET:
                        startActivity(new Intent(getApplicationContext(), RecordResident.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.INCIDENT:
                        startActivity(new Intent(getApplicationContext(), Incident.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                    case Common.WALK_IN_INVITEE:
                        startActivity(new Intent(getApplicationContext(), RecordDriveIn.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                    case Common.DRIVE_IN_INVITEE:
                        startActivity(new Intent(getApplicationContext(), RecordDriveIn.class));
                        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onPause();
    }

    private void displayResults() {
        String idN = "000000000";
        String scan_id_type = "ID";
        String classCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DOCUMENT_CLASS_CODE).replace("^", "\n");


        String idNumber = "";
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

        String mrzLines = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_MRZ_STRINGS);


        String visitorName = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SURNAME_AND_GIVEN_NAMES).replace("^", "\n");

        String gender = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_SEX).replace("^", "\n");
        String dateOfBirth = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_DATE_OF_BIRTH).replace("^", "\n");
        String nationality = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_NAME).replace("^", "\n");
        String nationCode = Constants.documentReaderResults.getTextFieldValueByType(eVisualFieldType.FT_ISSUING_STATE_CODE).replace("^", "\n");


        txtIdNo.setText(idNumber);
        txtIdType.setText(scan_id_type);
        txtBirthDate.setText(formatDate(dateOfBirth));
        txtGender.setText(gender.equals("M") ? "Male" : "Female");
        txtVisitorName.setText(Common.capitalizer(visitorName));
        txtNationality.setText(nationality);
        txtNationCode.setText(nationCode);


    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, receiveFilter);

        mResultItems.addAll(Constants.documentReaderResults.textResult.fields);
//        mAdapter.notifyDataSetChanged();

        Bitmap documentImage = Constants.documentReaderResults.getGraphicFieldImageByType(eGraphicFieldType.GT_DOCUMENT_FRONT);

        if (documentImage != null) {
            mrzImgView.setImageBitmap(documentImage);
        } else {
            mrzImgView.setVisibility(View.GONE);
        }


        super.onResume();
    }

    public String formatDate(String date) {
        Log.d(TAG, "formatDate: " + date);
        SimpleDateFormat format = new SimpleDateFormat("mm/dd/yy");

        try {
            Date oldDate = format.parse(date);
            String newDate;
            newDate = new SimpleDateFormat("EEE dd, MMM yyyy").format(oldDate);
            return newDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Date";

    }
}
