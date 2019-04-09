package miles.identigate.soja.logs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import miles.identigate.soja.R;
import miles.identigate.soja.UserInterface.RecordExit;
import miles.identigate.soja.adapters.DriveInRecyclerAdapter;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.Resident;
import miles.identigate.soja.models.ServiceProviderModel;

public class AllLogs extends SojaActivity implements OnItemClick {
    RecyclerView recyclerView;
    DatabaseHandler handler;
    DriveInRecyclerAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    //TextView type;
    ContentLoadingProgressBar progressBar;
    Preferences preferences;
    //    private EditText searchbox;
    String str;


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title);
        title.setText("Logs");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        driveIns = new ArrayList<>();
        walkIns = new ArrayList<>();
        serviceProviderModels = new ArrayList<>();
        residents = new ArrayList<>();
//        lv = findViewById(R.id.visitors);
//        lv.setVisibility(View.VISIBLE);
        recyclerView=findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.loading);
//        searchbox = findViewById(R.id.searchbox);
        progressBar.setVisibility(View.GONE);
//        lv.setEmptyView(findViewById(R.id.empty));
        //type=(TextView)findViewById(R.id.type);
        handler = new DatabaseHandler(this);
        if (getIntent() != null) {
            str = getIntent().getStringExtra("TYPE");
            if (str.equals("DRIVE")) {
                title.setText("List Of Recent Vehicles");
                driveIns = handler.getDriveIns(1);
                adapter = new DriveInRecyclerAdapter(this, handler.getDriveIns(1), 1, this);
                adapter.notifyDataSetChanged();
            } else if (str.equals("WALK")) {
                title.setText("List Of Recent Pedestrians");
                walkIns = handler.getWalk(1);
                adapter = new DriveInRecyclerAdapter(this, handler.getWalk(1), "WALK", this);
                adapter.notifyDataSetChanged();
            } else if (str.equals("PROVIDER")) {
                title.setText("Service providers");
                serviceProviderModels = handler.getProviders(1);
                adapter = new DriveInRecyclerAdapter(this, "TYPE", handler.getProviders(1), this);
                adapter.notifyDataSetChanged();
            } else if (str.equals("RESIDENTS")) {
                title.setText("List of Recent Residents");
                residents = handler.getResidents(1);
                adapter = new DriveInRecyclerAdapter(this, handler.getResidents(1), this);
                adapter.notifyDataSetChanged();
            }
        } else {
            finish();
        }
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
//        searchbox.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void afterTextChanged(Editable arg0) {
//
//                String text = searchbox.getText().toString().toLowerCase(Locale.getDefault());
//                adapter.filter(text);
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1,
//                                          int arg2, int arg3) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
//                                      int arg3) {
//                // TODO Auto-generated method stub
//            }
//        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visitors, menu);

        MenuItem mSearch = menu.findItem(R.id.nav_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setIconifiedByDefault(false);

        mSearchView.setQueryHint("Search");


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

//                String text = searchbox.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(newText);
                return true;
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onVisitorClick(Object object) {
        Intent exit = new Intent(getApplicationContext(), RecordExit.class);
        if (str.equals("DRIVE")) {
            DriveIn drive = (DriveIn) object;
            //Toast.makeText(getApplicationContext(),drive.getExitTime(),Toast.LENGTH_LONG).show();
            exit.putExtra("TYPE", "DRIVE");
            exit.putExtra("NAME", drive.getName());
            exit.putExtra("ID", drive.getNationalId());
            exit.putExtra("ENTRY", drive.getEntryTime());
            exit.putExtra("CAR", drive.getCarNumber());
            startActivity(exit);

        } else if (str.equals("WALK")) {
            DriveIn walk = (DriveIn) object;
            exit.putExtra("TYPE", "WALK");
            exit.putExtra("NAME", walk.getName());
            exit.putExtra("ID", walk.getNationalId());
            exit.putExtra("ENTRY", walk.getEntryTime());
            startActivity(exit);

        } else if (str.equals("PROVIDER")) {
            ServiceProviderModel service = (ServiceProviderModel) object;
            exit.putExtra("TYPE", "PROVIDER");
            exit.putExtra("NAME", service.getCompanyName());
            exit.putExtra("ID", service.getNationalId());
            exit.putExtra("ENTRY", service.getEntryTime());
            exit.putExtra("PROVIDERNAME", service.getProviderName());
            startActivity(exit);

        } else if (str.equals("RESIDENTS")) {
            Resident res = (Resident) object;
            exit.putExtra("TYPE", "RESIDENT");
            exit.putExtra("NAME", res.getName());
            exit.putExtra("ID", res.getNationalId());
            exit.putExtra("ENTRY", res.getEntryTime());
            exit.putExtra("HOUSE", res.getHouse());
            startActivity(exit);
        }
    }
}
