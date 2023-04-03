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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailTaskFragment extends Fragment {

    private Task currentTask;
    private User currentUser;
    private DetailTaskViewModel detailTaskViewModel;

    private EditText editTitle, editDescription;
    private Switch switchDeadline, switchReminder;
    private TextView textDeadlineDate, textDeadlineTime, textReminderDate, textReminderTime;
    private Button saveBtn;

    private LocalDate deadlineDate;
    private LocalDate reminderDate;
    private LocalTime deadlineTime;
    private LocalTime reminderTime;

    DateTimeFormatter saveDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter displayDateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    DateTimeFormatter saveTimeFormat = DateTimeFormatter.ofPattern("HHmm");
    DateTimeFormatter displayTimeFormat = DateTimeFormatter.ofPattern("HH:mm");

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

        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.VISIBLE){
            navBar.setVisibility(View.GONE);
        }

        currentUser = ((MainActivity) getActivity()).getCurrentUser();


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
                //Setup project dropdown list
                adapterProject = new ArrayAdapter<String>(getActivity(), R.layout.list_categories, projectStrings);
                projectAutoComplete.setAdapter(adapterProject);

            }
        });


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


        editTitle = getView().findViewById(R.id.editTitle);
        editDescription = getView().findViewById(R.id.editDescription);

        switchDeadline = getView().findViewById(R.id.switchDeadline);
        switchReminder = getView().findViewById(R.id.switchReminder);

        textDeadlineDate = getView().findViewById(R.id.textDeadlineDate);
        textDeadlineTime = getView().findViewById(R.id.textDeadlineTime);
        textReminderDate = getView().findViewById(R.id.textReminderDate);
        textReminderTime = getView().findViewById(R.id.textReminderTime);

        categoryAutoComplete = getView().findViewById(R.id.categoryAutoComplete);
        projectAutoComplete = getView().findViewById(R.id.projectAutoComplete);

        saveBtn = getView().findViewById(R.id.saveBtn);

        deadlineDate = LocalDate.now();
        deadlineTime = LocalTime.now();
        reminderDate = LocalDate.now();
        reminderTime = LocalTime.now();



        if (currentTask != null){
            editTitle.setText(currentTask.getTitle());
            editDescription.setText(currentTask.getDescription());

            switchDeadline.setChecked(currentTask.isAddDeadline());
            switchReminder.setChecked(currentTask.isAddReminder());

            //Convert from database saved format into the display format for the user
            deadlineDate = LocalDate.parse(currentTask.getDeadlineDate(), saveDateFormat);
            reminderDate = LocalDate.parse(currentTask.getReminderDate(), saveDateFormat);
            deadlineTime = LocalTime.parse(currentTask.getDeadlineTime(), saveTimeFormat);
            reminderTime = LocalTime.parse(currentTask.getReminderTime(), saveTimeFormat);

            categoryAutoComplete.setText(currentTask.getCategory());

        } else {
            Log.i("AHS", "Current task is = null so creating blank task screen.");
            ((MainActivity) getActivity()).setActionBarTitle("New Task");
        }

        textDeadlineDate.setText(displayDateFormat.format(deadlineDate));
        textDeadlineTime.setText(displayTimeFormat.format(deadlineTime));
        textReminderDate.setText(displayDateFormat.format(reminderDate));
        textReminderTime.setText(displayTimeFormat.format(reminderTime));

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

                if (editTitle.getText().toString().trim().isEmpty()  ||
                        editDescription.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
                } else {

                    for (Project p: userProjects) {
                        if (p.getName().toString().equals(projectAutoComplete.getText().toString())){
                            projectId = p.getId();
                        }
                    }

                    Task newTask = new Task(currentUser.getName().toString(), editTitle.getText().toString(), editDescription.getText().toString(), switchDeadline.isChecked(),
                            saveDateFormat.format(deadlineDate),
                            saveTimeFormat.format(deadlineTime), switchReminder.isChecked(), saveDateFormat.format(reminderDate), saveTimeFormat.format(reminderTime),
                            false, categoryAutoComplete.getText().toString(),
                            projectId, -1, -1);

                    if (currentTask != null){
                        newTask.setId(currentTask.getId());
                        newTask.setPosX((currentTask.getPosX()));
                        newTask.setPosY((currentTask.getPosY()));
                        detailTaskViewModel.updateTask(newTask);
                        Toast.makeText(getActivity(), "Task updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        detailTaskViewModel.insertTask(newTask);
                        Toast.makeText(getActivity(), "New task added successfully.", Toast.LENGTH_SHORT).show();
                    }


                    //NavHostFragment.findNavController(DetailTaskFragment.this)
                    //        .navigate(R.id.action_detailTaskFragment_to_navigation_home);

                    NavHostFragment.findNavController(DetailTaskFragment.this).popBackStack();
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


        projectAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String project = adapterView.getItemAtPosition(i).toString();
            }
        });

    }

    private void openDateDialog(TextView text){
        //Check whether its the deadline or reminder and then set the default opening values to their values
        LocalDate defaultDate;
        if (text == textDeadlineDate){
            defaultDate = deadlineDate;
        } else {
            defaultDate = reminderDate;
        }
        int defaultYear = defaultDate.getYear();
        int defaultMonth = defaultDate.getMonthValue() -1;
        int defaultDay = defaultDate.getDayOfMonth();


        DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                int correctMonth = month + 1;
                if (text == textDeadlineDate){
                    deadlineDate = LocalDate.of(year, month + 1, day);
                    text.setText(displayDateFormat.format(deadlineDate));
                } else {
                    reminderDate = LocalDate.of(year, month + 1, day);
                    text.setText(displayDateFormat.format(reminderDate));
                }
            }
        }, defaultYear, defaultMonth, defaultDay);

        dateDialog.show();

    }

    private void openTimeDialog(TextView text){

        //Check whether its the deadline or reminder and then set the default opening values to their values
        LocalTime defaultTime;
        if (text == textDeadlineTime){
            defaultTime = deadlineTime;
        } else {
            defaultTime = reminderTime;
        }
        int defaultHour = defaultTime.getHour();
        int defaultMinute = defaultTime.getMinute();

        TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                if (text == textDeadlineTime){
                    deadlineTime = LocalTime.of(hour,minute, 0);
                    text.setText(displayTimeFormat.format(deadlineTime));
                } else {
                    reminderTime = LocalTime.of(hour,minute, 0);
                    text.setText(displayTimeFormat.format(reminderTime));
                }
                //text.setText(checkDigit(hour) + ":" + checkDigit(minute));
            }
        }, defaultHour, defaultMinute, true);

        timeDialog.show();
    }
}