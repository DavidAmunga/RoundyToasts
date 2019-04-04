package miles.identigate.soja.Logs;

import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import miles.identigate.soja.Adapters.DriveInAdapter;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.R;

public class AllLogs extends SojaActivity {
    ListView lv;
    DatabaseHandler handler;
    DriveInAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    //TextView type;
    ContentLoadingProgressBar progressBar;
    Preferences preferences;
//    private EditText searchbox;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

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
        lv = findViewById(R.id.visitors);
        lv.setVisibility(View.VISIBLE);
        progressBar = findViewById(R.id.loading);
//        searchbox = findViewById(R.id.searchbox);
        progressBar.setVisibility(View.GONE);
        lv.setEmptyView(findViewById(R.id.empty));
        //type=(TextView)findViewById(R.id.type);
        handler = new DatabaseHandler(this);
        if (getIntent() != null) {
            String str = getIntent().getStringExtra("TYPE");
            if (str.equals("DRIVE")) {
                title.setText("List Of Recent Vehicles");
                driveIns = handler.getDriveIns(1);
                adapter = new DriveInAdapter(this, handler.getDriveIns(1), 1);
                adapter.notifyDataSetChanged();
            } else if (str.equals("WALK")) {
                title.setText("List Of Recent Pedestrians");
                walkIns = handler.getWalk(1);
                adapter = new DriveInAdapter(this, handler.getWalk(1), "WALK");
                adapter.notifyDataSetChanged();
            } else if (str.equals("PROVIDER")) {
                title.setText("Service providers");
                serviceProviderModels = handler.getProviders(1);
                adapter = new DriveInAdapter(this, "TYPE", handler.getProviders(1));
                adapter.notifyDataSetChanged();
            } else if (str.equals("RESIDENTS")) {
                title.setText("List of Recent Residents");
                residents = handler.getResidents(1);
                adapter = new DriveInAdapter(this, handler.getResidents(1));
                adapter.notifyDataSetChanged();
            }
        } else {
            finish();
        }
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
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
}
