package com.example.myapplication;

import java.time.LocalDate;

public final class AttendanceRecord {
    // A dated check-in lets the ranking use recent attendance, not just a lifetime total.
    private final long studentId;
    private final LocalDate date;

    public AttendanceRecord(long studentId, LocalDate date) {
        this.studentId = studentId;
        this.date = date;
    }

    public long getStudentId() { return studentId; }
    public LocalDate getDate() { return date; }
}
