package com.michaelaskew.avocadotimer.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.michaelaskew.avocadotimer.R;
import com.michaelaskew.avocadotimer.activities.AvocadoDetailActivity;
import com.michaelaskew.avocadotimer.models.Avocado;
import com.michaelaskew.avocadotimer.utilities.TimeUtils;
import com.michaelaskew.avocadotimer.views.CircleChartView;

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
        View view = LayoutInflater.from(context).inflate(R.layout.avocado_grid_item, parent, false);
        return new AvocadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvocadoViewHolder holder, int position) {
        if (position < 0 || position >= avocadoList.size()) {
            return;  // Safety check
        }
        Avocado avocado = avocadoList.get(position);
        Uri thumb = Uri.parse(avocado.getImagePath());

        boolean nameIsSet = !avocado.getName().isEmpty();
        String creationTime = avocado.getCreationTime();
        int squishiness = avocado.getSquishiness();
        holder.tvThumbnail.setImageURI(thumb);
        holder.tvName.setText(nameIsSet ? avocado.getName() : "[No Name]");
        holder.tvCreatedAt.setText(TimeUtils.getRelativeTimeText(creationTime));

        double fractionElapsed = (double) TimeUtils.getTimeRemaining(creationTime, 360, squishiness)[1]; // Assuming this method returns the correct value
        holder.circleChartView.setFractionElapsed(fractionElapsed);  // Set the fraction elapsed to the CircleChartView of the current item

        // Post a runnable to get width after the layout is complete
        holder.tvThumbnail.post(new Runnable() {
            @Override
            public void run() {
                int width = holder.tvThumbnail.getWidth();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, width);
                holder.tvThumbnail.setLayoutParams(layoutParams);
            }
        });
        // ... (set other attributes of avocado)
        // ... (set other attributes of avocado)
    }

    @Override
    public int getItemCount() {
        return (avocadoList != null) ? avocadoList.size() : 0;
    }

    public class AvocadoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView tvThumbnail;
        TextView tvName;
        TextView tvCreatedAt;
        CircleChartView circleChartView;
        // ... (other views)

        public AvocadoViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvThumbnail = itemView.findViewById(R.id.avocado_thumbnail);
            tvName = itemView.findViewById(R.id.avocado_name);
            tvCreatedAt = itemView.findViewById(R.id.avocado_creation_date);
            circleChartView = itemView.findViewById(R.id.circleChartView); // Initialize the CircleChartView
            // ... (initialize other views)
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Avocado clickedAvocado = avocadoList.get(position);

                Intent intent = new Intent(context, AvocadoDetailActivity.class);
                intent.putExtra("avocado_id", clickedAvocado.getId());

                // You can pass other details if needed
                context.startActivity(intent);
            }
        }
    }
}
