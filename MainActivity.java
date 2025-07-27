package com.example.expensify;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Button addExpenseButton, viewHistoryButton, financialCalculatorButton;
    private FloatingActionButton fabAddExpense;
    private TextView tvMonthlySpent;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        addExpenseButton = findViewById(R.id.buttonAddExpense);
        viewHistoryButton = findViewById(R.id.buttonViewHistory);
        financialCalculatorButton = findViewById(R.id.buttonFinancialCalculator);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        tvMonthlySpent = findViewById(R.id.tvMonthlySpent);

        // Set up navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Update user info in navigation header
        updateNavigationHeader(navigationView);

        // Set up button click listeners
        addExpenseButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddExpenseActivity.class)));
        viewHistoryButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ExpenseHistoryActivity.class)));
        financialCalculatorButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FinancialCalculatorActivity.class)));
        fabAddExpense.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddExpenseActivity.class)));

        // Load monthly expenses summary
        loadMonthlySummary();
    }

    private void updateNavigationHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView textViewUserName = headerView.findViewById(R.id.textViewUserName);
        TextView textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            textViewUserName.setText(displayName != null ? displayName : "Expensify User");
            textViewUserEmail.setText(currentUser.getEmail());
        }
    }

    private void loadMonthlySummary() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        // Get current month's start and end dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endOfMonth = calendar.getTimeInMillis();

        // Query Firestore for current month's expenses
        db.collection("users")
                .document(currentUser.getUid())
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalAmount = 0;
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Double amount = queryDocumentSnapshots.getDocuments().get(i).getDouble("amount");
                        if (amount != null) {
                            totalAmount += amount;
                        }
                    }
                    tvMonthlySpent.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalAmount));
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                        "Failed to load monthly summary", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home screen
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
            // Launch profile activity when implemented
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, ExpenseHistoryActivity.class));
        } else if (id == R.id.nav_calculator) {
            startActivity(new Intent(this, FinancialCalculatorActivity.class));
        } else if (id == R.id.nav_logout) {
            // Logout user
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish(); // Prevent going back to MainActivity
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh monthly summary data when returning to the activity
        loadMonthlySummary();
    }
}