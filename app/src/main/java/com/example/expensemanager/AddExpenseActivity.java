package com.example.expensemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.expensemanager.databinding.ActivityAddExpenseBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ActivityAddExpenseBinding binding;
    private String type;
    private String items;
    private ExpenseModel expenseModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Spinner category = findViewById(R.id.category);
        category.setOnItemSelectedListener(this);

        type=getIntent().getStringExtra("type");
        expenseModel=(ExpenseModel) getIntent().getSerializableExtra("model");

        if (type==null){
            type=expenseModel.getType();
            binding.amount.setText(String.valueOf(expenseModel.getAmount()));
           //binding.category.setText(expenseModel.getCategory());
            binding.note.setText(expenseModel.getNote());
        }

        if (type.equals("Income")){
            binding.incomeRadio.setChecked(true);
        }else {
            binding.expenseRadio.setChecked(true);
        }

        binding.incomeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type="Income";
            }
        });
        binding.expenseRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type="Expense";
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        if (expenseModel==null){
            menuInflater.inflate(R.menu.add_menu,menu);
        }else {
            menuInflater.inflate(R.menu.update_menu,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.saveExpense){
            if (type!=null){
                createExpense();
            }else {
                updateExpense();
            }
            return true;
        }
        if (id==R.id.deleteExpense){
            deleteExpense();
        }
        return false;
    }

    private void deleteExpense() {
        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseModel.getExpenseId())
                .delete();
        finish();
    }

    private void createExpense() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        String expenseId= UUID.randomUUID().toString();
        String amount=binding.amount.getText().toString();
        String note=binding.note.getText().toString();
//         String category = binding.items.getText().toString();
        expenseModel.setTime(currentDate.getTime());

        boolean incomeChecked=binding.incomeRadio.isChecked();
        if (incomeChecked){
            type="Income";
        }else {
            type="Expense";
        }

        if (amount.trim().length()==0){
            binding.amount.setError("Empty");
            return;
        }
        ExpenseModel expenseModel=new ExpenseModel(expenseId,note,items,type,Long.parseLong(amount), Calendar.getInstance().getTimeInMillis(),
                FirebaseAuth.getInstance().getUid());

        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseId)
                .set(expenseModel);
        finish();

    }
    private void updateExpense() {

        String expenseId= expenseModel.getExpenseId();
        String amount=binding.amount.getText().toString();
        String note=binding.note.getText().toString();
//       String category=binding.category.getText().toString();
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Set the current date to the model
        expenseModel.setTime(currentDate.getTime());
        boolean incomeChecked=binding.incomeRadio.isChecked();
        if (incomeChecked){
            type="Income";
        }else {
            type="Expense";
        }

        if (amount.trim().length()==0){
            binding.amount.setError("Empty");
            return;
        }
        ExpenseModel model=new ExpenseModel(expenseId,note,items,type,Long.parseLong(amount),expenseModel.getTime() ,
                FirebaseAuth.getInstance().getUid());

        FirebaseFirestore
                .getInstance()
                .collection("expenses")
                .document(expenseId)
                .set(model);
        finish();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        items = adapterView.getItemAtPosition(i).toString();
        // Toast.makeText(adapterView.getContext(),"Selected "+item, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}