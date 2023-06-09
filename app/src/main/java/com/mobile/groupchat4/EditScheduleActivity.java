package com.mobile.groupchat4;


import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile.groupchat4.adapters.ColorPickerAdapter;

public class EditScheduleActivity extends AppCompatActivity {

    private EditText titleEt, descriptionEt, startTimeEt, endTimeEt, colorEt;
    private Spinner dayOfWeekSpinner;

    private TextView cancelTv, saveTv;


    private String title;
    private int scheduleId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String color;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Schedule");

        cancelTv = findViewById(R.id.cancelTv);
        saveTv = findViewById(R.id.saveTv);

        titleEt = findViewById(R.id.titleEt);
        startTimeEt = findViewById(R.id.startTimeEt);
        endTimeEt = findViewById(R.id.endTimeEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        colorEt = findViewById(R.id.colorEt);

        initializeDayOfWeekSpinner();

        TimePickerDialog.OnTimeSetListener startTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Update the EditText with the selected time
                String time = String.format("%02d:%02d", hourOfDay, minute);
                startTimeEt.setText(time);
            }
        };

        TimePickerDialog.OnTimeSetListener endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Update the EditText with the selected time
                String time = String.format("%02d:%02d", hourOfDay, minute);
                endTimeEt.setText(time);
            }
        };

        startTimeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EditScheduleActivity.this,
                        startTimeSetListener,
                        0, 0, true);
                timePickerDialog.getWindow().setGravity(Gravity.CENTER);
                timePickerDialog.updateTime(12, 0);
                timePickerDialog.setTitle("Select Start Time");
                timePickerDialog.show();
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));
                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
                timePickerDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#000000"));
                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(startTimeEt);
            }
        });

        endTimeEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EditScheduleActivity.this,
                        endTimeSetListener,
                        0, 0, true);
                timePickerDialog.getWindow().setGravity(Gravity.CENTER);
                timePickerDialog.updateTime(12, 0);
                timePickerDialog.setTitle("Select End Time");
                timePickerDialog.show();
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));
                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
                timePickerDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.parseColor("#000000"));
                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTag(endTimeEt);
            }
        });

        saveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSchedule();
            }
        });

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        scheduleId = (int) intent.getLongExtra("id", -1L);
        title = intent.getStringExtra("title");
        dayOfWeek = intent.getStringExtra("dayOfWeek");
        startTime = intent.getStringExtra("startTime");
        endTime = intent.getStringExtra("endTime");
        color = intent.getStringExtra("color");
        description = intent.getStringExtra("description");

        if (scheduleId != -1) {
            titleEt.setText(title);
            startTimeEt.setText(startTime);
            endTimeEt.setText(endTime);
            colorEt.setText(color);
            descriptionEt.setText(description);

            // Set the selected dayOfWeek in the spinner
            int spinnerPosition = ((ArrayAdapter<String>) dayOfWeekSpinner.getAdapter()).getPosition(dayOfWeek);
            dayOfWeekSpinner.setSelection(spinnerPosition);
        }

        colorEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog();
            }
        });


    }

    private void updateSchedule() {
        TimetableDatabaseHelper dbHelper = new TimetableDatabaseHelper(this);
        String title = titleEt.getText().toString().trim();
        String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();
        String startTime = startTimeEt.getText().toString();
        String endTime = endTimeEt.getText().toString();
        String color = colorEt.getText().toString().trim();
        String description = descriptionEt.getText().toString();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Please enter schedule title...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }

        if(TextUtils.isEmpty(dayOfWeek)){
            Toast.makeText(this, "Please enter day of week...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }

        if(TextUtils.isEmpty(startTime)){
            Toast.makeText(this, "Please enter Start Time...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }

        if(TextUtils.isEmpty(endTime)){
            Toast.makeText(this, "Please enter End Time...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }


        if (scheduleId != -1) {
            int numberOfRowsUpdated = dbHelper.updateSchedule(scheduleId, title, dayOfWeek, startTime, endTime, color, description);

            if (numberOfRowsUpdated > 0) {
                Toast.makeText(this, "Schedule Updated Successfully...", Toast.LENGTH_SHORT).show();
                finish();
                // data updated successfully
            } else {
                Toast.makeText(this, "Failed to update schedule...", Toast.LENGTH_SHORT).show();
                // error occurred while updating data
            }
        } else {
            Toast.makeText(this, "Invalid Schedule ID", Toast.LENGTH_SHORT).show();
        }
    }


    private void initializeDayOfWeekSpinner() {
        //Category Spinner
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        // Set an item selected listener if needed
        dayOfWeekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item as a string
                String selectedItem = parent.getItemAtPosition(position).toString();

                // Set the productCategory variable to the selected item
                dayOfWeek = selectedItem;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void showColorPickerDialog() {
        final int[] colors = {
                Color.parseColor("#F58185"), Color.parseColor("#F07692"), Color.parseColor("#7FBAD0"),
                Color.parseColor("#4993C5"), Color.parseColor("#BCDE71"), Color.parseColor("#8BB876"),
                Color.parseColor("#CAA7DD"), Color.parseColor("#B08CC6")
        };

        ColorPickerAdapter adapter = new ColorPickerAdapter(this, colors);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Color");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedColor = colors[which];
                String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));
                colorEt.setText(hexColor);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}