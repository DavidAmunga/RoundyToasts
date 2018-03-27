package miles.identigate.soja;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.eGraphicFieldType;
import com.regula.sdk.results.GraphicField;
import com.regula.sdk.results.TextField;

import java.util.ArrayList;
import java.util.List;

import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.UserInterface.RecordDriveIn;
import miles.identigate.soja.UserInterface.RecordResident;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.UserInterface.ServiceProvider;
import miles.identigate.soja.app.Common;

public class ResultsActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
	private ImageView mrzImgView;
	private ListView mrzItemsList;
	private SimpleMrzDataAdapter mAdapter;
    private List<TextField> mResultItems;
    private Button cancel;
    private Button next;

    private IntentFilter receiveFilter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.RECORDED_VISITOR)){
                finish();
            }else if(action.equals(Constants.LOGOUT_BROADCAST)){
                finish();
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

        receiveFilter = new IntentFilter();
        receiveFilter.addAction(Constants.LOGOUT_BROADCAST);
        receiveFilter.addAction(Constants.RECORDED_VISITOR);


        mrzImgView = (ImageView) findViewById(R.id.mrzImgView);
		mrzItemsList = (ListView) findViewById(R.id.mrzItemsList);
        cancel=(Button)findViewById(R.id.cancel);
        next=(Button)findViewById(R.id.next);

        mResultItems = new ArrayList<>();
        mrzItemsList.setEmptyView(findViewById(R.id.empty));

        mAdapter = new SimpleMrzDataAdapter(ResultsActivity.this,0,mResultItems);
        mrzItemsList.setAdapter(mAdapter);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Dashboard.class));
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = getIntent().getExtras();
                int TargetActivity=args.getInt("TargetActivity");
                switch(TargetActivity){
                    case Common.DRIVE_IN:
                        startActivity(new Intent(getApplicationContext(), RecordDriveIn.class));
                        overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_right);
                        finish();
                        break;
                    case Common.WALK_IN:
                        startActivity(new Intent(getApplicationContext(), RecordWalkIn.class));
                        overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_right);
                        finish();
                        break;
                    case Common.SERVICE_PROVIDER:
                        startActivity(new Intent(getApplicationContext(), ServiceProvider.class));
                        overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_right);
                        finish();
                        break;
                    case Common.RESIDENTS:
                        startActivity(new Intent(getApplicationContext(), RecordResident.class));
                        overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_right);
                        finish();
                        break;
                    case Common.INCIDENT:
                        startActivity(new Intent(getApplicationContext(), Incident.class));
                        overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_right);
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
	@Override
	protected void onResume() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, receiveFilter);

        mResultItems.addAll(DocumentReader.getAllTextFields());
        mAdapter.notifyDataSetChanged();

        GraphicField graphicField = DocumentReader.getGraphicFieldByType(eGraphicFieldType.gt_Other);
        if(graphicField!=null && graphicField.fileImage!=null) {
            mrzImgView.setImageBitmap(graphicField.fileImage);
        } else {
            mrzImgView.setVisibility(View.GONE);
        }
        super.onResume();
	}
}
