package hcmute.edu.vn.note_taking.models;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.note_taking.sqlite.NoteTakingOpenHelper;

public class Note {
    private long id;
    private String title;
    private String text_content;
    private String listImages;
    private String voice;
    private String created_at;
    private int status;

    public Note() {
    }

    public Note(long id, String title, String text_content, String listImages, String voice, String created_at, int status) {
        this.id = id;
        this.title = title;
        this.text_content = text_content;
        this.listImages = listImages;
        this.voice = voice;
        this.created_at = created_at;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText_content() {
        return text_content;
    }

    public void setText_content(String text_content) {
        this.text_content = text_content;
    }

    public String getListImages() {
        return listImages;
    }

    public void setListImages(String listImages) {
        this.listImages = listImages;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text_content='" + text_content + '\'' +
                ", listImages='" + listImages + '\'' +
                ", voice='" + voice + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }


    public static Note fromCursor(Cursor cursor) {
        Note note = new Note();
        note.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        note.setText_content(cursor.getString(cursor.getColumnIndexOrThrow("text_content")));
        note.setListImages(cursor.getString(cursor.getColumnIndexOrThrow("list_image")));
        note.setVoice(cursor.getString(cursor.getColumnIndexOrThrow("voice")));
        note.setCreated_at(cursor.getString(cursor.getColumnIndexOrThrow("create_at")));
        note.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
        return note;
    }

    public static List<Note> listFromCursor(Cursor cursor) {
        List<Note> notes = new ArrayList<>();
        while (cursor.moveToNext()) {
            notes.add(fromCursor(cursor));
        }
        return notes;
    }
}
