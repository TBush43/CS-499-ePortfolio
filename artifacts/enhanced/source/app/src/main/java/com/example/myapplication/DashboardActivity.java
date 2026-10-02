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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private CoachViewModel model;
    private List<Student> visibleStudents = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        model = new ViewModelProvider(this).get(CoachViewModel.class);
        emptyMessage = findViewById(R.id.emptyRosterMessage);

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
        visibleStudents = model.getStudents();
        adapter.clear();
        // The displayed rows and student IDs must stay in the same order for row taps.
        for (Student student : visibleStudents) {
            String label = student.getName() + "  •  " + student.getBelt();
            if (student.needsFollowUp()) label += "  •  Follow up";
            adapter.add(label);
        }
        emptyMessage.setVisibility(visibleStudents.isEmpty() ? View.VISIBLE : View.GONE);
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
                    try {
                        model.addStudent(name.getText().toString(), belt.getSelectedItem().toString());
                        dialog.dismiss();
                        refreshRoster();
                    } catch (IllegalArgumentException error) {
                        name.setError(error.getMessage());
                    }
                }));
        dialog.show();
    }
}
