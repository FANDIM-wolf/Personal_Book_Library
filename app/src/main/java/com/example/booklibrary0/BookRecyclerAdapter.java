package com.example.booklibrary0;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {

    private final List<Book> books;
    private final DatabaseHelper dbHelper;
    private final Runnable refreshCallback;

    public BookRecyclerAdapter(List<Book> books, DatabaseHelper dbHelper, Runnable refreshCallback) {
        this.books = books;
        this.dbHelper = dbHelper;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);

        holder.titleView.setText(book.getTitle());
        holder.authorView.setText("Автор: " + book.getAuthor());
        holder.yearView.setText("Год: " + book.getYear());

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Book b = books.get(adapterPos);
            dbHelper.deleteBook(b.getId());

            books.remove(adapterPos);
            notifyItemRemoved(adapterPos);

            if (refreshCallback != null) refreshCallback.run();
        });

        holder.editButton.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Book b = books.get(adapterPos);

            View dialogView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.dialog_edit_book, null);

            EditText etTitle = dialogView.findViewById(R.id.etTitle);
            EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
            EditText etYear = dialogView.findViewById(R.id.etYear);

            etTitle.setText(b.getTitle());
            etAuthor.setText(b.getAuthor());
            etYear.setText(b.getYear());

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Изменить книгу")
                    .setView(dialogView)
                    .setPositiveButton("Сохранить", (d, w) -> {
                        String newTitle = etTitle.getText().toString().trim();
                        String newAuthor = etAuthor.getText().toString().trim();
                        String newYear = etYear.getText().toString().trim();
                        if (newTitle.isEmpty() || newAuthor.isEmpty() || newYear.isEmpty()) return;

                        Book updated = new Book(b.getId(), newTitle, newAuthor, newYear);
                        dbHelper.updateBook(updated);

                        books.set(adapterPos, updated);
                        notifyItemChanged(adapterPos);

                        if (refreshCallback != null) refreshCallback.run();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, authorView, yearView;
        Button deleteButton, editButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.bookTitle);
            authorView = itemView.findViewById(R.id.bookAuthor);
            yearView = itemView.findViewById(R.id.bookYear);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}

