package com.michaelaskew.avocadotimer.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.activities.AvocadoDetailActivity;
import com.michaelaskew.avocadotimer.models.Avocado;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AvocadoAdapter extends RecyclerView.Adapter<AvocadoAdapter.AvocadoViewHolder> {

    private List<Avocado> avocadoList;
    private Context context;

    public AvocadoAdapter(Context context, List<Avocado> avocados) {
        this.context = context;
        this.avocadoList = avocados;
    }

    @NonNull
    @Override
    public AvocadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.avocado_list_item, parent, false);
        return new AvocadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvocadoViewHolder holder, int position) {
        Avocado avocado = avocadoList.get(position);
        holder.tvName.setText(avocado.getName());
        if (avocado.getCreationTime() != null) {
//            holder.tvCreatedAt.setText(avocado.getCreationTime());
            // Formatting creation time for display
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM HH:mm:ss");
//            String formattedDateTimeString = avocado.getCreationTime().format(formatter);
//            holder.tvCreatedAt.setText(formattedDateTimeString);
        }
        // ... (set other attributes of avocado)
    }

    @Override
    public int getItemCount() {
        return avocadoList.size();
    }

    public class AvocadoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvName;
        TextView tvCreatedAt;
        // ... (other views)

        public AvocadoViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvName = itemView.findViewById(R.id.avocado_name);
            tvCreatedAt = itemView.findViewById(R.id.avocado_creation_date);

            // ... (initialize other views)
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Avocado clickedAvocado = avocadoList.get(position);

            Intent intent = new Intent(context, AvocadoDetailActivity.class);
            intent.putExtra("avocado_id", clickedAvocado.getId());

            Log.d("AvocadoDetailActivity", "Avocado Adapter " + clickedAvocado.getId());
            // You can pass other details if needed
            context.startActivity(intent);
        }
    }
}
