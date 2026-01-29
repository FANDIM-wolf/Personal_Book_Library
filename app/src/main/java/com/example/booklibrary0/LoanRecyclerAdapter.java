package com.example.booklibrary0;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LoanRecyclerAdapter extends RecyclerView.Adapter<LoanRecyclerAdapter.ViewHolder> {

    private final List<Loan> loans;
    private final DatabaseHelper dbHelper;
    private final Runnable refreshCallback;

    public LoanRecyclerAdapter(List<Loan> loans, DatabaseHelper dbHelper, Runnable refreshCallback) {
        this.loans = loans;
        this.dbHelper = dbHelper;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Loan loan = loans.get(position);

        holder.loanBookTitle.setText("Книга: " + loan.getBookTitle());
        holder.loanUserName.setText("Кому: " + loan.getUserName());
        holder.loanDate.setText("Дата: " + loan.getDateGiven());

        holder.deleteLoanButton.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Loan l = loans.get(adapterPos);

            // Удаление записи = возврат книги
            dbHelper.returnBookAndDeleteLoan(l.getId(), l.getBookId());

            loans.remove(adapterPos);
            notifyItemRemoved(adapterPos);

            if (refreshCallback != null) refreshCallback.run();
        });
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView loanBookTitle, loanUserName, loanDate;
        Button deleteLoanButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            loanBookTitle = itemView.findViewById(R.id.loanBookTitle);
            loanUserName = itemView.findViewById(R.id.loanUserName);
            loanDate = itemView.findViewById(R.id.loanDate);
            deleteLoanButton = itemView.findViewById(R.id.deleteLoanButton);
        }
    }
}
