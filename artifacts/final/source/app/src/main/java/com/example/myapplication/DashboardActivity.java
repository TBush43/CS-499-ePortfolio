package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class DashboardActivity extends AppCompatActivity {
    private CoachViewModel model;
    private List<Student> visibleStudents = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView emptyMessage;
    private TextView attentionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        model = new ViewModelProvider(this).get(CoachViewModel.class);
        emptyMessage = findViewById(R.id.emptyRosterMessage);
        attentionList = findViewById(R.id.attentionList);

        ListView roster = findViewById(R.id.studentList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        roster.setAdapter(adapter);
        roster.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, StudentActivity.class);
            intent.putExtra(StudentActivity.EXTRA_STUDENT_ID, visibleStudents.get(position).getId());
            startActivity(intent);
        });
        findViewById(R.id.addStudentButton).setOnClickListener(view -> showAddStudentDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRoster();
    }

    private void refreshRoster() {
        // Load the roster and review order together, away from the UI thread.
        model.submit(service -> new DashboardData(service.getStudents(),
                        service.getStudentsNeedingAttention(3, LocalDate.now())),
                data -> {
                    if (!isDestroyed()) renderRoster(data);
                }, this::showLoadError);
    }

    private void renderRoster(DashboardData data) {
        visibleStudents = data.students;
        adapter.clear();
        // The displayed rows and student IDs must stay in the same order for row taps.
        for (Student student : visibleStudents) {
            String label = student.getName() + "  •  " + student.getBelt();
            if (student.needsFollowUp()) label += "  •  Follow up";
            adapter.add(label);
        }
        emptyMessage.setVisibility(visibleStudents.isEmpty() ? View.VISIBLE : View.GONE);
        StringBuilder summary = new StringBuilder();
        // Show the reasons beside each student so the ranking is easy to check.
        for (RankedStudent item : data.attention) {
            // A zero score means there is nothing to follow up on.
            if (item.getScore() == 0) continue;
            if (summary.length() > 0) summary.append("\n\n");
            summary.append(item.getStudent().getName())
                    .append(" (").append(item.getScore()).append(")")
                    .append(" — ").append(android.text.TextUtils.join(", ", item.getReasons()));
        }
        attentionList.setText(summary.length() == 0
                ? getString(data.students.isEmpty() ? R.string.no_attention : R.string.all_up_to_date)
                : summary.toString());
    }

    private void showLoadError(RuntimeException error) {
        if (!isDestroyed()) Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
    }

    private void showAddStudentDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding / 2, padding, 0);

        EditText name = new EditText(this);
        name.setHint(R.string.student_name);
        name.setSingleLine(true);
        name.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        form.addView(name);

        Spinner belt = new Spinner(this);
        ArrayAdapter<CharSequence> belts = ArrayAdapter.createFromResource(this,
                R.array.belt_ranks, android.R.layout.simple_spinner_item);
        belts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        belt.setAdapter(belts);
        form.addView(belt);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_student)
                .setView(form)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String enteredName = name.getText().toString();
                    String selectedBelt = belt.getSelectedItem().toString();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    model.submit(service -> service.addStudent(enteredName, selectedBelt),
                            student -> {
                                if (isDestroyed()) return;
                                dialog.dismiss();
                                refreshRoster();
                            }, error -> {
                                if (isDestroyed()) return;
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                if (error instanceof IllegalArgumentException) name.setError(error.getMessage());
                                else showLoadError(error);
                            });
                }));
        dialog.show();
    }

    private static final class DashboardData {
        final List<Student> students;
        final List<RankedStudent> attention;

        DashboardData(List<Student> students, List<RankedStudent> attention) {
            this.students = students;
            this.attention = attention;
        }
    }
}
