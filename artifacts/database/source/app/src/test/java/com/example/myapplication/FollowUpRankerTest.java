package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FollowUpRankerTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 22);
    private final FollowUpRanker ranker = new FollowUpRanker();

    private Student student(long id, String name, boolean followUp) {
        return new Student(id, name, "Blue belt", "", followUp, 0, Collections.emptyList());
    }

    @Test
    public void emptyRosterAndZeroLimitReturnNothing() {
        assertTrue(ranker.rank(Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), 3, TODAY).isEmpty());
        assertTrue(ranker.rank(Collections.singletonList(student(1, "Alex", true)),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                0, TODAY).isEmpty());
    }

    @Test
    public void combinesSignalsAndExplainsScore() {
        List<Student> students = Arrays.asList(student(1, "Alex", true), student(2, "Blair", false));
        List<AttendanceRecord> attendance = Collections.singletonList(
                new AttendanceRecord(2, TODAY.minusDays(2)));
        List<SessionNoteRecord> notes = Collections.singletonList(
                new SessionNoteRecord(2, TODAY.minusDays(1)));
        List<GoalRecord> goals = Collections.singletonList(
                new GoalRecord(1, TODAY.minusDays(1), false));

        List<RankedStudent> result = ranker.rank(students, attendance, notes, goals, 2, TODAY);
        assertEquals(1L, result.get(0).getStudent().getId());
        assertEquals(14, result.get(0).getScore());
        assertEquals(Arrays.asList("marked for follow-up", "no attendance in 30 days",
                "no recent note", "goal past due"), result.get(0).getReasons());
        assertEquals(0, result.get(1).getScore());
    }

    @Test
    public void dateBoundariesAreInclusiveAndCompletedGoalIsIgnored() {
        Student student = student(1, "Alex", false);
        List<RankedStudent> result = ranker.rank(Collections.singletonList(student),
                Collections.singletonList(new AttendanceRecord(1, TODAY.minusDays(30))),
                Collections.singletonList(new SessionNoteRecord(1, TODAY.minusDays(14))),
                Collections.singletonList(new GoalRecord(1, TODAY.minusDays(1), true)), 3, TODAY);
        assertEquals(0, result.get(0).getScore());
        assertTrue(result.get(0).getReasons().isEmpty());

        result = ranker.rank(Collections.singletonList(student),
                Collections.singletonList(new AttendanceRecord(1, TODAY.minusDays(31))),
                Collections.singletonList(new SessionNoteRecord(1, TODAY.minusDays(15))),
                Collections.singletonList(new GoalRecord(1, TODAY, false)), 3, TODAY);
        assertEquals(5, result.get(0).getScore());
        assertFalse(result.get(0).getReasons().contains("goal past due"));
    }

    @Test
    public void heapKeepsTopKAndBreaksTiesByNameThenId() {
        List<Student> students = Arrays.asList(student(4, "Zoe", false),
                student(3, "Alex", false), student(2, "Alex", false),
                student(5, "Bea", false));
        List<RankedStudent> result = ranker.rank(students, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), 2, TODAY);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getStudent().getId());
        assertEquals(3L, result.get(1).getStudent().getId());
    }

    @Test
    public void missingHistoryAndFutureRecordsDoNotCountAsRecent() {
        Student student = student(1, "Alex", false);
        List<RankedStudent> result = ranker.rank(Collections.singletonList(student),
                Collections.singletonList(new AttendanceRecord(1, TODAY.plusDays(1))),
                Collections.singletonList(new SessionNoteRecord(1, TODAY.plusDays(1))),
                Collections.emptyList(), 4, TODAY);
        assertEquals(5, result.get(0).getScore());
    }
}
