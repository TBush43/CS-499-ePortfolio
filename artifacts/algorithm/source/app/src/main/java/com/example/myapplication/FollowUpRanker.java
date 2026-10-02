package com.example.myapplication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public final class FollowUpRanker {
    private static final int ATTENDANCE_WINDOW_DAYS = 30;
    private static final int NOTE_WINDOW_DAYS = 14;

    private static final Comparator<RankedStudent> BEST_FIRST =
            Comparator.comparingInt(RankedStudent::getScore).reversed()
                    .thenComparing(item -> item.getStudent().getName(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparingLong(item -> item.getStudent().getId());

    public List<RankedStudent> rank(List<Student> students, List<AttendanceRecord> attendance,
                                    List<SessionNoteRecord> notes, List<GoalRecord> goals,
                                    int limit, LocalDate today) {
        if (limit <= 0 || students.isEmpty()) return new ArrayList<>();

        Map<Long, Integer> recentAttendance = new HashMap<>();
        Map<Long, LocalDate> latestNotes = new HashMap<>();
        Map<Long, GoalRecord> goalsByStudent = new HashMap<>();
        LocalDate attendanceStart = today.minusDays(ATTENDANCE_WINDOW_DAYS);

        // Scan each history list once before scoring the roster.
        for (AttendanceRecord record : attendance) {
            if (!record.getDate().isBefore(attendanceStart) && !record.getDate().isAfter(today)) {
                recentAttendance.merge(record.getStudentId(), 1, Integer::sum);
            }
        }
        for (SessionNoteRecord note : notes) {
            if (!note.getDate().isAfter(today)) {
                latestNotes.merge(note.getStudentId(), note.getDate(),
                        (first, second) -> first.isAfter(second) ? first : second);
            }
        }
        for (GoalRecord goal : goals) goalsByStudent.put(goal.getStudentId(), goal);

        // The least useful entry sits at the top, so the heap holds only the best limit entries.
        PriorityQueue<RankedStudent> top = new PriorityQueue<>(
                Math.min(limit, students.size()), BEST_FIRST.reversed());
        for (Student student : students) {
            List<String> reasons = new ArrayList<>();
            int score = 0;
            if (student.needsFollowUp()) {
                score += 5;
                reasons.add("marked for follow-up");
            }
            if (recentAttendance.getOrDefault(student.getId(), 0) == 0) {
                score += 3;
                reasons.add("no attendance in 30 days");
            }
            LocalDate latestNote = latestNotes.get(student.getId());
            if (latestNote == null || latestNote.isBefore(today.minusDays(NOTE_WINDOW_DAYS))) {
                score += 2;
                reasons.add("no recent note");
            }
            GoalRecord goal = goalsByStudent.get(student.getId());
            if (goal != null && !goal.isComplete() && goal.getDueDate() != null
                    && goal.getDueDate().isBefore(today)) {
                score += 4;
                reasons.add("goal past due");
            }

            RankedStudent candidate = new RankedStudent(student, score, reasons);
            if (top.size() < limit) top.add(candidate);
            else if (BEST_FIRST.compare(candidate, top.peek()) < 0) {
                top.poll();
                top.add(candidate);
            }
        }

        List<RankedStudent> result = new ArrayList<>(top);
        result.sort(BEST_FIRST);
        return result;
    }
}
