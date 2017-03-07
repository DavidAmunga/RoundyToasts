package miles.identigate.soja.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import miles.identigate.soja.Adapters.Option;
import miles.identigate.soja.Logs.AllLogs;
import miles.identigate.soja.Logs.Incidents;
import miles.identigate.soja.R;

public class Logs extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String[] titles={
            "Drive Logs",
            "Pedestrian Logs",
            "Incidents"
    };
    private String[] descriptions={
            "Summary log of Motor Vehicles ",
            "Summary log of Walking Visitors",
            "List of incidents"

    };
    private int[] drawables={
            R.drawable.ic_action_car,
            R.drawable.ic_action_walk,
            R.drawable.ic_action_many

    };
    // TODO: Rename and change types of parameters
    public static Logs newInstance(String param1, String param2) {
        Logs fragment = new Logs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Logs() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Option option=new Option(getActivity(),titles,descriptions,drawables);
        // TODO: Change Adapter to display your content
        setListAdapter(option);
    }
    @Override
    public void onListItemClick (ListView l, View v, int position, long id){
        switch (position){
            case 0:
                Intent driveOut=new Intent(getActivity().getApplicationContext(), AllLogs.class);
                driveOut.putExtra("TYPE", "DRIVE");
                startActivity(driveOut);
                break;
            case 1:
                Intent walkOut=new Intent(getActivity().getApplicationContext(), AllLogs.class);
                walkOut.putExtra("TYPE","WALK");
                startActivity(walkOut);
                break;
            case 2:
                Intent resident=new Intent(getActivity().getApplicationContext(), Incidents.class);
                startActivity(resident);
                break;
        }
    }
}
