package miles.identigate.soja;

import android.bluetooth.BluetoothAdapter;;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import HPRTAndroidSDK.HPRTPrinterHelper;
import HPRTAndroidSDK.PublicFunction;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.SojaActivity;
import miles.identigate.soja.Printer.DeviceListActivity;
import miles.identigate.soja.Printer.PrinterProperty;
import miles.identigate.soja.Printer.PublicAction;

public class SlipActivity extends SojaActivity {
    private static final int REQUEST_ENABLE_BT = 200;
    private static final String TAG = SlipActivity.class.getName();
    ImageView ok;
    ImageView cancel;
    Toolbar toolbar;
    TextView slip;

    private static HPRTPrinterHelper HPRTPrinter=new HPRTPrinterHelper();
    private BluetoothAdapter mBluetoothAdapter;
    private PublicFunction PFun=null;
    private PublicAction PAct=null;
    MaterialDialog dialog;

    String firstName;
    String lastName;
    String idNumber;
    String house;
    String result_slip;
    private String ConnectType="Bluetooth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slip);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ok = (ImageView)findViewById(R.id.ok);
        cancel = (ImageView)findViewById(R.id.cancel);
        slip = (TextView)findViewById(R.id.title);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            String title = extras.getString("title");
            firstName = extras.getString("firstName");
            lastName = extras.getString("lastName");
            idNumber = extras.getString("idNumber");
            house = extras.getString("house");
            result_slip = extras.getString("result_slip");
            if (!title.isEmpty()){
               slip.setText(title);
            }
        }


        dialog = Constants.showProgressDialog(SlipActivity.this, "Printing", "Printing slip...");
        dialog.setCancelable(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        PFun=new PublicFunction(SlipActivity.this);
        PAct=new PublicAction(SlipActivity.this);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                //TODO: Print
                setupBT();
                String PrinterName="MPT-II";
                HPRTPrinter=new HPRTPrinterHelper(SlipActivity.this,PrinterName);
                CapturePrinterFunction();
                GetPrinterProperty();
                PrintSlip();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Dashboard.class));
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            //setupConnection();
        }else{
            new MaterialDialog.Builder(SlipActivity.this)
                    .title("Bluetooth Disabled")
                    .content("Bluetooth must be enabled to print the slip.")
                    .positiveText("OK")
                    .show();
        }
    }
    private void setupBT(){
        if (ContextCompat.checkSelfPermission(SlipActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SlipActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
        } else {
            Intent serverIntent = new Intent(SlipActivity.this,DeviceListActivity.class);
            startActivityForResult(serverIntent, HPRTPrinterHelper.ACTIVITY_CONNECT_BT);
            return;
        }
    }
    private void PrintSlip()
    {
        try
        {
            byte[] data=new byte[]{0x1b,0x40};
            HPRTPrinterHelper.WriteData(data);
            PAct.LanguageEncode();
            PAct.BeforePrintAction();
            String msg = "\t VISITOR SLIP";
            msg += "\n\n";
            msg += "------------------------------";
            msg += "\n\n";
            msg += "VISITOR NAME: " + firstName;
            msg += "\n";
            msg += "OFFICE VISITED: " + house;
            msg += "\n";
            msg += "ENTRY TIME: " + Constants.timeStamp();
            msg += "\n\n\n";
            msg += "HOST NAME: " + "------------------";
            msg += "\n\n";
            msg += "HOST SIGN: " + "------------------";
            msg += "\n\n";
           /* if ((!result_slip.isEmpty()) && (result_slip != "")){
                msg += "SCAN QR TO CHECKOUT VISITOR";
                msg += "\n\n";
            }*/
            msg += "POWERED BY WWW.SOJA.CO.KE";
            msg += "\n\n";
            HPRTPrinterHelper.PrintText(msg);
            HPRTPrinterHelper.PrintQRCode("1",16,(3+0x30),1);
//			byte[] data1=new byte[]{0x1D,0x0C};
//			HPRTPrinterHelper.WriteData(data1);
            PAct.AfterPrintAction();
        }
        catch(Exception e)
        {
            Log.e("HPRTSDKSample", (new StringBuilder("Activity_Main --> PrintSampleReceipt ")).append(e.getMessage()).toString());
        }
    }
    void showSuccess(){
        dialog.dismiss();
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
                        startActivity(new Intent(getApplicationContext(),Dashboard.class));
                        finish();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        startActivity(new Intent(getApplicationContext(),Dashboard.class));
                        finish();
                    }
                })
                .build();
        View view=dialog.getCustomView();
        TextView messageText=(TextView)view.findViewById(R.id.message);
        messageText.setText("Visitor has been successfully recorded.");
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

}

