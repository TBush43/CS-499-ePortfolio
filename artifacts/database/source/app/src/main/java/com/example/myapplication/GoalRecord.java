package com.example.myapplication;

import java.time.LocalDate;

public final class GoalRecord {
    // A completed goal cannot be counted as overdue.
    private final long studentId;
    private final LocalDate dueDate;
    private final boolean complete;

    public GoalRecord(long studentId, LocalDate dueDate, boolean complete) {
        this.studentId = studentId;
        this.dueDate = dueDate;
        this.complete = complete;
    }

    public long getStudentId() { return studentId; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isComplete() { return complete; }

    public GoalRecord withComplete(boolean value) {
        return new GoalRecord(studentId, dueDate, value);
    }
}
