package com.example.booklibrary0;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {

    private List<Book> books;

    public BookRecyclerAdapter(List<Book> books) {
        this.books = books;
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
            DatabaseHelper dbHelper = new DatabaseHelper(holder.itemView.getContext());
            dbHelper.deleteBook(book.getId());
            books.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, authorView, yearView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.bookTitle);
            authorView = itemView.findViewById(R.id.bookAuthor);
            yearView = itemView.findViewById(R.id.bookYear);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}