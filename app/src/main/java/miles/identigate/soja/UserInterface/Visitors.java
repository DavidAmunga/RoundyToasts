package miles.identigate.soja.UserInterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Predicate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import miles.identigate.soja.Adapters.DriveInAdapter;
import miles.identigate.soja.Helpers.CheckConnection;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.Models.Visitor;
import miles.identigate.soja.R;

public class Visitors extends AppCompatActivity {
    ListView lv;
    DatabaseHandler handler;
    DriveInAdapter adapter;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    String str;
    private ContentLoadingProgressBar loading;
    private Preferences preferences;
    private EditText searchbox;
    private static final String TAG = "Visitors";
    ImageView ic_filter;
    TextView info_message;
    String[] filterItems = {"Entry Time", "SMS Login", "Alphabetically"};


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors);
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
        lv = findViewById(R.id.visitors);
        loading = findViewById(R.id.loading);
        searchbox = findViewById(R.id.searchbox);
        handler = new DatabaseHandler(this);
        ic_filter = findViewById(R.id.ic_filter);
        info_message = findViewById(R.id.info_message);

        if (getIntent() != null) {
            str = getIntent().getStringExtra("TYPE");
            if (str.equals("DRIVE")) {
                title.setText("Drive Out");
                //driveIns=handler.getDriveIns(0);
                adapter = new DriveInAdapter(this, driveIns, 1);
                //adapter.notifyDataSetChanged();
            } else if (str.equals("WALK")) {
                title.setText("Walk Out");
                //walkIns=handler.getWalk(0);
                adapter = new DriveInAdapter(this, walkIns, "WALK");
                //adapter.notifyDataSetChanged();
            } else if (str.equals("PROVIDER")) {
                title.setText("Service Providers");
                //serviceProviderModels=handler.getProviders(0);
                adapter = new DriveInAdapter(this, "TYPE", serviceProviderModels);
                //adapter.notifyDataSetChanged();
            } else if (str.equals("RESIDENTS")) {
                title.setText("Residents");
                //residents=handler.getResidents(0);
                adapter = new DriveInAdapter(this, residents);
                //adapter.notifyDataSetChanged();
            }
        } else {
            finish();
        }

        ic_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });


        lv.setAdapter(adapter);
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
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent exit = new Intent(getApplicationContext(), RecordExit.class);
                if (str.equals("DRIVE")) {
                    DriveIn drive = (DriveIn) parent.getItemAtPosition(position);
                    //Toast.makeText(getApplicationContext(),drive.getExitTime(),Toast.LENGTH_LONG).show();
                    exit.putExtra("TYPE", "DRIVE");
                    exit.putExtra("NAME", drive.getName());
                    exit.putExtra("ID", drive.getNationalId());
                    exit.putExtra("ENTRY", drive.getEntryTime());
                    exit.putExtra("CAR", drive.getCarNumber());
                    startActivity(exit);

                } else if (str.equals("WALK")) {
                    DriveIn walk = (DriveIn) parent.getItemAtPosition(position);
                    exit.putExtra("TYPE", "WALK");
                    exit.putExtra("NAME", walk.getName());
                    exit.putExtra("ID", walk.getNationalId());
                    exit.putExtra("ENTRY", walk.getEntryTime());
                    startActivity(exit);

                } else if (str.equals("PROVIDER")) {
                    ServiceProviderModel service = (ServiceProviderModel) parent.getItemAtPosition(position);
                    exit.putExtra("TYPE", "PROVIDER");
                    exit.putExtra("NAME", service.getCompanyName());
                    exit.putExtra("ID", service.getNationalId());
                    exit.putExtra("ENTRY", service.getEntryTime());
                    exit.putExtra("PROVIDERNAME", service.getProviderName());
                    startActivity(exit);

                } else if (str.equals("RESIDENTS")) {
                    Resident res = (Resident) parent.getItemAtPosition(position);
                    exit.putExtra("TYPE", "RESIDENT");
                    exit.putExtra("NAME", res.getName());
                    exit.putExtra("ID", res.getNationalId());
                    exit.putExtra("ENTRY", res.getEntryTime());
                    exit.putExtra("HOUSE", res.getHouse());
                    startActivity(exit);
                }

            }
        });
        searchbox.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

                String text = searchbox.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
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
                                if (str.equals("DRIVE")) {
                                    DriveIn driveIn = new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);
                                    if (!item.isNull("registration")) {
                                        driveIn.setCarNumber(item.getString("registration"));
                                        driveIns.add(driveIn);
                                    }
                                } else if (str.equals("WALK")) {
                                    DriveIn driveIn = new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);
                                    if (item.isNull("registration")) {
                                        walkIns.add(driveIn);
                                    }
                                } else if (str.equals("PROVIDER")) {
                                    ServiceProviderModel model = new ServiceProviderModel();
                                    model.setCompanyName(name);
                                    model.setEntryTime(entry);
                                    model.setNationalId(id);
                                    serviceProviderModels.add(model);
                                } else if (str.equals("RESIDENTS")) {
                                    Resident resident = new Resident();
                                    resident.setName(name);
                                    resident.setEntryTime(entry);
                                    resident.setNationalId(id);
                                    resident.setHouse(house);
                                    if (!item.isNull("registration")) {
                                        residents.add(resident);
                                    }
                                }
                            }
                            if(walkIns.size()>0){
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

                                ;
                            });}
                            if(driveIns.size()>0){
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

                                    ;
                                });}

                            loading.setVisibility(View.GONE);
                            lv.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            adapter.reloadData();
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

                ;
            });

            adapter = new DriveInAdapter(Visitors.this, driveIns, 1);
            Log.d(TAG, "Items Desc" + driveIns.get(0).getEntryTime().toString());
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.reloadData();


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

                ;
            });

            adapter = new DriveInAdapter(Visitors.this, walkIns, 1);
            Log.d(TAG, "Items Desc" + walkIns.get(0).getEntryTime().toString());
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.reloadData();
        }

    }


    private void sortVisitorsBySMS() {
        ArrayList<DriveIn> smsFilterList = new ArrayList<>();
        if (driveIns.size() > 0) {
            for (DriveIn driveIn : driveIns) {
                if (driveIn.getName().equals(" ")) {
                    smsFilterList.add(driveIn);
                }
            }
            adapter = new DriveInAdapter(Visitors.this, smsFilterList, 1);
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.reloadData();
        } else if (walkIns.size() > 0) {

            for (DriveIn driveIn : walkIns) {
                if (driveIn.getName().equals(" ")) {
                    smsFilterList.add(driveIn);
                }
            }
            adapter = new DriveInAdapter(Visitors.this, smsFilterList, 1);
            Log.d(TAG, "Items Desc" + walkIns.get(0).getEntryTime().toString());
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.reloadData();
        }

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
            Log.d(TAG, "sortVisitorsAlphabetically: DriveIns"+driveIns.toString());
            adapter = new DriveInAdapter(Visitors.this, driveIns, 1);
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.reloadData();

        } else if (walkIns.size() > 0) {
            Collections.sort(walkIns, new Comparator<DriveIn>() {
                @Override
                public int compare(DriveIn s1, DriveIn s2) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                }
            });
            Log.d(TAG, "sortVisitorsAlphabetically: WalkIns"+walkIns.toString());
            adapter = new DriveInAdapter(Visitors.this, walkIns, 1);
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.reloadData();
        }
    }

}
