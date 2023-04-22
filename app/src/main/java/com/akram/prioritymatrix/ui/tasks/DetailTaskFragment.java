package com.akram.prioritymatrix.ui.tasks;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailTaskFragment extends Fragment {

    private Task currentTask;
    private User currentUser;
    private DetailTaskViewModel detailTaskViewModel;

    private EditText editTitle, editDescription;
    private Switch switchDeadline, switchReminder;
    private TextView textDeadlineDate, textDeadlineTime, textReminderDate, textReminderTime, textReminderView, textRepeatView;
    private Button saveBtn;

    private ConstraintLayout reminderButton;
    private ConstraintLayout repeatButton;
    private LinearLayout linearLayoutCheckboxes;

    private LocalDate deadlineDate;
    private LocalDate reminderDate;
    private LocalTime deadlineTime;
    private LocalTime reminderTime;
    private LocalTime endTime;

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

    //Variables for the reminder functionality
    private AlertDialog reminderDialog;
    private CheckBox whenDueCheck, tenMinsCheck, halfHourCheck, oneHourCheck, twoHourCheck, fourHourCheck, oneDayCheck;
    private ArrayList<CheckBox> reminderCheckboxes = new ArrayList<>();
    private ArrayList<String> reminderSelectedArray = new ArrayList<>();
    private String reminderSelectedString;
    private boolean boxChecked = false;

    //Variables for the repeat functionality
    private AlertDialog repeatDialog;
    private CheckBox everyDay, everyMonday, everyTuesday, everyWednesday, everyThursday, everyFriday, everySaturday, everySunday;
    private LinearLayout linearLayoutRepeatCheckboxes;
    private ArrayList<CheckBox> repeatCheckboxes = new ArrayList<>();
    private ArrayList<String> repeatSelectedArray = new ArrayList<>();
    private String repeatSelectedString;
    private boolean repeatBoxChecked = false;

    AlertDialog deleteDialog, completeDialog;

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
                //Log.i("AHS", "Set user projects");
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
                MenuItem menuCompleteIcon = menu.findItem(R.id.completeTask);


                if (currentTask == null){
                    menuCompleteIcon.setVisible(false);
                    menuDeleteIcon.setVisible(false);
                }

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.deleteTask) {

                    deleteDialog.show();

                } else if (menuItem.getItemId() == R.id.completeTask){
                    completeDialog.show();
                }


                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        return inflater.inflate(R.layout.fragment_detail_task, container, false);
    }

    @SuppressLint("MissingInflatedId")
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
        textReminderView = getView().findViewById(R.id.textReminderView);
        textRepeatView = getView().findViewById(R.id.textRepeatView);

        categoryAutoComplete = getView().findViewById(R.id.categoryAutoComplete);
        projectAutoComplete = getView().findViewById(R.id.projectAutoComplete);

        saveBtn = getView().findViewById(R.id.saveBtn);

        deadlineDate = LocalDate.now();
        deadlineTime = LocalTime.now();
        reminderDate = LocalDate.now();
        reminderTime = LocalTime.now();
        endTime = LocalTime.of(23,59);



        //Get reminder dialog view references
        View reminderDialogView = getLayoutInflater().inflate(R.layout.reminder_dialog, null);
        linearLayoutCheckboxes = reminderDialogView.findViewById(R.id.linearLayoutCheckboxes);
        whenDueCheck = reminderDialogView.findViewById(R.id.whenDueCheck);
        tenMinsCheck = reminderDialogView.findViewById(R.id.tenMinsCheck);
        halfHourCheck = reminderDialogView.findViewById(R.id.halfHourCheck);
        oneHourCheck = reminderDialogView.findViewById(R.id.oneHourCheck);
        twoHourCheck = reminderDialogView.findViewById(R.id.twoHourCheck);
        fourHourCheck = reminderDialogView.findViewById(R.id.fourHourCheck);
        oneDayCheck = reminderDialogView.findViewById(R.id.oneDayCheck);

        for ( int i = 0; i < linearLayoutCheckboxes.getChildCount(); i++){
            View child = linearLayoutCheckboxes.getChildAt(i);

            if (child instanceof CheckBox){
                reminderCheckboxes.add((CheckBox) child);
            }
        }

        //Build the delete dialog
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getContext());
        deleteBuilder.setTitle("Delete Task");
        deleteBuilder.setMessage("Are you sure you want to delete this task?");

        deleteBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                detailTaskViewModel.deleteTask(currentTask);
                deleteDialog.dismiss();
                NavHostFragment.findNavController(DetailTaskFragment.this).popBackStack();
                Toast.makeText(getActivity(), "Task deleted.", Toast.LENGTH_SHORT).show();
            }
        });

        deleteBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog = deleteBuilder.create();

        deleteDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button deleteButton = deleteDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (deleteButton != null){
                    deleteButton.setTextColor(Color.WHITE);
                    deleteButton.setBackgroundColor(Color.RED);
                } else {
                    Log.i("AHS", "DELETE BUTTON IS NULL");
                }

            }
        });

        //Build the delete dialog
        AlertDialog.Builder completeBuilder = new AlertDialog.Builder(getContext());
        completeBuilder.setTitle("Complete Task");
        completeBuilder.setMessage("Are you sure you want to complete this task?");

        completeBuilder.setPositiveButton("Complete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("AHS", "Task completed! " + currentTask.getTitle());
                completeDialog.dismiss();
                LocalDate todaysDate = LocalDate.now();
                currentTask.setCompletionDate(saveDateFormat.format(todaysDate));
                currentTask.setComplete(true);
                detailTaskViewModel.updateTask(currentTask);
                Toast.makeText(getActivity(), "Task completed.", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(DetailTaskFragment.this).popBackStack();

            }
        });

        completeBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                completeDialog.dismiss();
            }
        });

        completeDialog = completeBuilder.create();

        completeDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button completeButton = completeDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (completeButton != null){
                    completeButton.setTextColor(Color.WHITE);
                    completeButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue));
                } else {
                    Log.i("AHS", "COMPLETE BUTTON IS NULL");
                }

            }
        });


        //Build reminder dialog
        AlertDialog.Builder reminderBuilder = new AlertDialog.Builder(getContext());
        reminderBuilder.setTitle("Select Reminder Times:");
        reminderBuilder.setView(reminderDialogView);

        reminderBuilder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boxChecked = false;
                //Log.i("AHS", "Dialog set button has been clicked");
                textReminderView.setText("Reminder");
                reminderSelectedArray.clear();

                //Append selections to the textview
                for (CheckBox c : reminderCheckboxes){
                    if (c.isChecked()){
                        if (!boxChecked){
                            textReminderView.setText(c.getText());
                        } else {
                            textReminderView.append("\n" + c.getText());
                        }
                        //Log.i("AHS", c.getText() + " is checked");
                        boxChecked = true;
                        reminderSelectedArray.add(String.valueOf(c.getText()));
                    }
                }

                reminderSelectedString = String.join(",", reminderSelectedArray);

                //If no box is checked, reset the textview to just display reminder
                if (!boxChecked) {
                    textReminderView.setText("Reminder");
                }
                reminderDialog.dismiss();
            }
        });

        reminderBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Log.i("AHS", "Dialog close button has been clicked");
                reminderDialog.dismiss();
            }
        });

        reminderDialog = reminderBuilder.create();

        reminderButton = getView().findViewById(R.id.reminderButton);
        reminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminderDialog.show();
            }
        });


        //Get repeat dialog view references
        View repeatDialogView = getLayoutInflater().inflate(R.layout.repeat_dialog, null);
        linearLayoutCheckboxes = repeatDialogView.findViewById(R.id.linearLayoutRepeatCheckboxes);
        everyDay = repeatDialogView.findViewById(R.id.everyDay);
        everyMonday = repeatDialogView.findViewById(R.id.everyMonday);
        everyTuesday = repeatDialogView.findViewById(R.id.everyTuesday);
        everyWednesday = repeatDialogView.findViewById(R.id.everyWednesday);
        everyThursday = repeatDialogView.findViewById(R.id.everyThursday);
        everyFriday = repeatDialogView.findViewById(R.id.everyFriday);
        everySaturday = repeatDialogView.findViewById(R.id.everySaturday);
        everySunday = repeatDialogView.findViewById(R.id.everySunday);

        for ( int i = 0; i < linearLayoutCheckboxes.getChildCount(); i++){
            View child = linearLayoutCheckboxes.getChildAt(i);

            if (child instanceof CheckBox){
                repeatCheckboxes.add((CheckBox) child);
            }
        }

        //Build repeat dialog
        AlertDialog.Builder repeatBuilder = new AlertDialog.Builder(getContext());
        repeatBuilder.setTitle("When would you like to repeat the task:");
        repeatBuilder.setView(repeatDialogView);

        repeatBuilder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Initialise variables
                repeatBoxChecked = false;
                textRepeatView.setText("Repeat");
                repeatSelectedArray.clear();

                //Append selections to the textview
                for (CheckBox c : repeatCheckboxes){
                    if (c.isChecked()){
                        if (!repeatBoxChecked){
                            textRepeatView.setText(c.getText());
                        } else {
                            textRepeatView.append("\n" + c.getText());
                        }
                        //Log.i("AHS", c.getText() + " is checked");
                        repeatBoxChecked = true;
                        repeatSelectedArray.add(String.valueOf(c.getText()));
                    }
                }

                repeatSelectedString = String.join(",", repeatSelectedArray);

                //If no box is checked, reset the textview to just display reminder
                if (!repeatBoxChecked) {
                    textRepeatView.setText("Repeat");
                }

                repeatDialog.dismiss();
            }
        });

        repeatBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                repeatDialog.dismiss();
            }
        });

        repeatDialog = repeatBuilder.create();

        repeatButton = getView().findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repeatDialog.show();
            }
        });



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

            //Initialise the checkboxes and create an arraylist of selected checkboxes from the string saved in the task
            reminderSelectedString = currentTask.getReminders();
            /*if (reminderSelectedString != null){
                reminderSelectedArray = new ArrayList<>( Arrays.asList(reminderSelectedString.split(",")));
                whenDueCheck.setChecked(reminderSelectedArray.contains(String.valueOf(whenDueCheck.getText())));
                tenMinsCheck.setChecked(reminderSelectedArray.contains(String.valueOf(tenMinsCheck.getText())));
                halfHourCheck.setChecked(reminderSelectedArray.contains(String.valueOf(halfHourCheck.getText())));
                oneHourCheck.setChecked(reminderSelectedArray.contains(String.valueOf(oneHourCheck.getText())));
                twoHourCheck.setChecked(reminderSelectedArray.contains(String.valueOf(twoHourCheck.getText())));
                fourHourCheck.setChecked(reminderSelectedArray.contains(String.valueOf(fourHourCheck.getText())));
                oneDayCheck.setChecked(reminderSelectedArray.contains(String.valueOf(oneDayCheck.getText())));
            }*/

            if (reminderSelectedString != null){
                reminderSelectedArray = new ArrayList<>( Arrays.asList(reminderSelectedString.split(",")));
                for (CheckBox c: reminderCheckboxes){
                    c.setChecked(reminderSelectedArray.contains(String.valueOf(c.getText())));
                }
            }


            //Check if the first value in the array is not "" to check if we have any selected checkboxes
            if(reminderSelectedArray != null && !reminderSelectedArray.isEmpty() && reminderSelectedArray.get(0) != ""){
                String reminderText = String.join("\n", reminderSelectedArray);
                textReminderView.setText(reminderText);
                boxChecked = true;
            } else {
                textReminderView.setText("Reminder");
                Log.i("AHS", "Set text view reminder to reminder");
            }


            //Initialise checkboxes for repeating of task
            repeatSelectedString = currentTask.getRepeats();
            if (repeatSelectedString != null){
                repeatSelectedArray = new ArrayList<>(Arrays.asList(repeatSelectedString.split(",")));
                for (CheckBox c: repeatCheckboxes){
                    c.setChecked(repeatSelectedArray.contains(String.valueOf(c.getText())));
                }
            }

            //Check if the first value in the array is not "" to check if we have any selected checkboxes
            if(repeatSelectedArray != null && !repeatSelectedArray.isEmpty() && repeatSelectedArray.get(0) != ""){
                String repeatText = String.join("\n", repeatSelectedArray);
                textRepeatView.setText(repeatText);
                repeatBoxChecked = true;
            } else {
                textRepeatView.setText("Repeat");
            }



        } else {
            //Log.i("AHS", "Current task is = null so creating blank task screen.");
            ((MainActivity) getActivity()).setActionBarTitle("New Task");

            deadlineTime = endTime;
            reminderTime = endTime;
        }

        textDeadlineDate.setText(displayDateFormat.format(deadlineDate));
        textDeadlineTime.setText(displayTimeFormat.format(deadlineTime));
        textReminderDate.setText(displayDateFormat.format(reminderDate));
        textReminderTime.setText(displayTimeFormat.format(reminderTime));



        if (!switchDeadline.isChecked()){
            textDeadlineDate.setVisibility(View.GONE);
            textDeadlineTime.setVisibility(View.GONE);
            reminderButton.setVisibility(View.GONE);
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
                    reminderButton.setVisibility(View.VISIBLE);
                } else {
                    textDeadlineDate.setVisibility(View.GONE);
                    textDeadlineTime.setVisibility(View.GONE);
                    reminderButton.setVisibility(View.GONE);
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
                            projectId, -1, -1, reminderSelectedString, repeatSelectedString, false, "", true);

                    int taskId;

                    if (currentTask != null){
                        newTask.setId(currentTask.getId());
                        //If the user has not edited the tasks category, keep the task poisiton in the matrix
                        if (newTask.getCategory().equals(currentTask.getCategory())){
                            newTask.setPosX((currentTask.getPosX()));
                            newTask.setPosY((currentTask.getPosY()));
                        }
                        detailTaskViewModel.updateTask(newTask);
                        Toast.makeText(getActivity(), "Task updated successfully.", Toast.LENGTH_SHORT).show();

                        taskId = currentTask.getId();
                    } else {
                        long id = detailTaskViewModel.insertTask(newTask);
                        Toast.makeText(getActivity(), "New task added successfully.", Toast.LENGTH_SHORT).show();

                        taskId =(int) id;
                    }

                    /*if(switchReminder.isChecked()){

                        LocalDateTime reminderDateTime = reminderTime.atDate(reminderDate);

                        //Check to ensure the reminder is not set in the past
                        if(!reminderDateTime.isBefore(LocalDateTime.now())){

                            Log.i("AHS", "Reminder date time is: " + reminderDateTime);
                            ZonedDateTime zonedReminderDateTime = reminderDateTime.atZone(ZoneId.systemDefault());
                            Log.i("AHS", "Zoned time is: " + zonedReminderDateTime);
                            long millisUntilReminder = zonedReminderDateTime.toInstant().toEpochMilli();

                            Log.i("AHS", "Timer set in " + millisUntilReminder + "millis");


                            Intent intent = new Intent(getContext(), NotificationBroadcast.class);
                            intent.putExtra("taskTitle", newTask.getTitle());
                            intent.putExtra("intentId", System.currentTimeMillis());

                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                            *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,millisUntilReminder, pendingIntent);
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP,millisUntilReminder, pendingIntent);
                            }*//*

                            alarmManager.setExact(AlarmManager.RTC_WAKEUP,millisUntilReminder , pendingIntent);

                        } else {
                            Log.i("AHS", "Reminder date and time is in the past, no notification set");
                        }


                    }*/

                    if (boxChecked){
                        LocalDateTime deadlineDateTime = deadlineTime.atDate(deadlineDate);
                        ZonedDateTime zonedDeadlineDateTime = deadlineDateTime.atZone(ZoneId.systemDefault());

                        //Create id for pending intent with a starting prefix XXX000 which can be searched for later
                        String pendingItentIdString = String.valueOf(taskId) + String.valueOf(000) + String.valueOf(System.currentTimeMillis()) ;
                        long pendingIntentId = Long.parseLong(pendingItentIdString);
                        //Log.i("AHS", "pendingIntentId = " + String.valueOf(pendingIntentId));

                        cancelOldAlarms(taskId);

                        //This offset is used to make sure the taskIds are different
                        int offset = 1;
                        for (CheckBox c : reminderCheckboxes){
                            if (c.isChecked()){
                                long millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 86400000;
                                //Log.i("AHS", "C IS CHECKED: " + c.getText());

                                switch (String.valueOf(c.getText())){
                                    case "When Due":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli();
                                        //Log.i("AHS", "When due is checked");
                                        break;
                                    case "10 Minutes Before":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 600000;  //600000
                                        //Log.i("AHS", "10 Minutes Before is checked");
                                        break;
                                    case "Half An Hour Before":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 1800000;
                                        //Log.i("AHS", "Half An Hour Before is checked");
                                        break;
                                    case "One Hour Before":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 3600000;
                                        //Log.i("AHS", "One Hour Before is checked");
                                        break;
                                    case "2 Hours Before":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 7200000;
                                        //Log.i("AHS", "2 Hours Before is checked");
                                        break;
                                    case "4 Hours Before":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 14400000;
                                        //Log.i("AHS", "4 Hours Before is checked");
                                        break;
                                    case "The Day Before":
                                        millisUntilReminder = zonedDeadlineDateTime.toInstant().toEpochMilli() - 86400000;
                                        //Log.i("AHS", "The Day Before is checked");
                                        break;

                                }


                                Intent intent = new Intent(getContext(), NotificationBroadcast.class);
                                intent.putExtra("taskTitle", newTask.getTitle());
                                intent.putExtra("intentId", System.currentTimeMillis() + offset);
                                //Log.i("AHS", "Alarm with intent id: " + String.valueOf(System.currentTimeMillis()+offset));

                                String requestCodeString = String.valueOf(offset) + String.valueOf(taskId);
                                Log.i("AHS", "Task id =" + String.valueOf(taskId));
                                Log.i("AHS", "000 =" + String.valueOf(000));
                                Log.i("AHS", "offset =" + String.valueOf(offset));
                                Log.i("AHS", "Request code string =" + requestCodeString);
                                int requestCode = Integer.parseInt(requestCodeString);
                                offset++;


                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                Log.i("AHS", "PENDING INTENT CREATED WITH REQUEST CODE: " + String.valueOf(requestCode));
                                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP,millisUntilReminder , pendingIntent);
                                Toast.makeText(getActivity(), "Notification set, millis = " + millisUntilReminder, Toast.LENGTH_SHORT).show();
                            }
                        }
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
        }, defaultHour + 1, defaultMinute, true);

        timeDialog.show();
    }

    private void cancelOldAlarms(int id){

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), NotificationBroadcast.class);

        for (int i = 1; i<11; i++){
            String requestCodeString = String.valueOf(i) + String.valueOf(id);
            int requestCode = Integer.parseInt(requestCodeString);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), requestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.i("AHS","AKRAM SHAH Pending intent cancelled for: " + String.valueOf(requestCode));
            } else {
                Log.i("AHS","AKRAM SHAH Pending intent is null for: " + String.valueOf(requestCode));
            }
        }

    }
}