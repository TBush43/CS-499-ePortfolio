package com.example.myapplication;

import java.time.LocalDate;

public final class SessionNoteRecord {
    // Only the note date is needed to check whether coaching notes are recent.
    private final long studentId;
    private final LocalDate date;

    public SessionNoteRecord(long studentId, LocalDate date) {
        this.studentId = studentId;
        this.date = date;
    }

    public long getStudentId() { return studentId; }
    public LocalDate getDate() { return date; }
}
