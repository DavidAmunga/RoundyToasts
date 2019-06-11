package miles.identigate.soja.activities;

import android.arch.paging.PagedList;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import HPRTAndroidSDK.HPRTPrinterHelper;
import HPRTAndroidSDK.PublicFunction;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;
import miles.identigate.soja.Printer.DeviceListActivity;
import miles.identigate.soja.Printer.PrinterProperty;
import miles.identigate.soja.Printer.PublicAction;
import miles.identigate.soja.R;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.SojaActivity;
import miles.identigate.soja.models.Ticket;

public class TicketList extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    DatabaseReference mDatabase;
    private static final String TAG = "TicketList";

    private static final int REQUEST_ENABLE_LOCATION = 300;


    private static HPRTPrinterHelper HPRTPrinter = new HPRTPrinterHelper();
    private BluetoothAdapter mBluetoothAdapter;
    private PublicFunction PFun = null;
    private PublicAction PAct = null;

    String qrCode = "";


    FirebaseRecyclerAdapter<Ticket, TicketViewHolder> adapter;
    FirebaseRecyclerPagingAdapter<Ticket, TicketViewHolder> pagingAdapter;

    @BindView(R.id.title)
    TextViewBold title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    MaterialDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);
        ButterKnife.bind(this);


        DatabaseReference ticketsRef = FirebaseDatabase.getInstance().getReference(Common.TICKETS);
        ticketsRef.keepSynced(true);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        title.setText("Tickets");


        mDatabase = FirebaseDatabase.getInstance().getReference(Common.TICKETS);

//        Query query = FirebaseDatabase.getInstance()
//                .getReference(Common.TICKETS)
////                .limitToFirst(10)
////                .orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                ;


        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        DatabasePagingOptions<Ticket> options = new DatabasePagingOptions.Builder<Ticket>()
                .setLifecycleOwner(this)
                .setQuery(mDatabase, config, Ticket.class)
                .build();


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        FirebaseRecyclerOptions<Ticket> options =
//                new FirebaseRecyclerOptions.Builder<Ticket>()
//                        .setQuery(query, Ticket.class)
//                        .build();

        pagingAdapter = new FirebaseRecyclerPagingAdapter<Ticket, TicketViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TicketViewHolder holder, int position, @NonNull Ticket ticket) {
                Log.d(TAG, "onBindViewHolder:Ticket ");
                holder.ticket_id.setText(ticket.getTicketId());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        qrCode = ticket.getTicketId();
                        doPrint();
                    }
                });
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        // Do your loading animation
                        swipeRefresh.setRefreshing(true);
                        break;

                    case LOADED:
                        // Stop Animation
                        swipeRefresh.setRefreshing(false);
                        break;

                    case FINISHED:
                        //Reached end of Data set
                        swipeRefresh.setRefreshing(false);
                        break;

                    case ERROR:
//                        retry();
                        break;
                }
            }

            @NonNull
            @Override
            public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
                return new TicketViewHolder(view);
            }

//            @Override
//            protected void onError(@NonNull DatabaseError databaseError) {
//                retry();
//
//            }
        };


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pagingAdapter.refresh();
            }
        });


        recyclerView.setAdapter(pagingAdapter);

//        Get Connected Status
//        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
//        connectedRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean connected = snapshot.getValue(Boolean.class);
//                if (connected) {
//                    Toast.makeText(TicketList.this, "Connected", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, "connected");
//                } else {
//                    Toast.makeText(TicketList.this, "Disconnected", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.w(TAG, "Listener was cancelled");
//            }
//        });

        dialog = Constants.showProgressDialog(TicketList.this, "Printing", "Printing Ticket...");
        dialog.setCancelable(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        PFun = new PublicFunction(TicketList.this);
        PAct = new PublicAction(TicketList.this);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String strIsConnected;
        Log.d(TAG, "onActivityResult: " + String.valueOf(resultCode));
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
                    HPRTPrinter = new HPRTPrinterHelper(TicketList.this, PrinterName);
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

    private void doPrint() {
        setupBT();
        if (!dialog.isShowing())
            dialog.show();
        String PrinterName = "MPT-II";
        HPRTPrinter = new HPRTPrinterHelper(TicketList.this, PrinterName);
        CapturePrinterFunction();
        GetPrinterProperty();
        PrintSlip();
    }

    private void setupBT() {
        if (ContextCompat.checkSelfPermission(TicketList.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            dialog.dismiss();
            ActivityCompat.requestPermissions(TicketList.this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ENABLE_LOCATION);
        } else {
            Intent serverIntent = new Intent(TicketList.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
            return;
        }
    }

    private void PrintSlip() {
        Log.d(TAG, "PrintSlip: Start");
        try {
            byte[] data = new byte[]{0x1b, 0x40};
            HPRTPrinterHelper.WriteData(data);
            PAct.LanguageEncode();
            PAct.BeforePrintAction();
//            HPRTPrinterHelper.PrintText("\t " + preferences.getPremiseName() + "\n");


            HPRTPrinterHelper.PrintQRCode(qrCode, 7, (3 + 0x30), 1);


//            HPRTPrinterHelper.PrintText("\n" + Common.centerString(16, "Powered By soja.co.ke"), 0, 1, 0);
//            HPRTPrinterHelper.PrintText("\n" + ">>>> Powered By soja.co.ke <<<<", 0, 0, 0);


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


    private void CapturePrinterFunction() {
        try {
            int[] propType = new int[1];
            byte[] Value = new byte[500];
            int[] DataLen = new int[1];
            String strValue = "";
            boolean isCheck = false;

            int iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BEEP, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Buzzer = (Value[0] == 0 ? false : true);

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Cut = (Value[0] == 0 ? false : true);
            //btnCut.setVisibility((PrinterProperty.Cut?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_DRAWER, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Cashdrawer = (Value[0] == 0 ? false : true);
            //btnOpenCashDrawer.setVisibility((PrinterProperty.Cashdrawer?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_BARCODE, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Barcode = new String(Value);
            isCheck = PrinterProperty.Barcode.replace("QRCODE", "").replace("PDF417", "").replace(",,", ",").replace(",,", ",").length() > 0;
            //btn1DBarcodes.setVisibility((isCheck?View.VISIBLE:View.GONE));
            isCheck = PrinterProperty.Barcode.contains("QRCODE");
            //btnQRCode.setVisibility((isCheck?View.VISIBLE:View.GONE));
            //btnPDF417.setVisibility((isCheck?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.Pagemode = (Value[0] == 0 ? false : true);

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_GET_REMAINING_POWER, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.GetRemainingPower = (Value[0] == 0 ? false : true);
            //btnGetRemainingPower.setVisibility((PrinterProperty.GetRemainingPower?View.VISIBLE:View.GONE));

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_CONNECT_TYPE, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.ConnectType = (Value[1] << 8) + Value[0];

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PRINT_RECEIPT, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.SampleReceipt = (Value[0] == 0 ? false : true);
            //btnSampleReceipt.setVisibility((PrinterProperty.SampleReceipt?View.VISIBLE:View.GONE));
        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
        }
    }

    private void GetPrinterProperty() {
        try {
            int[] propType = new int[1];
            byte[] Value = new byte[500];
            int[] DataLen = new int[1];
            String strValue = "";
            int iRtn = 0;

            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_STATUS_MODEL, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.StatusMode = Value[0];

            if (PrinterProperty.Cut) {
                iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_CUT_SPACING, propType, Value, DataLen);
                if (iRtn != 0)
                    return;
                PrinterProperty.CutSpacing = Value[0];
            } else {
                iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_TEAR_SPACING, propType, Value, DataLen);
                if (iRtn != 0)
                    return;
                PrinterProperty.TearSpacing = Value[0];
            }

            if (PrinterProperty.Pagemode) {
                iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_PAGEMODE_AREA, propType, Value, DataLen);
                if (iRtn != 0)
                    return;
                PrinterProperty.PagemodeArea = new String(Value).trim();
            }
            Value = new byte[500];
            iRtn = HPRTPrinterHelper.CapturePrinterFunction(HPRTPrinterHelper.HPRT_MODEL_PROPERTY_KEY_WIDTH, propType, Value, DataLen);
            if (iRtn != 0)
                return;
            PrinterProperty.PrintableWidth = (int) (Value[0] & 0xFF | ((Value[1] & 0xFF) << 8));
        } catch (Exception e) {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> CapturePrinterFunction ")).append(e.getMessage()).toString());
        }
    }


    public class TicketViewHolder extends RecyclerView.ViewHolder {

        TextView ticket_id;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticket_id = itemView.findViewById(R.id.ticket_id);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        pagingAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        pagingAdapter.startListening();

    }

    void showSuccess() {
        dialog.dismiss();
        LocalBroadcastManager.getInstance(TicketList.this).sendBroadcast(new Intent(Constants.RECORDED_VISITOR));
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
        messageText.setText("Ticket Recorded");
        dialog.show();
    }

}
