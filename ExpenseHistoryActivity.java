package com.example.expensify;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseHistoryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LinearLayout expensesContainer;
    private LinearLayout suggestionsContainer;
    private ListenerRegistration expensesListener;
    private double lastDayTotalSpend = 0.0;
    private static final String TAG = "ExpenseHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            expensesContainer = findViewById(R.id.expensesContainer);
            suggestionsContainer = findViewById(R.id.suggestionsContainer);

            if (expensesContainer == null) {
                Log.e(TAG, "expensesContainer not found in layout");
                Toast.makeText(this, "Layout error", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (suggestionsContainer == null) {
                Log.e(TAG, "suggestionsContainer not found in layout");
                Toast.makeText(this, "Layout error", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (mAuth.getCurrentUser() != null) {
                loadExpenses(); // Load expenses on start
            } else {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (expensesListener != null) {
            expensesListener.remove();  // Remove listener when activity stops to avoid memory leaks
        }
    }

    private void loadExpenses() {
        try {
            String userId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "Loading expenses for user: " + userId);

            expensesListener = db.collection("users")
                    .document(userId)
                    .collection("expenses")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING) // Sort by date
                    .addSnapshotListener((querySnapshot, e) -> {
                        try {
                            if (e != null) {
                                Log.e(TAG, "Error loading expenses", e);
                                Toast.makeText(this, "Error loading expenses", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Log.d(TAG, "Query snapshot received");

                            if (querySnapshot != null) {
                                Log.d(TAG, "Documents count: " + querySnapshot.size());
                            }

                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Map<String, List<Map<String, Object>>> expensesByDate = new HashMap<>();
                                Map<String, Double> dailyTotalMap = new HashMap<>();

                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    try {
                                        String category = document.getString("category");
                                        Double amount = document.getDouble("amount");
                                        String note = document.getString("note");
                                        Long dateLong = document.getLong("date");

                                        if (category != null && amount != null && dateLong != null) {
                                            String dateString = dateFormat.format(new Date(dateLong));

                                            // Create a map to store the expense data
                                            Map<String, Object> expenseMap = new HashMap<>();
                                            expenseMap.put("category", category);
                                            expenseMap.put("amount", amount);
                                            expenseMap.put("note", note != null ? note : "");
                                            expenseMap.put("timestamp", dateLong);
                                            expenseMap.put("date", dateString);

                                            // Group by date
                                            if (!expensesByDate.containsKey(dateString)) {
                                                expensesByDate.put(dateString, new ArrayList<>());
                                            }
                                            expensesByDate.get(dateString).add(expenseMap);

                                            // Calculate daily totals
                                            dailyTotalMap.put(dateString, dailyTotalMap.getOrDefault(dateString, 0.0) + amount);
                                        } else {
                                            Log.w(TAG, "Expense data incomplete: " + document.getId());
                                        }
                                    } catch (Exception docException) {
                                        Log.e(TAG, "Error processing document", docException);
                                    }
                                }

                                // Sort dates in descending order
                                List<String> sortedDates = new ArrayList<>(expensesByDate.keySet());
                                Collections.sort(sortedDates, Collections.reverseOrder());

                                // Display data
                                runOnUiThread(() -> {
                                    try {
                                        expensesContainer.removeAllViews();
                                        displayExpenses(sortedDates, expensesByDate, dailyTotalMap);
                                        displaySuggestions(dailyTotalMap);
                                    } catch (Exception uiException) {
                                        Log.e(TAG, "Error updating UI", uiException);
                                        Toast.makeText(ExpenseHistoryActivity.this,
                                                "Error displaying expenses", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(() -> {
                                    try {
                                        expensesContainer.removeAllViews();
                                        TextView noExpensesText = new TextView(this);
                                        noExpensesText.setText("No expenses found. Add some expenses to see them here.");
                                        noExpensesText.setPadding(20, 20, 20, 20);
                                        expensesContainer.addView(noExpensesText);

                                        suggestionsContainer.removeAllViews();
                                    } catch (Exception uiException) {
                                        Log.e(TAG, "Error updating UI for empty state", uiException);
                                    }
                                });
                            }
                        } catch (Exception snapshotException) {
                            Log.e(TAG, "Error processing snapshot", snapshotException);
                            Toast.makeText(ExpenseHistoryActivity.this,
                                    "Error processing data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up expense listener", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayExpenses(List<String> sortedDates, Map<String, List<Map<String, Object>>> expensesByDate, Map<String, Double> dailyTotalMap) {
        try {
            for (String date : sortedDates) {
                List<Map<String, Object>> expenses = expensesByDate.get(date);

                TextView dateHeader = new TextView(this);
                dateHeader.setText("Date: " + date);
                dateHeader.setTextSize(18);
                dateHeader.setPadding(0, 20, 0, 10);
                expensesContainer.addView(dateHeader);

                for (Map<String, Object> expense : expenses) {
                    MaterialCardView cardView = new MaterialCardView(this);
                    cardView.setRadius(8);
                    cardView.setCardElevation(4);
                    cardView.setUseCompatPadding(true);

                    TextView expenseTextView = new TextView(this);
                    expenseTextView.setText("Category: " + expense.get("category") + "\n" +
                            "Amount: ₹" + expense.get("amount") + "\n" +
                            "Note: " + expense.get("note"));
                    expenseTextView.setTextSize(16);
                    expenseTextView.setPadding(20, 10, 20, 10);
                    cardView.addView(expenseTextView);
                    expensesContainer.addView(cardView);
                }

                double totalSpend = dailyTotalMap.get(date);
                TextView totalSpendTextView = new TextView(this);
                totalSpendTextView.setText("Total for " + date + ": ₹" + totalSpend);
                totalSpendTextView.setTextSize(16);
                totalSpendTextView.setPadding(0, 10, 0, 20);
                expensesContainer.addView(totalSpendTextView);

                // Store last day's spend for comparison
                if (sortedDates.indexOf(date) == 0) {
                    lastDayTotalSpend = totalSpend;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying expenses", e);
        }
    }

    private void displaySuggestions(Map<String, Double> dailyTotalMap) {
        try {
            suggestionsContainer.removeAllViews();

            List<String> sortedDates = new ArrayList<>(dailyTotalMap.keySet());
            if (sortedDates.isEmpty()) {
                TextView noDataText = new TextView(this);
                noDataText.setText("No expense data available for suggestions.");
                noDataText.setTextSize(16);
                suggestionsContainer.addView(noDataText);
                return;
            }

            Collections.sort(sortedDates); // ascending order: [older dates ... newer]

            if (sortedDates.size() < 2) {
                TextView noComparisonText = new TextView(this);
                noComparisonText.setText("Add more expenses on different days to see spending insights!");
                noComparisonText.setTextSize(16);
                suggestionsContainer.addView(noComparisonText);
                return;
            }

            String yesterday = sortedDates.get(sortedDates.size() - 2);
            String today = sortedDates.get(sortedDates.size() - 1);

            double yesterdaySpend = dailyTotalMap.get(yesterday);
            double todaySpend = dailyTotalMap.get(today);

            TextView comparisonText = new TextView(this);
            comparisonText.setText("Yesterday: ₹" + yesterdaySpend + " | Today: ₹" + todaySpend);
            comparisonText.setTextSize(16);
            suggestionsContainer.addView(comparisonText);

            String suggestionMessage;
            double difference = yesterdaySpend - todaySpend;

            if (difference > 0) {
                suggestionMessage = "Great job! You saved ₹" + difference + " today compared to yesterday!";
            } else if (difference < 0) {
                suggestionMessage = "You spent ₹" + Math.abs(difference) + " more today than yesterday. Try to reduce spending tomorrow!";
            } else {
                suggestionMessage = "You spent the same as yesterday! Keep it steady!";
            }

            TextView suggestionText = new TextView(this);
            suggestionText.setText(suggestionMessage);
            suggestionText.setTextSize(18);
            suggestionText.setTextColor(getResources().getColor(R.color.teal_200));
            suggestionText.setPadding(0, 20, 0, 20);
            suggestionsContainer.addView(suggestionText);
        } catch (Exception e) {
            Log.e(TAG, "Error displaying suggestions", e);
        }
    }
}