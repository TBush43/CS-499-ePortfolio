package com.example.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryCoachRepository implements CoachRepository {
    private final Map<Long, Student> students = new LinkedHashMap<>();
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
    public synchronized void saveStudent(Student student) {
        if (!students.containsKey(student.getId())) {
            throw new IllegalArgumentException("Student not found.");
        }
        students.put(student.getId(), student);
    }
}
