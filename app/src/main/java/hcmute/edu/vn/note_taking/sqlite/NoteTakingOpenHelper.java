package hcmute.edu.vn.note_taking.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class NoteTakingOpenHelper extends SQLiteOpenHelper {
    public NoteTakingOpenHelper(Context context) {
        super(context, "NoteTaking.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (id CHAR(15), title TEXT, content TEXT, user_id INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}
