package miles.identigate.soja.UserInterface;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import miles.identigate.soja.R;

public class MenuFragment extends Fragment {
    CardView recordDrive;
    CardView recordWalk;
    CardView activeVisitors;
    CardView logout;
    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_menu, container, false);
        recordDrive = view.findViewById(R.id.recordDrive);
        recordWalk = view.findViewById(R.id.recordWalk);
        activeVisitors = view.findViewById(R.id.activeVisitors);
        logout = view.findViewById(R.id.logout);
        recordDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(),RecordDriveIn.class));
            }
        });
        recordWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(),RecordWalkIn.class));
            }
        });
        activeVisitors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(), Visitors.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getActivity().getApplicationContext(),RecordDriveIn.class));
                new MaterialDialog.Builder(getActivity())
                        .title("Logout")
                        .content("You are about to logout of Soja")
                        .positiveText("Ok")
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                getActivity().finish();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        return view;
    }

}
