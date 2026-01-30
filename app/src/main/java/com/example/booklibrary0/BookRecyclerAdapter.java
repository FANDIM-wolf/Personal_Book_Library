package com.example.booklibrary0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.titleView.setText(book.getTitle());

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Book b = books.get(adapterPos);
            showBookInfoDialog(holder.itemView.getContext(), b, adapterPos);
        });
    }

    private void showBookInfoDialog(Context context, Book book, int adapterPos) {
        String message =
                "Автор: " + book.getAuthor() + "\n" +
                        "Год: " + book.getYear() + "\n" +
                        "Количество: " + book.getAmount();

        new AlertDialog.Builder(context)
                .setTitle(book.getTitle())
                .setMessage(message)
                .setPositiveButton("Закрыть", null)

                // Если нужен только просмотр — удалите 2 кнопки ниже
                .setNeutralButton("Изменить", (d, w) -> showEditDialog(context, book, adapterPos))
                .setNegativeButton("Удалить", (d, w) -> {
                    dbHelper.deleteBook(book.getId());
                    books.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                    if (refreshCallback != null) refreshCallback.run();
                })
                .show();
    }

    private void showEditDialog(Context context, Book book, int adapterPos) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_book, null);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etYear = dialogView.findViewById(R.id.etYear);
        EditText etAmount = dialogView.findViewById(R.id.etAmount); // важно: etAmount

        etTitle.setText(book.getTitle());
        etAuthor.setText(book.getAuthor());
        etYear.setText(book.getYear());
        etAmount.setText(String.valueOf(book.getAmount()));

        new AlertDialog.Builder(context)
                .setTitle("Изменить книгу")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String newTitle = etTitle.getText().toString().trim();
                    String newAuthor = etAuthor.getText().toString().trim();
                    String newYear = etYear.getText().toString().trim();
                    String newAmountStr = etAmount.getText().toString().trim();

                    if (newTitle.isEmpty() || newAuthor.isEmpty() || newYear.isEmpty() || newAmountStr.isEmpty()) return;

                    int newAmount;
                    try {
                        newAmount = Integer.parseInt(newAmountStr);
                    } catch (Exception e) {
                        return;
                    }

                    Book updated = new Book(book.getId(), newTitle, newAuthor, newYear, newAmount);
                    dbHelper.updateBook(updated);

                    books.set(adapterPos, updated);
                    notifyItemChanged(adapterPos);

                    if (refreshCallback != null) refreshCallback.run();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.bookTitle);
        }
    }
}
