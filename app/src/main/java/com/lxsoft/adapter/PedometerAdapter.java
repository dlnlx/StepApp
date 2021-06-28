package com.lxsoft.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lxsoft.bean.PedometerBean;
import com.lxsoft.stepapp.History2DetailActivity;
import com.lxsoft.stepapp.HistoryActivity2;
import com.lxsoft.stepapp.R;
import com.lxsoft.stepapp.SettingActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PedometerAdapter extends RecyclerView.Adapter<PedometerAdapter.ViewHolder> {
    private List<PedometerBean> pedometerBeanList;



    static class ViewHolder extends RecyclerView.ViewHolder{
        View pedometerView;
        TextView startTime;
        TextView stepCount;

        public ViewHolder(View view){
            super(view);
            pedometerView = view;
            startTime = view.findViewById(R.id.startTime);
            stepCount = view.findViewById(R.id.stepCount);
        }
    }

    public PedometerAdapter(List<PedometerBean> pedometerBeanList1){
        pedometerBeanList = pedometerBeanList1;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pedometer_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.pedometerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                PedometerBean pedometerBean = pedometerBeanList.get(position);
                Intent intent = new Intent("com.example.activitytest.ACTION_START");
                intent.putExtra("Pedometer", pedometerBean);
                parent.getContext().startActivity(intent);
                //Toast.makeText(v.getContext(), "The calorie is :"+pedometerBean.getCalorie(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        PedometerBean pedometerBean = pedometerBeanList.get(position);
        String day = pedometerBean.getDay();




        holder.startTime.setText(day);
        holder.stepCount.setText(String.valueOf(pedometerBean.getStepCount())+"步");
    }



    @Override
    public int getItemCount() {
        return pedometerBeanList.size();
    }
}
