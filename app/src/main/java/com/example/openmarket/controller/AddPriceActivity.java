package com.example.openmarket.controller;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;

import com.example.openmarket.model.PriceRecord;

import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openmarket.model.Commodity;
import com.example.openmarket.R;
import com.example.openmarket.db.Repository;
import com.example.openmarket.utility.Unit;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AddPriceActivity extends AppCompatActivity {
    private TextView commodityError;
    private Spinner spinnerCommodity;

    private Drawable defaultSpinnerBackground;
    private TextView textUnit;
    private TextView editPriceError;
    private EditText editPrice;
    private TextView editDateError;
    private EditText editDate;
    private Button saveButton;
    private LocalDate date;
    private double price;
    private String selectedCommodityName;


    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.activity_price_entry);

        // Get the commodity name passed from the previous activity
        selectedCommodityName = getIntent().getStringExtra("COMMODITY_NAME");

        initViews();
        resetErrors();
        setupSpinnerCommodity();
        setUpSaveButton();
    }

    private void initViews() {
        //load views from activity_price_entry.xml
        commodityError = findViewById(R.id.commodity_error);
        spinnerCommodity = findViewById(R.id.spinner_commodity);
        textUnit = findViewById(R.id.text_unit);
        editDateError = findViewById(R.id.edit_date_error);
        editDate = findViewById(R.id.text_date);
        editPriceError = findViewById(R.id.edit_price_error);
        editPrice = findViewById(R.id.edit_price);
        saveButton = findViewById(R.id.button_save);

        //set default date to current date
        date = LocalDate.now();
        editDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        editDate.setOnClickListener(v -> showDatePicker());

        //temporarily store default background
        defaultSpinnerBackground = spinnerCommodity.getBackground();
    }

    private void setupSpinnerCommodity() {
        List<Commodity> commodities = Repository.getCommodities(this);
        //represents no commodity selected
        if (commodities.get(0).getUnit() != Unit.DEFAULT) {
            commodities.add(0, new Commodity("Select Commodity", Unit.DEFAULT));
        }

        ArrayAdapter<Commodity> adapter = getCommodityArrayAdapter(commodities, this);

        spinnerCommodity.setAdapter(adapter);

        // Pre-select the commodity if one was passed
        if (selectedCommodityName != null) {
            for (int i = 0; i < commodities.size(); i++) {
                if (commodities.get(i).getName().equals(selectedCommodityName)) {
                    spinnerCommodity.setSelection(i);
                    break;
                }
            }
        }

        spinnerCommodity.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Commodity selected = (Commodity) parent.getItemAtPosition(position);
                        textUnit.setText(String.format("%s%s", getString(R.string.unit), selected.getUnit().getAbbreviation()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        textUnit.setText(R.string.unit_e_g_per_kg);
                    }
                }
        );
    }

    @NonNull
    private static ArrayAdapter<Commodity> getCommodityArrayAdapter(List<Commodity> commodities, AddPriceActivity addPriceActivity) {
        ArrayAdapter<Commodity> adapter = new ArrayAdapter<>(
                addPriceActivity,
                android.R.layout.simple_spinner_item,
                commodities
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; //disable selection of default item
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private  void setUpSaveButton() {
        saveButton.setOnClickListener(e -> {
            resetErrors();  //erase error messages and highlights

            //validate input
            if (!validCommodity()) return;

            if (!isValidPrice()) return;

            if (editDate.getText().toString().isEmpty()) {
                date = LocalDate.now();
                editDate.setText(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }

            //collect input and encapsulate data into their respective objects
            Commodity commodity = (Commodity) spinnerCommodity.getSelectedItem();

            PriceRecord priceRecord = new PriceRecord(commodity, price, date);

            Repository.addPrice(this, priceRecord);

            //reset input fields
            editPrice.setText("");
            editDate.setText(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

            //indicate success
            Toast.makeText(this, "Price successfully saved", Toast.LENGTH_SHORT).show();

            // Close activity and return to commodity list
            finish();
        });
    }

    private void showDatePicker() {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue() - 1;
        int day = currentDate.getDayOfMonth();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                    editDate.setText(date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private boolean validCommodity() {
        int pos = spinnerCommodity.getSelectedItemPosition();

        if (pos == 0) {
            commodityError.setText(R.string.please_select_a_commodity);
            commodityError.setVisibility(View.VISIBLE);
            spinnerCommodity.setBackgroundResource(R.drawable.edit_text_error_backgroud);
            spinnerCommodity.requestFocus();
            return false;
        }
        return true;
    }

    private void resetErrors() {
        commodityError.setVisibility(View.GONE);
        spinnerCommodity.setBackground(defaultSpinnerBackground);

        editDateError.setVisibility(View.GONE);
        editDate.setBackgroundResource(R.drawable.edit_text_background);

        editPriceError.setVisibility((View.GONE));
        editPrice.setBackgroundResource(R.drawable.edit_text_background);
    }

    private boolean isValidPrice() {
        String input = editPrice.getText().toString().trim();

        if (input.isEmpty()) {
            editPriceError.setText(R.string.edit_price_error);
            editPriceError.setVisibility(View.VISIBLE);
            editPrice.setBackgroundResource(R.drawable.edit_text_error_backgroud);
            editPrice.requestFocus();
            return false;
        }

        try {
            price = Double.parseDouble(input);

            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            editPriceError.setText((price <= 0 ? "Price must be greater than 0.00" : "Invalid number entered"));
            editPriceError.setVisibility(View.VISIBLE);
            editPrice.setBackgroundResource(R.drawable.edit_text_error_backgroud);
            editPrice.requestFocus();
            return false;
        }
        return true;
    }
}