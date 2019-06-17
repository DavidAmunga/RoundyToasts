package miles.identigate.soja.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnEventClick;
import miles.identigate.soja.models.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    Context context;
    ArrayList<Event> eventArrayList;
    OnEventClick mCallBack;

    public EventAdapter(Context context, ArrayList<Event> eventArrayList, OnEventClick listener) {
        this.context = context;
        this.eventArrayList = eventArrayList;
        this.mCallBack = listener;
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_layout_item, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int i) {
        Event event = eventArrayList.get(i);

        holder.eventName.setText(event.getName());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.onEventClick(event);

            }
        });

    }

    @Override
    public int getItemCount() {
        return eventArrayList.size();
    }


    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.name);

        }
    }
}
