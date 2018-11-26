package miles.identigate.soja;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import miles.identigate.soja.Fragments.FingerprintRegistrationFragment;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.Visitor;

public class FingerprintActivity extends SojaActivity implements FingerprintRegistrationFragment.OnFragmentInteractionListener {
    ImageView fingerprint;
    TextView place_finger;
    RelativeLayout progressLayout;
    ProgressBar progressBar;
    Button ok_button;
    LinearLayout info;
    TextView name;
    TextView idNUmber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fingerprint = (ImageView)findViewById(R.id.fingerprint);
        place_finger = (TextView)findViewById(R.id.place_finger);
        progressLayout = (RelativeLayout)findViewById(R.id.progressLayout);
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
    }
}
