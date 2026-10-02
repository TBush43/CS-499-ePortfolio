package com.example.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RankedStudent {
    private final Student student;
    private final int score;
    private final List<String> reasons;

    public RankedStudent(Student student, int score, List<String> reasons) {
        this.student = student;
        this.score = score;
        // Keep the explanation stable after ranking.
        this.reasons = Collections.unmodifiableList(new ArrayList<>(reasons));
    }

    public Student getStudent() { return student; }
    public int getScore() { return score; }
    public List<String> getReasons() { return reasons; }
}
