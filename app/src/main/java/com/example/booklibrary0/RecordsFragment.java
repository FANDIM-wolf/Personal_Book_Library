package com.example.booklibrary0;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecordsFragment extends Fragment {

    private static final int REQ_CONTACTS = 100;

    private RecyclerView recyclerView;
    private LoanRecyclerAdapter adapter;

    private final List<Loan> loans = new ArrayList<>();
    private DatabaseHelper dbHelper;

    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLoans);
        Button btnAddRecord = view.findViewById(R.id.btnAddRecord);
        Button btnSyncContacts = view.findViewById(R.id.btnSyncContacts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(requireContext());
        adapter = new LoanRecyclerAdapter(loans, dbHelper, this::loadAll);
        recyclerView.setAdapter(adapter);

        btnAddRecord.setOnClickListener(v -> showAddRecordDialog());
        btnSyncContacts.setOnClickListener(v -> requestContactsPermission());

        loadAll();
        return view;
    }

    private void loadAll() {
        loans.clear();
        loans.addAll(dbHelper.getAllLoans());
        adapter.notifyDataSetChanged();

        books = dbHelper.getAllBooks();
        users = dbHelper.getAllUsers();
    }

    private void showAddRecordDialog() {
        books = dbHelper.getAllBooks();
        users = dbHelper.getAllUsers();

        // Берём только книги, которые есть в наличии
        List<Book> availableBooks = new ArrayList<>();
        for (Book b : books) {
            if (b.getAmount() > 0) availableBooks.add(b);
        }

        if (availableBooks.isEmpty()) {
            Toast.makeText(getContext(), "Нет книг в наличии", Toast.LENGTH_SHORT).show();
            return;
        }

        if (users.isEmpty()) {
            Toast.makeText(getContext(), "Сначала загрузите пользователей из контактов", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_loan, null);
        Spinner spinnerBooks = dialogView.findViewById(R.id.spinnerBooks);
        Spinner spinnerUsers = dialogView.findViewById(R.id.spinnerUsers);
        EditText etDate = dialogView.findViewById(R.id.etDate);

        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        bookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Book b : availableBooks) {
            bookAdapter.add(b.getTitle() + " (" + b.getAmount() + ")");
        }
        spinnerBooks.setAdapter(bookAdapter);

        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (User u : users) userAdapter.add(u.getName());
        spinnerUsers.setAdapter(userAdapter);

        Calendar cal = Calendar.getInstance();
        etDate.setText(String.format("%d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)));

        new AlertDialog.Builder(requireContext())
                .setTitle("Выдать книгу")
                .setView(dialogView)
                .setPositiveButton("Добавить", (d, w) -> {
                    int bookPos = spinnerBooks.getSelectedItemPosition();
                    int userPos = spinnerUsers.getSelectedItemPosition();
                    String date = etDate.getText().toString().trim();
                    if (date.isEmpty()) return;

                    Book selectedBook = availableBooks.get(bookPos);
                    User selectedUser = users.get(userPos);

                    boolean ok = dbHelper.issueBook(selectedBook.getId(), selectedUser.getId(), date);
                    if (!ok) {
                        Toast.makeText(getContext(), "Книги нет в наличии", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    loadAll();
                    Toast.makeText(getContext(), "Запись добавлена", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            syncContactsToDb();
            return;
        }
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQ_CONTACTS);
    }

    private void syncContactsToDb() {
        ContentResolver resolver = requireContext().getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            Toast.makeText(getContext(), "Не удалось прочитать контакты", Toast.LENGTH_SHORT).show();
            return;
        }

        int idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int hasPhoneIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

        while (cursor.moveToNext()) {
            String contactId = cursor.getString(idIdx);
            String name = cursor.getString(nameIdx);
            int hasPhone = cursor.getInt(hasPhoneIdx);
            if (hasPhone <= 0) continue;

            Cursor phones = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[]{contactId},
                    null
            );

            if (phones != null) {
                int phoneIdx = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (phones.moveToFirst()) {
                    String phone = phones.getString(phoneIdx);
                    dbHelper.addUser(new User(0, name, phone));
                }
                phones.close();
            }
        }

        cursor.close();
        loadAll();
        Toast.makeText(getContext(), "Пользователи обновлены из контактов", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CONTACTS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            syncContactsToDb();
        } else {
            Toast.makeText(getContext(), "Нужно разрешение на контакты", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAll();
    }
}

