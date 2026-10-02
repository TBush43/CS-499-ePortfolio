package com.example.myapplication;

import java.util.List;

public final class CoachService {
    private final CoachRepository repository;

    public CoachService(CoachRepository repository) {
        this.repository = repository;
    }

    public List<Student> getStudents() {
        return repository.getStudents();
    }

    public Student getStudent(long id) {
        Student student = repository.getStudent(id);
        if (student == null) {
            throw new IllegalArgumentException("Student not found.");
        }
        return student;
    }

    public Student addStudent(String name, String belt) {
        // Keep input checks outside the activities so the same rules apply in tests and future screens.
        return repository.addStudent(required(name, 80, "Name"),
                required(belt, 30, "Belt rank"));
    }

    public void setGoal(long id, String goal) {
        String cleaned = goal == null ? "" : goal.trim();
        if (cleaned.length() > 200) {
            throw new IllegalArgumentException("Goal must be 200 characters or fewer.");
        }
        repository.saveStudent(getStudent(id).withGoal(cleaned));
    }

    public void addNote(long id, String note) {
        repository.saveStudent(getStudent(id).withNote(required(note, 1000, "Note")));
    }

    public void recordAttendance(long id) {
        repository.saveStudent(getStudent(id).withAttendance());
    }

    public void setFollowUp(long id, boolean needed) {
        repository.saveStudent(getStudent(id).withFollowUp(needed));
    }

    private static String required(String value, int maxLength, String label) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty() || cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be 1 to " + maxLength + " characters.");
        }
        return cleaned;
    }
}
