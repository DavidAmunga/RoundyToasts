package miles.identigate.soja.Logs;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import miles.identigate.soja.Adapters.DriveInAdapter;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.Models.Visitor;
import miles.identigate.soja.R;

public class AllLogs extends SojaActivity {
    ListView lv;
    DatabaseHandler handler;
    DriveInAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    TextView type;
    ContentLoadingProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        driveIns=new ArrayList<>();
        walkIns=new ArrayList<>();
        serviceProviderModels=new ArrayList<>();
        residents=new ArrayList<>();
        lv=(ListView)findViewById(R.id.visitors);
        lv.setVisibility(View.VISIBLE);
        progressBar=(ContentLoadingProgressBar)findViewById(R.id.loading);
        progressBar.setVisibility(View.GONE);
        lv.setEmptyView(findViewById(R.id.empty));
        type=(TextView)findViewById(R.id.type);
        handler=new DatabaseHandler(this);
        if(getIntent()!=null){
            String str=getIntent().getStringExtra("TYPE");
            if(str.equals("DRIVE")){
                type.setText("List Of Recent Vehicles");
                driveIns=handler.getDriveIns(1);
                adapter=new DriveInAdapter(this,handler.getDriveIns(1),1);
                adapter.notifyDataSetChanged();
            }else if(str.equals("WALK")){
                type.setText("List Of Recent Pedestrians");
                walkIns=handler.getWalk(1);
                adapter=new DriveInAdapter(this,handler.getWalk(1),"WALK");
                adapter.notifyDataSetChanged();
            }else if(str.equals("PROVIDER")){
                type.setText("Service providers");
                serviceProviderModels=handler.getProviders(1);
                adapter=new DriveInAdapter(this,"TYPE",handler.getProviders(1));
                adapter.notifyDataSetChanged();
            }else if(str.equals("RESIDENTS")){
                type.setText("List of Recent Residents");
                residents=handler.getResidents(1);
                adapter=new DriveInAdapter(this,handler.getResidents(1));
                adapter.notifyDataSetChanged();
            }
        }else{
            finish();
        }
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              /* Intent exit= new Intent(getApplicationContext(), RecordExit.class);
                Visitor vist=(Visitor)parent.getItemAtPosition(position);
                exit.putExtra("id",vist.getId());
                startActivity(exit);*/

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
}
