package com.example.expensemanager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensemanager.R;
//import com.example.expensemanager.databinding.ActivityAddExpenseBinding;
//import android.R;
import com.example.expensemanager.databinding.ActivityAddExpenseBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    ActivityAddExpenseBinding binding;
    private String type;
    private String items;
    private ExpenseModel expenseModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner category = findViewById(R.id.category);
        category.setOnItemSelectedListener(this);

        type = getIntent().getStringExtra("type");
        expenseModel = (ExpenseModel) getIntent().getSerializableExtra("model");

        if (type == null) {
            type = expenseModel.getType();
            binding.amount.setText(String.valueOf(expenseModel.getAmount()));
            binding.note.setText(expenseModel.getNote());
        }

        if (type.equals("Income")) {
            binding.incomeRadio.setChecked(true);
        } else {
            binding.expenseRadio.setChecked(true);
        }

        binding.incomeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "Income";
            }
        });

        binding.expenseRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "Expense";
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if (expenseModel == null) {
            menuInflater.inflate(R.menu.add_menu, menu);
        } else {
            menuInflater.inflate(R.menu.update_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.saveExpense) {
            if (type != null) {
                createOrUpdateExpense(); // Combined the logic for creating and updating expense
            }
            return true;
        }
        if (id == R.id.deleteExpense) {
            deleteExpense();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteExpense() {
        if (expenseModel != null) {
            FirebaseFirestore
                    .getInstance()
                    .collection("expenses")
                    .document(expenseModel.getExpenseId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddExpenseActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddExpenseActivity.this, "Failed to delete expense", Toast.LENGTH_SHORT).show();
                        Log.e("DeleteExpense", "Failed to delete expense", e);
                    });
        }
    }

    private void createOrUpdateExpense() {
        String expenseId;
        if (expenseModel == null) {
            expenseId = UUID.randomUUID().toString();
        } else {
            expenseId = expenseModel.getExpenseId();
        }

        String amount = binding.amount.getText().toString();
        String note = binding.note.getText().toString();

        if (amount.trim().isEmpty()) {
            binding.amount.setError("Empty");
            return;
        }

        long currentTime = Calendar.getInstance().getTimeInMillis();
        ExpenseModel model = new ExpenseModel(
                expenseId,
                note,
                items,
                type,
                Long.parseLong(amount),
                currentTime,
                FirebaseAuth.getInstance().getUid()
        );

        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseId)
                .set(model)
                .addOnSuccessListener(aVoid -> {
                    if (expenseModel == null) {
                        Toast.makeText(AddExpenseActivity.this, "Expense added", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddExpenseActivity.this, "Expense updated", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddExpenseActivity.this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                    Log.e("SaveExpense", "Failed to save expense", e);
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        items = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // No implementation needed
    }
}
