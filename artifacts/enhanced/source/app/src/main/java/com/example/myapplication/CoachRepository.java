package com.example.myapplication;

import java.util.List;

public interface CoachRepository {
    // The service can keep this contract when the temporary store is replaced with Room.
    List<Student> getStudents();
    Student getStudent(long id);
    Student addStudent(String name, String belt);
    void saveStudent(Student student);
}
