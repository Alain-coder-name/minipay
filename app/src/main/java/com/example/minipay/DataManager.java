package com.example.minipay;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataManager {
    private static final String PREF_NAME = "minipay_data";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_TRANSACTIONS = "transactions";
    private static final String KEY_IS_INITIALIZED = "is_initialized";

    public static void loadData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        boolean isInitialized = prefs.getBoolean(KEY_IS_INITIALIZED, false);
        if (!isInitialized) {
            // Seed initial data once
            DashboardActivity.balance = 12500.00;
            DashboardActivity.transactions.clear();
            DashboardActivity.transactions.add(new Transaction("Sent to Priya", 2000.00, "10 Apr 2024", Transaction.TYPE_SENT));
            DashboardActivity.transactions.add(new Transaction("Received from Aman", 1500.00, "08 Apr 2024", Transaction.TYPE_RECEIVED));
            DashboardActivity.transactions.add(new Transaction("Sent to Rahul", 800.00, "05 Apr 2024", Transaction.TYPE_SENT));
            DashboardActivity.transactions.add(new Transaction("Received from Neha", 2200.00, "02 Apr 2024", Transaction.TYPE_RECEIVED));
            DashboardActivity.transactions.add(new Transaction("Sent to Karan", 1000.00, "29 Mar 2024", Transaction.TYPE_SENT));
            
            saveData(context); // Save this initial state
            return;
        }

        // Load balance
        String balanceStr = prefs.getString(KEY_BALANCE, "12500.00");
        try {
            DashboardActivity.balance = Double.parseDouble(balanceStr);
        } catch (Exception e) {
            DashboardActivity.balance = 12500.00;
        }

        // Load transactions
        DashboardActivity.transactions.clear();
        String json = prefs.getString(KEY_TRANSACTIONS, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String title = obj.getString("title");
                double amount = obj.getDouble("amount");
                String date = obj.getString("date");
                int type = obj.getInt("type");
                DashboardActivity.transactions.add(new Transaction(title, amount, date, type));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        JSONArray array = new JSONArray();
        try {
            for (Transaction tx : DashboardActivity.transactions) {
                JSONObject obj = new JSONObject();
                obj.put("title", tx.getTitle());
                obj.put("amount", tx.getAmount());
                obj.put("date", tx.getDate());
                obj.put("type", tx.getType());
                array.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        prefs.edit()
            .putBoolean(KEY_IS_INITIALIZED, true)
            .putString(KEY_BALANCE, String.valueOf(DashboardActivity.balance))
            .putString(KEY_TRANSACTIONS, array.toString())
            .apply();
    }
}
