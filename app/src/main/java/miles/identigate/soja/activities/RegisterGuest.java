package miles.identigate.soja.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.font.EditTextRegular;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.R;

public class RegisterGuest extends AppCompatActivity {
    private static final String TAG = "RegisterGuest";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.phoneNo)
    EditTextRegular phoneNo;
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
    @BindView(R.id.txtName)
    EditTextRegular txtName;
    @BindView(R.id.nameLayout)
    LinearLayout nameLayout;
    @BindView(R.id.txtCompany)
    EditTextRegular txtCompany;


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
        if (Constants.documentReaderResults == null)
            finish();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register Guest");



        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
