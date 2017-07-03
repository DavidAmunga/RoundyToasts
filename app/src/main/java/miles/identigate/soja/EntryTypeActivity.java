package miles.identigate.soja;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.regula.sdk.CaptureActivity;
import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.MRZDetectorErrorCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import miles.identigate.soja.Fragments.EntryTypeFragment;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.app.Common;

public class EntryTypeActivity extends SojaActivity implements EntryTypeFragment.OnEntrySelectedListener {

    private static boolean sIsLicenseOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_type);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
