package com.example.minipay;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectUserActivity extends AppCompatActivity {

    // ── Data ──────────────────────────────────────────────────────
    private final List<Contact> allContacts = new ArrayList<>();
    private List<Contact> filteredContacts  = new ArrayList<>();

    // ── Views ─────────────────────────────────────────────────────
    private EditText       etSearch;
    private LinearLayout   frequentRow, allContactsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        seedContacts();

        etSearch              = findViewById(R.id.searchUser);
        frequentRow           = findViewById(R.id.frequentContactsRow);
        allContactsContainer  = findViewById(R.id.allContactsContainer);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.addContactBtn).setOnClickListener(v -> showAddContactDialog());

        buildFrequentRow();
        filteredContacts = new ArrayList<>(allContacts);
        buildAllContactsList(filteredContacts);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterContacts(s.toString().trim().toLowerCase());
            }
        });

        findViewById(R.id.nativeContactBtn).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        });
    }

    // ── Native Contact Picker ─────────────────────────────────────
    private final ActivityResultLauncher<String> requestContactsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    pickContact();
                } else {
                    Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                    try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String name = cursor.getString(nameIndex);
                            String number = cursor.getString(numberIndex);
                            
                            openSendMoney(new Contact(name, number, false));
                        }
                    }
                }
            });

    private void pickContact() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        pickContactLauncher.launch(pickIntent);
    }

    // ── Add Contact ───────────────────────────────────────────────
    private void showAddContactDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16));

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        layout.addView(nameInput);

        final EditText phoneInput = new EditText(this);
        phoneInput.setHint("Phone Number");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(phoneInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Contact")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(SelectUserActivity.this, "Please enter name and phone", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Contact newContact = new Contact(name, phone, false);
                    allContacts.add(newContact);
                    Collections.sort(allContacts, (a, b) -> Character.compare(a.getSortKey(), b.getSortKey()));

                    // Refresh current view based on search
                    filterContacts(etSearch.getText().toString().trim().toLowerCase());
                    Toast.makeText(SelectUserActivity.this, "Contact added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Seed ──────────────────────────────────────────────────────
    private void seedContacts() {
        allContacts.add(new Contact("Aman Kumar",    "+91 98765 43210", true));
        allContacts.add(new Contact("Anjali Mehta",  "+91 91234 56789", false));
        allContacts.add(new Contact("Arjun Nair",    "+91 99876 54321", false));
        allContacts.add(new Contact("Karan Singh",   "+91 87654 32109", true));
        allContacts.add(new Contact("Kavya Reddy",   "+91 93456 78901", false));
        allContacts.add(new Contact("Neha Patel",    "+91 90123 45678", true));
        allContacts.add(new Contact("Priya Sharma",  "+91 98765 12345", true));
        allContacts.add(new Contact("Rahul Verma",   "+91 99999 00001", true));
        allContacts.add(new Contact("Sneha Kapoor",  "+91 88888 77777", false));
        allContacts.add(new Contact("Vikram Das",    "+91 77777 88888", false));

        // Sort by last name
        Collections.sort(allContacts, (a, b) -> Character.compare(a.getSortKey(), b.getSortKey()));
    }

    // ── Frequent Row (horizontal bubbles) ─────────────────────────
    private void buildFrequentRow() {
        frequentRow.removeAllViews();
        for (Contact c : allContacts) {
            if (!c.isFrequent()) continue;
            frequentRow.addView(buildFrequentBubble(c));
        }
    }

    private View buildFrequentBubble(Contact contact) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                dpToPx(72), LinearLayout.LayoutParams.WRAP_CONTENT);
        colLp.setMarginEnd(dpToPx(8));
        col.setLayoutParams(colLp);
        col.setClickable(true);
        col.setFocusable(true);
        col.setOnClickListener(v -> openSendMoney(contact));

        // Avatar
        TextView avatar = new TextView(this);
        avatar.setText(contact.getInitials());
        avatar.setTextSize(15);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.avatar_bg);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dpToPx(54), dpToPx(54));
        avatar.setLayoutParams(avatarLp);

        // Name label
        TextView name = new TextView(this);
        name.setText(contact.getName().split(" ")[0]); // first name only
        name.setTextSize(11);
        name.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        name.setPadding(0, dpToPx(4), 0, 0);

        col.addView(avatar);
        col.addView(name);
        return col;
    }

    // ── Alphabetical Contact List ──────────────────────────────────
    private void buildAllContactsList(List<Contact> contacts) {
        allContactsContainer.removeAllViews();

        // Group by sort key
        Map<Character, List<Contact>> grouped = new LinkedHashMap<>();
        for (Contact c : contacts) {
            char key = c.getSortKey();
            if (!grouped.containsKey(key)) grouped.put(key, new ArrayList<>());
            grouped.get(key).add(c);
        }

        for (Map.Entry<Character, List<Contact>> entry : grouped.entrySet()) {
            // Section header
            TextView header = new TextView(this);
            header.setText(String.valueOf(entry.getKey()));
            header.setTextSize(13);
            header.setTypeface(null, Typeface.BOLD);
            header.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            header.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_main));
            header.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(4));
            allContactsContainer.addView(header);

            // Contacts in this section
            for (Contact c : entry.getValue()) {
                allContactsContainer.addView(buildContactRow(c));
            }
        }

        if (contacts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No contacts found");
            empty.setGravity(Gravity.CENTER);
            empty.setTextSize(14);
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            empty.setPadding(dpToPx(24), dpToPx(40), dpToPx(24), dpToPx(40));
            allContactsContainer.addView(empty);
        }
    }

    private View buildContactRow(Contact contact) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        row.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_card));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> openSendMoney(contact));

        // Avatar circle
        TextView avatar = new TextView(this);
        avatar.setText(contact.getInitials());
        avatar.setTextSize(13);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.avatar_bg);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dpToPx(44), dpToPx(44));
        avLp.setMarginEnd(dpToPx(14));
        avatar.setLayoutParams(avLp);

        // Name + Phone column
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        col.setLayoutParams(colLp);

        TextView name = new TextView(this);
        name.setText(contact.getName());
        name.setTextSize(15);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(ContextCompat.getColor(this, R.color.text_dark));

        TextView phone = new TextView(this);
        phone.setText(contact.getPhone());
        phone.setTextSize(12);
        phone.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        phone.setPadding(0, dpToPx(2), 0, 0);

        col.addView(name);
        col.addView(phone);

        // Chevron
        TextView chevron = new TextView(this);
        chevron.setText("›");
        chevron.setTextSize(22);
        chevron.setTextColor(ContextCompat.getColor(this, R.color.text_light_gray));

        row.addView(avatar);
        row.addView(col);
        row.addView(chevron);

        // Divider below
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        View divider = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divLp.setMarginStart(dpToPx(74));
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
        wrapper.addView(row);
        wrapper.addView(divider);
        return wrapper;
    }

    // ── Filter ────────────────────────────────────────────────────
    private void filterContacts(String query) {
        filteredContacts.clear();
        for (Contact c : allContacts) {
            if (c.getName().toLowerCase().contains(query) ||
                c.getPhone().replace(" ", "").contains(query)) {
                filteredContacts.add(c);
            }
        }
        buildAllContactsList(filteredContacts);
    }

    // ── Navigation ────────────────────────────────────────────────
    private void openSendMoney(Contact contact) {
        Intent intent = new Intent(this, SendMoneyActivity.class);
        intent.putExtra("receiver_name",  contact.getName());
        intent.putExtra("receiver_phone", contact.getPhone());
        startActivity(intent);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}