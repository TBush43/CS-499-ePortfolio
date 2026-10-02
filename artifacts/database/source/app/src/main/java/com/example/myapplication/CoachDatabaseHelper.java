package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

final class CoachDatabaseHelper extends SQLiteOpenHelper {
    static final String NAME = "onweight_coach.db";
    static final int VERSION = 1;

    CoachDatabaseHelper(Context context) {
        this(context, NAME);
    }

    CoachDatabaseHelper(Context context, String name) {
        super(context, name, null, VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Reject related rows that point to a missing student.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, belt TEXT NOT NULL, follow_up INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE goals (" +
                "student_id INTEGER PRIMARY KEY, description TEXT NOT NULL, " +
                "due_on TEXT, complete INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, " +
                "attended_on TEXT NOT NULL, " +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE session_notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, " +
                "note TEXT NOT NULL, noted_on TEXT NOT NULL, " +
                "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX attendance_student_date ON attendance(student_id, attended_on)");
        db.execSQL("CREATE INDEX notes_student_date ON session_notes(student_id, noted_on)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // A future version must migrate records instead of deleting this database.
        throw new IllegalStateException("No database migration from " + oldVersion + " to " + newVersion);
    }
}
