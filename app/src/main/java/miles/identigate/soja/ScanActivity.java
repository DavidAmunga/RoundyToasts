package miles.identigate.soja;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;


import com.regula.documentreader.api.DocumentReader;
import com.regula.documentreader.api.enums.DocReaderAction;
import com.regula.documentreader.api.results.DocumentReaderResults;
import com.regula.documentreader.api.results.DocumentReaderScenario;

import java.io.InputStream;
import java.util.ArrayList;

import miles.identigate.soja.Fragments.EntryTypeFragment;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.app.Common;


public class ScanActivity extends Activity implements EntryTypeFragment.OnEntrySelectedListener{
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    private boolean isLicenseOk = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_entry_type);
        Constants.fieldItems.clear();
        FragmentTransaction transaction=getFragmentManager().beginTransaction();
        //transaction.setCustomAnimations(R.anim.pull_in_left,R.anim.push_out_left);
        EntryTypeFragment entryTypeFragment=new EntryTypeFragment();
        Bundle args = new Bundle();
        args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
        entryTypeFragment.setArguments(args);
        transaction.replace(R.id.parent,entryTypeFragment);
        transaction.commit();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

        }
    }


    @Override
    public void OnEntrySelected(int type) {
        if (isLicenseOk) {
            if(type== Common.SCAN){
                doScan();
            }else{
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
            builder.setTitle(R.string.strError);
            builder.setMessage("Invalid license.");
            builder.setPositiveButton(R.string.strOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }
            });
            //builder.show();
            Toast.makeText(ScanActivity.this, "Invalid license.", Toast.LENGTH_LONG).show();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            InputStream licInput = getResources().openRawResource(R.raw.regula);
            int available = licInput.available();
            byte[] license = new byte[available];
            //noinspection ResultOfMethodCallIgnored
            licInput.read(license);
            //Initializing the reader
            DocumentReader.Instance().initializeReader(ScanActivity.this, license, new DocumentReader.DocumentReaderInitCompletion() {
                @Override
                public void onInitCompleted(boolean success, String error) {

                    //initialization successful
                    if (success) {
                        isLicenseOk = true;
                    }else {
                        Toast.makeText(ScanActivity.this, "Initializing failed:" + error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void doScan(){
        //getting current processing scenario
        //String currentScenario = DocumentReader.Instance().processParams.scenario;
        ArrayList<String> scenarios = new ArrayList<>();
        for(DocumentReaderScenario scenario: DocumentReader.Instance().availableScenarios){
            scenarios.add(scenario.name);
        }
        DocumentReader.Instance().processParams.scenario = scenarios.get(0);
        //starting video processing
        DocumentReader.Instance().showScanner(new DocumentReader.DocumentReaderCompletion() {
            @Override
            public void onCompleted(int action, DocumentReaderResults documentReaderResults, String error) {
                //processing is finished, all results are ready
                if (action == DocReaderAction.COMPLETE) {
                    Constants.documentReaderResults = documentReaderResults;
                    Intent i = new Intent(ScanActivity.this, ResultsActivity.class);
                    Bundle args = new Bundle();
                    args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
                    i.putExtras(args);
                    ScanActivity.this.startActivity(i);
                    finish();
                } else {
                    //something happened before all results were ready
                    if (action == DocReaderAction.CANCEL) {
                        Toast.makeText(ScanActivity.this, "Scanning was cancelled", Toast.LENGTH_LONG).show();
                    } else if (action == DocReaderAction.ERROR) {
                        Toast.makeText(ScanActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}

