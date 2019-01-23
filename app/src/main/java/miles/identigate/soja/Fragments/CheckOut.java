package miles.identigate.soja.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import miles.identigate.soja.Adapters.CheckInAdapter;
import miles.identigate.soja.ExpressCheckoutActivity;
import miles.identigate.soja.FingerprintActivity;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanQRActivity;
import miles.identigate.soja.UserInterface.Visitors;

public class CheckOut extends ListFragment {


    private String[] titles;
    private String[] descriptions;
    private Integer[] drawables;
    Preferences preferences;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CheckOut() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= new Preferences(getActivity());

        ArrayList<String> checkoutTitles = new ArrayList<>();
        checkoutTitles.add("Express Checkout");
        checkoutTitles.add("Drive Out");
        checkoutTitles.add("Walk Out");
        checkoutTitles.add("Residents");
        if (preferences.isFingerprintsEnabled())
            checkoutTitles.add("Biometric Checkout");


        ArrayList<String> checkoutDescriptions =  new ArrayList<>();
        checkoutDescriptions.add("Scan QR to check out visitor");
        checkoutDescriptions.add("Check out a driving visitor");
        checkoutDescriptions.add("Check out a visitor on foot");
        checkoutDescriptions.add("Check out a resident");
        if (preferences.isFingerprintsEnabled())
            checkoutDescriptions.add("Check out using biometrics");

        ArrayList<Integer> checkoutDrawables = new ArrayList<>();
        checkoutDrawables.add(R.drawable.ic_qr_code);
        checkoutDrawables.add(R.drawable.ic_car);
        checkoutDrawables.add(R.drawable.ic_walk);
        checkoutDrawables.add(R.drawable.ic_resident);
        if (preferences.isFingerprintsEnabled())
            checkoutDrawables.add(R.drawable.fingerprint);

        Object[] a = checkoutTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = checkoutDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c  = checkoutDrawables.toArray();
        drawables = Arrays.copyOf(c, c.length, Integer[].class);

        CheckInAdapter checkInAdapter =new CheckInAdapter(getActivity(),titles,descriptions,drawables);
        setListAdapter(checkInAdapter);

    }
    @Override
    public void onListItemClick (ListView l, View v, int position, long id){
        switch (position){
            case 0:
                startActivity(new Intent(getActivity().getApplicationContext(), ExpressCheckoutActivity.class));
                break;
            case 1:
                Intent driveOut=new Intent(getActivity().getApplicationContext(), Visitors.class);
                driveOut.putExtra("TYPE", "DRIVE");
                startActivity(driveOut);
                break;
            case 2:
                Intent walkOut=new Intent(getActivity().getApplicationContext(), Visitors.class);
                walkOut.putExtra("TYPE","WALK");
                startActivity(walkOut);
                break;
            case 3:
                Intent resident=new Intent(getActivity().getApplicationContext(), Visitors.class);
                resident.putExtra("TYPE","RESIDENTS");
                startActivity(resident);
                break;
            case 4:
                if (preferences.isFingerprintsEnabled()){
                    Intent fingerPrint = new Intent(getActivity(), FingerprintActivity.class);
                    fingerPrint.putExtra("CHECKOUT", true);
                    startActivity(fingerPrint);
                }
                break;
        }
    }

}
