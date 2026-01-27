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
        if (position == 0) {
            return new LibraryFragment();
        } else {
            return new AddBookFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}