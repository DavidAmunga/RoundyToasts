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
        preferences = new Preferences(getActivity());

        ArrayList<String> checkoutTitles = new ArrayList<>();


        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutTitles.add("Express Checkout");
        }

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutTitles.add("Drive Out");
        }

        checkoutTitles.add("Walk Out");

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutTitles.add("Residents");
        }

        if (preferences.isFingerprintsEnabled())
            checkoutTitles.add("Biometric Checkout");


        ArrayList<String> checkoutDescriptions = new ArrayList<>();

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutDescriptions.add("Scan QR to check out visitor");
        }

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutDescriptions.add("Check out a driving visitor");
        }
        if (preferences.getBaseURL().contains("casuals")) {
            checkoutDescriptions.add("Check out a supervisor on foot");
        } else {
            checkoutDescriptions.add("Check out a visitor on foot");

        }
        checkoutDescriptions.add("Check out a resident");
        if (preferences.isFingerprintsEnabled())
            checkoutDescriptions.add("Check out using biometrics");

        ArrayList<Integer> checkoutDrawables = new ArrayList<>();

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutDrawables.add(R.drawable.ic_qr);
        }

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutDrawables.add(R.drawable.ic_drive_in_new);
        }

        checkoutDrawables.add(R.drawable.ic_walk_in_new);

        if (!preferences.getBaseURL().contains("casuals")) {
            checkoutDrawables.add(R.drawable.ic_resident_icon_new);
        }

        if (preferences.isFingerprintsEnabled())
            checkoutDrawables.add(R.drawable.fingerprint);

        Object[] a = checkoutTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = checkoutDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c = checkoutDrawables.toArray();
        drawables = Arrays.copyOf(c, c.length, Integer[].class);

        CheckInAdapter checkInAdapter = new CheckInAdapter(getActivity(), titles, descriptions, drawables, "checkout");
        setListAdapter(checkInAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        String item = l.getItemAtPosition(position).toString();

        switch (item) {
            case "Express Checkout":
                startActivity(new Intent(getActivity().getApplicationContext(), ExpressCheckoutActivity.class));
                break;
            case "Drive Out":
                Intent driveOut = new Intent(getActivity().getApplicationContext(), Visitors.class);
                driveOut.putExtra("TYPE", "DRIVE");
                startActivity(driveOut);
                break;
            case "Walk Out":
                Intent walkOut = new Intent(getActivity().getApplicationContext(), Visitors.class);
                walkOut.putExtra("TYPE", "WALK");
                startActivity(walkOut);
                break;
            case "Residents":
                Intent resident = new Intent(getActivity().getApplicationContext(), Visitors.class);
                resident.putExtra("TYPE", "RESIDENTS");
                startActivity(resident);
                break;
            case "Biometric Checkout":
                if (preferences.isFingerprintsEnabled()) {
                    Intent fingerPrint = new Intent(getActivity(), FingerprintActivity.class);
                    fingerPrint.putExtra("CHECKOUT", true);
                    startActivity(fingerPrint);
                }
                break;
        }
    }

}
