package com.example.openmarket.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openmarket.R;
import com.example.openmarket.db.Repository;
import com.example.openmarket.model.Commodity;
import com.example.openmarket.model.PriceRecord;
import com.example.openmarket.utility.Unit;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class  AddCommodityActivity extends AppCompatActivity {

    private EditText editCommodityName;
    private Spinner spinnerUnit;
    private EditText editPrice;
    private EditText textDate;
    private Button buttonSave;
    private TextView editPriceError;
    private TextView editDateError;

    private LocalDate selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_commodity);

        // Initialize views
        editCommodityName = findViewById(R.id.enter_commodity);
        spinnerUnit = findViewById(R.id.spinner_commodity);
        editPrice = findViewById(R.id.edit_price);
        textDate = findViewById(R.id.text_date);
        buttonSave = findViewById(R.id.button_save);
        editPriceError = findViewById(R.id.edit_price_error);
        editDateError = findViewById(R.id.edit_date_error);

        // Setup unit spinner
        setupUnitSpinner();

        // Setup date picker
        setupDatePicker();

        // Setup save button
        buttonSave.setOnClickListener(v -> saveCommodity());
    }

    private void setupUnitSpinner() {
        // Get all unit values
        Unit[] units = Unit.values();
        String[] unitNames = new String[units.length];

        for (int i = 0; i < units.length; i++) {
            // Use the enum name directly (KILOGRAMS, BAGS, etc.)
            unitNames[i] = units[i].name();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                unitNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);
    }

    private void setupDatePicker() {
        textDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddCommodityActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                        textDate.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        editDateError.setVisibility(View.GONE);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void saveCommodity() {
        // Reset error messages
        editPriceError.setVisibility(View.GONE);
        editDateError.setVisibility(View.GONE);

        // Get input values
        String commodityName = editCommodityName.getText().toString().trim();
        String priceString = editPrice.getText().toString().trim();
        String selectedUnitName = spinnerUnit.getSelectedItem().toString();

        // Validate inputs
        boolean isValid = true;

        if (commodityName.isEmpty()) {
            editCommodityName.setError("Commodity name is required");
            isValid = false;
        }

        if (priceString.isEmpty()) {
            editPriceError.setText("Price is required");
            editPriceError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        double price = 0;
        if (!priceString.isEmpty()) {
            try {
                price = Double.parseDouble(priceString);
                if (price <= 0) {
                    editPriceError.setText("Price must be greater than 0");
                    editPriceError.setVisibility(View.VISIBLE);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                editPriceError.setText("Invalid price format");
                editPriceError.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        if (selectedDate == null) {
            editDateError.setText("Date is required");
            editDateError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Convert unit string to Unit enum
        Unit unit = Unit.valueOf(selectedUnitName);

        // Check if commodity already exists
        Commodity existingCommodity = findCommodityByName(commodityName);

        if (existingCommodity != null) {
            // Add price to existing commodity
            PriceRecord priceRecord = new PriceRecord(existingCommodity, price, selectedDate);
            Repository.addPrice(this, priceRecord);
            Toast.makeText(this, "Price added to existing commodity", Toast.LENGTH_SHORT).show();
        } else {
            // Create new commodity and add price
            Commodity newCommodity = new Commodity(commodityName, unit);
            Repository.addCommodity(this, newCommodity);

            PriceRecord priceRecord = new PriceRecord(newCommodity, price, selectedDate);
            Repository.addPrice(this,priceRecord);
            Toast.makeText(this, "New commodity added successfully", Toast.LENGTH_SHORT).show();
        }

        // Clear form
        clearForm();

        // Go back to previous activity
        finish();
    }

    private Commodity findCommodityByName(String name) {
        for (Commodity commodity : Repository.getCommodities(this)) {
            if (commodity.getName().equalsIgnoreCase(name)) {
                return commodity;
            }
        }
        return null;
    }

    private void clearForm() {
        editCommodityName.setText("");
        editPrice.setText("");
        textDate.setText("");
        selectedDate = null;
        spinnerUnit.setSelection(0);
    }
}