package com.example.myapplication;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

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
        setGoal(id, goal, "");
    }

    public void setGoal(long id, String goal, String dueDateText) {
        String cleaned = goal == null ? "" : goal.trim();
        if (cleaned.length() > 200) {
            throw new IllegalArgumentException("Goal must be 200 characters or fewer.");
        }
        String dateValue = dueDateText == null ? "" : dueDateText.trim();
        LocalDate dueDate = null;
        if (!dateValue.isEmpty()) {
            if (cleaned.isEmpty()) throw new IllegalArgumentException("Add a goal before setting a due date.");
            try {
                dueDate = LocalDate.parse(dateValue);
            } catch (DateTimeParseException error) {
                throw new IllegalArgumentException("Use YYYY-MM-DD for the due date.");
            }
        }
        Student student = getStudent(id);
        GoalRecord previous = repository.getGoalRecord(id);
        boolean unchanged = student.getGoal().equals(cleaned) && previous != null
                && Objects.equals(previous.getDueDate(), dueDate);
        // Save the goal text and its due-date record together.
        repository.saveGoal(student.withGoal(cleaned), new GoalRecord(id,
                cleaned.isEmpty() ? null : dueDate, unchanged && previous.isComplete()));
    }

    public GoalRecord getGoalRecord(long id) {
        getStudent(id);
        return repository.getGoalRecord(id);
    }

    public void setGoalComplete(long id, boolean complete) {
        GoalRecord goal = getGoalRecord(id);
        if (goal == null || getStudent(id).getGoal().isEmpty()) {
            throw new IllegalArgumentException("Save a goal first.");
        }
        repository.saveGoalRecord(goal.withComplete(complete));
    }

    public void addNote(long id, String note) {
        addNote(id, note, LocalDate.now());
    }

    void addNote(long id, String note, LocalDate date) {
        getStudent(id);
        repository.addNote(id, required(note, 1000, "Note"), date);
    }

    public void recordAttendance(long id) {
        recordAttendance(id, LocalDate.now());
    }

    void recordAttendance(long id, LocalDate date) {
        getStudent(id);
        repository.recordAttendance(id, date);
    }

    public void setFollowUp(long id, boolean needed) {
        getStudent(id);
        repository.setFollowUp(id, needed);
    }

    public List<RankedStudent> getStudentsNeedingAttention(int limit, LocalDate today) {
        return new FollowUpRanker().rank(repository.getStudents(),
                repository.getAttendanceRecords(), repository.getSessionNoteRecords(),
                repository.getGoalRecords(), limit, today);
    }

    private static String required(String value, int maxLength, String label) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isEmpty() || cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be 1 to " + maxLength + " characters.");
        }
        return cleaned;
    }
}
