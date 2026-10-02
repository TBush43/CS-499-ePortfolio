package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class StudentActivity extends AppCompatActivity {
    public static final String EXTRA_STUDENT_ID = "student_id";

    private CoachViewModel model;
    private long studentId;
    private TextView nameLabel;
    private TextView beltLabel;
    private TextView attendanceLabel;
    private TextView notesLabel;
    private TextView emptyNotesLabel;
    private EditText goalInput;
    private EditText goalDueInput;
    private EditText noteInput;
    private CheckBox followUpBox;
    private CheckBox goalCompleteBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        model = new ViewModelProvider(this).get(CoachViewModel.class);
        studentId = getIntent().getLongExtra(EXTRA_STUDENT_ID, -1);

        nameLabel = findViewById(R.id.studentName);
        beltLabel = findViewById(R.id.studentBelt);
        attendanceLabel = findViewById(R.id.attendanceCount);
        notesLabel = findViewById(R.id.noteList);
        emptyNotesLabel = findViewById(R.id.emptyNotes);
        goalInput = findViewById(R.id.goalInput);
        goalDueInput = findViewById(R.id.goalDueInput);
        noteInput = findViewById(R.id.noteInput);
        followUpBox = findViewById(R.id.followUpBox);
        goalCompleteBox = findViewById(R.id.goalCompleteBox);

        findViewById(R.id.saveGoalButton).setOnClickListener(view -> {
            try {
                model.setGoal(studentId, goalInput.getText().toString(),
                        goalDueInput.getText().toString());
                refreshStudent();
                Toast.makeText(this, R.string.goal_saved, Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException error) {
                goalDueInput.setError(error.getMessage());
            }
        });
        findViewById(R.id.saveNoteButton).setOnClickListener(view -> {
            try {
                model.addNote(studentId, noteInput.getText().toString());
                noteInput.setText("");
                refreshStudent();
            } catch (IllegalArgumentException error) {
                noteInput.setError(error.getMessage());
            }
        });
        findViewById(R.id.attendanceButton).setOnClickListener(view -> {
            model.recordAttendance(studentId);
            refreshStudent();
        });
        followUpBox.setOnCheckedChangeListener((button, checked) -> model.setFollowUp(studentId, checked));
        refreshStudent();
    }

    private void refreshStudent() {
        Student student;
        try {
            student = model.getStudent(studentId);
        } catch (IllegalArgumentException error) {
            Toast.makeText(this, R.string.student_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nameLabel.setText(student.getName());
        beltLabel.setText(student.getBelt());
        goalInput.setText(student.getGoal());
        GoalRecord goal = model.getGoalRecord(studentId);
        goalDueInput.setText(goal == null || goal.getDueDate() == null ? ""
                : goal.getDueDate().toString());
        // Restore the checkbox without treating the refresh as a user edit.
        goalCompleteBox.setOnCheckedChangeListener(null);
        goalCompleteBox.setChecked(goal != null && goal.isComplete());
        goalCompleteBox.setEnabled(!student.getGoal().isEmpty());
        goalCompleteBox.setOnCheckedChangeListener((button, checked) -> {
            try {
                model.setGoalComplete(studentId, checked);
            } catch (IllegalArgumentException error) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                refreshStudent();
            }
        });
        attendanceLabel.setText(getString(R.string.attendance_total, student.getAttendanceCount()));
        // Do not save a follow-up change while restoring the checkbox state.
        followUpBox.setOnCheckedChangeListener(null);
        followUpBox.setChecked(student.needsFollowUp());
        followUpBox.setOnCheckedChangeListener((button, checked) -> model.setFollowUp(studentId, checked));
        emptyNotesLabel.setVisibility(student.getNotes().isEmpty() ? View.VISIBLE : View.GONE);
        notesLabel.setText(android.text.TextUtils.join("\n\n", student.getNotes()));
    }
}
