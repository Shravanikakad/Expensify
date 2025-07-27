package com.example.expensify;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class FinancialCalculatorActivity extends AppCompatActivity {

    private Spinner calculatorTypeSpinner;
    private EditText input1, input2, input3;
    private TextView label1, label2, label3, resultText;
    private Button calculateButton;
    private TextView resultLabel;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_calculator);

        // Initialize views
        calculatorTypeSpinner = findViewById(R.id.calculatorTypeSpinner);
        input1 = findViewById(R.id.input1);
        input2 = findViewById(R.id.input2);
        input3 = findViewById(R.id.input3);
        label1 = findViewById(R.id.label1);
        label2 = findViewById(R.id.label2);
        label3 = findViewById(R.id.label3);
        calculateButton = findViewById(R.id.calculateButton);
        resultLabel = findViewById(R.id.resultLabel);
        resultText = findViewById(R.id.resultText);

        decimalFormat = new DecimalFormat("#,##0.00");

        // Setup spinner with calculator types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.calculator_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calculatorTypeSpinner.setAdapter(adapter);

        // Set up spinner listener to change input fields
        calculatorTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupInputFields(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setupInputFields(0);
            }
        });

        // Set calculate button click listener
        calculateButton.setOnClickListener(v -> {
            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Perform calculation based on selected calculator type
            int calculatorType = calculatorTypeSpinner.getSelectedItemPosition();
            calculate(calculatorType);
        });
    }

    private void setupInputFields(int calculatorType) {
        // Clear previous inputs and results
        input1.setText("");
        input2.setText("");
        input3.setText("");
        resultText.setText("");

        // Set up input fields based on calculator type
        switch (calculatorType) {
            case 0: // Loan Calculator
                label1.setText("Loan Amount (₹)");
                label2.setText("Interest Rate (% per year)");
                label3.setText("Loan Term (months)");
                resultLabel.setText("Monthly Payment:");
                break;
            case 1: // Savings Goal
                label1.setText("Target Amount (₹)");
                label2.setText("Monthly Contribution (₹)");
                label3.setText("Interest Rate (% per year)");
                resultLabel.setText("Months to Goal:");
                break;
            case 2: // Budget Distribution
                label1.setText("Monthly Income (₹)");
                label2.setText("Essential Expenses (%)");
                label3.setText("Savings Target (%)");
                resultLabel.setText("Budget Breakdown:");
                break;
            case 3: // Return on Investment
                label1.setText("Initial Investment (₹)");
                label2.setText("Current Value (₹)");
                label3.setText("Time Period (years)");
                resultLabel.setText("Annual Return:");
                break;
        }

        // Make all fields visible initially
        input1.setVisibility(View.VISIBLE);
        input2.setVisibility(View.VISIBLE);
        input3.setVisibility(View.VISIBLE);
        label1.setVisibility(View.VISIBLE);
        label2.setVisibility(View.VISIBLE);
        label3.setVisibility(View.VISIBLE);
    }

    private void calculate(int calculatorType) {
        try {
            switch (calculatorType) {
                case 0: // Loan Calculator
                    calculateLoan();
                    break;
                case 1: // Savings Goal
                    calculateSavingsGoal();
                    break;
                case 2: // Budget Distribution
                    calculateBudget();
                    break;
                case 3: // Return on Investment
                    calculateROI();
                    break;
            }
        } catch (NumberFormatException e) {
            resultText.setText("Please enter valid numbers");
        } catch (Exception e) {
            resultText.setText("Error: " + e.getMessage());
        }
    }

    private void calculateLoan() {
        double loanAmount = Double.parseDouble(input1.getText().toString());
        double interestRate = Double.parseDouble(input2.getText().toString()) / 100 / 12; // Monthly interest rate
        int termMonths = Integer.parseInt(input3.getText().toString());

        double monthlyPayment = (loanAmount * interestRate * Math.pow(1 + interestRate, termMonths)) /
                (Math.pow(1 + interestRate, termMonths) - 1);

        resultText.setText("₹" + decimalFormat.format(monthlyPayment) + " per month\n" +
                "Total payment: ₹" + decimalFormat.format(monthlyPayment * termMonths) + "\n" +
                "Total interest: ₹" + decimalFormat.format((monthlyPayment * termMonths) - loanAmount));
    }

    private void calculateSavingsGoal() {
        double targetAmount = Double.parseDouble(input1.getText().toString());
        double monthlyContribution = Double.parseDouble(input2.getText().toString());
        double annualInterestRate = Double.parseDouble(input3.getText().toString()) / 100;
        double monthlyInterestRate = annualInterestRate / 12;

        // Calculate months required to reach target
        double months = Math.log(1 + (targetAmount * monthlyInterestRate / monthlyContribution)) /
                Math.log(1 + monthlyInterestRate);

        int wholeMonths = (int) Math.ceil(months);
        int years = wholeMonths / 12;
        int remainingMonths = wholeMonths % 12;

        StringBuilder result = new StringBuilder();
        result.append("Time to reach goal: ");
        if (years > 0) {
            result.append(years).append(" year");
            if (years > 1) result.append("s");
        }
        if (remainingMonths > 0) {
            if (years > 0) result.append(" and ");
            result.append(remainingMonths).append(" month");
            if (remainingMonths > 1) result.append("s");
        }

        // Calculate total contributions and interest earned
        double totalContributions = monthlyContribution * wholeMonths;
        double futureValue = monthlyContribution * ((Math.pow(1 + monthlyInterestRate, wholeMonths) - 1) / monthlyInterestRate);
        double interestEarned = futureValue - totalContributions;

        result.append("\nTotal contributions: ₹").append(decimalFormat.format(totalContributions));
        result.append("\nInterest earned: ₹").append(decimalFormat.format(interestEarned));

        resultText.setText(result.toString());
    }

    private void calculateBudget() {
        double monthlyIncome = Double.parseDouble(input1.getText().toString());
        double essentialPercent = Double.parseDouble(input2.getText().toString());
        double savingsPercent = Double.parseDouble(input3.getText().toString());

        if (essentialPercent + savingsPercent > 100) {
            resultText.setText("Error: Essential expenses and savings percentages cannot exceed 100%");
            return;
        }

        double essentialAmount = monthlyIncome * essentialPercent / 100;
        double savingsAmount = monthlyIncome * savingsPercent / 100;
        double discretionaryAmount = monthlyIncome - essentialAmount - savingsAmount;
        double discretionaryPercent = 100 - essentialPercent - savingsPercent;

        StringBuilder result = new StringBuilder();
        result.append("Essential expenses: ₹").append(decimalFormat.format(essentialAmount))
                .append(" (").append(essentialPercent).append("%)\n");
        result.append("Savings: ₹").append(decimalFormat.format(savingsAmount))
                .append(" (").append(savingsPercent).append("%)\n");
        result.append("Discretionary: ₹").append(decimalFormat.format(discretionaryAmount))
                .append(" (").append(decimalFormat.format(discretionaryPercent)).append("%)");

        resultText.setText(result.toString());
    }

    private void calculateROI() {
        double initialInvestment = Double.parseDouble(input1.getText().toString());
        double currentValue = Double.parseDouble(input2.getText().toString());
        double years = Double.parseDouble(input3.getText().toString());

        // Calculate compound annual growth rate (CAGR)
        double annualReturn = Math.pow(currentValue / initialInvestment, 1 / years) - 1;
        double totalReturn = (currentValue - initialInvestment) / initialInvestment * 100;

        StringBuilder result = new StringBuilder();
        result.append("Annual return: ").append(decimalFormat.format(annualReturn * 100)).append("%\n");
        result.append("Total return: ").append(decimalFormat.format(totalReturn)).append("%\n");
        result.append("Profit/Loss: ₹").append(decimalFormat.format(currentValue - initialInvestment));

        resultText.setText(result.toString());
    }
}