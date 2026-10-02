package com.example.myapplication;

import java.util.List;

public interface CoachRepository {
    // The service can keep this contract when the temporary store is replaced with Room.
    List<Student> getStudents();
    Student getStudent(long id);
    Student addStudent(String name, String belt);
    void saveStudent(Student student);
    List<AttendanceRecord> getAttendanceRecords();
    List<SessionNoteRecord> getSessionNoteRecords();
    List<GoalRecord> getGoalRecords();
    GoalRecord getGoalRecord(long studentId);
    void saveGoalRecord(GoalRecord goal);
    void addAttendanceRecord(AttendanceRecord record);
    void addSessionNoteRecord(SessionNoteRecord record);
}
