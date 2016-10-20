package miles.identigate.soja;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.UserInterface.RecordDriveIn;
import miles.identigate.soja.UserInterface.RecordResident;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.UserInterface.ServiceProvider;
import miles.identigate.soja.app.Common;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String activity = "TargetActivity";

    // TODO: Rename and change types of parameters
    private int TargetActivity;


    public ManualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ManualFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManualFragment newInstance(int param1) {
        ManualFragment fragment = new ManualFragment();
        Bundle args = new Bundle();
        args.putInt(activity, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TargetActivity = getArguments().getInt(activity);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_manual, container, false);
        Button next=(Button)view.findViewById(R.id.next);
        final EditText name=(EditText) view.findViewById(R.id.name);
        final EditText id=(EditText) view.findViewById(R.id.id);
        final Spinner idTypes=(Spinner)view.findViewById(R.id.id_types);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.id_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        idTypes.setAdapter(adapter);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.getText().toString().equals(null)|| name.getText().toString().equals(null)){
                    Snackbar.make(v,"All fields are required",Snackbar.LENGTH_SHORT).show();
                }else {
                    //Collect the faqin details about the visitor before proceeding!
                    Bundle extras = new Bundle();
                    extras.putString(Common.ID_NUMBER, id.getText().toString());
                    extras.putString(Common.FIRST_NAME, name.getText().toString());
                    extras.putString(Common.ID_TYPE, idTypes.getSelectedItem().toString());
                    StartActivity(TargetActivity, extras);
                }
            }
        });
        return view;
    }
    public void StartActivity(int target,Bundle data){
        //direct to the correct activity
      switch (target) {
          case Common.DRIVE_IN:
              Intent intent0 = new Intent(getActivity(), RecordDriveIn.class);
              intent0.putExtras(data);
              startActivity(intent0);
              break;
          case Common.WALK_IN:
              Intent intent1 = new Intent(getActivity(), RecordWalkIn.class);
              intent1.putExtra("EntryType", Common.MANUAL);
              intent1.putExtras(data);
              startActivity(intent1);
              break;
          case Common.SERVICE_PROVIDER:
              Intent intent2 = new Intent(getActivity(), ServiceProvider.class);
              intent2.putExtras(data);
              startActivity(intent2);
              break;
          case Common.RESIDENTS:
              Intent intent3 = new Intent(getActivity(), RecordResident.class);
              intent3.putExtras(data);
              break;
          case Common.INCIDENT:
              Intent intent4 = new Intent(getActivity(), Incident.class);
              intent4.putExtras(data);
              startActivity(intent4);
              break;
      }
    }

}
