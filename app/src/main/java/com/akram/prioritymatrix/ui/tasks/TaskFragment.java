package com.akram.prioritymatrix.ui.tasks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment {

    private TaskAdapter adapter;

    TaskViewModel taskViewModel;
    private User currentUser;

    private List<Task> userTasks = new ArrayList<>();
    private List<Task> prioritisedTasks = new ArrayList<>();
    private List<Task> outstandingTasks = new ArrayList<>();
    private List<Task> completedTasks = new ArrayList<>();
    private List<Task> overdueTasks = new ArrayList<>();

    DateTimeFormatter saveDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter saveTimeFormat = DateTimeFormatter.ofPattern("HHmm");

    private TabLayout tabLayout;

    AlertDialog sortDialog;
    String sortMode = "MatrixDue";

    ConstraintLayout taskLogin;
    TextView taskLogout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        taskViewModel =
                new ViewModelProvider(this).get(TaskViewModel.class);


        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.priority_menu, menu);
                MenuItem menuLogoutIcon = menu.findItem(R.id.logout);
                MenuItem menuLoginIcon = menu.findItem(R.id.login);
                MenuItem menuWelcomeIcon = menu.findItem(R.id.welcomeUser);
                MenuItem menuSearchIcon = menu.findItem(R.id.searchBar);
                MenuItem menuSortIcon = menu.findItem(R.id.sortIcon);

                currentUser = ((MainActivity) getActivity()).getCurrentUser();
                if (currentUser == null) {
                    menuLogoutIcon.setVisible(false);
                    menuLoginIcon.setVisible(true);
                    menuWelcomeIcon.setVisible(false);
                    menuSearchIcon.setVisible(false);
                    menuSortIcon.setVisible(false);
                } else {
                    menuLogoutIcon.setVisible(true);
                    menuLoginIcon.setVisible(false);
                    menuWelcomeIcon.setVisible(true);
                    menuSearchIcon.setVisible(true);
                    menuSortIcon.setVisible(true);
                    menuWelcomeIcon.setTitle("Logged in as: " + currentUser.getName());
                }

                SearchView searchView = (SearchView) menuSearchIcon.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {

                        List<Task> searchTasks = new ArrayList<>();
                        for (Task t: prioritisedTasks){
                            if (t.getTitle().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))){
                                searchTasks.add(t);
                            }
                        }

                        //adapter.setTasks(searchTasks);
                        splitTasks(searchTasks);

                        return true;
                    }
                });


            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.logout) {
                    ((MainActivity) getActivity()).logout();
                    NavHostFragment.findNavController(TaskFragment.this)
                            .navigate(R.id.action_navigation_home_to_loginFragment);
                    //Toast.makeText(getActivity(), "Logout button was pressed.", Toast.LENGTH_SHORT).show();
                } else if (menuItem.getItemId() == R.id.login) {
                    NavHostFragment.findNavController(TaskFragment.this)
                            .navigate(R.id.action_navigation_home_to_loginFragment);
                    //Toast.makeText(getActivity(), "Login button was pressed.", Toast.LENGTH_SHORT).show();
                } else if (menuItem.getItemId() == R.id.sortIcon){
                    sortDialog.show();
                }


                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();
        //taskLogin = getView().findViewById(R.id.taskLogin);
        //taskLogout = getView().findViewById(R.id.taskLogout);

        if (currentUser!= null) {
            //taskLogin.setVisibility(View.VISIBLE);
            //taskLogout.setVisibility(View.GONE);
        } else {
            //taskLogin.setVisibility(View.GONE);
            //taskLogout.setVisibility(View.VISIBLE);
            NavHostFragment.findNavController(TaskFragment.this)
                    .navigate(R.id.action_navigation_home_to_loginFragment);
        }

        tabLayout = getView().findViewById(R.id.tabLayout);

        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if (navBar != null){
            navBar.setVisibility(View.VISIBLE);
        }

        FloatingActionButton addFab = getView().findViewById(R.id.addFab);

        if (currentUser == null) {
            addFab.setVisibility(View.INVISIBLE);
        }

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TaskFragment.this)
                        .navigate(R.id.action_navigation_home_to_detailTaskFragment);
            }
        });

        //Build the delete dialog
        AlertDialog.Builder sortBuilder = new AlertDialog.Builder(getContext());
        View sortDialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.sort_dialog, null);
        ConstraintLayout sortMatrixButton, sortMatrixDueDateButton;

        sortMatrixButton = sortDialogLayout.findViewById(R.id.sortMatrixButton);
        sortMatrixDueDateButton = sortDialogLayout.findViewById(R.id.sortMatrixDueDateButton);

        sortMatrixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("AHS", "Pressed sort matrix button");
                sortMode = "Matrix";
                splitTasks(prioritiseTasks(userTasks));
                sortDialog.dismiss();
            }
        });

        sortMatrixDueDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("AHS", "Pressed sort matrix and due date button");
                sortMode = "MatrixDue";
                splitTasks(prioritiseTasks(userTasks));
                sortDialog.dismiss();
            }
        });

        sortBuilder.setTitle("Sort Tasks");
        sortBuilder.setView(sortDialogLayout);

        sortDialog = sortBuilder.create();

        RecyclerView recyclerView = getView().findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        if (currentUser != null) {

            taskViewModel.getUserTasks(currentUser.getUserName().toString()).observe(getActivity(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {

                    //Clear all task arrays
                    userTasks.clear();
                    completedTasks.clear();
                    outstandingTasks.clear();
                    overdueTasks.clear();

                    //Get all tasks
                    userTasks = tasks;

                    //Prioritise tasks into order
                    prioritisedTasks = prioritiseTasks(userTasks);

                    //Call function to split tasks into tabs
                    splitTasks(prioritisedTasks);

                    //createRepeatingTasks(tasks);

                }
            });
        }

        checkForOverdueTasks(userTasks);
        //updateRepeatingTasks(userTasks);
        createRepeatingTasks(userTasks);

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Task", task);
                NavHostFragment.findNavController(TaskFragment.this)
                        .navigate(R.id.action_navigation_home_to_detailTaskFragment, bundle);
            }

            @Override
            public void completeTask(Task task) {
                if(!task.getComplete()){
                    Log.i("AHS", "Task completed! " + task.getTitle());
                    LocalDate todaysDate = LocalDate.now();
                    task.setCompletionDate(saveDateFormat.format(todaysDate));
                    task.setComplete(true);
                    taskViewModel.updateTask(task);
                    createRepeatingTasks(userTasks);
                    Toast.makeText(getActivity(), "Task completed.", Toast.LENGTH_SHORT).show();
                    //createRepeatingTasks(userTasks);
                }
            }

            @Override
            public void incompleteTask(Task task) {
                if(task.getComplete()){
                    Log.i("AHS", "Task incompleted! " + task.getTitle());
                    LocalDate todaysDate = LocalDate.now();
                    task.setDeadlineDate(saveDateFormat.format(todaysDate));
                    task.setOverDue(false);
                    task.setCompletionDate("");
                    task.setComplete(false);
                    taskViewModel.updateTask(task);
                    createRepeatingTasks(userTasks);
                    Toast.makeText(getActivity(), "Task archived.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Log.i("AHS", "Saved tab selected = " + String.valueOf(taskViewModel.getCurrentTab()));
        tabLayout.selectTab(tabLayout.getTabAt(taskViewModel.getCurrentTab()));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabSelected = tab.getPosition();

                switch (tabSelected){
                    case 0:
                        Log.i("AHS", "Outstanding");
                        adapter.setTasks(outstandingTasks);
                        taskViewModel.setCurrentTab(0);
                        break;
                    case 1:
                        Log.i("AHS", "Completed");
                        adapter.setTasks(completedTasks);
                        taskViewModel.setCurrentTab(1);
                        break;
                    case 2:
                        Log.i("AHS", "Overdue");
                        //Reverse the list to show latest overdues first
                        if (overdueTasks.size() >= 2 && Integer.parseInt(overdueTasks.get(0).getDeadlineDate()) < Integer.parseInt(overdueTasks.get(overdueTasks.size()-1).getDeadlineDate())){
                            Collections.reverse(overdueTasks);
                        }
                        adapter.setTasks(overdueTasks);
                        taskViewModel.setCurrentTab(2);
                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private List<Task> prioritiseTasks(List<Task> tasks) {
        //This is the order we will prioritise our tasks by (same as elsewhere in app)
        List<String> matrixCategoryOrder = Arrays.asList("Do", "Schedule", "Delegate", "Delete");
        List<Task> prioritisedTasks = tasks;
        Collections.sort(prioritisedTasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {

                //Check if we are sorting by due date, if so compare dates, if dates are difference return earliest
                if (sortMode.equals("MatrixDue")){
                    int deadlineImportance = Integer.compare(Integer.valueOf(t1.getDeadlineDate()), Integer.valueOf(t2.getDeadlineDate()));
                    if (deadlineImportance != 0){
                        return deadlineImportance;
                    }
                }

                int t1Category = matrixCategoryOrder.indexOf(t1.getCategory());
                int t2Category = matrixCategoryOrder.indexOf(t2.getCategory());

                //If this returns 0 they are in the same category and we therefore need to prioritise by its importance
                int categoryImportance = Integer.compare(t1Category, t2Category);

                if (categoryImportance == 0){
                    if (Float.compare(t1.getPosY(), t2.getPosY()) != 0){
                        //If they are the same category, the task with greater importance is prioritised
                        return Float.compare(t1.getPosY(), t2.getPosY());
                    } else {
                        //If category and importance is the same, tasks are prioritised by urgency
                        return Float.compare(t1.getPosX(), t2.getPosX());
                    }

                }
                return categoryImportance; //This will return if the categories are different

            }
        });

        return prioritisedTasks;
    }


    private void checkForOverdueTasks(List<Task> tasks){
        for (Task t: tasks){
            LocalDate deadlineDate = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);
            LocalTime deadlineTime = LocalTime.parse(t.getDeadlineTime(), saveTimeFormat);
            LocalDateTime deadlineDateTime = deadlineDate.atTime(deadlineTime);

            if (deadlineDateTime.isBefore(LocalDateTime.now())){
                t.setOverDue(true);
                taskViewModel.updateTask(t);
            }

        }
    }

    private void updateRepeatingTasks(List<Task> tasks){
        for (Task t: tasks){
            LocalDate taskDate = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);
            if ((t.getComplete() || t.isOverDue()) && t.getRepeats() != null && !t.getRepeats().equals("") && !t.getDeadlineDate().equals(saveDateFormat.format(LocalDate.now()))){
                Log.i("AHS", "Task " + t.getTitle() + " is overdue and complete and is a repeater:      " + t.getRepeats());

                String repeatString = t.getRepeats();
                List<String> repeatArray = Arrays.asList(repeatString.split(","));

                LocalDate todaysDate = LocalDate.now();
                LocalDate nearestDate = null;
                boolean dateSet = false;

                LocalDate lastCompleted;
                if (t.getCompletionDate().isEmpty() || t.getCompletionDate().equals("")){
                    lastCompleted = LocalDate.of(2000,6,26);
                } else {
                    lastCompleted = LocalDate.parse(t.getCompletionDate(), saveDateFormat);
                }

                LocalDate oldDeadline = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);


                for (String s: repeatArray){
                    switch (s){
                        case "Daily":
                            Log.i("AHS", "Daily");
                            if (!dateSet){
                                dateSet = true;
                                nearestDate = LocalDate.now();
                            } else {
                                if (LocalDate.now().isBefore(nearestDate)){
                                    nearestDate = LocalDate.now();
                                }
                            }

                        case "Every Monday":
                            Log.i("AHS", "Every Monday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                                }
                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).isBefore(nearestDate)
                                && todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).isAfter(todaysDate)
                                && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                                }
                            }

                        case "Every Tuesday":
                            Log.i("AHS", "Every Tuesday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
                                }
                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).isBefore(nearestDate)
                                        && todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
                                }
                            }

                        case "Every Wednesday":
                            Log.i("AHS", "Every Wednesday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
                                }
                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).isBefore(nearestDate)
                                        && todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
                                }
                            }

                        case "Every Thursday":
                            Log.i("AHS", "Every Thursday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
                                }
                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).isBefore(nearestDate)
                                        && todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
                                }
                            }

                        case "Every Friday":
                            Log.i("AHS", "Every Friday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
                                }
                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).isBefore(nearestDate)
                                        && todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
                                }
                            }

                        case "Every Saturday":
                            Log.i("AHS", "Every Saturday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
                                }
                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).isBefore(nearestDate)
                                        && todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
                                }
                            }

                        case "Every Sunday":
                            Log.i("AHS", "Every Sunday");

                            if (!dateSet){
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).equals(oldDeadline)){
                                    dateSet = true;
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
                                }

                            } else {
                                if (todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).isBefore(nearestDate)
                                        && todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).isAfter(todaysDate)
                                        && !todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).equals(oldDeadline)){
                                    nearestDate = todaysDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
                                }
                            }

                    }


                    String formatNewDate = saveDateFormat.format(nearestDate);
                    t.setDeadlineDate(formatNewDate);
                    t.setComplete(false);
                    t.setOverDue(false);
                    taskViewModel.updateTask(t);


                }

            }
        }
    }

    //This function is used to keep track of repeating tasks and create or update tasks once completed/overdue
    //to bring them back to the user on another day
    private void createRepeatingTasks(List<Task> tasks){

        for (Task t: tasks){

            //Check if the task is complete or overdue and repeating and not completed today
            if ((t.getComplete() || t.isOverDue()) && t.getRepeats() != null && !t.getRepeats().equals("") && t.isOriginal()){

                Log.i("AHS", "Updating task repeater! with task " + t.getTitle());

                //Get previous due date of task
                LocalDate taskDeadline = LocalDate.parse(t.getDeadlineDate(), saveDateFormat);
                //Log.i("AHS", "Task Deadline: " + taskDeadline);

                //Create a hashmap of each repeat option and their values
                HashMap<String, TemporalAdjuster> repeatValues = new HashMap<>();
                LocalDate todaysDate = LocalDate.now();

                //Change from todays date to taskDeadlineDate
                //If task is overdue we will get the next day from today, else we will get the next deadline
                //from the tasks deadline, this is to avoid creating overdue tasks
                LocalDate repeatFromDate;
                if (t.isOverDue()){
                    repeatFromDate = LocalDate.now();
                } else {
                    repeatFromDate = taskDeadline;
                }
                repeatValues.put("Every Monday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
                repeatValues.put("Every Tuesday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)));
                repeatValues.put("Every Wednesday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY)));
                repeatValues.put("Every Thursday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)));
                repeatValues.put("Every Friday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)));
                repeatValues.put("Every Saturday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)));
                repeatValues.put("Every Sunday",repeatFromDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY)));


                //Create an arraylist of all the repeats selected for the task
                String repeatString = t.getRepeats();
                List<String> repeatArray = Arrays.asList(repeatString.split(","));

                //Save the key values for possible dates here
                List<LocalDate> repeatDates = new ArrayList<>();

                //Loop through and get dates for each possible repeat date
                for (String s: repeatArray){
                    //Log.i("AHS", "Repeat Array: " + s);
                    if (repeatValues.containsKey(s)){
                        repeatDates.add((LocalDate) repeatValues.get(s));
                    }
                }

                //Get the nearest repeat date that is after the last completed date
                LocalDate targetDate = null;
                for (LocalDate l: repeatDates){
                    //Log.i("AHS", "RepeatDates: " + l);
                    if (targetDate == null && l.isAfter(taskDeadline)){
                        //Log.i("AHS", "Target date is null so setting target date to: " + l);
                        targetDate = l;
                    } else if (targetDate != null && l.isBefore(targetDate) && l.isAfter(taskDeadline)){
                        //Log.i("AHS", "Target date is not null so setting target date to: " + l);
                        targetDate = l;
                    } else {
                        //Log.i("AHS", "Same date and target date is null");
                    }
                }

                //Get task ready and update
                //t.setDeadlineDate(saveDateFormat.format(targetDate));
                //t.setComplete(false);
                //t.setOverDue(false);
                t.setOriginal(false);
                taskViewModel.updateTask(t);


                Task newTask = new Task(currentUser.getUserName(), t.getTitle(),
                        t.getDescription(), t.isAddDeadline(),
                        saveDateFormat.format(targetDate),
                        t.getDeadlineTime(), t.isAddReminder(),
                        t.getReminderDate(), t.getReminderTime(),
                        false, t.getCategory(),
                        t.getProjectId(), t.getPosX(), t.getPosY(), t.getReminders(), t.getRepeats(),
                        false, "", true);

                taskViewModel.insertTask(newTask);

            }
        }
    }

    private void splitTasks(List<Task> tasks){
        completedTasks.clear();
        outstandingTasks.clear();
        overdueTasks.clear();

        //Split prioritised tasks into their respective lists
        for (Task t: tasks){
            if (t.getComplete()){
                completedTasks.add(t);
            } else {
                if (!t.isOverDue()){
                    outstandingTasks.add(t);
                } else {
                    overdueTasks.add(t);
                }

            }
        }

        //Set the adapter to display the list of tasks respective to the currently selected tab
        int tabSelected = tabLayout.getSelectedTabPosition();
        switch (tabSelected){
            case 0:
                adapter.setTasks(outstandingTasks);
                break;
            case 1:
                adapter.setTasks(completedTasks);
                break;
            case 2:
                adapter.setTasks(overdueTasks);
                break;
        }
    }
}