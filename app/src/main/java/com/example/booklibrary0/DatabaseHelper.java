package com.example.booklibrary0;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "library.db";
    private static final int DATABASE_VERSION = 2;

    // Books
    private static final String TABLE_BOOKS = "books";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_YEAR = "year";

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
                        COLUMN_YEAR + " TEXT" +
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        onCreate(db);
    }

    // ---------- BOOKS ----------
    public long addBook(Book book) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_YEAR, book.getYear());
        long id = db.insert(TABLE_BOOKS, null, values);
        db.close();
        return id;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String author = cursor.getString(2);
                String year = cursor.getString(3);
                books.add(new Book(id, title, author, year));
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

    // ---------- LOANS ----------
    public long addLoan(int bookId, int userId, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_ID, bookId);
        values.put(COLUMN_USER_ID_FK, userId);
        values.put(COLUMN_DATE_GIVEN, date);
        long id = db.insert(TABLE_LOANS, null, values);
        db.close();
        return id;
    }

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

    public int deleteLoan(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_LOANS, COLUMN_LOAN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }
}
