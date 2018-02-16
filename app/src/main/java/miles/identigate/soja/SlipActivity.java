package miles.identigate.soja;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.BitSet;
import java.util.Set;
import java.util.UUID;

import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.PrinterCommands;
import miles.identigate.soja.Helpers.SojaActivity;

public class SlipActivity extends SojaActivity {
    private static final int REQUEST_ENABLE_BT = 200;
    private static final String TAG = SlipActivity.class.getName();
    ImageView ok;
    ImageView cancel;
    Toolbar toolbar;
    TextView slip;
    BluetoothAdapter mBluetoothAdapter;

    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    MaterialDialog dialog;

    String firstName;
    String lastName;
    String idNumber;
    String house;
    String result_slip;
    byte[] imageBytes;
    
    BitSet dots;
    private int mWidth;
    private int mHeight;
    private String mStatus;

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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                setupBluetooth();
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
    private void setupBluetooth(){
        findBT();
        try {
            openBT();
            //sendData();
            //closeBT();
            if ((!result_slip.isEmpty()) && (result_slip != "")){
                new ImageService().execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    // This will find a bluetooth printer device
    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(SlipActivity.this,"No bluetooth adapter available", Toast.LENGTH_SHORT);
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                    .getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // MP300 is the name of the bluetooth printer device
                    if (device.getName().contains("MP")) {
                        mmDevice = device;
                        break;
                    }
                }
            }
            Toast.makeText(SlipActivity.this,"Bluetooth Device Found", Toast.LENGTH_SHORT);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tries to open a connection to the bluetooth printer device
    void openBT() throws IOException {
        try {
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            //mmSocket.connect();
            try {
                mmSocket.connect();
                Log.e("","Connected");
            } catch (IOException e) {
                Log.e("",e.getMessage());
                try {
                    Log.e("","trying fallback...");

                    mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();

                    Log.e("","Connected");
                }
                catch (Exception e2) {
                    Log.e("", "Couldn't establish Bluetooth connection!");
                }
            }
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();
            Toast.makeText(SlipActivity.this,"Bluetooth Opened", Toast.LENGTH_SHORT);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // After opening a connection to bluetooth printer device,
    // we have to listen and check if a data were sent to be printed.
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // This is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted()
                            && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length);
                                        final String data = new String(
                                                encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
                                                Toast.makeText(SlipActivity.this,data, Toast.LENGTH_SHORT);
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * This will send data to be printed by the bluetooth printer
     */
    void sendData() throws IOException {
        try {
            // the text typed by the user
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
            if ((!result_slip.isEmpty()) && (result_slip != "")){
                msg += "SCAN QR TO CHECKOUT VISITOR";
                msg += "\n\n";
            }
            msg += "POWERED BY WWW.SOJA.CO.KE";
            msg += "\n\n";
            mmOutputStream.write(msg.getBytes());
            if ((!result_slip.isEmpty()) && (result_slip != "") && imageBytes.length > 0){
                //mmOutputStream.write(imageBytes);
                print_image();
            }
            mmOutputStream.write("\n\n".getBytes());
            showSuccess();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void print_image() throws IOException {
        Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes.length);
        convertBitmap(bmp);
        mmOutputStream.write(PrinterCommands.SET_LINE_SPACING_24);

        int offset = 0;
        while (offset < bmp.getHeight()) {
            mmOutputStream.write(PrinterCommands.SELECT_BIT_IMAGE_MODE);
            for (int x = 0; x < bmp.getWidth(); ++x) {

                for (int k = 0; k < 3; ++k) {

                    byte slice = 0;
                    for (int b = 0; b < 8; ++b) {
                        int y = (((offset / 8) + k) * 8) + b;
                        int i = (y * bmp.getWidth()) + x;
                        boolean v = false;
                        if (i < dots.length()) {
                            v = dots.get(i);
                        }
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }
                    mmOutputStream.write(slice);
                }
            }
            offset += 24;
            mmOutputStream.write(PrinterCommands.FEED_LINE);
            mmOutputStream.write(PrinterCommands.FEED_LINE);
            mmOutputStream.write(PrinterCommands.FEED_LINE);
            mmOutputStream.write(PrinterCommands.FEED_LINE);
            mmOutputStream.write(PrinterCommands.FEED_LINE);
            mmOutputStream.write(PrinterCommands.FEED_LINE);
        }
        mmOutputStream.write(PrinterCommands.SET_LINE_SPACING_30);

    }
    public String convertBitmap(Bitmap inputBitmap) {

        mWidth = inputBitmap.getWidth();
        mHeight = inputBitmap.getHeight();

        convertArgbToGrayscale(inputBitmap, mWidth, mHeight);
        mStatus = "ok";
        return mStatus;

    }

    private void convertArgbToGrayscale(Bitmap bmpOriginal, int width,
                                        int height) {
        int pixel;
        int k = 0;
        int B = 0, G = 0, R = 0;
        dots = new BitSet();
        try {

            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    // get one pixel color
                    pixel = bmpOriginal.getPixel(y, x);

                    // retrieve color of all channels
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);
                    // take conversion up to one single value by calculating
                    // pixel intensity.
                    R = G = B = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                    // set bit into bitset, by calculating the pixel's luma
                    if (R < 55) {
                        dots.set(k);//this is the bitset that i'm printing
                    }
                    k++;

                }


            }


        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, e.toString());
        }
    }
    private  class  ImageService extends AsyncTask<Void,Void, byte[]>{
        @Override
        protected byte[] doInBackground(Void... params) {
            return getByteArrayImage(result_slip);
        }
        protected void onPostExecute(byte[] image){
            imageBytes = image;
            try {
                sendData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private byte[] getByteArrayImage(String url){
        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            ByteArrayBuffer baf = new ByteArrayBuffer(500);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            return baf.toByteArray();
        } catch (Exception e) {
            Log.d("ImageManager", "Error: " + e.toString());
        }
        return null;
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
                        try {
                            closeBT();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(getApplicationContext(),Dashboard.class));
                        finish();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        try {
                            closeBT();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    // Close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

