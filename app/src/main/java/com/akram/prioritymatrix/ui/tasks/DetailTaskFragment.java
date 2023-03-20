package com.akram.prioritymatrix.ui.tasks;

import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

public class DetailTaskFragment extends Fragment {

    private Task currentTask;
    private User currentUser;
    private DetailTaskViewModel detailTaskViewModel;

    private EditText editTitle, editDescription, editRating;
    private Switch switchDeadline, switchReminder;
    private TextView textDeadlineDate, textDeadlineTime, textReminderDate, textReminderTime;
    private Button saveBtn;

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

        detailTaskViewModel = new ViewModelProvider(this).get(DetailTaskViewModel.class);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();





        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.detail_task_menu, menu);
                MenuItem menuDeleteIcon = menu.findItem(R.id.deleteTask);

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.deleteTask) {

                    detailTaskViewModel.deleteTask(currentTask);

                    NavHostFragment.findNavController(DetailTaskFragment.this)
                            .navigate(R.id.action_detailTaskFragment_to_navigation_home);
                    Toast.makeText(getActivity(), "Task deleted.", Toast.LENGTH_SHORT).show();
                }


                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        return inflater.inflate(R.layout.fragment_detail_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        currentTask = (Task) bundle.getSerializable("Task");

        detailTaskViewModel.getUserProjects(currentUser.getUserName()).observe(getViewLifecycleOwner(), new Observer<List<Project>>() {
            @Override
            public void onChanged(List<Project> projects) {
                userProjects = projects;
                Log.i("AHS", "Set user projects");
                for (Project p: projects) {
                    projectStrings.add(p.getName().toString());
                    if (currentTask != null){
                        if (currentTask.getProjectId() == p.getId()){
                            projectAutoComplete = getView().findViewById(R.id.projectAutoComplete);
                            projectAutoComplete.setText(p.getName());
                        }
                    }
                }

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
        projectAutoComplete = getView().findViewById(R.id.projectAutoComplete);

        saveBtn = getView().findViewById(R.id.saveBtn);

        if (currentTask != null){
            editTitle.setText(currentTask.getTitle());
            editDescription.setText(currentTask.getDescription());
            editRating.setText(String.valueOf(currentTask.getRating()));

            switchDeadline.setChecked(currentTask.isAddDeadline());
            switchReminder.setChecked(currentTask.isAddReminder());

            textDeadlineDate.setText(formatDate(currentTask.getDeadlineDate()));
            textDeadlineTime.setText(formatTime(currentTask.getDeadlineTime()));
            textReminderDate.setText(formatDate(currentTask.getReminderDate()));
            textReminderTime.setText(formatTime(currentTask.getReminderTime()));

            categoryAutoComplete.setText(currentTask.getCategory());

        } else {
            Log.i("AHS", "Current task is = null so creating blank task screen.");
            ((MainActivity) getActivity()).setActionBarTitle("New Task");
        }

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

                    if (currentTask != null){
                        newTask.setId(currentTask.getId());
                        detailTaskViewModel.updateTask(newTask);
                        Toast.makeText(getActivity(), "Task updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        detailTaskViewModel.insertTask(newTask);
                        Toast.makeText(getActivity(), "New task added successfully.", Toast.LENGTH_SHORT).show();
                    }


                    NavHostFragment.findNavController(DetailTaskFragment.this)
                            .navigate(R.id.action_detailTaskFragment_to_navigation_home);
                }

            }
        });

        adapterCategories = new ArrayAdapter<String>(getActivity(), R.layout.list_categories, categories);
        categoryAutoComplete.setAdapter(adapterCategories);

        categoryAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String category = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getActivity(), "Category = " + categoryAutoComplete.getText(), Toast.LENGTH_SHORT).show();
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
    }

    private String formatDate(String date){
        String formattedDate = date.substring(0,2) + "/" + date.substring(2,4) + "/" + date.substring(4,8);
        return formattedDate;
    }

    private String formatTime(String time){
        String formattedTime = time.substring(0,2) + ":" + time.substring(2,4) ;
        return formattedTime;
    }
}