package hcmute.edu.vn.note_taking.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import hcmute.edu.vn.note_taking.models.Note;


public class NoteTakingOpenHelper extends SQLiteOpenHelper {
    public NoteTakingOpenHelper(Context context) {
        super(context, "NoteTaking.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (\n" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "  title TEXT,\n" +
                "  text_content TEXT,\n" +
                "  list_image TEXT,\n" +
                "  voice TEXT,\n" +
                "  create_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                "  status INTEGER\n" +
                ");\n");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public Note insertNote(Note note) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("title", note.getTitle());
            values.put("text_content", note.getText_content());
            values.put("list_image", note.getListImages());
            values.put("voice", note.getVoice());
            values.put("status", 1);

            long newRowId = db.insert("notes", null, values);
            note.setId(newRowId);
        }
        return note;
    }


    public Note getNoteById(long id) {
        Note note = null;
        try (SQLiteDatabase db = this.getReadableDatabase()) {

            String[] projection = {
                    "id",
                    "title",
                    "text_content",
                    "list_image",
                    "voice",
                    "create_at",
                    "status",
            };

            String selection = "id = ?";
            String[] selectionArgs = {String.valueOf(id)};

            Cursor cursor = db.query(
                    "notes",                     // Tên bảng
                    projection,                  // Các cột cần trả về
                    selection,                   // Câu lệnh WHERE
                    selectionArgs,               // Giá trị của câu lệnh WHERE
                    null,                        // GROUP BY
                    null,                        // HAVING
                    null                         // ORDER BY
            );

            if (cursor.moveToFirst()) {
                note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                note.setText_content(cursor.getString(cursor.getColumnIndexOrThrow("text_content")));
                note.setListImages(cursor.getString(cursor.getColumnIndexOrThrow("list_image")));
                note.setVoice(cursor.getString(cursor.getColumnIndexOrThrow("voice")));
                note.setCreated_at(cursor.getString(cursor.getColumnIndexOrThrow("create_at")));
                note.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
            }
            cursor.close();
        }
        return note;
    }


    public Note insertNote(String title, String text_content, String list_image, String voice) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("text_content", text_content);
        values.put("list_image", list_image);
        values.put("voice", voice);
        values.put("status", 1);

        long newRowId = db.insert("notes", null, values);
        db.close();
        return getNoteById(newRowId);
    }


    public void updateNoteStatus(long id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", 2);

        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update("notes", values, selection, selectionArgs);
        db.close();
    }

    public List<Note> getAllNotes() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notes", null);
        List<Note> notes = Note.listFromCursor(cursor);
        cursor.close();
        db.close();
        return notes;
    }
}
