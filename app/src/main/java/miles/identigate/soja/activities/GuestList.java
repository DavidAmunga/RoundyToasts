package miles.identigate.soja.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.arch.persistence.room.util.StringUtil;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.apache.commons.codec.binary.StringUtils;

import java.util.List;

import HPRTAndroidSDK.HPRTPrinterHelper;
import HPRTAndroidSDK.PublicFunction;
import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.Printer.DeviceListActivity;
import miles.identigate.soja.Printer.PrinterProperty;
import miles.identigate.soja.Printer.PublicAction;
import miles.identigate.soja.R;
import miles.identigate.soja.SlipActivity;
import miles.identigate.soja.adapters.GuestsAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.guests.GuestsViewModel;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.search.guests.SearchGuestsViewModel;
import miles.identigate.soja.service.network.api.APIClient;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.utility.NetworkUtility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestList extends SojaActivity implements OnItemClick {

    private static final String TAG = "GuestList";

    private static final int REQUEST_ENABLE_BT = 200;
    private static final int REQUEST_ENABLE_LOCATION = 300;


    Preferences preferences;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.loading)
    RelativeLayout loading;

    private static HPRTPrinterHelper HPRTPrinter = new HPRTPrinterHelper();
    private BluetoothAdapter mBluetoothAdapter;
    private PublicFunction PFun = null;
    private PublicAction PAct = null;
    MaterialDialog dialog;
    String qrCode;

    Guest selectedGuest;


    SearchGuestsViewModel searchGuestsViewModel;
    GuestsViewModel guestsViewModel;
    List<Guest> guestList;
    GuestsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_list);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title);
        setSupportActionBar(toolbar);
        title.setText("Guest List");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new GuestsAdapter(GuestList.this, GuestList.this);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(GuestList.this, DividerItemDecoration.VERTICAL));

        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).color(getResources().getColor(R.color.colorGreyLight3)).sizeResId(R.dimen.divider).build());


//        loadData();

        getGuests();

//        Printer


        dialog = Constants.showProgressDialog(GuestList.this, "Printing", "Printing Ticket...");
        dialog.setCancelable(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        PFun = new PublicFunction(GuestList.this);
        PAct = new PublicAction(GuestList.this);


//        EnableBluetooth();
//        InitSetting();


    }


    public void getGuests() {
        if (NetworkUtility.getNetworkState(this).isNetworkAvailable()) {

            guestsViewModel = ViewModelProviders.of(this).get(GuestsViewModel.class);

            guestsViewModel.getListLiveData().observe(this, new Observer<PagedList<Guest>>() {
                @Override
                public void onChanged(@Nullable PagedList<Guest> guests) {
                    Log.d(TAG, "onChanged: Guests" + guests.toString());

                    if (guests != null) {
//                        if (guests.size() != 0) {
                        adapter.submitList(guests);
                        guestList = guests;

                        recyclerView.setAdapter(adapter);

//                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Check your network connection", Toast.LENGTH_SHORT).show();
        }
    }


    public void search(String query) {
        searchGuestsViewModel = ViewModelProviders.of(this).get(SearchGuestsViewModel.class);
        searchGuestsViewModel.setSearchQuery(query);

        try {
            searchGuestsViewModel.getListLiveData().observe(this, new Observer<PagedList<Guest>>() {
                @Override
                public void onChanged(@Nullable PagedList<Guest> guests) {
                    if (guests != null) {
                        adapter.submitList(guests);
                        guestList = guests;

                        recyclerView.setAdapter(adapter);

                    }
                }
            });
        } catch (Exception e) {
            Log.e("error", e.getMessage());

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_guests, menu);

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
                search(newText);

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
        } else if (item.getItemId() == R.id.nav_refresh) {
            refreshGuests();
        }
        return super.onOptionsItemSelected(item);

    }

    private void refreshGuests() {
        Log.d(TAG, "refreshGuests: Refresh");

//        guestListModel.refresh();

//        loadData();

    }

    private void showFilterDialog() {

    }

    @Override
    public void onVisitorClick(Object object) {
        selectedGuest = (Guest) object;


        new MaterialDialog.Builder(GuestList.this)
                .title("Printing Ticket")
                .content("Are you sure you want to print ticket for " + selectedGuest.getFirstName() + " ?")
                .positiveText("OK")
                .negativeText("CANCEL")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();

                        getQRCode();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();

                    }
                })
                .show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String strIsConnected;
        if (data == null || data.getExtras() == null)
            return;
        switch (resultCode) {
            case HPRTPrinterHelper.ACTIVITY_CONNECT_BT:
                String strBTAddress = "";

                strIsConnected = data.getExtras().getString("is_connected");
                if (strIsConnected.equals("NO")) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    doPrint();

                } else {

                    String PrinterName = "MPT-II";
                    HPRTPrinter = new HPRTPrinterHelper(GuestList.this, PrinterName);
                    CapturePrinterFunction();
                    GetPrinterProperty();
                    PrintSlip();
                }
                break;
            case REQUEST_ENABLE_LOCATION:
                doPrint();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void getQRCode() {
        APIClient.getClient(preferences, "").getResidentsQR(selectedGuest.getHostId(), preferences.getCurrentUser().getPremiseId()).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        QueryResponse queryResponse = response.body();

                        Log.d(TAG, "onResponse: " + queryResponse.getResultContent().toString());

                        if (queryResponse.getResultCode() == 0 && queryResponse.getResultText().equals("OK")) {
                            qrCode = (String) queryResponse.getResultContent();

                            if (!TextUtils.isEmpty(qrCode)) {
                                doPrint();
                            }
                        } else {
                            Toast.makeText(GuestList.this, "Please try Again", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Toast.makeText(GuestList.this, "Please try again", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(GuestList.this, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {

                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    private void doPrint() {
        setupBT();
        if (!dialog.isShowing())
            dialog.show();
        String PrinterName = "MPT-II";
        HPRTPrinter = new HPRTPrinterHelper(GuestList.this, PrinterName);
        CapturePrinterFunction();
        GetPrinterProperty();
        PrintSlip();
    }


    private void setupBT() {
        if (ContextCompat.checkSelfPermission(GuestList.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            dialog.dismiss();
            ActivityCompat.requestPermissions(GuestList.this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ENABLE_LOCATION);
        } else {
            Intent serverIntent = new Intent(GuestList.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
            return;
        }
    }

    private void PrintSlip() {
        Log.d(TAG, "PrintSlip: Start");
        Log.d(TAG, "PrintSlip: QR Code"+qrCode);
        try {
            byte[] data = new byte[]{0x1b, 0x40};
            HPRTPrinterHelper.WriteData(data);
            PAct.LanguageEncode();
            PAct.BeforePrintAction();
            HPRTPrinterHelper.PrintText(
                    Common.centerString(18, Common.formatString(selectedGuest.getFirstName()+ " " + (selectedGuest.getLastName()!=null?selectedGuest.getLastName():"")))
                    , 32, 2, 16);
            String msg = Common.centerString(18, selectedGuest.getCompany() != null ? selectedGuest.getCompany() + "\n" : "");


//            HPRTPrinterHelper.PrintText(msg, 32, 0, 16);
//            if(!idNumber.equals("") && idNumber!=null){
//                Log.d(TAG, "PrintSlip: ID No");


            HPRTPrinterHelper.PrintQRCode(qrCode, 7, (3 + 0x30), 1);


//            HPRTPrinterHelper.PrintText("\n" + Common.centerString(16, "Powered By soja.co.ke"), 0, 1, 0);
            HPRTPrinterHelper.PrintText("\n" + ">>>> Powered By soja.co.ke <<<<", 0, 0, 0);


            HPRTPrinterHelper.PrintText("\n", 0, 1, 0);

            PAct.AfterPrintAction();
            Log.d(TAG, "PrintSlip: Done");
            if (dialog.isShowing())
                dialog.dismiss();
            showSuccess();

//            startActivity(new Intent(getApplicationContext(), Dashboard.class));
//            finish();
        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }

    void showSuccess() {
        dialog.dismiss();
        LocalBroadcastManager.getInstance(GuestList.this).sendBroadcast(new Intent(Constants.RECORDED_VISITOR));
        dialog = new MaterialDialog.Builder(this)
                .title("PRINTED")
                .titleGravity(GravityEnum.CENTER)
                .customView(R.layout.success_dialog, true)
                .positiveText("OK")
                .negativeText("CANCEL")
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
//                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
//                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
//                        finish();
                    }
                })
                .build();
        View view = dialog.getCustomView();
        TextView messageText = (TextView) view.findViewById(R.id.message);
        messageText.setText("Guest has been recorded");
        dialog.show();
    }


    private void CapturePrinterFunction()
    {
        try
        {
            int[] propType=new int[1];
            byte[] Value=new byte[500];
            int[] DataLen=new int[1];
            String strValue="";
            boolean isCheck=false;

            int iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BEEP, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.Buzzer=(Value[0]==0?false:true);

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.Cut=(Value[0]==0?false:true);
            //btnCut.setVisibility((PrinterProperty.Cut?View.VISIBLE:View.GONE));

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_DRAWER, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.Cashdrawer=(Value[0]==0?false:true);
            //btnOpenCashDrawer.setVisibility((PrinterProperty.Cashdrawer?View.VISIBLE:View.GONE));

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BARCODE, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.Barcode=new String(Value);
            isCheck=PrinterProperty.Barcode.replace("QRCODE", "").replace("PDF417", "").replace(",,", ",").replace(",,", ",").length()>0;
            //btn1DBarcodes.setVisibility((isCheck?View.VISIBLE:View.GONE));
            isCheck = PrinterProperty.Barcode.contains("QRCODE");
            //btnQRCode.setVisibility((isCheck?View.VISIBLE:View.GONE));
            //btnPDF417.setVisibility((isCheck?View.VISIBLE:View.GONE));

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.Pagemode=(Value[0]==0?false:true);

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_GET_REMAINING_POWER, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.GetRemainingPower=(Value[0]==0?false:true);
            //btnGetRemainingPower.setVisibility((PrinterProperty.GetRemainingPower?View.VISIBLE:View.GONE));

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_CONNECT_TYPE, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.ConnectType=(Value[1]<<8)+Value[0];

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PRINT_RECEIPT, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.SampleReceipt=(Value[0]==0?false:true);
            //btnSampleReceipt.setVisibility((PrinterProperty.SampleReceipt?View.VISIBLE:View.GONE));
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
        }
    }
    private void GetPrinterProperty()
    {
        try
        {
            int[] propType=new int[1];
            byte[] Value=new byte[500];
            int[] DataLen=new int[1];
            String strValue="";
            int iRtn=0;

            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_STATUS_MODEL, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.StatusMode=Value[0];

            if(PrinterProperty.Cut)
            {
                iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT_SPACING, propType, Value,DataLen);
                if(iRtn!=0)
                    return;
                PrinterProperty.CutSpacing=Value[0];
            }
            else
            {
                iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_TEAR_SPACING, propType, Value,DataLen);
                if(iRtn!=0)
                    return;
                PrinterProperty.TearSpacing=Value[0];
            }

            if(PrinterProperty.Pagemode)
            {
                iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE_AREA, propType, Value,DataLen);
                if(iRtn!=0)
                    return;
                PrinterProperty.PagemodeArea=new String(Value).trim();
            }
            Value=new byte[500];
            iRtn=HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_WIDTH, propType, Value,DataLen);
            if(iRtn!=0)
                return;
            PrinterProperty.PrintableWidth=(int)(Value[0] & 0xFF | ((Value[1] & 0xFF) <<8));
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
        }
    }

    private boolean EnableBluetooth() {
        boolean bRet = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled())
                return true;
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                bRet = true;
                Log.d("PRTLIB", "BTO_EnableBluetooth --> Open OK");
            }
        } else {
            Log.d("HPRTSDKSample", (new StringBuilder("Activity_Main --> EnableBluetooth ").append("Bluetooth Adapter is null.")).toString());
        }
        return bRet;
    }

    private void InitSetting() {
        String SettingValue = "";
        SettingValue = PFun.ReadSharedPreferencesData("Codepage");
        if (SettingValue.equals(""))
            PFun.WriteSharedPreferencesData("Codepage", "0,PC437(USA:Standard Europe)");

        SettingValue = PFun.ReadSharedPreferencesData("Cut");
        if (SettingValue.equals(""))
            PFun.WriteSharedPreferencesData("Cut", "0");    //0:��ֹ,1:��ӡǰ,2:��ӡ��

        SettingValue = PFun.ReadSharedPreferencesData("Cashdrawer");
        if (SettingValue.equals(""))
            PFun.WriteSharedPreferencesData("Cashdrawer", "0");

        SettingValue = PFun.ReadSharedPreferencesData("Buzzer");
        if (SettingValue.equals(""))
            PFun.WriteSharedPreferencesData("Buzzer", "0");

        SettingValue = PFun.ReadSharedPreferencesData("Feeds");
        if (SettingValue.equals(""))
            PFun.WriteSharedPreferencesData("Feeds", "0");
    }

}

