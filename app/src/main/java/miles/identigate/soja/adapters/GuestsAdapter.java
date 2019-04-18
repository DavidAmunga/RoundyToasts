package miles.identigate.soja.adapters;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.gesture.GestureUtils;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.Resident;
import miles.identigate.soja.models.ServiceProviderModel;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.ui.adapters.GuestsPagedAdapter;
import miles.identigate.soja.ui.listeners.ItemClickListener;

public class GuestsAdapter extends PagedListAdapter<Guest, RecyclerView.ViewHolder> {

    private static final String TAG = "GuestsAdapter";

    Context context;
    OnItemClick mCallback;

    ArrayList<Guest> guestsSearch = new ArrayList<>();


    public GuestsAdapter(Context context, OnItemClick mCallback) {
        super(Guest.DIFF_CALLBACK);
        this.context = context;
        this.mCallback = mCallback;
    }

    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_guest, parent, false);


        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((GuestViewHolder) holder).bindTo(position);

    }


    public class GuestViewHolder extends RecyclerView.ViewHolder {


        TextView txtGuest, txtCompany;


        public GuestViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGuest = itemView.findViewById(R.id.txtGuest);
            txtCompany = itemView.findViewById(R.id.txtCompany);

        }

        void bindTo(int position) {
            Guest guest = getItem(position);

            txtGuest.setText(guest.getFirstName() + " " + guest.getLastName());
            txtCompany.setText(guest.getCompany());

            itemView.setOnClickListener(v -> {
                mCallback.onVisitorClick(guest);

            });
        }


    }


}

