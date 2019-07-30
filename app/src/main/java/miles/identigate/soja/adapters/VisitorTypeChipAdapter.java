package miles.identigate.soja.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnVisitorTypeChipClick;
import miles.identigate.soja.models.TypeObject;

public class VisitorTypeChipAdapter extends RecyclerView.Adapter<VisitorTypeChipAdapter.VisitorTypeChipViewHolder> {

    private static final String TAG = "VisitorTypeChipAdapter";


    Context context;
    List<TypeObject> visitorTypeChipList = new ArrayList<>();
    OnVisitorTypeChipClick mCallback;

    public VisitorTypeChipAdapter(Context context, List<TypeObject> visitorTypeChipList, OnVisitorTypeChipClick mCallback) {
        this.context = context;
        this.visitorTypeChipList = visitorTypeChipList;
        this.mCallback = mCallback;
    }


    @NonNull
    @Override
    public VisitorTypeChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visitor_type_chip, parent, false);
        Log.d(TAG, "onCreateViewHolder: Create View");
        return new VisitorTypeChipViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorTypeChipViewHolder holder, int i) {
        TypeObject chip = visitorTypeChipList.get(i);
        holder.text.setText(chip.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onVisitorTypeChipClick(chip, holder.itemView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return visitorTypeChipList.size();
    }

    public class VisitorTypeChipViewHolder extends RecyclerView.ViewHolder {
        TextView text;


        public VisitorTypeChipViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }
}
