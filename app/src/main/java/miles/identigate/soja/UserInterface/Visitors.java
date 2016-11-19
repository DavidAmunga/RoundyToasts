package miles.identigate.soja.UserInterface;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import miles.identigate.soja.Adapters.DriveInAdapter;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.R;

public class Visitors extends AppCompatActivity {
    ListView lv;
    DatabaseHandler handler;
    DriveInAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    TextView type;
    String str;
    private ContentLoadingProgressBar loading;
    private Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferences=new Preferences(this);
        driveIns=new ArrayList<>();
        walkIns=new ArrayList<>();
        serviceProviderModels=new ArrayList<>();
        residents=new ArrayList<>();
        lv=(ListView)findViewById(R.id.visitors);
        //lv.setEmptyView(findViewById(R.id.empty));
        type=(TextView)findViewById(R.id.type);
        loading=(ContentLoadingProgressBar)findViewById(R.id.loading);
        handler=new DatabaseHandler(this);
        if(getIntent()!=null){
            str=getIntent().getStringExtra("TYPE");
            if(str.equals("DRIVE")){
                type.setText("Drive in");
                //driveIns=handler.getDriveIns(0);
                adapter=new DriveInAdapter(this,driveIns,1);
                //adapter.notifyDataSetChanged();
            }else if(str.equals("WALK")){
                type.setText("Walk in");
                //walkIns=handler.getWalk(0);
                adapter=new DriveInAdapter(this,walkIns,"WALK");
                //adapter.notifyDataSetChanged();
            }else if(str.equals("PROVIDER")){
                type.setText("Service providers");
                //serviceProviderModels=handler.getProviders(0);
                adapter=new DriveInAdapter(this,"TYPE",serviceProviderModels);
                //adapter.notifyDataSetChanged();
            }else if(str.equals("RESIDENTS")){
                type.setText("Residents");
                //residents=handler.getResidents(0);
                adapter=new DriveInAdapter(this,residents);
                //adapter.notifyDataSetChanged();
            }
        }else{
            finish();
        }
        lv.setAdapter(adapter);
        if (new CheckConnection().check(this)){
            new GetActiveVisitors().execute(Constants.GET_VISITORS_URL+preferences.getPremise());
        }else {
            loading.setVisibility(View.GONE);
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent exit= new Intent(getApplicationContext(), RecordExit.class);
                if(str.equals("DRIVE")){
                        DriveIn drive=(DriveIn)parent.getItemAtPosition(position);
                    //Toast.makeText(getApplicationContext(),drive.getExitTime(),Toast.LENGTH_LONG).show();
                        exit.putExtra("TYPE","DRIVE");
                        exit.putExtra("NAME",drive.getName());
                        exit.putExtra("ID",drive.getNationalId());
                        exit.putExtra("ENTRY",drive.getEntryTime());
                        exit.putExtra("CAR",drive.getCarNumber());
                    startActivity(exit);

                }else if(str.equals("WALK")){
                    DriveIn walk=(DriveIn)parent.getItemAtPosition(position);
                    exit.putExtra("TYPE","WALK");
                    exit.putExtra("NAME",walk.getName());
                    exit.putExtra("ID",walk.getNationalId());
                    exit.putExtra("ENTRY",walk.getEntryTime());
                    startActivity(exit);

                }else if(str.equals("PROVIDER")){
                    ServiceProviderModel service=(ServiceProviderModel)parent.getItemAtPosition(position);
                    exit.putExtra("TYPE","PROVIDER");
                    exit.putExtra("NAME",service.getCompanyName());
                    exit.putExtra("ID",service.getNationalId());
                    exit.putExtra("ENTRY",service.getEntryTime());
                    exit.putExtra("PROVIDERNAME",service.getProviderName());
                    startActivity(exit);

                }else if(str.equals("RESIDENTS")){
                    Resident res=(Resident)parent.getItemAtPosition(position);
                    exit.putExtra("TYPE","RESIDENT");
                    exit.putExtra("NAME",res.getName());
                    exit.putExtra("ID",res.getNationalId());
                    exit.putExtra("ENTRY",res.getEntryTime());
                    exit.putExtra("HOUSE",res.getHouse());
                    startActivity(exit);
                }

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_visitors, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
       /* switch (item.getItemId()) {
            case R.id.search:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
        // }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }
    private class GetActiveVisitors extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            return new NetworkHandler().GET(strings[0]);
        }
        @Override
        public void onPostExecute(String s){
            driveIns.clear();
            walkIns.clear();
            serviceProviderModels.clear();
            residents.clear();
            //adapter.notifyDataSetChanged();
            if (s != null){
                Log.e("Result",s);
                Object json=null;
                try {
                    json=new JSONTokener(s).nextValue();
                    if (json instanceof JSONObject){
                        JSONObject object=new JSONObject(s);
                        JSONArray array=object.getJSONArray("result_content");
                        if (array.length() >0 ){
                            for (int i=0;i<array.length();i++){
                                JSONObject item=array.getJSONObject(i);
                                String name=item.getString("fullname");
                                String id=item.getString("id_number");
                                String entry=item.getString("entry_time");
                                String house=item.getString("house");
                                if(str.equals("DRIVE")){
                                    DriveIn driveIn=new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);
                                    driveIn.setCarNumber(item.getString("registration"));
                                    driveIns.add(driveIn);
                                }else if(str.equals("WALK")){
                                    DriveIn driveIn=new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);
                                    walkIns.add(driveIn);
                                }else if(str.equals("PROVIDER")){
                                    ServiceProviderModel model=new ServiceProviderModel();
                                    model.setCompanyName(name);
                                    model.setEntryTime(entry);
                                    model.setNationalId(id);
                                    serviceProviderModels.add(model);
                                }else if(str.equals("RESIDENTS")){
                                    Resident resident=new Resident();
                                    resident.setName(name);
                                    resident.setEntryTime(entry);
                                    resident.setNationalId(id);
                                    resident.setHouse(house);
                                    residents.add(resident);
                                }
                            }
                            loading.setVisibility(View.GONE);
                            lv.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }else{
                            loading.setVisibility(View.GONE);
                            findViewById(R.id.empty).setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                loading.setVisibility(View.GONE);
                findViewById(R.id.empty).setVisibility(View.VISIBLE);
              //No logs
            }

        }
    }
}
