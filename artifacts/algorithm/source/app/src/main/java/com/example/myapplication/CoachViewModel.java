package com.example.myapplication;

import androidx.lifecycle.ViewModel;

import java.util.List;
import java.time.LocalDate;

public final class CoachViewModel extends ViewModel {
    // This store is temporary; a persistent repository will replace it in the database milestone.
    private static final CoachRepository STORE = new InMemoryCoachRepository();
    private final CoachService service = new CoachService(STORE);

    public List<Student> getStudents() { return service.getStudents(); }
    public Student getStudent(long id) { return service.getStudent(id); }
    public Student addStudent(String name, String belt) { return service.addStudent(name, belt); }
    public void setGoal(long id, String goal) { service.setGoal(id, goal); }
    public void setGoal(long id, String goal, String dueDate) { service.setGoal(id, goal, dueDate); }
    public GoalRecord getGoalRecord(long id) { return service.getGoalRecord(id); }
    public void setGoalComplete(long id, boolean complete) { service.setGoalComplete(id, complete); }
    public void addNote(long id, String note) { service.addNote(id, note); }
    public void recordAttendance(long id) { service.recordAttendance(id); }
    public void setFollowUp(long id, boolean needed) { service.setFollowUp(id, needed); }
    public List<RankedStudent> getStudentsNeedingAttention(int limit) {
        return service.getStudentsNeedingAttention(limit, LocalDate.now());
    }
}
