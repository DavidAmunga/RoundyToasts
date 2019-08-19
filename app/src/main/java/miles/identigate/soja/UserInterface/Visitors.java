package miles.identigate.soja.UserInterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import miles.identigate.soja.adapters.VisitorAdapter;
import miles.identigate.soja.adapters.VisitorTypeChipAdapter;
import miles.identigate.soja.helpers.CheckConnection;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.RecyclerItemTouchHelper;
import miles.identigate.soja.helpers.RecyclerItemTouchHelperListener;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.interfaces.OnVisitorTypeChipClick;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.models.Resident;
import miles.identigate.soja.models.ServiceProviderModel;
import miles.identigate.soja.models.TypeObject;
import miles.identigate.soja.models.Visitor;
import miles.identigate.soja.services.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Visitors extends AppCompatActivity implements RecyclerItemTouchHelperListener, OnItemClick, OnVisitorTypeChipClick {
    DatabaseHandler handler;
    //    DriveInAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<Visitor> visitors;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    ArrayList<DriveIn> sortedDriveIns = new ArrayList<>();
    ArrayList<DriveIn> sortedWalkIns = new ArrayList<>();
    ArrayList<Visitor> sortedVisitors = new ArrayList<>();

    ArrayList<TypeObject> visitorTypes = new ArrayList<>();


    VisitorAdapter visitorAdapter;
    String str;
    @BindView(R.id.empty)
    LinearLayout empty;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.root_layout)
    RelativeLayout rootLayout;
    @BindView(R.id.visitor_type_list)
    RecyclerView visitorTypeList;
    private ContentLoadingProgressBar loading;
    private Preferences preferences;
    private static final String TAG = "Visitors";
    TextView info_message;
    String[] filterItems = {"Entry Time", "SMS Login", "Alphabetically"};
    private boolean canDeleteItem = true;

    Date selectedDate = new Date();
    TextView toolbarTitle;


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
        toolbarTitle = toolbar.findViewById(R.id.title);
        toolbarTitle.setText("All Check Ins");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        preferences = new Preferences(this);
        visitors = new ArrayList<>();
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
            visitorAdapter = new VisitorAdapter(this, visitors, this);


        } else {
            finish();
        }


        recyclerView.setAdapter(visitorAdapter);
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

//        Setup Visitor Chips
        visitorTypes = handler.getTypes("visitors", null);

        visitorTypes.add(0, new TypeObject("0", "All"));
        visitorTypeList.setHasFixedSize(true);
        visitorTypeList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        VisitorTypeChipAdapter visitorTypeChipAdapter = new VisitorTypeChipAdapter(this, visitorTypes, this);
        visitorTypeChipAdapter.notifyDataSetChanged();
        visitorTypeList.setAdapter(visitorTypeChipAdapter);


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
                visitorAdapter.filter(newText);
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
        visitorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onVisitorClick(Object object) {
//        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        Intent exit = new Intent(getApplicationContext(), RecordExit.class);


        Visitor visitor = (Visitor) object;
        //Toast.makeText(getApplicationContext(),drive.getExitTime(),Toast.LENGTH_LONG).show();
        exit.putExtra("TYPE", "DRIVE");
        exit.putExtra("NAME", visitor.getName());
        exit.putExtra("ID", visitor.getNational_id());
        exit.putExtra("ENTRY", visitor.getEntry_time());
        if (visitor.getCar_reg() != null && !visitor.getCar_reg().isEmpty()) {
            exit.putExtra("CAR", visitor.getCar_reg());

        }
        startActivity(exit);


    }

    @Override
    public void onVisitorTypeChipClick(TypeObject typeObject, View itemView) {

        if (!typeObject.getName().equals("All Check Ins")) {
            toolbarTitle.setText("Check Out " + typeObject.getName());
        } else {
            toolbarTitle.setText("All Check Ins");
        }

        sortVisitorsByVisitorType(typeObject.getName());

    }

    private void sortVisitorsByVisitorType(String visitorType) {
        sortedVisitors.clear();

        if (visitors.size() > 0) {
            Collections.sort(visitors, new Comparator<Visitor>() {
                public int compare(Visitor o1, Visitor o2) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date entryTimeOne = format.parse(o1.getEntry_time());
                        Date entryTimeTwo = format.parse(o2.getEntry_time());

                        if (entryTimeOne == null || entryTimeTwo == null)
                            return 0;
                        return o2.getEntry_time().compareTo(o1.getEntry_time());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;

                }

            });


            for (Visitor visitor : visitors) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(visitor.getEntry_time());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);
                Log.d(TAG, "onPostExecute: Select Day " + selectCal.get(Calendar.DAY_OF_YEAR) + " Entry Day" + entryCal.get(Calendar.DAY_OF_YEAR));

                if (isSameDay(selectCal, entryCal)) {
                    Log.d(TAG, "onPostExecute: " + visitor.getName());

                    if (visitorType.equals("All")) {
                        sortedVisitors.add(visitor);
                    } else if (visitor.getType().equals(visitorType)) {
                        sortedVisitors.add(visitor);

                    }

                }
            }

            visitorAdapter = new VisitorAdapter(Visitors.this, sortedVisitors, this);
            Log.d(TAG, "Items Desc" + visitors.get(0).getEntry_time());
            recyclerView.setAdapter(visitorAdapter);
            visitorAdapter.notifyDataSetChanged();
            visitorAdapter.reloadData();


        }

    }

    private class GetActiveVisitors extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }

        @Override
        public void onPostExecute(String s) {
            visitors.clear();
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

                                Visitor visitor = new Visitor();
                                visitor.setName(name);
                                visitor.setNational_id(id);
                                visitor.setEntry_time(entry);
                                visitor.setType(visitorType);
                                if (!item.isNull("registration")) {
                                    visitor.setCar_reg(item.getString("registration"));

                                }
                                Log.d(TAG, "onPostExecute: Visitor Type" + visitorType);

                                visitors.add(visitor);


                            }

                            if (visitors.size() > 0) {
                                Collections.sort(visitors, new Comparator<Visitor>() {
                                    public int compare(Visitor o1, Visitor o2) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        try {
                                            Date entryTimeOne = format.parse(o1.getEntry_time());
                                            Date entryTimeTwo = format.parse(o2.getEntry_time());

                                            if (entryTimeOne == null || entryTimeTwo == null)
                                                return 0;
                                            return o2.getEntry_time().compareTo(o1.getEntry_time());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        return 0;

                                    }

                                });
                            }

                            loading.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            visitorAdapter.notifyDataSetChanged();
                            visitorAdapter.reloadData();


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
        sortedVisitors.clear();

        if (visitors.size() > 0) {
            Collections.sort(visitors, new Comparator<Visitor>() {
                public int compare(Visitor o1, Visitor o2) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date entryTimeOne = format.parse(o1.getEntry_time());
                        Date entryTimeTwo = format.parse(o2.getEntry_time());

                        if (entryTimeOne == null || entryTimeTwo == null)
                            return 0;
                        return o2.getEntry_time().compareTo(o1.getEntry_time());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;

                }

            });


            for (Visitor visitor : visitors) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(visitor.getEntry_time());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);
                Log.d(TAG, "onPostExecute: Select Day " + selectCal.get(Calendar.DAY_OF_YEAR) + " Entry Day" + entryCal.get(Calendar.DAY_OF_YEAR));

                if (isSameDay(selectCal, entryCal)) {
                    Log.d(TAG, "onPostExecute: " + visitor.getName());

                    sortedVisitors.add(visitor);
                }
            }

            visitorAdapter = new VisitorAdapter(Visitors.this, sortedVisitors, this);
            Log.d(TAG, "Items Desc" + visitors.get(0).getEntry_time());
            recyclerView.setAdapter(visitorAdapter);
            visitorAdapter.notifyDataSetChanged();
            visitorAdapter.reloadData();


        }


    }


    private void sortVisitorsBySMS() {
        sortedVisitors.clear();

//        ArrayList<DriveIn> smsFilterList = new ArrayList<>();
        if (visitors.size() > 0) {
            for (Visitor visitor : visitors) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(visitor.getEntry_time());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (visitor.getName().equals(" ") && isSameDay(selectCal, entryCal)) {

                    sortedVisitors.add(visitor);
                }
            }


            visitorAdapter = new VisitorAdapter(Visitors.this, sortedVisitors, this);
            recyclerView.setAdapter(visitorAdapter);
            visitorAdapter.notifyDataSetChanged();
            visitorAdapter.reloadData();

        }


        updateRecyclerViewState();


    }

    private void sortVisitorsAlphabetically() {
        ArrayList<Visitor> alphaFilterList = new ArrayList<>();

        if (visitors.size() > 0) {
            Collections.sort(visitors, new Comparator<Visitor>() {
                @Override
                public int compare(Visitor s1, Visitor s2) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                }
            });

            ArrayList<Visitor> alphaFilterListA = new ArrayList<>();


            for (Visitor visitor : visitors) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date entryDate = null;
                try {
                    entryDate = format.parse(visitor.getEntry_time());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTime(entryDate);
                Calendar selectCal = Calendar.getInstance();
                selectCal.setTime(selectedDate);

                if (isSameDay(entryCal, selectCal)) {
                    alphaFilterListA.add(visitor);
                }
            }


            Log.d(TAG, "sortVisitorsAlphabetically: DriveIns" + visitors.toString());
            visitorAdapter = new VisitorAdapter(Visitors.this, alphaFilterListA, this);
            recyclerView.setAdapter(visitorAdapter);
            visitorAdapter.notifyDataSetChanged();
            visitorAdapter.reloadData();

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

        if (viewHolder instanceof VisitorAdapter.VisitorViewHolder) {

            if (visitors.size() > 0) {
                final String name = visitors.get(viewHolder.getAdapterPosition()).getName();
                final String id = visitors.get(viewHolder.getAdapterPosition()).getNational_id();


                final Visitor removedVisitor = visitors.get(viewHolder.getAdapterPosition());
                final int deleteIndex = viewHolder.getAdapterPosition();

                visitorAdapter.removeItem(deleteIndex);

                Log.d(TAG, "onSwiped: " + deleteIndex + removedVisitor.getName());

                Snackbar snackbar = Snackbar.make(rootLayout,
                        (name == null || TextUtils.isEmpty(name) || name.equals("null") ?
                                id : name
                        )
                                + " checked out", Snackbar.LENGTH_LONG);

                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        canDeleteItem = false;

                        visitorAdapter.restoreItem(removedVisitor, deleteIndex);
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
                            recordCheckOut(removedVisitor);
                            updateRecyclerViewState();

                            canDeleteItem = true;
                        }


                    }
                });

            }
        }
    }


    public void recordCheckOut(Object object) {


        final Visitor visitor = (Visitor) object;
        RetrofitClient.getDataService(this).visitorExit(visitor.getNational_id(), preferences.getDeviceId(), Constants.getCurrentTimeStamp()).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                Log.d(TAG, "onResponse: " + response.body());
                if (response.body() != null) {
                    QueryResponse queryResponse = response.body();

                    if (queryResponse.getResultCode() != null) {
                        if (queryResponse.getResultCode() == 0 && queryResponse.getResultText().equals("OK") && queryResponse.getResultContent().equals("success")) {
//                           Query Successful, Visitor Successfully exited
                            Snackbar snackbar = Snackbar.make(rootLayout, "Checked " + visitor.getName() + " out...", Snackbar.LENGTH_LONG);
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


    public void updateRecyclerViewState() {

        if (sortedVisitors.size() == 0) {
            empty.setVisibility(View.VISIBLE);
            info_message.setText("No Check Ins Available for this day");
        } else {
            empty.setVisibility(View.GONE);
        }

    }
}
