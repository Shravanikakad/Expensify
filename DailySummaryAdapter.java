package com.example.expensify;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailySummaryAdapter extends RecyclerView.Adapter<DailySummaryAdapter.ViewHolder> {

    private Context context;
    private List<DailyExpenseSummary> summaryList;

    public DailySummaryAdapter(Context context, List<DailyExpenseSummary> summaryList) {
        this.context = context;
        this.summaryList = summaryList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, totalTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            totalTextView = itemView.findViewById(R.id.totalTextView);
        }
    }

    @NonNull
    @Override
    public DailySummaryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailySummaryAdapter.ViewHolder holder, int position) {
        DailyExpenseSummary summary = summaryList.get(position);
        holder.dateTextView.setText("Date: " + summary.getDate());
        holder.totalTextView.setText("Total: ₹" + summary.getTotalAmount());
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }
}
