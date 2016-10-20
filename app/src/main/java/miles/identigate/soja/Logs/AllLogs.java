package miles.identigate.soja.Logs;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
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
        lv.setEmptyView(findViewById(R.id.empty));
        type=(TextView)findViewById(R.id.type);
        handler=new DatabaseHandler(this);
        if(getIntent()!=null){
            String str=getIntent().getStringExtra("TYPE");
            if(str.equals("DRIVE")){
                type.setText("Drive in");
                driveIns=handler.getDriveIns(1);
                adapter=new DriveInAdapter(this,handler.getDriveIns(1),1);
                adapter.notifyDataSetChanged();
            }else if(str.equals("WALK")){
                type.setText("Walk in");
                walkIns=handler.getWalk(1);
                adapter=new DriveInAdapter(this,handler.getWalk(1),"WALK");
                adapter.notifyDataSetChanged();
            }else if(str.equals("PROVIDER")){
                type.setText("Service providers");
                serviceProviderModels=handler.getProviders(1);
                adapter=new DriveInAdapter(this,"TYPE",handler.getProviders(1));
                adapter.notifyDataSetChanged();
            }else if(str.equals("RESIDENTS")){
                type.setText("Residents");
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
}
