package com.example.myapplication;

import java.util.List;
import java.time.LocalDate;

public interface CoachRepository {
    // Keep storage details out of the screens and coaching rules.
    List<Student> getStudents();
    Student getStudent(long id);
    Student addStudent(String name, String belt);
    void setFollowUp(long studentId, boolean needed);
    List<AttendanceRecord> getAttendanceRecords();
    List<SessionNoteRecord> getSessionNoteRecords();
    List<GoalRecord> getGoalRecords();
    GoalRecord getGoalRecord(long studentId);
    void saveGoal(Student student, GoalRecord goal);
    void saveGoalRecord(GoalRecord goal);
    void recordAttendance(long studentId, LocalDate date);
    void addNote(long studentId, String note, LocalDate date);
}
