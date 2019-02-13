package miles.identigate.soja.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import miles.identigate.soja.Adapters.CheckInAdapter;
import miles.identigate.soja.Logs.AllLogs;
import miles.identigate.soja.Logs.Incidents;
import miles.identigate.soja.R;

public class Logs extends ListFragment {


    private String[] titles = {
            "Drive Logs",
            "Pedestrian Logs"
//            "Incidents"
    };
    private String[] descriptions = {
            "Summary log of Motor Vehicles ",
            "Summary log of Walking Visitors"
//            "List of incidents"

    };
    private Integer[] drawables = {
            R.drawable.ic_car,
            R.drawable.ic_walk
//            R.drawable.ic_resident

    };


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Logs() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckInAdapter checkInAdapter =new CheckInAdapter(getActivity(),titles,descriptions,drawables,"logs");
        setListAdapter(checkInAdapter);
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
