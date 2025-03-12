package com.example.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_NOTES = "notes";

    // Note Table Columns
    private static final String KEY_NOTE_ID = "id";
    private static final String KEY_NOTE_TITLE = "title";
    private static final String KEY_NOTE_CONTENT = "content";
    private static final String KEY_NOTE_CATEGORY = "category";
    private static final String KEY_NOTE_TIMESTAMP = "timestamp";
    private static final String KEY_NOTE_PRIORITY = "priority";

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + KEY_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NOTE_TITLE + " TEXT,"
                + KEY_NOTE_CONTENT + " TEXT,"
                + KEY_NOTE_CATEGORY + " TEXT DEFAULT 'Personal',"
                + KEY_NOTE_TIMESTAMP + " INTEGER,"
                + KEY_NOTE_PRIORITY + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            onCreate(db);
        }
    }

    // Insert or update a note
    public long saveNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        long noteId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NOTE_TITLE, note.getTitle());
            values.put(KEY_NOTE_CONTENT, note.getContent());
            values.put(KEY_NOTE_CATEGORY, note.getCategory());
            values.put(KEY_NOTE_TIMESTAMP, note.getTimestamp());
            values.put(KEY_NOTE_PRIORITY, note.getPriority());

            // Update or insert
            if (note.getId() > 0) {
                // Update existing note
                db.update(TABLE_NOTES, values, KEY_NOTE_ID + " = ?",
                        new String[] { String.valueOf(note.getId()) });
                noteId = note.getId();
            } else {
                // Insert new note
                noteId = db.insertOrThrow(TABLE_NOTES, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return noteId;
    }

    // Delete a note
    public void deleteNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_NOTES, KEY_NOTE_ID + " = ?",
                    new String[] { String.valueOf(note.getId()) });
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Get all notes
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();

        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_TIMESTAMP);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(NOTES_SELECT_QUERY, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_NOTE_ID);
                int titleIndex = cursor.getColumnIndex(KEY_NOTE_TITLE);
                int contentIndex = cursor.getColumnIndex(KEY_NOTE_CONTENT);
                int categoryIndex = cursor.getColumnIndex(KEY_NOTE_CATEGORY);
                int timestampIndex = cursor.getColumnIndex(KEY_NOTE_TIMESTAMP);
                int priorityIndex = cursor.getColumnIndex(KEY_NOTE_PRIORITY);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return notes;
    }

    // Get a single note by ID
    public Note getNoteById(int noteId) {
        Note note = null;

        SQLiteDatabase db = getReadableDatabase();
        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ?",
                TABLE_NOTES, KEY_NOTE_ID);

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(NOTES_SELECT_QUERY, new String[] { String.valueOf(noteId) });
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_NOTE_ID);
                int titleIndex = cursor.getColumnIndex(KEY_NOTE_TITLE);
                int contentIndex = cursor.getColumnIndex(KEY_NOTE_CONTENT);
                int categoryIndex = cursor.getColumnIndex(KEY_NOTE_CATEGORY);
                int timestampIndex = cursor.getColumnIndex(KEY_NOTE_TIMESTAMP);
                int priorityIndex = cursor.getColumnIndex(KEY_NOTE_PRIORITY);

                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;

                note = new Note(id, title, content, category, timestamp, priority);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return note;
    }

    // Get notes by category
    public List<Note> getNotesByCategory(String category) {
        List<Note> notes = new ArrayList<>();

        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_CATEGORY, KEY_NOTE_TIMESTAMP);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(NOTES_SELECT_QUERY, new String[] { category });
        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID));
                    String title = cursor.getString(cursor.getColumnIndex(KEY_NOTE_TITLE));
                    String content = cursor.getString(cursor.getColumnIndex(KEY_NOTE_CONTENT));
                    long timestamp = cursor.getLong(cursor.getColumnIndex(KEY_NOTE_TIMESTAMP));
                    int priority = cursor.getInt(cursor.getColumnIndex(KEY_NOTE_PRIORITY));

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return notes;
    }

    // Search notes by query
    public List<Note> searchNotes(String query) {
        List<Note> notes = new ArrayList<>();
        String searchQuery = "%" + query.toLowerCase() + "%";

        String NOTES_SELECT_QUERY = String.format(
                "SELECT * FROM %s WHERE LOWER(%s) LIKE ? OR LOWER(%s) LIKE ? ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_TITLE, KEY_NOTE_CONTENT, KEY_NOTE_TIMESTAMP);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(NOTES_SELECT_QUERY, new String[] { searchQuery, searchQuery });
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_NOTE_ID);
                int titleIndex = cursor.getColumnIndex(KEY_NOTE_TITLE);
                int contentIndex = cursor.getColumnIndex(KEY_NOTE_CONTENT);
                int categoryIndex = cursor.getColumnIndex(KEY_NOTE_CATEGORY);
                int timestampIndex = cursor.getColumnIndex(KEY_NOTE_TIMESTAMP);
                int priorityIndex = cursor.getColumnIndex(KEY_NOTE_PRIORITY);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return notes;
    }
}
