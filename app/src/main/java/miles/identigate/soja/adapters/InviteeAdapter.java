package miles.identigate.soja.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnInviteeClick;
import miles.identigate.soja.models.Invitee;

public class InviteeAdapter extends RecyclerView.Adapter<InviteeAdapter.InviteeViewHolder> {

    Context context;
    List<Invitee> inviteeList = new ArrayList<>();
    OnInviteeClick mCallback;

    public InviteeAdapter(Context context, List<Invitee> inviteeList, OnInviteeClick mListener) {
        this.context = context;
        this.inviteeList = inviteeList;
        this.mCallback = mListener;
    }

    @NonNull
    @Override
    public InviteeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_invitee, parent, false);

        return new InviteeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteeViewHolder holder, int i) {
        Invitee invitee = inviteeList.get(i);

        holder.txtResident.setText("With " + invitee.getHostFirstName() + " " + (invitee.getHostLastName() != null ? invitee.getHostLastName() : ""));
        holder.txtAppointeeName.setText(invitee.getFirstName() + " " + (invitee.getLastName() != null ? invitee.getLastName() : ""));

        holder.setTime(invitee.getArrivalTime());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onInviteeClick(invitee);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inviteeList.size();
    }

    public class InviteeViewHolder extends RecyclerView.ViewHolder {

        TextView txtAppointeeName;
        TextView txtResident;
        TextView txtTime;

        public InviteeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAppointeeName = itemView.findViewById(R.id.txt_appointee_name);
            txtResident = itemView.findViewById(R.id.txt_resident_name);
            txtTime = itemView.findViewById(R.id.txt_time);

        }

        public void setTime(String time) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

            try {
                Date oldDate = format.parse(time);
                String newDate;
                newDate = new SimpleDateFormat("h:mm a").format(oldDate);

                txtTime.setText(newDate);

            } catch (
                    ParseException e) {
                e.printStackTrace();
            }


        }

    }
}
