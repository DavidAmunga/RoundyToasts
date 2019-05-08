package miles.identigate.soja.UserInterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;
import miles.identigate.soja.R;
import miles.identigate.soja.adapters.DriveInRecyclerAdapter;
import miles.identigate.soja.helpers.CheckConnection;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.RecyclerItemTouchHelper;
import miles.identigate.soja.helpers.RecyclerItemTouchHelperListener;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.models.Resident;
import miles.identigate.soja.models.ServiceProviderModel;
import miles.identigate.soja.services.DataService;
import miles.identigate.soja.services.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Visitors extends AppCompatActivity implements RecyclerItemTouchHelperListener, OnItemClick {
    DatabaseHandler handler;
    //    DriveInAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    ArrayList<DriveIn> sortedDriveIns = new ArrayList<>();
    ArrayList<DriveIn> sortedWalkIns = new ArrayList<>();
    ArrayList<Resident> sortedResidentList = new ArrayList<>();

    DriveInRecyclerAdapter driveInRecyclerAdapter;
    String str;
    @BindView(R.id.empty)
    LinearLayout empty;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.root_layout)
    RelativeLayout rootLayout;
    private ContentLoadingProgressBar loading;
    private Preferences preferences;
    private static final String TAG = "Visitors";
    TextView info_message;
    String[] filterItems = {"Entry Time", "SMS Login", "Alphabetically"};
    private boolean canDeleteItem = true;

    Date selectedDate = new Date();


    /**
     * end after 1 day from now
     */
    Calendar endDate = Calendar.getInstance();

    /**
     * start before  day from now
     */
    Calendar startDate = Calendar.getInstance();


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
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title);
        title.setText("Check Out");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        preferences = new Preferences(this);
        driveIns = new ArrayList<>();
        walkIns = new ArrayList<>();
        serviceProviderModels = new ArrayList<>();
        residents = new ArrayList<>();
        loading = findViewById(R.id.loading);
        handler = new DatabaseHandler(this);
        info_message = findViewById(R.id.info_message);


        endDate.add(Calendar.DAY_OF_MONTH, 1);
        startDate.add(Calendar.DAY_OF_MONTH, -3);


        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .startDate(startDate.getTime())
                .endDate(endDate.getTime())
                .defaultSelectedDate(Calendar.getInstance().getTime())
                .build();


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {

                Log.d(TAG, "onDateSelected: Sorting");
                selectedDate = date;
                sortVisitorsByEntryTime();

//                Toast.makeText(Visitors.this, date.toString(), Toast.LENGTH_SHORT).show();
            }
        });

//        Setup Recycler View

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);


        if (getIntent() != null) {
            str = getIntent().getStringExtra("TYPE");
            if (str.equals("DRIVE")) {
                title.setText("Drive Out");
                //driveIns=handler.getDriveIns(0);
                driveInRecyclerAdapter = new DriveInRecyclerAdapter(this, driveIns, 1, this);
//                adapter = new DriveInAdapter(this, driveIns, 1);
                //adapter.notifyDataSetChanged();
            } else if (str.equals("WALK")) {
                title.setText("Walk Out");
                //walkIns=handler.getWalk(0);
                driveInRecyclerAdapter = new DriveInRecyclerAdapter(this, walkIns, "WALK", this);

//                adapter = new DriveInAdapter(this, walkIns, "WALK");
                //adapter.notifyDataSetChanged();
            } else if (str.equals("PROVIDER")) {
                title.setText("Service Providers");
                //serviceProviderModels=handler.getProviders(0);
                driveInRecyclerAdapter = new DriveInRecyclerAdapter(this, "TYPE", serviceProviderModels, this);

//                adapter = new DriveInAdapter(this, "TYPE", serviceProviderModels);
                //adapter.notifyDataSetChanged();
            } else if (str.equals("RESIDENTS")) {
                title.setText("Residents");
                //residents=handler.getResidents(0);
                driveInRecyclerAdapter = new DriveInRecyclerAdapter(this, residents, this);

//                adapter = new DriveInAdapter(this, residents);
                //adapter.notifyDataSetChanged();
            }
        } else {
            finish();
        }


        recyclerView.setAdapter(driveInRecyclerAdapter);
        if (new CheckConnection().check(this)) {
            String s = preferences.getBaseURL();
            String url = s.substring(0, s.length() - 11);
            new GetActiveVisitors().execute(url + "api/visitors/visitors_in/" + preferences.getPremise());
            Log.d(TAG, "Visitors: " + url + "api/visitors/visitors_in/" + preferences.getPremise());
        } else {
            loading.setVisibility(View.GONE);
            info_message.setText("No Data Available");
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
        }

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent exit = new Intent(getApplicationContext(), RecordExit.class);
//                if (str.equals("DRIVE")) {
//                    DriveIn drive = (DriveIn) parent.getItemAtPosition(position);
//                    //Toast.makeText(getApplicationContext(),drive.getExitTime(),Toast.LENGTH_LONG).show();
//                    exit.putExtra("TYPE", "DRIVE");
//                    exit.putExtra("NAME", drive.getName());
//                    exit.putExtra("ID", drive.getNationalId());
//                    exit.putExtra("ENTRY", drive.getEntryTime());
//                    exit.putExtra("CAR", drive.getCarNumber());
//                    startActivity(exit);
//
//                } else if (str.equals("WALK")) {
//                    DriveIn walk = (DriveIn) parent.getItemAtPosition(position);
//                    exit.putExtra("TYPE", "WALK");
//                    exit.putExtra("NAME", walk.getName());
//                    exit.putExtra("ID", walk.getNationalId());
//                    exit.putExtra("ENTRY", walk.getEntryTime());
//                    startActivity(exit);
//
//                } else if (str.equals("PROVIDER")) {
//                    ServiceProviderModel service = (ServiceProviderModel) parent.getItemAtPosition(position);
//                    exit.putExtra("TYPE", "PROVIDER");
//                    exit.putExtra("NAME", service.getCompanyName());
//                    exit.putExtra("ID", service.getNationalId());
//                    exit.putExtra("ENTRY", service.getEntryTime());
//                    exit.putExtra("PROVIDERNAME", service.getProviderName());
//                    startActivity(exit);
//
//                } else if (str.equals("RESIDENTS")) {
//                    Resident res = (Resident) parent.getItemAtPosition(position);
//                    exit.putExtra("TYPE", "RESIDENT");
//                    exit.putExtra("NAME", res.getName());
//                    exit.putExtra("ID", res.getNationalId());
//                    exit.putExtra("ID", res.getNationalId());
//                    exit.putExtra("ENTRY", res.getEntryTime());
//                    exit.putExtra("HOUSE", res.getHouse());
//                    startActivity(exit);
//                }
//
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
                driveInRecyclerAdapter.filter(newText);
                return true;
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.nav_filter) {
            showFilterDialog();
        }
        return super.onOptionsItemSelected(item);
    }


    public void showFilterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Visitors.this);
        builder.setTitle("Filter Visitors By");

        final int checkedItem = 0; //this will checked the item when user open the dialog
        builder.setSingleChoiceItems(filterItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(Visitors.this, "Position: " + which + " Value: " + listItems[which], Toast.LENGTH_LONG).show();

//                        String click=listItems[which];
//                        Log.d(TAG, "onClick: "+click);

                switch (filterItems[which]) {
                    case "Entry Time":
                        sortVisitorsByEntryTime();
                        break;
                    case "SMS Login":
                        sortVisitorsBySMS();
                        break;
                    case "Alphabetically":
                        sortVisitorsAlphabetically();
                        break;
                    default:
                        break;

                }


            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        driveInRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onVisitorClick(Object object) {
//        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
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

    private class GetActiveVisitors extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }

        @Override
        public void onPostExecute(String s) {
            driveIns.clear();
            walkIns.clear();
            serviceProviderModels.clear();
            residents.clear();
            loading.setVisibility(View.GONE);
            //adapter.notifyDataSetChanged();
            if (s != null) {
                //Log.e("Result",s);
                Object json = null;
                try {
                    json = new JSONTokener(s).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject object = new JSONObject(s);
                        JSONArray array = object.getJSONArray("result_content");
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject item = array.getJSONObject(i);
                                String name = item.getString("fullname");
                                String id = item.getString("id_number");
                                String entry = item.getString("entry_time");
                                String house = item.getString("house");
                                String visitorType = item.getString("visitor_type");
                                if (str.equals("DRIVE")) {
                                    DriveIn driveIn = new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);

                                    Log.d(TAG, "onPostExecute: Visitor Type" + visitorType);

                                    if (!item.isNull("registration") && !visitorType.equals("Resident")) {
                                        driveIn.setCarNumber(item.getString("registration"));
                                        driveIns.add(driveIn);
                                    }
                                } else if (str.equals("WALK")) {
                                    DriveIn driveIn = new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);


//                                    Log.d(TAG, "onPostExecute: Select Day "+selectCal.get(Calendar.DAY_OF_YEAR)+" Entry Day"+entryCal.get(Calendar.DAY_OF_YEAR));

                                    Log.d(TAG, "onPostExecute: Visitor Type" + visitorType);
                                    if (item.isNull("registration") && !visitorType.equals("Resident")) {
                                        walkIns.add(driveIn);

                                    }
                                } else if (str.equals("PROVIDER")) {
                                    ServiceProviderModel model = new ServiceProviderModel();
                                    model.setCompanyName(name);
                                    model.setEntryTime(entry);
                                    model.setNationalId(id);
                                    serviceProviderModels.add(model);
                                } else if (str.equals("RESIDENTS")) {
                                    Log.d(TAG, "onPostExecute: "+visitorType);
                                    if (visitorType.equals("Resident")) {

                                        Resident resident = new Resident();
                                        resident.setName(name);
                                        resident.setEntryTime(entry);
                                        resident.setNationalId(id);
                                        resident.setHouse(house);

//                                        if (!item.isNull("registration")) {
                                        residents.add(resident);
//                                        }
                                    }

                                }
                            }
                            if (walkIns.size() > 0) {
                                Collections.sort(walkIns, new Comparator<DriveIn>() {
                                    public int compare(DriveIn o1, DriveIn o2) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        try {
                                            Date entryTimeOne = format.parse(o1.getEntryTime());
                                            Date entryTimeTwo = format.parse(o2.getEntryTime());

                                            if (entryTimeOne == null || entryTimeTwo == null)
                                                return 0;
                                            return o2.getEntryTime().compareTo(o1.getEntryTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        return 0;

                                    }

                                });
                            }
                            if (driveIns.size() > 0) {
                                Collections.sort(driveIns, new Comparator<DriveIn>() {
                                    public int compare(DriveIn o1, DriveIn o2) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        try {
                                            Date entryTimeOne = format.parse(o1.getEntryTime());
                                            Date entryTimeTwo = format.parse(o2.getEntryTime());

                                            if (entryTimeOne == null || entryTimeTwo == null)
                                                return 0;
                                            return o2.getEntryTime().compareTo(o1.getEntryTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        return 0;

                                    }

                                });
                            }
                            if (residents.size() > 0) {
                                Collections.sort(residents, new Comparator<Resident>() {
                                    public int compare(Resident o1, Resident o2) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        try {
                                            Date entryTimeOne = format.parse(o1.getEntryTime());
                                            Date entryTimeTwo = format.parse(o2.getEntryTime());

                                            if (entryTimeOne == null || entryTimeTwo == null)
                                                return 0;
                                            return o2.getEntryTime().compareTo(o1.getEntryTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        return 0;

                                    }

                                });
                            }


                            loading.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            driveInRecyclerAdapter.notifyDataSetChanged();
                            driveInRecyclerAdapter.reloadData();


                            sortVisitorsByEntryTime();


                        } else {
                            loading.setVisibility(View.GONE);
                            findViewById(R.id.empty).setVisibility(View.VISIBLE);
                        }
                    } else {
                        loading.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                loading.setVisibility(View.GONE);
                findViewById(R.id.empty).setVisibility(View.VISIBLE);
                //No logs
            }

        }
    }

    public void sortVisitorsByEntryTime() {
        sortedDriveIns.clear();
        sortedResidentList.clear();
        sortedWalkIns.clear();

        if (driveIns.size() > 0) {
            Collections.sort(driveIns, new Comparator<DriveIn>() {
                public int compare(DriveIn o1, DriveIn o2) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date entryTimeOne = format.parse(o1.getEntryTime());
                        Date entryTimeTwo = format.parse(o2.getEntryTime());

                        if (entryTimeOne == null || entryTimeTwo == null)
                            return 0;
                        return o2.getEntryTime().compareTo(o1.getEntryTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;

                }

            });


            for (DriveIn d : driveIns) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(d.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);
                Log.d(TAG, "onPostExecute: Select Day " + selectCal.get(Calendar.DAY_OF_YEAR) + " Entry Day" + entryCal.get(Calendar.DAY_OF_YEAR));

                if (isSameDay(selectCal, entryCal)) {
                    Log.d(TAG, "onPostExecute: " + d.getName());

                    sortedDriveIns.add(d);
                }
            }

            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, sortedDriveIns, 1, this);
            Log.d(TAG, "Items Desc" + driveIns.get(0).getEntryTime());
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();


        } else if (walkIns.size() > 0) {
            Collections.sort(walkIns, new Comparator<DriveIn>() {
                public int compare(DriveIn o1, DriveIn o2) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date entryTimeOne = format.parse(o1.getEntryTime());
                        Date entryTimeTwo = format.parse(o2.getEntryTime());

                        if (entryTimeOne == null || entryTimeTwo == null)
                            return 0;
                        return o2.getEntryTime().compareTo(o1.getEntryTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;

                }

            });


            for (DriveIn d : walkIns) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(d.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);
                Log.d(TAG, "onPostExecute: Select Day " + selectCal.get(Calendar.DAY_OF_YEAR) + " Entry Day" + entryCal.get(Calendar.DAY_OF_YEAR));

                if (isSameDay(selectCal, entryCal)) {
                    Log.d(TAG, "onPostExecute: " + d.getName());

                    sortedWalkIns.add(d);
                }
            }

            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, sortedWalkIns, "type", this);
            Log.d(TAG, "Items Desc" + walkIns.get(0).getEntryTime());
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();
            updateRecyclerViewState();

        } else if (residents.size() > 0) {
            Collections.sort(residents, new Comparator<Resident>() {
                public int compare(Resident o1, Resident o2) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date entryTimeOne = format.parse(o1.getEntryTime());
                        Date entryTimeTwo = format.parse(o2.getEntryTime());

                        if (entryTimeOne == null || entryTimeTwo == null)
                            return 0;
                        return o2.getEntryTime().compareTo(o1.getEntryTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;

                }

            });


            for (Resident resident : residents) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(resident.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);
                Log.d(TAG, "onPostExecute: Select Day " + selectCal.get(Calendar.DAY_OF_YEAR) + " Entry Day" + entryCal.get(Calendar.DAY_OF_YEAR));

                if (isSameDay(selectCal, entryCal)) {
                    Log.d(TAG, "onPostExecute: " + resident.getName());

                    sortedResidentList.add(resident);
                }
            }

            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, sortedResidentList, this);
            Log.d(TAG, "Items Desc" + residents.get(0).getEntryTime());
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();


        }


    }


    private void sortVisitorsBySMS() {
        sortedDriveIns.clear();
        sortedResidentList.clear();
        sortedWalkIns.clear();

//        ArrayList<DriveIn> smsFilterList = new ArrayList<>();
        if (driveIns.size() > 0) {
            for (DriveIn driveIn : driveIns) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(driveIn.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (driveIn.getName().equals(" ") && isSameDay(selectCal, entryCal)) {

                    sortedDriveIns.add(driveIn);
                }
            }


            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, sortedDriveIns, 1, this);
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();
        } else if (walkIns.size() > 0) {

            for (DriveIn driveIn : walkIns) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(driveIn.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (driveIn.getName().equals(" ") && isSameDay(selectCal, entryCal)) {
                    sortedWalkIns.add(driveIn);
                }
            }
            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, sortedWalkIns, 1, this);
            Log.d(TAG, "Items Desc" + sortedWalkIns.get(0).getEntryTime());
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();
        } else if (residents.size() > 0) {

            for (Resident resident : residents) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(resident.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (resident.getName().equals(" ") && isSameDay(selectCal, entryCal)) {
                    sortedResidentList.add(resident);
                }
            }
            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, sortedResidentList, this);
            Log.d(TAG, "Items Desc" + sortedResidentList.get(0).getEntryTime());
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();
        }

        updateRecyclerViewState();


    }

    private void sortVisitorsAlphabetically() {
        ArrayList<DriveIn> alphaFilterList = new ArrayList<>();

        if (driveIns.size() > 0) {
            Collections.sort(driveIns, new Comparator<DriveIn>() {
                @Override
                public int compare(DriveIn s1, DriveIn s2) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                }
            });

            ArrayList<DriveIn> alphaFilterListA = new ArrayList<>();


            for (DriveIn d : driveIns) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(d.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (isSameDay(entryCal, selectCal)) {
                    alphaFilterListA.add(d);
                }
            }


            Log.d(TAG, "sortVisitorsAlphabetically: DriveIns" + driveIns.toString());
            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, alphaFilterListA, 1, this);
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();

        } else if (walkIns.size() > 0) {
            Collections.sort(walkIns, new Comparator<DriveIn>() {
                @Override
                public int compare(DriveIn s1, DriveIn s2) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                }
            });

            ArrayList<DriveIn> alphaFilterListA = new ArrayList<>();


            for (DriveIn d : driveIns) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(d.getEntryTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (isSameDay(entryCal, selectCal)) {
                    alphaFilterListA.add(d);
                }
            }


            Log.d(TAG, "sortVisitorsAlphabetically: WalkIns" + alphaFilterListA.toString());
            driveInRecyclerAdapter = new DriveInRecyclerAdapter(Visitors.this, alphaFilterListA, 1, this);
            recyclerView.setAdapter(driveInRecyclerAdapter);
            driveInRecyclerAdapter.notifyDataSetChanged();
            driveInRecyclerAdapter.reloadData();
        }
        updateRecyclerViewState();

    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof DriveInRecyclerAdapter.DriveInViewHolder) {
            if (driveIns.size() > 0) {
                final String name = driveIns.get(viewHolder.getAdapterPosition()).getName();

                final DriveIn deletedDriveIn = driveIns.get(viewHolder.getAdapterPosition());
                final int deleteIndex = viewHolder.getAdapterPosition();

                driveInRecyclerAdapter.removeItem(deleteIndex);

                Log.d(TAG, "onSwiped: " + deleteIndex + deletedDriveIn.getName());

                Snackbar snackbar = Snackbar.make(rootLayout, name + " checked out", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        canDeleteItem = false;

                        driveInRecyclerAdapter.restoreItem(deletedDriveIn, deleteIndex);
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getResources().getColor(R.color.white));
                snackbar.show();

                snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {

                        if (canDeleteItem) {
                            recordCheckOut(deletedDriveIn);
                            updateRecyclerViewState();

                            canDeleteItem = true;
                        }


                    }
                });

            } else if (walkIns.size() > 0) {
                String name = walkIns.get(viewHolder.getAdapterPosition()).getName();

                final DriveIn deletedWalkIn = walkIns.get(viewHolder.getAdapterPosition());
                final int deleteIndex = viewHolder.getAdapterPosition();

                driveInRecyclerAdapter.removeItem(deleteIndex);

                Log.d(TAG, "onSwiped: " + deleteIndex + deletedWalkIn.getName());

                Snackbar snackbar = Snackbar.make(rootLayout, name + " checked out", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        canDeleteItem = false;

                        driveInRecyclerAdapter.restoreItem(deletedWalkIn, deleteIndex);
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getResources().getColor(R.color.white));
                snackbar.show();

                snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {

                        if (canDeleteItem) {
                            recordCheckOut(deletedWalkIn);
                            updateRecyclerViewState();

                            canDeleteItem = true;
                        }


                    }
                });


            } else if (residents.size() > 0) {
                String name = residents.get(viewHolder.getAdapterPosition()).getName();

                final Resident deletedResident = residents.get(viewHolder.getAdapterPosition());
                final int deleteIndex = viewHolder.getAdapterPosition();

                driveInRecyclerAdapter.removeItem(deleteIndex);

                Log.d(TAG, "onSwiped: " + deleteIndex + deletedResident.getName());

                Snackbar snackbar = Snackbar.make(rootLayout, name + " checked out", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        canDeleteItem = false;

                        driveInRecyclerAdapter.restoreItem(deletedResident, deleteIndex);
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getResources().getColor(R.color.white));
                snackbar.show();

                snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {

                        if (canDeleteItem) {
                            recordCheckOut(deletedResident);
                            updateRecyclerViewState();

                            canDeleteItem = true;
                        }


                    }
                });
            } else if (serviceProviderModels.size() > 0) {
                String name = serviceProviderModels.get(viewHolder.getAdapterPosition()).getOtherNames();

                final ServiceProviderModel deletedModel = serviceProviderModels.get(viewHolder.getAdapterPosition());
                final int deleteIndex = viewHolder.getAdapterPosition();

                driveInRecyclerAdapter.removeItem(deleteIndex);

                Log.d(TAG, "onSwiped: " + deleteIndex + deletedModel.getOtherNames());

                Snackbar snackbar = Snackbar.make(rootLayout, name + " checked out", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        canDeleteItem = false;

                        driveInRecyclerAdapter.restoreItem(deletedModel, deleteIndex);
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getResources().getColor(R.color.white));
                snackbar.show();

                snackbar.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {

                        if (canDeleteItem) {
                            recordCheckOut(deletedModel);
                            updateRecyclerViewState();

                            canDeleteItem = true;
                        }


                    }
                });
            }


        }
    }


    public void recordCheckOut(Object object) {

        if (str.equals("DRIVE")) {

            final DriveIn driveIn = (DriveIn) object;
            RetrofitClient.getDataService(this).visitorExit(driveIn.getNationalId(), preferences.getDeviceId(), Constants.getCurrentTimeStamp()).enqueue(new Callback<QueryResponse>() {
                @Override
                public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                    Log.d(TAG, "onResponse: " + response.body());
                    if (response.body() != null) {
                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode() != null) {
                            if (queryResponse.getResultCode() == 0 && queryResponse.getResultText().equals("OK") && queryResponse.getResultContent().equals("success")) {
//                           Query Successful, Visitor Successfully exited
                                Snackbar snackbar = Snackbar.make(rootLayout, "Checked "+driveIn.getName() + " out...", Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            } else {
//                           Not Successful
                                Snackbar snackbar = Snackbar.make(rootLayout, "Error: " + queryResponse.getResultText(), Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            }


                        } else {
//                       Possibly poor internet connection
                            Snackbar snackbar = Snackbar.make(rootLayout, "Error,Poor Internet Connection", Snackbar.LENGTH_LONG);
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                        }
                    }
                }

                @Override
                public void onFailure(Call<QueryResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                }
            });

        } else if (str.equals("WALK")) {

            final DriveIn walkIn = (DriveIn) object;
            RetrofitClient.getDataService(this).visitorExit(walkIn.getNationalId(), preferences.getDeviceId(), Constants.getCurrentTimeStamp()).enqueue(new Callback<QueryResponse>() {
                @Override
                public void onResponse(Call<QueryResponse> call, retrofit2.Response<QueryResponse> response) {
                    Log.d(TAG, "onResponse: " + response.body());
                    if (response .body() != null) {
                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode() != null) {
                            if (queryResponse.getResultCode() == 0 && queryResponse.getResultText().equals("OK") && queryResponse.getResultContent().equals("success")) {
//                           Query Successful, Visitor Successfully exited
                                Snackbar snackbar = Snackbar.make(rootLayout, "Checked "+walkIn.getName() + " out", Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            } else {
//                           Not Successful
                                Snackbar snackbar = Snackbar.make(rootLayout, "Error: " + queryResponse.getResultText(), Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            }


                        } else {
//                       Possibly poor internet connection
                            Snackbar snackbar = Snackbar.make(rootLayout, "Error,Poor Internet Connection", Snackbar.LENGTH_LONG);
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                        }
                    }
                }

                @Override
                public void onFailure(Call<QueryResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                }
            });

        } else if (str.equals("PROVIDER")) {
            final ServiceProviderModel model = (ServiceProviderModel) object;
            RetrofitClient.getDataService(this).visitorExit(model.getNationalId(), preferences.getDeviceId(), Constants.getCurrentTimeStamp()).enqueue(new Callback<QueryResponse>() {
                @Override
                public void onResponse(Call<QueryResponse> call, retrofit2.Response<QueryResponse> response) {
                    Log.d(TAG, "onResponse: " + response.body());
                    if (response.body() != null) {
                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode() != null) {
                            if (queryResponse.getResultCode() == 0 && queryResponse.getResultText().equals("OK") && queryResponse.getResultContent().equals("success")) {
//                           Query Successful, Visitor Successfully exited
                                Snackbar snackbar = Snackbar.make(rootLayout, "Checked "+model.getOtherNames() + "out", Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            } else {
//                           Not Successful
                                Snackbar snackbar = Snackbar.make(rootLayout, "Error: " + queryResponse.getResultText(), Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            }


                        } else {
//                       Possibly poor internet connection
                            Snackbar snackbar = Snackbar.make(rootLayout, "Error,Poor Internet Connection", Snackbar.LENGTH_LONG);
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                        }
                    }
                }

                @Override
                public void onFailure(Call<QueryResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                }
            });

        } else if (str.equals("RESIDENTS")) {
            final Resident resident = (Resident) object;
            RetrofitClient.getDataService(this).visitorExit(resident.getNationalId(), preferences.getDeviceId(), Constants.getCurrentTimeStamp()).enqueue(new Callback<QueryResponse>() {
                @Override
                public void onResponse(Call<QueryResponse> call, retrofit2.Response<QueryResponse> response) {
                    Log.d(TAG, "onResponse: " + response.body());
                    if (response.body() != null) {
                        QueryResponse queryResponse = response.body();

                        if (queryResponse.getResultCode() != null) {
                            if (queryResponse.getResultCode() == 0 && queryResponse.getResultText().equals("OK") && queryResponse.getResultContent().equals("success")) {
//                           Query Successful, Visitor Successfully exited
                                Snackbar snackbar = Snackbar.make(rootLayout, "Checked "+resident.getName() + " out", Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            } else {
//                           Not Successful
                                Snackbar snackbar = Snackbar.make(rootLayout, "Error: " + queryResponse.getResultText(), Snackbar.LENGTH_LONG);
                                snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                                TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                snackbar.show();

                            }


                        } else {
//                       Possibly poor internet connection
                            Snackbar snackbar = Snackbar.make(rootLayout, "Error,Poor Internet Connection", Snackbar.LENGTH_LONG);
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                            TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(getResources().getColor(R.color.white));
                            snackbar.show();

                        }
                    }
                }

                @Override
                public void onFailure(Call<QueryResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t);
                }
            });

        }


    }


    public void updateRecyclerViewState() {
        if (str.equals("DRIVE")) {
            if (sortedDriveIns.size() == 0) {
                empty.setVisibility(View.VISIBLE);
                info_message.setText("No Drive Ins Available for this day");
            } else {
                empty.setVisibility(View.GONE);
            }
        } else if (str.equals("WALK")) {
            if (sortedWalkIns.size() == 0) {
                empty.setVisibility(View.VISIBLE);
                info_message.setText("No Walk Ins Available for this day");

            } else {
                empty.setVisibility(View.GONE);
            }

        } else if (str.equals("PROVIDER")) {
            if (serviceProviderModels.size() == 0) {
                empty.setVisibility(View.VISIBLE);
                info_message.setText("No Logs Available for this day");

            } else {
                empty.setVisibility(View.GONE);

            }
        } else if (str.equals("RESIDENTS")) {
            if (sortedResidentList.size() == 0) {
                empty.setVisibility(View.VISIBLE);
                info_message.setText("No Resident Ins Available");
            } else {
                empty.setVisibility(View.GONE);

            }

        }


    }
}
