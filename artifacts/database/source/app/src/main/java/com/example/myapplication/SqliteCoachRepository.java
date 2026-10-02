package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SqliteCoachRepository implements CoachRepository {
    private static final String STUDENT_QUERY = "SELECT s.id, s.name, s.belt, s.follow_up, " +
            "COALESCE(g.description, ''), " +
            "(SELECT COUNT(*) FROM attendance a WHERE a.student_id = s.id) " +
            "FROM students s LEFT JOIN goals g ON g.student_id = s.id";
    private final CoachDatabaseHelper helper;

    public SqliteCoachRepository(Context context) {
        this(context, CoachDatabaseHelper.NAME);
    }

    SqliteCoachRepository(Context context, String databaseName) {
        helper = new CoachDatabaseHelper(context.getApplicationContext(), databaseName);
    }

    public void close() {
        helper.close();
    }

    @Override
    public synchronized List<Student> getStudents() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Student> students = new ArrayList<>();
        try (Cursor rows = db.rawQuery(STUDENT_QUERY + " ORDER BY s.id", null)) {
            while (rows.moveToNext()) students.add(readStudent(db, rows));
        }
        return students;
    }

    @Override
    public synchronized Student getStudent(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor rows = db.rawQuery(STUDENT_QUERY + " WHERE s.id = ?",
                new String[]{Long.toString(id)})) {
            return rows.moveToFirst() ? readStudent(db, rows) : null;
        }
    }

    private Student readStudent(SQLiteDatabase db, Cursor row) {
        long id = row.getLong(0);
        List<String> notes = new ArrayList<>();
        // Read newest notes first, matching the student screen's existing order.
        try (Cursor noteRows = db.rawQuery("SELECT note FROM session_notes WHERE student_id = ? " +
                "ORDER BY id DESC", new String[]{Long.toString(id)})) {
            while (noteRows.moveToNext()) notes.add(noteRows.getString(0));
        }
        return new Student(id, row.getString(1), row.getString(2), row.getString(4),
                row.getInt(3) != 0, row.getInt(5), notes);
    }

    @Override
    public synchronized Student addStudent(String name, String belt) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("belt", belt);
        long id = helper.getWritableDatabase().insertOrThrow("students", null, values);
        return getStudent(id);
    }

    @Override
    public synchronized void setFollowUp(long studentId, boolean needed) {
        ContentValues values = new ContentValues();
        values.put("follow_up", needed ? 1 : 0);
        int changed = helper.getWritableDatabase().update("students", values, "id = ?",
                new String[]{Long.toString(studentId)});
        if (changed != 1) throw new IllegalArgumentException("Student not found.");
    }

    @Override
    public synchronized List<AttendanceRecord> getAttendanceRecords() {
        List<AttendanceRecord> records = new ArrayList<>();
        try (Cursor rows = helper.getReadableDatabase().rawQuery(
                "SELECT student_id, attended_on FROM attendance ORDER BY id", null)) {
            while (rows.moveToNext()) records.add(new AttendanceRecord(rows.getLong(0),
                    LocalDate.parse(rows.getString(1))));
        }
        return records;
    }

    @Override
    public synchronized List<SessionNoteRecord> getSessionNoteRecords() {
        List<SessionNoteRecord> records = new ArrayList<>();
        try (Cursor rows = helper.getReadableDatabase().rawQuery(
                "SELECT student_id, noted_on FROM session_notes ORDER BY id", null)) {
            while (rows.moveToNext()) records.add(new SessionNoteRecord(rows.getLong(0),
                    LocalDate.parse(rows.getString(1))));
        }
        return records;
    }

    @Override
    public synchronized List<GoalRecord> getGoalRecords() {
        List<GoalRecord> records = new ArrayList<>();
        try (Cursor rows = helper.getReadableDatabase().rawQuery(
                "SELECT student_id, due_on, complete FROM goals ORDER BY student_id", null)) {
            while (rows.moveToNext()) records.add(readGoal(rows));
        }
        return records;
    }

    @Override
    public synchronized GoalRecord getGoalRecord(long studentId) {
        try (Cursor rows = helper.getReadableDatabase().rawQuery(
                "SELECT student_id, due_on, complete FROM goals WHERE student_id = ?",
                new String[]{Long.toString(studentId)})) {
            return rows.moveToFirst() ? readGoal(rows) : null;
        }
    }

    private static GoalRecord readGoal(Cursor row) {
        return new GoalRecord(row.getLong(0), row.isNull(1) ? null : LocalDate.parse(row.getString(1)),
                row.getInt(2) != 0);
    }

    @Override
    public synchronized void saveGoal(Student student, GoalRecord goal) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            requireStudent(db, student.getId());
            if (student.getGoal().isEmpty()) {
                db.delete("goals", "student_id = ?", new String[]{Long.toString(student.getId())});
            } else {
                ContentValues values = new ContentValues();
                values.put("student_id", student.getId());
                values.put("description", student.getGoal());
                values.put("due_on", goal.getDueDate() == null ? null : goal.getDueDate().toString());
                values.put("complete", goal.isComplete() ? 1 : 0);
                db.insertWithOnConflict("goals", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public synchronized void saveGoalRecord(GoalRecord goal) {
        ContentValues values = new ContentValues();
        values.put("due_on", goal.getDueDate() == null ? null : goal.getDueDate().toString());
        values.put("complete", goal.isComplete() ? 1 : 0);
        int changed = helper.getWritableDatabase().update("goals", values, "student_id = ?",
                new String[]{Long.toString(goal.getStudentId())});
        if (changed != 1) throw new IllegalArgumentException("Goal not found.");
    }

    @Override
    public synchronized void recordAttendance(long studentId, LocalDate date) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            requireStudent(db, studentId);
            ContentValues values = new ContentValues();
            values.put("student_id", studentId);
            values.put("attended_on", date.toString());
            db.insertOrThrow("attendance", null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public synchronized void addNote(long studentId, String note, LocalDate date) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            requireStudent(db, studentId);
            ContentValues values = new ContentValues();
            values.put("student_id", studentId);
            values.put("note", note);
            values.put("noted_on", date.toString());
            db.insertOrThrow("session_notes", null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static void requireStudent(SQLiteDatabase db, long studentId) {
        try (Cursor row = db.rawQuery("SELECT id FROM students WHERE id = ?",
                new String[]{Long.toString(studentId)})) {
            if (!row.moveToFirst()) throw new IllegalArgumentException("Student not found.");
        }
    }
}
