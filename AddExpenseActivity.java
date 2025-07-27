package com.example.expensify;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    EditText editAmount, editCategory, editNote;
    Button buttonSave;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        editAmount = findViewById(R.id.editAmount);
        editCategory = findViewById(R.id.editCategory);
        editNote = findViewById(R.id.editNote);
        buttonSave = findViewById(R.id.buttonSave);

        mAuth = FirebaseAuth.getInstance();

        buttonSave.setOnClickListener(v -> saveExpenseToFirebase());
    }

    private void saveExpenseToFirebase() {
        String amountStr = editAmount.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (amountStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", amount);
        expense.put("category", category);
        expense.put("note", note);
        expense.put("date", System.currentTimeMillis()); // ✅ Save date as long

        Log.d("SaveExpense", "Saving expense with date: " + System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddExpenseActivity.this, "Expense saved!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddExpenseActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FirestoreError", "Saving failed", e);
                });
    }
}
