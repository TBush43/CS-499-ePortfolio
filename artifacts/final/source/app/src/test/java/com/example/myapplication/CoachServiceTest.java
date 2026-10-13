package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class CoachServiceTest {
    // Exercise the same validation and record changes used by the screens.
    private CoachService service;

    @Before
    public void setUp() {
        service = new CoachService(new InMemoryCoachRepository());
    }

    @Test
    public void newRosterStartsEmpty() {
        assertTrue(service.getStudents().isEmpty());
    }

    @Test
    public void addingStudentTrimsFieldsAndAssignsId() {
        Student student = service.addStudent("  Alex  ", "Blue belt");
        assertEquals(1L, student.getId());
        assertEquals("Alex", service.getStudent(student.getId()).getName());
        assertEquals(1, service.getStudents().size());
    }

    @Test
    public void blankNameIsRejectedWithoutCreatingRecord() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addStudent("   ", "White belt"));
        assertTrue(service.getStudents().isEmpty());
    }

    @Test
    public void goalNoteAttendanceAndFollowUpUpdateStudent() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.setGoal(id, "  Improve guard retention  ");
        service.addNote(id, "  Practiced frames  ");
        service.recordAttendance(id);
        service.setFollowUp(id, true);

        Student updated = service.getStudent(id);
        assertEquals("Improve guard retention", updated.getGoal());
        assertEquals("Practiced frames", updated.getNotes().get(0));
        assertEquals(1, updated.getAttendanceCount());
        assertTrue(updated.needsFollowUp());
    }

    @Test
    public void invalidNoteDoesNotChangeStudent() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        assertThrows(IllegalArgumentException.class, () -> service.addNote(id, " "));
        assertTrue(service.getStudent(id).getNotes().isEmpty());
    }

    @Test
    public void unknownStudentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.getStudent(999));
        assertThrows(IllegalArgumentException.class, () -> service.recordAttendance(999));
    }

    @Test
    public void returnedNotesCannotBeChanged() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.addNote(id, "Good progress");
        assertThrows(UnsupportedOperationException.class,
                () -> service.getStudent(id).getNotes().clear());
        assertFalse(service.getStudent(id).getNotes().isEmpty());
    }

    @Test
    public void datedActivityChangesPriority() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        LocalDate today = LocalDate.of(2026, 9, 22);
        assertEquals(5, service.getStudentsNeedingAttention(1, today).get(0).getScore());
        service.recordAttendance(id, today);
        service.addNote(id, "Practiced guard", today);
        assertEquals(0, service.getStudentsNeedingAttention(1, today).get(0).getScore());
        service.setGoal(id, "Guard retention", "2026-09-21");
        assertEquals(4, service.getStudentsNeedingAttention(1, today).get(0).getScore());
        service.setGoalComplete(id, true);
        assertEquals(0, service.getStudentsNeedingAttention(1, today).get(0).getScore());
    }

    @Test
    public void invalidDueDateLeavesGoalUnchanged() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        assertThrows(IllegalArgumentException.class,
                () -> service.setGoal(id, "Guard retention", "09/22/2026"));
        assertEquals("", service.getStudent(id).getGoal());
    }

    @Test
    public void savingUnchangedGoalKeepsCompletion() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.setGoal(id, "Guard retention", "2026-09-30");
        service.setGoalComplete(id, true);
        service.setGoal(id, "Guard retention", "2026-09-30");
        assertTrue(service.getGoalRecord(id).isComplete());
        service.setGoal(id, "Improve passing", "2026-09-30");
        assertFalse(service.getGoalRecord(id).isComplete());
    }

    @Test
    public void editingStudentKeepsCoachingHistory() {
        long id = service.addStudent("Alex", "White belt").getId();
        service.setGoal(id, "Guard retention");
        service.addNote(id, "Worked on frames");
        service.recordAttendance(id);
        service.updateStudent(id, "  Alex Rivera  ", "Blue belt");

        Student updated = service.getStudent(id);
        assertEquals("Alex Rivera", updated.getName());
        assertEquals("Blue belt", updated.getBelt());
        assertEquals("Guard retention", updated.getGoal());
        assertEquals(1, updated.getAttendanceCount());
        assertEquals("Worked on frames", updated.getNotes().get(0));
    }

    @Test
    public void invalidEditLeavesStudentUnchanged() {
        long id = service.addStudent("Alex", "White belt").getId();
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStudent(id, " ", "Blue belt"));
        assertEquals("Alex", service.getStudent(id).getName());
    }

    @Test
    public void deletingStudentRemovesRelatedRecordsFromRanking() {
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.setGoal(id, "Guard retention");
        service.addNote(id, "Worked on frames");
        service.recordAttendance(id);
        service.deleteStudent(id);

        assertTrue(service.getStudents().isEmpty());
        assertTrue(service.getStudentsNeedingAttention(3, LocalDate.now()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.getStudent(id));
        assertThrows(IllegalArgumentException.class, () -> service.deleteStudent(id));
    }

    @Test
    public void deletingOneStudentKeepsOtherStudentsRecords() {
        long removedId = service.addStudent("Alex", "Blue belt").getId();
        long keptId = service.addStudent("Blair", "White belt").getId();
        service.addNote(keptId, "Worked on guard retention");

        service.deleteStudent(removedId);

        assertEquals(1, service.getStudents().size());
        assertEquals("Worked on guard retention", service.getStudent(keptId).getNotes().get(0));
    }
}
