package com.example.booklibrary0;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BooksFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookRecyclerAdapter adapter;
    private final List<Book> books = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(requireContext());
        adapter = new BookRecyclerAdapter(books, dbHelper, this::loadBooks);
        recyclerView.setAdapter(adapter);

        loadBooks();
        return view;
    }

    private void loadBooks() {
        books.clear();
        books.addAll(dbHelper.getAllBooks());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks();
    }
}
