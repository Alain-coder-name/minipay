package com.example.minipay;

import android.graphics.Color;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction tx);
    }

    public TransactionAdapter(List<Transaction> list) {
        this.list = new java.util.ArrayList<>(list);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Transaction> newList) {
        this.list.clear();
        this.list.addAll(newList);
        notifyDataSetChanged();
    }

    public Transaction getTransactionAt(int position) {
        return list.get(position);
    }

    public void removeItem(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    // ── ViewHolder ────────────────────────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, transactionText, dateText, amountText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon            = itemView.findViewById(R.id.icon);
            transactionText = itemView.findViewById(R.id.transactionText);
            dateText        = itemView.findViewById(R.id.dateText);
            amountText      = itemView.findViewById(R.id.amountText);
        }
    }

    // ── Inflate ───────────────────────────────────────────────────
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    // ── Bind ──────────────────────────────────────────────────────
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = list.get(position);

        holder.transactionText.setText(tx.getTitle());
        holder.dateText.setText(tx.getDate());
        holder.amountText.setText(tx.getFormattedAmount());

        if (tx.isSent()) {
            holder.icon.setText("↑");
            holder.icon.setBackgroundResource(R.drawable.icon_sent_bg);
            holder.amountText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_debit));
        } else if (tx.isRequested()) {
            holder.icon.setText("⌛");
            holder.icon.setBackgroundResource(R.drawable.avatar_bg); // Neutral background
            holder.amountText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_gray));
        } else {
            holder.icon.setText("↓");
            holder.icon.setBackgroundResource(R.drawable.icon_received_bg);
            holder.amountText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_credit));
        }

        // Alternate row background for readability
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.bg_card));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.bg_alternate));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(tx);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }
}
