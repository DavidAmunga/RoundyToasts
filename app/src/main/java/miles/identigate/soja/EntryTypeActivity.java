package miles.identigate.soja;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;


import miles.identigate.soja.fragments.EntryTypeFragment;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.app.Common;

public class EntryTypeActivity extends SojaActivity implements EntryTypeFragment.OnEntrySelectedListener {

    private static boolean sIsLicenseOk;

    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_entry_type);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.pull_in_left,R.anim.push_out_left);
        EntryTypeFragment entryTypeFragment=new EntryTypeFragment();
        Bundle args = new Bundle();
        args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
        entryTypeFragment.setArguments(args);
        transaction.replace(R.id.parent,entryTypeFragment);
        transaction.addToBackStack(null);
        transaction.commit();*/
    }

    @Override
    public void OnEntrySelected(int type) {
        //Direct to correct activity
        if(type== Common.SCAN){
            /*Intent intent=new Intent(getApplicationContext(), ScanActivity.class);
            Bundle arg=new Bundle();
            arg.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
            intent.putExtras(arg);
            startActivity(intent);*/

        }else{
            //Replace with Manual fragment first
            FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.pull_in_left,R.anim.push_out_left);
            ManualFragment entryTypeFragment=new ManualFragment();
            Bundle args = new Bundle();
            args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
            entryTypeFragment.setArguments(args);
            transaction.replace(R.id.parent,entryTypeFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();


    }


}
