package com.akram.prioritymatrix.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class AddTaskFragment extends Fragment {

    /*User currentUser;

    EditText editTitle, editDescription, editRating;
    Switch switchDeadline, switchReminder;
    TextView textDeadlineDate, textDeadlineTime, textReminderDate, textReminderTime;
    Button saveBtn;

    String[] categories = {"Do", "Schedule", "Delegate", "Delete"};
    List<String> projectStrings = new ArrayList<String>();

    List<Project> userProjects;
    int projectId;

    AutoCompleteTextView categoryAutoComplete;
    AutoCompleteTextView projectAutoComplete;

    ArrayAdapter<String> adapterCategories;
    ArrayAdapter<String> adapterProject;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AddTaskViewModel addTaskViewModel = new ViewModelProvider(this).get(AddTaskViewModel.class);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();

        addTaskViewModel.getUserProjects(currentUser.getUserName()).observe(getActivity(), new Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                for (Project p: projects) {
                    projectStrings.add(p.getName().toString());
                }
                userProjects = projects;
            }
        });

        editTitle = getView().findViewById(R.id.editTitle);
        editDescription = getView().findViewById(R.id.editDescription);
        editRating = getView().findViewById(R.id.editRating);

        switchDeadline = getView().findViewById(R.id.switchDeadline);
        switchReminder = getView().findViewById(R.id.switchReminder);

        textDeadlineDate = getView().findViewById(R.id.textDeadlineDate);
        textDeadlineTime = getView().findViewById(R.id.textDeadlineTime);
        textReminderDate = getView().findViewById(R.id.textReminderDate);
        textReminderTime = getView().findViewById(R.id.textReminderTime);

        categoryAutoComplete = getView().findViewById(R.id.categoryAutoComplete);
        categoryAutoComplete.setText("Do");

        projectAutoComplete = getView().findViewById(R.id.projectAutoComplete);
        projectAutoComplete.setText("Default");

        saveBtn = getView().findViewById(R.id.saveBtn);

        //Show date and times when switches are checked
        if (!switchDeadline.isChecked()){
            textDeadlineDate.setVisibility(View.GONE);
            textDeadlineTime.setVisibility(View.GONE);
        }
        if (!switchReminder.isChecked()){
            textReminderDate.setVisibility(View.GONE);
            textReminderTime.setVisibility(View.GONE);
        }

        //Show the date and time fields if deadline/reminder is selected to be true
        switchDeadline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    textDeadlineDate.setVisibility(View.VISIBLE);
                    textDeadlineTime.setVisibility(View.VISIBLE);
                } else {
                    textDeadlineDate.setVisibility(View.GONE);
                    textDeadlineTime.setVisibility(View.GONE);
                }
            }
        });

        switchReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    textReminderDate.setVisibility(View.VISIBLE);
                    textReminderTime.setVisibility(View.VISIBLE);
                } else {
                    textReminderDate.setVisibility(View.GONE);
                    textReminderTime.setVisibility(View.GONE);
                }
            }
        });

        //Open date dialogs when selecting date text view
        textDeadlineDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDateDialog(textDeadlineDate);

            }
        });

        textReminderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDateDialog(textReminderDate);

            }
        });

        //Open time dialogs when selecting time textview
        textDeadlineTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openTimeDialog(textDeadlineTime);

            }
        });

        textReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openTimeDialog(textReminderTime);

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String deadlineDate = textDeadlineDate.getText().toString().replaceAll("/", "");
                String deadlineTime = textDeadlineTime.getText().toString().replaceAll(":", "");

                String reminderDate = textReminderDate.getText().toString().replaceAll("/", "");
                String reminderTime = textReminderTime.getText().toString().replaceAll(":", "");




                if (editTitle.getText().toString().trim().isEmpty()  ||
                    editDescription.getText().toString().trim().isEmpty()  ||
                        editRating.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
                } else {
                    for (Project p: userProjects) {
                        if (p.getName().toString().equals(projectAutoComplete.getText().toString())){
                            projectId = p.getId();
                        }
                    }
                    Task newTask = new Task(currentUser.getName().toString(), editTitle.getText().toString(), editDescription.getText().toString(),
                            Integer.valueOf(editRating.getText().toString()), switchDeadline.isChecked(), deadlineDate,
                            deadlineTime, switchReminder.isChecked(), reminderDate, reminderTime, false, categoryAutoComplete.getText().toString(),
                            projectId);
//projectAutoComplete.getText().toString()
                    addTaskViewModel.insertTask(newTask);
                    Toast.makeText(getActivity(), "New task added successfully.", Toast.LENGTH_SHORT).show();
                    Log.i("AHS", "New task:  Username: " + currentUser.getName() + " Title: " + editTitle.getText().toString() +
                            " Description: " + editDescription.getText().toString() + " Rating: " + editRating.getText().toString() +
                            " Deadline switch: " + switchDeadline.isChecked() + " Deadline date: " + textDeadlineDate.getText().toString() +
                            " Deadline time: " + textDeadlineTime.getText().toString() + " Reminder switch: " + switchReminder.isChecked() +
                            " Reminder date: " + textReminderDate.getText().toString() + " Reminder time: " + textReminderTime.getText().toString() );

                    NavHostFragment.findNavController(AddTaskFragment.this)
                            .navigate(R.id.action_addTaskFragment_to_navigation_home);
                }

            }
        });

        //Setup adapter dropdown list
        adapterCategories = new ArrayAdapter<String>(getActivity(), R.layout.list_categories, categories);
        categoryAutoComplete.setAdapter(adapterCategories);


        categoryAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String category = adapterView.getItemAtPosition(i).toString();
                //Toast.makeText(getActivity(), "Category = " + categoryAutoComplete.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        //Setup project dropdown list
        adapterProject = new ArrayAdapter<String>(getActivity(), R.layout.list_categories, projectStrings);
        projectAutoComplete.setAdapter(adapterProject);

        projectAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String project = adapterView.getItemAtPosition(i).toString();
            }
        });

    }

    private void openDateDialog(TextView text){

        DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                int correctMonth = month + 1;
                text.setText(checkDigit(day) + "/" + checkDigit(correctMonth) + "/" + checkDigit(year));

            }
        }, 2023, 0, 0);

        dateDialog.show();

    }

    private void openTimeDialog(TextView text){

        TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                text.setText(checkDigit(hour) + ":" + checkDigit(minute));
            }
        }, 10, 10, true);

        timeDialog.show();
    }

    public String checkDigit(int number) { //https://stackoverflow.com/questions/38191945/android-timepicker-dialog-returns-no-preceding-zeros/38196212
        return number <= 9 ? "0" + number : String.valueOf(number);
    }*/


}