package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;

@RunWith(AndroidJUnit4.class)
public class SqliteCoachRepositoryTest {
    private static final String TEST_DATABASE = "coach_repository_test.db";
    private Context context;
    private SqliteCoachRepository repository;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        // Use a separate database so these tests never clear a coach's app records.
        repository = new SqliteCoachRepository(context, TEST_DATABASE);
    }

    @After
    public void tearDown() {
        repository.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void studentAndRelatedRecordsSurviveRepositoryReopen() {
        CoachService service = new CoachService(repository);
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.setGoal(id, "Guard retention", "2026-10-01");
        service.addNote(id, "Worked on frames", LocalDate.of(2026, 9, 30));
        service.recordAttendance(id, LocalDate.of(2026, 9, 30));
        service.setFollowUp(id, true);
        repository.close();

        repository = new SqliteCoachRepository(context, TEST_DATABASE);
        CoachService reopened = new CoachService(repository);
        Student student = reopened.getStudent(id);
        assertEquals("Guard retention", student.getGoal());
        assertEquals("Worked on frames", student.getNotes().get(0));
        assertEquals(1, student.getAttendanceCount());
        assertTrue(student.needsFollowUp());
        assertEquals(LocalDate.of(2026, 10, 1), reopened.getGoalRecord(id).getDueDate());
        assertEquals(1, repository.getAttendanceRecords().size());
        assertEquals(1, repository.getSessionNoteRecords().size());
    }

    @Test
    public void clearingGoalRemovesItsRelatedRecord() {
        CoachService service = new CoachService(repository);
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.setGoal(id, "Guard retention", "2026-10-01");
        service.setGoal(id, "");
        assertEquals("", service.getStudent(id).getGoal());
        assertNull(service.getGoalRecord(id));
    }

    @Test
    public void apostropheInNameDoesNotChangeQueryMeaning() {
        CoachService service = new CoachService(repository);
        long id = service.addStudent("O'Neil", "White belt").getId();
        assertEquals("O'Neil", service.getStudent(id).getName());
        assertEquals(1, service.getStudents().size());
    }

    @Test
    public void foreignKeyRejectsAnOrphanAttendanceRow() {
        CoachDatabaseHelper helper = new CoachDatabaseHelper(context, TEST_DATABASE);
        try {
            assertThrows(SQLiteConstraintException.class, () -> helper.getWritableDatabase()
                    .execSQL("INSERT INTO attendance(student_id, attended_on) " +
                            "VALUES (99999, '2026-09-30')"));
            assertTrue(repository.getAttendanceRecords().isEmpty());
        } finally {
            helper.close();
        }
    }

    @Test
    public void completionStatePersists() {
        CoachService service = new CoachService(repository);
        long id = service.addStudent("Alex", "Blue belt").getId();
        service.setGoal(id, "Guard retention", "2026-09-01");
        assertFalse(service.getGoalRecord(id).isComplete());
        service.setGoalComplete(id, true);
        repository.close();
        repository = new SqliteCoachRepository(context, TEST_DATABASE);
        assertTrue(new CoachService(repository).getGoalRecord(id).isComplete());
    }
}
