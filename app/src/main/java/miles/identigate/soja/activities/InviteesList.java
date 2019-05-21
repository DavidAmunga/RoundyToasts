package miles.identigate.soja.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.regula.documentreader.api.DocumentReader;
import com.regula.documentreader.api.enums.DocReaderAction;
import com.regula.documentreader.api.enums.DocReaderOrientation;
import com.regula.documentreader.api.results.DocumentReaderResults;
import com.regula.documentreader.api.results.DocumentReaderScenario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.adapters.InviteeAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.interfaces.OnInviteeClick;
import miles.identigate.soja.models.Invitee;
import miles.identigate.soja.services.DataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteesList extends AppCompatActivity implements OnInviteeClick {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private static final String TAG = "InviteesList";
    InviteeAdapter inviteeAdapter;
    List<Invitee> inviteeList = new ArrayList<>();

    int targetAction = 0;

    Bundle bundle = new Bundle();

    DataService mService;

    Preferences preferences;
    @BindView(R.id.title)
    TextViewBold title;
    @BindView(R.id.lin_loading)
    LinearLayout linLoading;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private boolean isLicenseOk = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitees_list);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = new Preferences(this);

        TextView title = toolbar.findViewById(R.id.title);
        title.setText("Invitees");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        mService = Common.getDataObjService(this);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getInvitees();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: ");
                if (!swipeRefresh.isRefreshing()) {
                    Log.d(TAG, "Starting: ");
                    getInvitees();
                }
            }
        });


        if (getIntent().getExtras() != null) {
            targetAction = getIntent().getIntExtra("TargetAction", 0);
        }


        Constants.fieldItems.clear();

    }

    private void getInvitees() {

        inviteeList.clear();

        Log.d(TAG, "getInvitees: Start");
        mService.getInvitees(
                preferences.getCurrentUser().getPremiseId(),
                preferences.getDeviceId(),
                1,
                200000000
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "onResponse: Start");
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String resultText = jsonObject.getString("result_text");
                        int resultCode = jsonObject.getInt("result_code");
                        JSONArray resultContent = jsonObject.optJSONArray("result_content");

                        if (resultCode == 0 && resultText.equals("OK") && resultContent != null) {


                            Gson gson = new GsonBuilder().create();
                            inviteeList = gson.fromJson(resultContent.toString(), new TypeToken<ArrayList<Invitee>>() {
                            }.getType());


                            Log.d(TAG, "onResponse: " + inviteeList.size());

                            if (inviteeList.size() > 0) {

                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                    linLoading.setVisibility(View.GONE);

                                    inviteeAdapter = new InviteeAdapter(InviteesList.this, inviteeList, InviteesList.this::onInviteeClick);


                                    recyclerView.setAdapter(inviteeAdapter);

                                } else {
                                    swipeRefresh.setRefreshing(false);

                                    linLoading.setVisibility(View.GONE);

                                    inviteeAdapter = new InviteeAdapter(InviteesList.this, inviteeList, InviteesList.this::onInviteeClick);


                                    recyclerView.setAdapter(inviteeAdapter);

                                }


                            }


                        } else {
                            Log.d(TAG, "onResponse: ");
                            Toast.makeText(InviteesList.this, "Error", Toast.LENGTH_SHORT).show();
//                            updateLayout("");
//                            Snackbar.make(rootLayout, resultText, Snackbar.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });


    }

    @Override
    public void onInviteeClick(Invitee invitee) {
        if (invitee != null && targetAction != 0) {

            Log.d(TAG, "onInviteeClick: Invitee");

            if (isLicenseOk) {
                doScan(invitee);

            }
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
            DocumentReader.Instance().initializeReader(InviteesList.this, license, new DocumentReader.DocumentReaderInitCompletion() {
                @Override
                public void onInitCompleted(boolean success, String error) {

                    //initialization successful
                    if (success) {
                        isLicenseOk = true;
                    } else {
                        Toast.makeText(InviteesList.this, "Initializing failed:" + error, Toast.LENGTH_LONG).show();
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doScan(Invitee invitee) {
        //getting current processing scenario
        //String currentScenario = DocumentReader.Instance().processParams.scenario;
        ArrayList<String> scenarios = new ArrayList<>();
        for (DocumentReaderScenario scenario : DocumentReader.Instance().availableScenarios) {
            scenarios.add(scenario.name);
        }
        DocumentReader.Instance().processParams.scenario = scenarios.get(0);

        //starting video processing
        DocumentReader.Instance().showScanner(new DocumentReader.DocumentReaderCompletion() {
            @Override
            public void onCompleted(int action, DocumentReaderResults documentReaderResults, String error) {
                //processing is finished, all results are ready
                if (action == DocReaderAction.COMPLETE) {
                    bundle.putParcelable("invitee", invitee);
                    bundle.putInt("TargetAction", targetAction);
                    Constants.documentReaderResults = documentReaderResults;
                    Intent i = new Intent(InviteesList.this, InviteeResults.class);
                    i.putExtras(bundle);
                    InviteesList.this.startActivity(i);
                    finish();
                } else {
                    //something happened before all results were ready
                    if (action == DocReaderAction.CANCEL) {
                        Toast.makeText(InviteesList.this, "Scanning was cancelled", Toast.LENGTH_LONG).show();
                    } else if (action == DocReaderAction.ERROR) {
                        Toast.makeText(InviteesList.this, "Error:" + error, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        DocumentReader.Instance().functionality.orientation = DocReaderOrientation.LANDSCAPE;

    }

}
