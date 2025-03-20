package com.example.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.example.notes.model.UserTag;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 3;

    // Table Names
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_TAGS = "note_tags";

    // Note Table Columns
    private static final String KEY_NOTE_ID = "id";
    private static final String KEY_NOTE_TITLE = "title";
    private static final String KEY_NOTE_CONTENT = "content";
    private static final String KEY_NOTE_CATEGORY = "category";
    private static final String KEY_NOTE_TIMESTAMP = "timestamp";
    private static final String KEY_NOTE_PRIORITY = "priority";
    private static final String KEY_NOTE_FAVORITE = "is_favorite";
    private static final String KEY_NOTE_DELETED = "is_deleted";

    // Tag Table Columns
    private static final String KEY_TAG_ID = "tag_id";
    private static final String KEY_TAG_NOTE_ID = "note_id";
    private static final String KEY_TAG_USER_ID = "user_id";
    private static final String KEY_TAG_EMAIL = "email";

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
                + KEY_NOTE_PRIORITY + " INTEGER DEFAULT 0,"
                + KEY_NOTE_FAVORITE + " INTEGER DEFAULT 0,"
                + KEY_NOTE_DELETED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);

        String CREATE_TAGS_TABLE = "CREATE TABLE " + TABLE_TAGS + "("
                + KEY_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TAG_NOTE_ID + " INTEGER,"
                + KEY_TAG_USER_ID + " TEXT,"
                + KEY_TAG_EMAIL + " TEXT,"
                + "FOREIGN KEY(" + KEY_TAG_NOTE_ID + ") REFERENCES " + TABLE_NOTES + "(" + KEY_NOTE_ID + ")"
                + ")";
        db.execSQL(CREATE_TAGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add is_deleted column if upgrading from version 1 or 2
            try {
                db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + KEY_NOTE_DELETED + " INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Column might already exist, recreate the table in case of issues
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
                onCreate(db);
            }
        }
    }

    // Insert or update a note
    public long saveNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        long noteId = -1;

        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TITLE, note.getTitle());
        values.put(KEY_NOTE_CONTENT, note.getContent());
        values.put(KEY_NOTE_CATEGORY, note.getCategory());
        values.put(KEY_NOTE_TIMESTAMP, note.getTimestamp());
        values.put(KEY_NOTE_PRIORITY, note.getPriority());
        values.put(KEY_NOTE_FAVORITE, note.isFavorite() ? 1 : 0);

        try {
            db.beginTransaction();
            if (note.getId() > 0) {
                // Update existing note
                int rows = db.update(TABLE_NOTES, values, KEY_NOTE_ID + " = ?",
                        new String[] { String.valueOf(note.getId()) });
                if (rows > 0) {
                    noteId = note.getId();
                } else {
                    // If no rows were updated, possibly the note doesn't exist yet
                    noteId = db.insertOrThrow(TABLE_NOTES, null, values);
                }
            } else {
                // Insert new note
                noteId = db.insertOrThrow(TABLE_NOTES, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }

        return noteId;
    }

    // Delete a note
    public void deleteNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete(TABLE_NOTES, KEY_NOTE_ID + " = ?",
                    new String[] { String.valueOf(note.getId()) });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    // Soft delete - move to trash
    public void trashNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(KEY_NOTE_DELETED, 1);

            db.update(TABLE_NOTES, values, KEY_NOTE_ID + " = ?",
                    new String[] { String.valueOf(note.getId()) });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    // Restore from trash
    public void restoreNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(KEY_NOTE_DELETED, 0);

            db.update(TABLE_NOTES, values, KEY_NOTE_ID + " = ?",
                    new String[] { String.valueOf(note.getId()) });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    // Get trashed notes
    public List<Note> getTrashedNotes() {
        List<Note> notes = new ArrayList<>();
        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = 1 ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_DELETED, KEY_NOTE_TIMESTAMP);

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
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    note.setFavorite(isFavorite);
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

    // Get all notes
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = 0 ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_DELETED, KEY_NOTE_TIMESTAMP);

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
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    note.setFavorite(isFavorite);
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
        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ? AND %s = 0",
                TABLE_NOTES, KEY_NOTE_ID, KEY_NOTE_DELETED);

        SQLiteDatabase db = getReadableDatabase();
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
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                note = new Note(id, title, content, category, timestamp, priority);
                note.setFavorite(isFavorite);
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
        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = ? AND %s = 0 ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_CATEGORY, KEY_NOTE_DELETED, KEY_NOTE_TIMESTAMP);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(NOTES_SELECT_QUERY, new String[] { category });
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_NOTE_ID);
                int titleIndex = cursor.getColumnIndex(KEY_NOTE_TITLE);
                int contentIndex = cursor.getColumnIndex(KEY_NOTE_CONTENT);
                int timestampIndex = cursor.getColumnIndex(KEY_NOTE_TIMESTAMP);
                int priorityIndex = cursor.getColumnIndex(KEY_NOTE_PRIORITY);
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    note.setFavorite(isFavorite);
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
                "SELECT * FROM %s WHERE %s = 0 AND (LOWER(%s) LIKE ? OR LOWER(%s) LIKE ?) ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_DELETED, KEY_NOTE_TITLE, KEY_NOTE_CONTENT, KEY_NOTE_TIMESTAMP);

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
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    note.setFavorite(isFavorite);
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

    public List<Note> searchNotesByCategory(String query, String category) {
        List<Note> notes = new ArrayList<>();
        String searchQuery = "%" + query.toLowerCase() + "%";

        String NOTES_SELECT_QUERY = String.format(
                "SELECT * FROM %s WHERE %s = ? AND %s = 0 AND (LOWER(%s) LIKE ? OR LOWER(%s) LIKE ?) ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_CATEGORY, KEY_NOTE_DELETED, KEY_NOTE_TITLE, KEY_NOTE_CONTENT, KEY_NOTE_TIMESTAMP);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(NOTES_SELECT_QUERY,
                    new String[] { category, searchQuery, searchQuery });
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_NOTE_ID);
                int titleIndex = cursor.getColumnIndex(KEY_NOTE_TITLE);
                int contentIndex = cursor.getColumnIndex(KEY_NOTE_CONTENT);
                int categoryIndex = cursor.getColumnIndex(KEY_NOTE_CATEGORY);
                int timestampIndex = cursor.getColumnIndex(KEY_NOTE_TIMESTAMP);
                int priorityIndex = cursor.getColumnIndex(KEY_NOTE_PRIORITY);
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String categoryResult = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, categoryResult, timestamp, priority);
                    note.setFavorite(isFavorite);
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

    public List<Note> getFavoriteNotes() {
        List<Note> notes = new ArrayList<>();
        String NOTES_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = 1 AND %s = 0 ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_FAVORITE, KEY_NOTE_DELETED, KEY_NOTE_TIMESTAMP);

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
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    note.setFavorite(isFavorite);
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

    public List<Note> searchFavoriteNotes(String query) {
        List<Note> notes = new ArrayList<>();
        String searchQuery = "%" + query.toLowerCase() + "%";

        String NOTES_SELECT_QUERY = String.format(
                "SELECT * FROM %s WHERE %s = 1 AND %s = 0 AND (LOWER(%s) LIKE ? OR LOWER(%s) LIKE ?) ORDER BY %s DESC",
                TABLE_NOTES, KEY_NOTE_FAVORITE, KEY_NOTE_DELETED, KEY_NOTE_TITLE, KEY_NOTE_CONTENT, KEY_NOTE_TIMESTAMP);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(NOTES_SELECT_QUERY,
                    new String[] { searchQuery, searchQuery });
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(KEY_NOTE_ID);
                int titleIndex = cursor.getColumnIndex(KEY_NOTE_TITLE);
                int contentIndex = cursor.getColumnIndex(KEY_NOTE_CONTENT);
                int categoryIndex = cursor.getColumnIndex(KEY_NOTE_CATEGORY);
                int timestampIndex = cursor.getColumnIndex(KEY_NOTE_TIMESTAMP);
                int priorityIndex = cursor.getColumnIndex(KEY_NOTE_PRIORITY);
                int favoriteIndex = cursor.getColumnIndex(KEY_NOTE_FAVORITE);

                do {
                    int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                    String content = contentIndex != -1 ? cursor.getString(contentIndex) : "";
                    String category = categoryIndex != -1 ? cursor.getString(categoryIndex) : "Personal";
                    long timestamp = timestampIndex != -1 ? cursor.getLong(timestampIndex) : System.currentTimeMillis();
                    int priority = priorityIndex != -1 ? cursor.getInt(priorityIndex) : 0;
                    boolean isFavorite = favoriteIndex != -1 && cursor.getInt(favoriteIndex) == 1;

                    Note note = new Note(id, title, content, category, timestamp, priority);
                    note.setFavorite(isFavorite);
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

    /**
     * Delete notes that have been in trash longer than the retention period
     */
    public void cleanupTrash(Context context) {
        SettingsManager settingsManager = SettingsManager.getInstance(context);
        int retentionDays = settingsManager.getTrashRetentionDays();
        long cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L);

        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            // Delete notes that have been in trash for longer than the retention period
            db.delete(TABLE_NOTES, KEY_NOTE_DELETED + " = 1 AND " + KEY_NOTE_TIMESTAMP + " < ?",
                    new String[] { String.valueOf(cutoffTime) });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Only clear the notes table since others don't exist
            db.delete(TABLE_NOTES, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    public void addTagToNote(long noteId, String userId, String email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TAG_NOTE_ID, noteId);
        values.put(KEY_TAG_USER_ID, userId);
        values.put(KEY_TAG_EMAIL, email);

        db.insert(TABLE_TAGS, null, values);
    }

    public List<UserTag> getTagsForNote(long noteId) {
        List<UserTag> tags = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TAGS, null,
                KEY_TAG_NOTE_ID + "=?",
                new String[] { String.valueOf(noteId) }, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                UserTag tag = new UserTag(
                        cursor.getString(cursor.getColumnIndex(KEY_TAG_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TAG_EMAIL)),
                        null);
                tags.add(tag);
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return tags;
    }
}
