package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class StudentActivity extends AppCompatActivity {
    public static final String EXTRA_STUDENT_ID = "student_id";

    private CoachViewModel model;
    private long studentId;
    private Student currentStudent;
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
    private Button saveGoalButton;
    private Button saveNoteButton;
    private Button attendanceButton;
    private Button editStudentButton;
    private Button deleteStudentButton;

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
        saveGoalButton = findViewById(R.id.saveGoalButton);
        saveNoteButton = findViewById(R.id.saveNoteButton);
        attendanceButton = findViewById(R.id.attendanceButton);
        editStudentButton = findViewById(R.id.editStudentButton);
        deleteStudentButton = findViewById(R.id.deleteStudentButton);

        setBusy(true);
        saveGoalButton.setOnClickListener(view -> {
            String goal = goalInput.getText().toString();
            String due = goalDueInput.getText().toString();
            setBusy(true);
            model.submit(service -> {
                service.setGoal(studentId, goal, due);
                return null;
            }, unused -> {
                if (isDestroyed()) return;
                refreshStudent(true);
                Toast.makeText(this, R.string.goal_saved, Toast.LENGTH_SHORT).show();
            }, error -> {
                if (isDestroyed()) return;
                setBusy(false);
                if (error instanceof IllegalArgumentException) {
                    EditText field = error.getMessage().startsWith("Use YYYY")
                            ? goalDueInput : goalInput;
                    field.setError(error.getMessage());
                } else showStorageError(error);
            });
        });
        saveNoteButton.setOnClickListener(view -> {
            String note = noteInput.getText().toString();
            setBusy(true);
            model.submit(service -> {
                service.addNote(studentId, note);
                return null;
            }, unused -> {
                if (isDestroyed()) return;
                noteInput.setText("");
                refreshStudent(false);
            }, error -> {
                if (isDestroyed()) return;
                setBusy(false);
                if (error instanceof IllegalArgumentException) noteInput.setError(error.getMessage());
                else showStorageError(error);
            });
        });
        attendanceButton.setOnClickListener(view -> {
            setBusy(true);
            model.submit(service -> {
                service.recordAttendance(studentId);
                return null;
            }, unused -> {
                if (isDestroyed()) return;
                refreshStudent(false);
                Toast.makeText(this, R.string.attendance_saved, Toast.LENGTH_SHORT).show();
            }, this::handleWriteError);
        });
        editStudentButton.setOnClickListener(view -> showEditDialog());
        deleteStudentButton.setOnClickListener(view -> showDeleteConfirmation());
        refreshStudent(true);
    }

    private void refreshStudent(boolean updateGoalFields) {
        // Keep an unsaved goal draft when another action refreshes the profile.
        setBusy(true);
        model.submit(service -> new StudentData(service.getStudent(studentId),
                        service.getGoalRecord(studentId)),
                data -> {
                    if (!isDestroyed()) renderStudent(data, updateGoalFields);
                }, error -> {
                    if (isDestroyed()) return;
                    if (error instanceof IllegalArgumentException) {
                        Toast.makeText(this, R.string.student_not_found, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        setBusy(false);
                        showStorageError(error);
                    }
                });
    }

    private void renderStudent(StudentData data, boolean updateGoalFields) {
        currentStudent = data.student;
        nameLabel.setText(data.student.getName());
        beltLabel.setText(data.student.getBelt());
        if (updateGoalFields) {
            goalInput.setText(data.student.getGoal());
            goalDueInput.setText(data.goal == null || data.goal.getDueDate() == null ? ""
                    : data.goal.getDueDate().toString());
        }
        // Restore the checkboxes without saving them as new edits.
        goalCompleteBox.setOnCheckedChangeListener(null);
        goalCompleteBox.setChecked(data.goal != null && data.goal.isComplete());
        goalCompleteBox.setEnabled(!data.student.getGoal().isEmpty());
        goalCompleteBox.setOnCheckedChangeListener((button, checked) -> {
            setBusy(true);
            model.submit(service -> {
                service.setGoalComplete(studentId, checked);
                return null;
            }, unused -> {
                if (!isDestroyed()) refreshStudent(false);
            }, error -> {
                if (!isDestroyed()) handleWriteError(error);
            });
        });
        attendanceLabel.setText(getString(R.string.attendance_total,
                data.student.getAttendanceCount()));
        followUpBox.setOnCheckedChangeListener(null);
        followUpBox.setChecked(data.student.needsFollowUp());
        followUpBox.setOnCheckedChangeListener((button, checked) -> {
            setBusy(true);
            model.submit(service -> {
                service.setFollowUp(studentId, checked);
                return null;
            }, unused -> {
                if (!isDestroyed()) refreshStudent(false);
            }, error -> {
                if (!isDestroyed()) handleWriteError(error);
            });
        });
        emptyNotesLabel.setVisibility(data.student.getNotes().isEmpty() ? View.VISIBLE : View.GONE);
        notesLabel.setText(android.text.TextUtils.join("\n\n", data.student.getNotes()));
        setBusy(false);
    }

    private void showEditDialog() {
        if (currentStudent == null) return;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding / 2, padding, 0);
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setText(currentStudent.getName());
        name.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        form.addView(name);
        Spinner belt = new Spinner(this);
        ArrayAdapter<CharSequence> belts = ArrayAdapter.createFromResource(this,
                R.array.belt_ranks, android.R.layout.simple_spinner_item);
        belts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        belt.setAdapter(belts);
        int selected = belts.getPosition(currentStudent.getBelt());
        belt.setSelection(Math.max(selected, 0));
        form.addView(belt);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_student)
                .setView(form)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String enteredName = name.getText().toString();
                    String selectedBelt = belt.getSelectedItem().toString();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    model.submit(service -> {
                        service.updateStudent(studentId, enteredName, selectedBelt);
                        return null;
                    }, result -> {
                        if (isDestroyed()) return;
                        dialog.dismiss();
                        refreshStudent(false);
                    }, error -> {
                        if (isDestroyed()) return;
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        if (error instanceof IllegalArgumentException) name.setError(error.getMessage());
                        else showStorageError(error);
                    });
                }));
        dialog.show();
    }

    private void showDeleteConfirmation() {
        if (currentStudent == null) return;
        // Deletion also removes the student's goal, notes, and check-ins.
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_student)
                .setMessage(getString(R.string.delete_student_confirmation, currentStudent.getName()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_student, (dialog, which) -> {
                    setBusy(true);
                    model.submit(service -> {
                        service.deleteStudent(studentId);
                        return null;
                    }, unused -> {
                        if (!isDestroyed()) finish();
                    }, this::handleWriteError);
                })
                .show();
    }

    private void setBusy(boolean busy) {
        // Disable repeat taps while a database operation is pending.
        saveGoalButton.setEnabled(!busy);
        saveNoteButton.setEnabled(!busy);
        attendanceButton.setEnabled(!busy);
        editStudentButton.setEnabled(!busy);
        deleteStudentButton.setEnabled(!busy);
        followUpBox.setEnabled(!busy);
        goalCompleteBox.setEnabled(!busy && currentStudent != null
                && !currentStudent.getGoal().isEmpty());
    }

    private void handleWriteError(RuntimeException error) {
        if (isDestroyed()) return;
        showStorageError(error);
        refreshStudent(false);
    }

    private void showStorageError(RuntimeException error) {
        Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
    }

    private static final class StudentData {
        final Student student;
        final GoalRecord goal;

        StudentData(Student student, GoalRecord goal) {
            this.student = student;
            this.goal = goal;
        }
    }
}
