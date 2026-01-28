package com.example.booklibrary0;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddBookFragment extends Fragment {

    private EditText titleInput, authorInput, yearInput;
    private Button saveButton;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_book, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleInput = view.findViewById(R.id.titleInput);
        authorInput = view.findViewById(R.id.authorInput);
        yearInput = view.findViewById(R.id.yearInput);
        saveButton = view.findViewById(R.id.saveButton);

        dbHelper = new DatabaseHelper(requireContext());

        saveButton.setOnClickListener(v -> saveBook());
    }

    private void saveBook() {
        String title = titleInput.getText().toString().trim();
        String author = authorInput.getText().toString().trim();
        String year = yearInput.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || year.isEmpty()) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.addBook(new Book(0, title, author, year));
        if (result > 0) {
            Toast.makeText(getContext(), "Книга добавлена", Toast.LENGTH_SHORT).show();
            titleInput.setText("");
            authorInput.setText("");
            yearInput.setText("");
        } else {
            Toast.makeText(getContext(), "Ошибка добавления", Toast.LENGTH_SHORT).show();
        }
    }
}
