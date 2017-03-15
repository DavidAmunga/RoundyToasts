package miles.identigate.soja;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;



import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.ZxingHelperActivity;


public class ScanQRActivity extends AppCompatActivity {
    ImageView scan_icon;
    MaterialDialog dialog;
    private static String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialog=new MaterialDialog.Builder(ScanQRActivity.this)
                .title("QR")
                .content("Checking QR...")
                .progress(true,0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();
        scan_icon=(ImageView)findViewById(R.id.scan_icon);
        scan_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanQRActivity.this).setCaptureActivity(ZxingHelperActivity.class).initiateScan();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
            } else {
                Log.v("QR",result.getContents());
                token=result.getContents();
                new Validate().execute(Constants.BASE_URL+"qr_data/"+result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private class Validate extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (dialog!= null && !dialog.isShowing())
                dialog.show();
        }
        protected String  doInBackground(String... params) {
            return new NetworkHandler().GET(params[0]);
        }
        protected void onPostExecute(String result) {
            if (dialog!= null && dialog.isShowing())
                dialog.dismiss();
            if (result != null){
                //Log.e("QR",result);
                //Log.e("SCAN",result);
                Object json=null;
                try{
                    json=new JSONTokener(result).nextValue();
                    if (json instanceof JSONObject){
                        Intent intent=new Intent(getApplicationContext(),RecordResidentVehicleActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("token",token);
                        JSONObject object=new JSONObject(result);
                        int result_code=object.getInt("result_code");
                        if (result_code==0){
                            bundle.putInt("Type",0);
                            JSONObject content=object.getJSONObject("result_content");
                            String registration=content.getString("registration");
                            String model=content.getString("model");
                            String type=content.getString("type");
                            String name=content.getString("name");
                            String house=content.getString("house");
                            bundle.putString("registration",registration);
                            bundle.putString("model",model);
                            bundle.putString("type",type);
                            bundle.putString("name",name);
                            bundle.putString("house",house);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else{
                            bundle.putInt("Type",result_code);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

        }
    }
}
