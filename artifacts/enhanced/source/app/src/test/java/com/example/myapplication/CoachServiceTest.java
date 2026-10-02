package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CoachServiceTest {
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
}
