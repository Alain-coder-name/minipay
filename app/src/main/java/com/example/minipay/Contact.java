package com.example.minipay;

/**
 * Model class for a contact (user that can receive money).
 */
public class Contact {

    private String name;
    private String phone;
    private boolean isFrequent;

    public Contact(String name, String phone, boolean isFrequent) {
        this.name       = name;
        this.phone      = phone;
        this.isFrequent = isFrequent;
    }

    public String  getName()      { return name; }
    public String  getPhone()     { return phone; }
    public boolean isFrequent()   { return isFrequent; }

    /** Returns up to 2 uppercase initials from the name */
    public String getInitials() {
        String[] parts = name.trim().split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    /** First letter of last name (or first name) — used for alphabetical grouping */
    public char getSortKey() {
        String[] parts = name.trim().split(" ");
        return Character.toUpperCase(parts[parts.length - 1].charAt(0));
    }
}
