package com.example.minipay;

/**
 * Model class for a single transaction entry.
 */
public class Transaction {

    public static final int TYPE_SENT      = 0;
    public static final int TYPE_RECEIVED  = 1;
    public static final int TYPE_REQUESTED = 2;

    private String title;      // e.g. "Sent to Priya" / "Received from Aman"
    private double amount;
    private String date;
    private int type;          // TYPE_SENT, TYPE_RECEIVED, or TYPE_REQUESTED

    public Transaction(String title, double amount, String date, int type) {
        this.title  = title;
        this.amount = amount;
        this.date   = date;
        this.type   = type;
    }

    public String getTitle()  { return title; }
    public double getAmount() { return amount; }
    public String getDate()   { return date; }
    public int    getType()   { return type; }

    public boolean isSent() { return type == TYPE_SENT; }
    public boolean isRequested() { return type == TYPE_REQUESTED; }

    /** Formatted amount string with ₹ symbol */
    public String getFormattedAmount() {
        String prefix = isSent() ? "-₹" : "+₹";
        return prefix + String.format("%,.2f", amount);
    }

    /** Returns initials for the avatar circle */
    public String getInitials() {
        // Extract name part after "to" or "from"
        String name = title.replaceAll("(?i)sent to |received from |requested from ", "").trim();
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }
}
