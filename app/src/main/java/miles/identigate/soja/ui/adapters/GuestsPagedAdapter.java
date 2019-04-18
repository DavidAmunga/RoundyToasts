package miles.identigate.soja.ui.adapters;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import miles.identigate.soja.R;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.model.NetworkState;
import miles.identigate.soja.ui.listeners.ItemClickListener;

public class GuestsPagedAdapter extends PagedListAdapter<Guest, RecyclerView.ViewHolder> {

    private static final String TAG = "GuestsPagedAdapter";


    private NetworkState networkState;
    private ItemClickListener itemClickListener;

    public GuestsPagedAdapter(ItemClickListener itemClickListener) {
        super(Guest.DIFF_CALLBACK);
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == R.layout.list_item_guest) {
            View view = layoutInflater.inflate(R.layout.list_item_guest, parent, false);
            GuestViewHolder viewHolder = new GuestViewHolder(view, itemClickListener);

            return viewHolder;
        } else if (viewType == R.layout.network_state_item) {
            View view = layoutInflater.inflate(R.layout.network_state_item, parent, false);
            return new NetworkStateItemViewHolder(view);
        } else {
            throw new IllegalArgumentException("unknown view type");
        }

    }
    @Override
    public int getItemViewType(int position) {
        if(hasExtraRow() && position==getItemCount()-1){
            return R.layout.network_state_item;
        }else{
            return R.layout.list_item_guest;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case R.layout.list_item_guest:
                Log.d(TAG, "onBindViewHolder: Appointment");
                ((GuestViewHolder) holder).bindTo(getItem(position));
                break;
            case R.layout.network_state_item:
                ((NetworkStateItemViewHolder) holder).bindView(networkState);
                break;
        }
    }

    public boolean hasExtraRow() {
        if (networkState != null && networkState != NetworkState.LOADED) {
            return true;
        } else {
            return false;
        }
    }


    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();

        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(getItemCount());
            } else {
                notifyItemInserted(getItemCount());
            }
        } else if (newExtraRow && previousState != newNetworkState) {
            notifyItemChanged(getItemCount() - 1);
        }
    }


    public class GuestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView txtGuest, txtCompany;
        private ItemClickListener itemClickListener;
        private Guest guest;


        public GuestViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            txtGuest = itemView.findViewById(R.id.txtGuest);
            txtCompany = itemView.findViewById(R.id.txtCompany);
            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);

        }


        public void bindTo(Guest guest) {
            this.guest = guest;
            txtGuest.setText(guest.getFirstName() + " " + guest.getLastName());
            txtCompany.setText(guest.getCompany());


        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(guest); // call the onClick in the OnItemClickListener
            }
        }
    }

    public class NetworkStateItemViewHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;
        private final TextView errorMsg;


        public NetworkStateItemViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
            errorMsg = itemView.findViewById(R.id.error_msg);
        }

        public void bindView(NetworkState networkState) {
            if (networkState != null && networkState.getStatus() == NetworkState.Status.RUNNING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (networkState != null && networkState.getStatus() == NetworkState.Status.FAILED) {
//                errorMsg.setVisibility(View.VISIBLE);
//                errorMsg.setText(networkState.getMsg());
            } else {
                errorMsg.setVisibility(View.GONE);
            }
        }

    }


}
