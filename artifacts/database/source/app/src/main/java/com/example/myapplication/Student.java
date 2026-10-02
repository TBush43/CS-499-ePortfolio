package com.example.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Student {
    private final long id;
    private final String name;
    private final String belt;
    private final String goal;
    private final boolean followUp;
    private final int attendanceCount;
    private final List<String> notes;

    public Student(long id, String name, String belt, String goal, boolean followUp,
                   int attendanceCount, List<String> notes) {
        this.id = id;
        this.name = name;
        this.belt = belt;
        this.goal = goal;
        this.followUp = followUp;
        this.attendanceCount = attendanceCount;
        // Keep a separate notes list so a screen cannot change this record directly.
        this.notes = Collections.unmodifiableList(new ArrayList<>(notes));
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getBelt() { return belt; }
    public String getGoal() { return goal; }
    public boolean needsFollowUp() { return followUp; }
    public int getAttendanceCount() { return attendanceCount; }
    public List<String> getNotes() { return notes; }

    public Student withGoal(String value) {
        return new Student(id, name, belt, value, followUp, attendanceCount, notes);
    }

    public Student withFollowUp(boolean value) {
        return new Student(id, name, belt, goal, value, attendanceCount, notes);
    }

    public Student withAttendance() {
        return new Student(id, name, belt, goal, followUp, attendanceCount + 1, notes);
    }

    public Student withNote(String value) {
        List<String> updated = new ArrayList<>(notes);
        updated.add(0, value);
        return new Student(id, name, belt, goal, followUp, attendanceCount, updated);
    }
}
