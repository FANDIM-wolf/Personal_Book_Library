package com.example.booklibrary0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BookAdapter extends FragmentStateAdapter {

    public BookAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RecordsFragment();
            case 1:
                return new BooksFragment();
            case 2:
                return new AddBookFragment();
            default:
                return new BooksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}