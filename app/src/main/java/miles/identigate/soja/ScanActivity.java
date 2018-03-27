package miles.identigate.soja;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.regula.sdk.CaptureActivity;
import com.regula.sdk.DocumentReader;
import com.regula.sdk.enums.MRZDetectorErrorCode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import miles.identigate.soja.Fragments.EntryTypeFragment;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.app.Common;

public class ScanActivity extends Activity implements EntryTypeFragment.OnEntrySelectedListener{
    private static boolean sIsLicenseOk;
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_entry_type);
        new Constants().fieldItems.clear();
        FragmentTransaction transaction=getFragmentManager().beginTransaction();
        //transaction.setCustomAnimations(R.anim.pull_in_left,R.anim.push_out_left);
        EntryTypeFragment entryTypeFragment=new EntryTypeFragment();
        Bundle args = new Bundle();
        args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
        entryTypeFragment.setArguments(args);
        transaction.replace(R.id.parent,entryTypeFragment);
        transaction.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!sIsLicenseOk) {
            try {
                InputStream licInput = getResources().openRawResource(R.raw.regula);
                byte[] license = new byte[licInput.available()];
                licInput.read(license);
                sIsLicenseOk = DocumentReader.setLibLicense(ScanActivity.this,license);
                licInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1){
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Bitmap bmp = getBitmap(selectedImage);
                    int status = DocumentReader.processBitmap(bmp);
                    if(status == MRZDetectorErrorCode.MRZ_RECOGNIZED_CONFIDENTLY) {
                        Intent i = new Intent(ScanActivity.this, ResultsActivity.class);
                        Bundle args = new Bundle();
                        args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
                        i.putExtras(args);
                        ScanActivity.this.startActivity(i);
                        finish();
                    } else{
                        Toast.makeText(ScanActivity.this, R.string.no_mrz,Toast.LENGTH_LONG).show();
                    }
                }
            } else if(requestCode == DocumentReader.READER_REQUEST_CODE){
                Intent i = new Intent(ScanActivity.this, ResultsActivity.class);
                Bundle args = new Bundle();
                args.putInt("TargetActivity", getIntent().getExtras().getInt("TargetActivity"));
                i.putExtras(args);
                ScanActivity.this.startActivity(i);
                finish();
            }
        }
    }

    private Bitmap getBitmap(Uri selectedImage) {
        ContentResolver resolver = ScanActivity.this.getContentResolver();
        InputStream is = null;
        try {
            is = resolver.openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        //Re-reading the input stream to move it's pointer to start
        try {
            is = resolver.openInputStream(selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 1280, 720);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void OnEntrySelected(int type) {
        if (sIsLicenseOk) {
            if(type== Common.SCAN){
                Intent intent = new Intent(ScanActivity.this, CaptureActivity.class);
                ScanActivity.this.startActivityForResult(intent, DocumentReader.READER_REQUEST_CODE);
            }else{
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
            builder.setTitle(R.string.strError);
            builder.setMessage(R.string.strLicenseInvalid);
            builder.setPositiveButton(R.string.strOK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }
            });
            builder.show();
        }
    }
}

