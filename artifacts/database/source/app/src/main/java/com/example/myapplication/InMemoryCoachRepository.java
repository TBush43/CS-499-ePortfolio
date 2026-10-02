package com.example.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public final class InMemoryCoachRepository implements CoachRepository {
    private final Map<Long, Student> students = new LinkedHashMap<>();
    private final List<AttendanceRecord> attendance = new ArrayList<>();
    private final List<SessionNoteRecord> notes = new ArrayList<>();
    private final Map<Long, GoalRecord> goals = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public synchronized List<Student> getStudents() {
        // Return a separate list so callers cannot change the roster directly.
        return new ArrayList<>(students.values());
    }

    @Override
    public synchronized Student getStudent(long id) {
        return students.get(id);
    }

    @Override
    public synchronized Student addStudent(String name, String belt) {
        Student student = new Student(nextId++, name, belt, "", false, 0,
                Collections.emptyList());
        students.put(student.getId(), student);
        return student;
    }

    @Override
    public synchronized void setFollowUp(long studentId, boolean needed) {
        Student student = students.get(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found.");
        }
        students.put(studentId, student.withFollowUp(needed));
    }

    @Override
    public synchronized List<AttendanceRecord> getAttendanceRecords() {
        return new ArrayList<>(attendance);
    }

    @Override
    public synchronized List<SessionNoteRecord> getSessionNoteRecords() {
        return new ArrayList<>(notes);
    }

    @Override
    public synchronized List<GoalRecord> getGoalRecords() {
        return new ArrayList<>(goals.values());
    }

    @Override
    public synchronized GoalRecord getGoalRecord(long studentId) {
        return goals.get(studentId);
    }

    @Override
    public synchronized void saveGoalRecord(GoalRecord goal) {
        if (!students.containsKey(goal.getStudentId())) {
            throw new IllegalArgumentException("Student not found.");
        }
        goals.put(goal.getStudentId(), goal);
    }

    @Override
    public synchronized void saveGoal(Student student, GoalRecord goal) {
        if (!students.containsKey(student.getId())) {
            throw new IllegalArgumentException("Student not found.");
        }
        students.put(student.getId(), student);
        if (student.getGoal().isEmpty()) {
            goals.remove(student.getId());
        } else {
            saveGoalRecord(goal);
        }
    }

    @Override
    public synchronized void recordAttendance(long studentId, LocalDate date) {
        Student student = students.get(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found.");
        }
        students.put(studentId, student.withAttendance());
        attendance.add(new AttendanceRecord(studentId, date));
    }

    @Override
    public synchronized void addNote(long studentId, String note, LocalDate date) {
        Student student = students.get(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found.");
        }
        students.put(studentId, student.withNote(note));
        notes.add(new SessionNoteRecord(studentId, date));
    }
}
