package com.example.minipay;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView         recyclerView;
    TransactionAdapter   adapter;
    android.widget.EditText searchHistory;
    com.google.android.material.button.MaterialButton filterAll, filterSent, filterReceived, filterRequested;
    int currentFilter = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);

        // Get the shared transaction list (never null)
        List<Transaction> list = DashboardActivity.transactions;
        if (list == null) list = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(list);
        adapter.setOnItemClickListener(tx -> {
            android.content.Intent intent = new android.content.Intent(HistoryActivity.this, ReceiptActivity.class);
            intent.putExtra("title", tx.getTitle());
            intent.putExtra("amount", tx.getFormattedAmount());
            intent.putExtra("date", tx.getDate());
            intent.putExtra("type", tx.getType());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        searchHistory = findViewById(R.id.searchHistory);
        filterAll = findViewById(R.id.filterAll);
        filterSent = findViewById(R.id.filterSent);
        filterReceived = findViewById(R.id.filterReceived);
        filterRequested = findViewById(R.id.filterRequested);

        searchHistory.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        filterAll.setOnClickListener(v -> setFilter(-1));
        filterSent.setOnClickListener(v -> setFilter(Transaction.TYPE_SENT));
        filterReceived.setOnClickListener(v -> setFilter(Transaction.TYPE_RECEIVED));
        filterRequested.setOnClickListener(v -> setFilter(Transaction.TYPE_REQUESTED));

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Transaction tx = adapter.getTransactionAt(position);
                    DashboardActivity.transactions.remove(tx);
                    adapter.removeItem(position);
                    DataManager.saveData(HistoryActivity.this);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Back button
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void setFilter(int type) {
        currentFilter = type;
        
        int teal = androidx.core.content.ContextCompat.getColor(this, R.color.teal_primary);
        int white = androidx.core.content.ContextCompat.getColor(this, R.color.white);
        
        filterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(type == -1 ? teal : white));
        filterAll.setTextColor(type == -1 ? white : teal);
        
        filterSent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(type == Transaction.TYPE_SENT ? teal : white));
        filterSent.setTextColor(type == Transaction.TYPE_SENT ? white : teal);
        
        filterReceived.setBackgroundTintList(android.content.res.ColorStateList.valueOf(type == Transaction.TYPE_RECEIVED ? teal : white));
        filterReceived.setTextColor(type == Transaction.TYPE_RECEIVED ? white : teal);

        filterRequested.setBackgroundTintList(android.content.res.ColorStateList.valueOf(type == Transaction.TYPE_REQUESTED ? teal : white));
        filterRequested.setTextColor(type == Transaction.TYPE_REQUESTED ? white : teal);
        
        applyFilters();
    }

    private void applyFilters() {
        String query = searchHistory.getText().toString().toLowerCase().trim();
        List<Transaction> filtered = new ArrayList<>();
        
        for (Transaction tx : DashboardActivity.transactions) {
            boolean matchesType = (currentFilter == -1) || (tx.getType() == currentFilter);
            boolean matchesText = query.isEmpty() || tx.getTitle().toLowerCase().contains(query) || tx.getFormattedAmount().contains(query);
            
            if (matchesType && matchesText) {
                filtered.add(tx);
            }
        }
        
        adapter.updateList(filtered);
    }
}
