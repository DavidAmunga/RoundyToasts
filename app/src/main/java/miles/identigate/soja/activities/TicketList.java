package miles.identigate.soja.activities;

import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.identigate.soja.R;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewBold;
import miles.identigate.soja.models.Ticket;

public class TicketList extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    DatabaseReference mDatabase;
    private static final String TAG = "TicketList";


    FirebaseRecyclerAdapter<Ticket, TicketViewHolder> adapter;
    FirebaseRecyclerPagingAdapter<Ticket, TicketViewHolder> pagingAdapter;

    @BindView(R.id.title)
    TextViewBold title;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);
        ButterKnife.bind(this);




        DatabaseReference ticketsRef = FirebaseDatabase.getInstance().getReference(Common.TICKETS);
        ticketsRef.keepSynced(true);



        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        title.setText("Tickets");


        mDatabase = FirebaseDatabase.getInstance().getReference(Common.TICKETS);

//        Query query = FirebaseDatabase.getInstance()
//                .getReference(Common.TICKETS)
////                .limitToFirst(10)
////                .orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                ;


        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        DatabasePagingOptions<Ticket> options = new DatabasePagingOptions.Builder<Ticket>()
                .setLifecycleOwner(this)
                .setQuery(mDatabase, config, Ticket.class)
                .build();


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        FirebaseRecyclerOptions<Ticket> options =
//                new FirebaseRecyclerOptions.Builder<Ticket>()
//                        .setQuery(query, Ticket.class)
//                        .build();

        pagingAdapter = new FirebaseRecyclerPagingAdapter<Ticket, TicketViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TicketViewHolder holder, int position, @NonNull Ticket ticket) {
                Log.d(TAG, "onBindViewHolder:Ticket ");
                holder.ticket_id.setText(ticket.getTicketId());
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        // Do your loading animation
                        swipeRefresh.setRefreshing(true);
                        break;

                    case LOADED:
                        // Stop Animation
                        swipeRefresh.setRefreshing(false);
                        break;

                    case FINISHED:
                        //Reached end of Data set
                        swipeRefresh.setRefreshing(false);
                        break;

                    case ERROR:
//                        retry();
                        break;
                }
            }

            @NonNull
            @Override
            public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
                return new TicketViewHolder(view);
            }

//            @Override
//            protected void onError(@NonNull DatabaseError databaseError) {
//                retry();
//
//            }
        };


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pagingAdapter.refresh();
            }
        });

//        adapter = new FirebaseRecyclerAdapter<Ticket, TicketViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull TicketViewHolder holder, int position, @NonNull Ticket ticket) {
//
//            }
//
//            @NonNull
//            @Override
//            public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
//
//            }
//        };


        recyclerView.setAdapter(pagingAdapter);

//        Get Connected Status
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Toast.makeText(TicketList.this, "Connected", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "connected");
                } else {
                    Toast.makeText(TicketList.this, "Disconnected", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Listener was cancelled");
            }
        });


    }

    public class TicketViewHolder extends RecyclerView.ViewHolder {

        TextView ticket_id;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ticket_id = itemView.findViewById(R.id.ticket_id);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        pagingAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        pagingAdapter.startListening();

    }
}
