package miles.identigate.soja.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import miles.identigate.soja.adapters.CheckInAdapter;
import miles.identigate.soja.ExpressCheckoutActivity;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.UserInterface.Visitors;

public class CheckOut extends ListFragment {


    private String[] titles;
    private String[] descriptions;
    private Integer[] drawables;
    Preferences preferences;


    ArrayList<String> checkoutTitles = new ArrayList<>();
    ArrayList<String> checkOutDescriptions = new ArrayList<>();
    ArrayList<Integer> checkOutDrawables = new ArrayList<>();

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


        Constants.setDashboardCheckOut(preferences, checkoutTitles, checkOutDrawables, checkOutDescriptions);


        Object[] a = checkoutTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = checkOutDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c = checkOutDrawables.toArray();
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
//                    Intent fingerPrint = new Intent(getActivity(), FingerprintActivity.class);
//                    fingerPrint.putExtra("CHECKOUT", true);
//                    startActivity(fingerPrint);
                }
                break;
        }
    }

}
