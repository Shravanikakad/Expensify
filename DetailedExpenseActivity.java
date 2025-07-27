package com.example.expensify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DetailedExpenseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private TextView dateTextView;
    private ArrayList<Expense> expensesForDay; // ✅ Now using Expense directly

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_expense);

        recyclerView = findViewById(R.id.expenseRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dateTextView = findViewById(R.id.dateTextView);

        // Get date and expenses passed from previous activity
        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        expensesForDay = intent.getParcelableArrayListExtra("expenses");

        if (date != null) {
            dateTextView.setText("Expenses for " + date);
        }

        if (expensesForDay != null && !expensesForDay.isEmpty()) {
            expenseAdapter = new ExpenseAdapter(this, expensesForDay);
            recyclerView.setAdapter(expenseAdapter);
        } else {
            Toast.makeText(this, "No expenses found for this day", Toast.LENGTH_SHORT).show();
        }
    }
}
