package com.example.booklibrary0;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "library.db";
    private static final int DATABASE_VERSION = 3;

    // Books
    private static final String TABLE_BOOKS = "books";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_AMOUNT = "amount";

    // Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_PHONE = "phone";

    // Loans
    private static final String TABLE_LOANS = "loans";
    private static final String COLUMN_LOAN_ID = "loan_id";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_USER_ID_FK = "user_id_fk";
    private static final String COLUMN_DATE_GIVEN = "date_given";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKS_TABLE =
                "CREATE TABLE " + TABLE_BOOKS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TITLE + " TEXT, " +
                        COLUMN_AUTHOR + " TEXT, " +
                        COLUMN_YEAR + " TEXT, " +
                        COLUMN_AMOUNT + " INTEGER NOT NULL DEFAULT 0" +
                        ")";
        db.execSQL(CREATE_BOOKS_TABLE);

        String CREATE_USERS_TABLE =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_NAME + " TEXT, " +
                        COLUMN_USER_PHONE + " TEXT UNIQUE" +
                        ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_LOANS_TABLE =
                "CREATE TABLE " + TABLE_LOANS + " (" +
                        COLUMN_LOAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_BOOK_ID + " INTEGER, " +
                        COLUMN_USER_ID_FK + " INTEGER, " +
                        COLUMN_DATE_GIVEN + " TEXT, " +
                        "FOREIGN KEY(" + COLUMN_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_ID + "), " +
                        "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                        ")";
        db.execSQL(CREATE_LOANS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_BOOKS +
                    " ADD COLUMN " + COLUMN_AMOUNT + " INTEGER NOT NULL DEFAULT 0");
        }
    }

    // ---------- BOOKS ----------
    public long addBook(Book book) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_YEAR, book.getYear());
        values.put(COLUMN_AMOUNT, book.getAmount());
        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKS, null);

        if (cursor.moveToFirst()) {
            int amountIndex = cursor.getColumnIndex(COLUMN_AMOUNT);
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String author = cursor.getString(2);
                String year = cursor.getString(3);
                int amount = (amountIndex >= 0) ? cursor.getInt(amountIndex) : 0;

                books.add(new Book(id, title, author, year, amount));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return books;
    }

    public int deleteBook(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_BOOKS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    public int updateBook(Book book) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_YEAR, book.getYear());
        values.put(COLUMN_AMOUNT, book.getAmount());
        int result = db.update(TABLE_BOOKS, values, COLUMN_ID + "=?", new String[]{String.valueOf(book.getId())});
        db.close();
        return result;
    }

    // ---------- USERS ----------
    public long addUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_PHONE, user.getPhone());
        long id = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                users.add(new User(id, name, phone));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return users;
    }

    // ---------- LOANS (чтение) ----------
    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();

        String query =
                "SELECT l." + COLUMN_LOAN_ID + ", l." + COLUMN_BOOK_ID + ", b." + COLUMN_TITLE + ", " +
                        "l." + COLUMN_USER_ID_FK + ", u." + COLUMN_USER_NAME + ", l." + COLUMN_DATE_GIVEN +
                        " FROM " + TABLE_LOANS + " l " +
                        "JOIN " + TABLE_BOOKS + " b ON l." + COLUMN_BOOK_ID + " = b." + COLUMN_ID + " " +
                        "JOIN " + TABLE_USERS + " u ON l." + COLUMN_USER_ID_FK + " = u." + COLUMN_USER_ID;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int bookId = cursor.getInt(1);
                String bookTitle = cursor.getString(2);
                int userId = cursor.getInt(3);
                String userName = cursor.getString(4);
                String date = cursor.getString(5);
                loans.add(new Loan(id, bookId, bookTitle, userId, userName, date));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return loans;
    }

    // Выдать книгу: amount-- и создать запись в loans (атомарно)
    public boolean issueBook(int bookId, int userId, String date) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            SQLiteStatement st = db.compileStatement(
                    "UPDATE " + TABLE_BOOKS +
                            " SET " + COLUMN_AMOUNT + " = " + COLUMN_AMOUNT + " - 1" +
                            " WHERE " + COLUMN_ID + " = ? AND " + COLUMN_AMOUNT + " > 0"
            );
            st.bindLong(1, bookId);
            int updatedRows = st.executeUpdateDelete();
            if (updatedRows == 0) return false; // нет в наличии

            ContentValues values = new ContentValues();
            values.put(COLUMN_BOOK_ID, bookId);
            values.put(COLUMN_USER_ID_FK, userId);
            values.put(COLUMN_DATE_GIVEN, date);

            long loanId = db.insert(TABLE_LOANS, null, values);
            if (loanId == -1) return false;

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Возврат книги при удалении записи: amount++ и delete loan (атомарно)
    public boolean returnBookAndDeleteLoan(int loanId, int bookId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            SQLiteStatement inc = db.compileStatement(
                    "UPDATE " + TABLE_BOOKS +
                            " SET " + COLUMN_AMOUNT + " = " + COLUMN_AMOUNT + " + 1" +
                            " WHERE " + COLUMN_ID + " = ?"
            );
            inc.bindLong(1, bookId);
            inc.executeUpdateDelete();

            int del = db.delete(TABLE_LOANS, COLUMN_LOAN_ID + "=?", new String[]{String.valueOf(loanId)});
            if (del <= 0) return false;

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}
