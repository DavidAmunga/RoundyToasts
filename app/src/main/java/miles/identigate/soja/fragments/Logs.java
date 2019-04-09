package miles.identigate.soja.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import miles.identigate.soja.adapters.CheckInAdapter;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.logs.AllLogs;
import miles.identigate.soja.logs.Incidents;
import miles.identigate.soja.R;

public class Logs extends ListFragment {

    Preferences preferences;


    private String[] titles;


    private String[] descriptions;
    private Integer[] drawables;

    ArrayList<String> logsTitles = new ArrayList<>();
    ArrayList<String> logsDescriptions = new ArrayList<>();
    ArrayList<Integer> logsDrawables = new ArrayList<>();


    String entity_owner = "visitor";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Logs() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new Preferences(getActivity());


        if (!preferences.getBaseURL().contains("casuals")) {
            logsTitles.add("Drive Logs");
            logsDescriptions.add("Summary log of Motor Vehicles");
            logsDrawables.add(R.drawable.ic_drive_in_log_new);

        }

        if (preferences.getBaseURL().contains("casuals")) {
            entity_owner = "employee";

        }

        logsTitles.add("Pedestrian Logs");


        logsDescriptions.add("Summary log of Walking "+entity_owner);
        logsDrawables.add(R.drawable.ic_walk_in_log_new);


        Object[] a = logsTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = logsDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c = logsDrawables.toArray();
        drawables = Arrays.copyOf(c, c.length, Integer[].class);


        CheckInAdapter checkInAdapter = new CheckInAdapter(getActivity(), titles, descriptions, drawables, "logs");
        setListAdapter(checkInAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                Intent driveOut = new Intent(getActivity().getApplicationContext(), AllLogs.class);
                driveOut.putExtra("TYPE", "DRIVE");
                startActivity(driveOut);
                break;
            case 1:
                Intent walkOut = new Intent(getActivity().getApplicationContext(), AllLogs.class);
                walkOut.putExtra("TYPE", "WALK");
                startActivity(walkOut);
                break;
            case 2:
                Intent resident = new Intent(getActivity().getApplicationContext(), Incidents.class);
                startActivity(resident);
                break;
        }
    }
}
