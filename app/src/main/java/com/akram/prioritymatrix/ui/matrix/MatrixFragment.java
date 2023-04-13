package com.akram.prioritymatrix.ui.matrix;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.OnDragTouchListener;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.DetailTaskFragment;
import com.akram.prioritymatrix.ui.tasks.TaskFragment;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MatrixFragment extends Fragment {

    ConstraintLayout matrixView, matrixOverlay;
    //TaskViewModel taskViewModel;
    MatrixViewModel matrixViewModel;
    User currentUser;

    Button matrixDo;
    Button matrixSchedule;
    Button matrixDelegate;
    Button matrixDelete;

    MenuItem timeScaleBiWeekly;
    MenuItem timeScaleMonthly;
    MenuItem timeScaleAll;
    MenuItem timeScaleDisplay;

    MenuItem cancelFilter;

    float screenWidth;
    float screenHeight;
    float screenCenterX;
    float screenCenterY;

    HashMap<TextView, Task> textViewTasks = new HashMap<TextView, Task>();
    ArrayList<Task> tasksToUpdate = new ArrayList<Task>();

    int date;
    LocalDate currentDate;
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    String timeFilter = "2 Weeks";
    List<Task> allTasks = new ArrayList<>();
    //List<Task> filteredTasks = new ArrayList<>();


    float viewStartX;
    float viewStartY;
    float viewEndX;
    float viewEndY;

    AlertDialog filterDialog;

    AutoCompleteTextView projectAutoComplete;
    ArrayAdapter<String> adapterProject;
    List<Project> userProjects;
    List<String> projectStrings = new ArrayList<String>();
    Project selectedProject;
    String selectedProjectString;

    AutoCompleteTextView timeAutoComplete;
    ArrayAdapter<String> adapterTime;
    List<String> times = new ArrayList<String>();
    String selectedTimeString;
    HashMap<String, Integer> timesHash = new HashMap<String, Integer>();


    OnDragTouchListener.OnDragActionListener onDragActionListener = new OnDragTouchListener.OnDragActionListener() {
        @Override
        public void onDragStart(View view) {

            matrixOverlay.setVisibility(View.VISIBLE);

            Log.i("AHS", "Drag started");
            matrixOverlay.bringToFront();
            view.bringToFront();


            viewStartX = view.getX();
            viewStartY = view.getY();
        }

        @Override
        public void onDragEnd(View view) {

            matrixOverlay.setVisibility(View.GONE);

            float x = view.getX();
            float y = view.getY();
            Log.i("AHS", "Drag ended with view dropped at X: " + x + " Y: " + y);

            float centerX = view.getX() + view.getWidth() / 2;
            float centerY = view.getY() + view.getHeight() / 2;

            Task draggedTask = textViewTasks.get(view);
            draggedTask.setPosX(x);
            draggedTask.setPosY(y);

            int droppedQuadrant = getQuadrant(centerX, centerY);

            switch (droppedQuadrant){
                case 1:
                    Log.i("AHS", "Do");
                    draggedTask.setCategory("Do");
                    break;
                case 2:
                    Log.i("AHS", "Schedule");
                    draggedTask.setCategory("Schedule");
                    break;
                case 3:
                    Log.i("AHS", "Delegate");
                    draggedTask.setCategory("Delegate");
                    break;
                case 4:
                    Log.i("AHS", "Delete");
                    draggedTask.setCategory("Delete");
                    break;

            }

            Log.i("AHS", String.valueOf(droppedQuadrant));
            tasksToUpdate.add(draggedTask);



        }

        @Override
        public void onLongPress(View view) {
            Log.i("AHS", "View was long pressed: " + view.toString());

            float sensitivity = 5f;

            viewEndX = view.getX();
            viewEndY = view.getY();

            //Check to make sure the view hasnt moved, decreases sensitivity of onLongPress
            //Can increase sensitivity by increasing sensitivity value
            if (Math.abs(viewEndX-viewStartX) < sensitivity && Math.abs(viewEndY-viewStartY) < sensitivity){
                Task draggedTask = textViewTasks.get(view);

                Bundle bundle = new Bundle();
                bundle.putSerializable("Task", draggedTask);
                NavHostFragment.findNavController(MatrixFragment.this)
                        .navigate(R.id.action_navigation_matrix_to_navigation_detail_task, bundle);
            }
        }
    };



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.VISIBLE){
            //navBar.setVisibility(View.GONE);
        }

        //taskViewModel =
        //        new ViewModelProvider(this).get(TaskViewModel.class);

        matrixViewModel = new ViewModelProvider(this).get(MatrixViewModel.class);


        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.matrix_menu_new, menu);

                cancelFilter = menu.findItem(R.id.cancelFilter);
                cancelFilter.setVisible(false);

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.filter){
                    filterDialog.show();
                } else if (menuItem.getItemId() == R.id.cancelFilter){
                    selectedProjectString = "All";
                    selectedTimeString = "All";
                    projectAutoComplete.setSelection(0);
                    timeAutoComplete.setSelection(0);
                    plotTasks(filterTasks(allTasks));
                    cancelFilter.setVisible(false);
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return inflater.inflate(R.layout.fragment_matrix, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();
        matrixView = getView().findViewById(R.id.matrixView);

        matrixDo = getView().findViewById(R.id.matrixDo);
        matrixSchedule = getView().findViewById(R.id.matrixSchedule);
        matrixDelegate = getView().findViewById(R.id.matrixDelegate);
        matrixDelete = getView().findViewById(R.id.matrixDelete);

        matrixOverlay = getView().findViewById(R.id.matrixOverlay);
        matrixOverlay.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        String dayFormat = checkDigit(day);
        String monthFormat = checkDigit(month);
        String yearFormat = String.valueOf(year);
        String dateString = dayFormat + "" + monthFormat + "" + yearFormat;
        date = Integer.valueOf(dateString);
        Log.i("AHS", "Date: " + String.valueOf(date));

        currentDate = LocalDate.now();

        //Use view tree observer to get matrix width and height once it has been inflated
        ViewTreeObserver matrixViewTreeObserver = matrixView.getViewTreeObserver();
        matrixViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                screenWidth = matrixView.getWidth();
                screenHeight = matrixView.getHeight();
                screenCenterX = screenWidth / 2;
                screenCenterY = screenHeight / 2;

                matrixViewTreeObserver.removeOnGlobalLayoutListener(this);

            }
        });

        if( currentUser != null){

            matrixViewModel.getUserProjects(currentUser.getUserName()).observe(getViewLifecycleOwner(), new Observer<List<Project>>() {
                @Override
                public void onChanged(List<Project> projects) {
                    userProjects = projects;
                    projectStrings.clear();
                    projectStrings.add("All");
                    for (Project p: projects) {
                        projectStrings.add(p.getName());
                    }


                    //Setup project dropdown list
                    adapterProject = new ArrayAdapter<String>(getActivity(), R.layout.list_categories, projectStrings);
                    projectAutoComplete.setAdapter(adapterProject);
                }
            });



            matrixViewModel.getUserTasks(currentUser.getUserName()).observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {

                    allTasks.clear();

                    for (Task t: tasks){
                        if (!t.getComplete() && !t.isOverDue()){
                            allTasks.add(t);
                        }
                    }

                    plotTasks(filterTasks(allTasks));

                    //allTasks = tasks;
                    /*List<Task> filteredTasks = new ArrayList<>();
                    switch (timeFilter){
                        case "2 Weeks":
                            filteredTasks.clear();
                            for (Task t: allTasks){
                                String taskDate = t.getDeadlineDate();
                                LocalDate taskDateLocal = LocalDate.parse(taskDate,dateFormat);
                                long daysApart = ChronoUnit.DAYS.between(currentDate, taskDateLocal );
                                Log.i("AHS", "Days apart for task: " + t.getTitle() + " : " + String.valueOf(daysApart));
                                if (daysApart <= 14){
                                    filteredTasks.add(t);
                                }
                            }
                            plotTasks(filteredTasks);
                            break;
                        case "1 Month":
                            filteredTasks.clear();
                            for (Task t: allTasks){
                                String taskDate = t.getDeadlineDate();
                                LocalDate taskDateLocal = LocalDate.parse(taskDate,dateFormat);
                                long daysApart = ChronoUnit.DAYS.between(taskDateLocal, currentDate );
                                if (daysApart <= 31){
                                    filteredTasks.add(t);
                                }
                            }
                            plotTasks(filteredTasks);
                            break;
                        case "All":
                            filteredTasks = allTasks;
                            plotTasks(filteredTasks);
                            break;
                        default:
                            Log.i("AHS", "Default was run");
                            break;

                    }*/

                }
            });

        }



        //Build the filter dialog
        AlertDialog.Builder filterBuilder = new AlertDialog.Builder(getContext());
        View filterDialogLayout = LayoutInflater.from(getActivity()).inflate(R.layout.matrix_filter, null);
        filterBuilder.setTitle("Filter tasks");
        filterBuilder.setView(filterDialogLayout);

        projectAutoComplete = filterDialogLayout.findViewById(R.id.matrixProjectAutoComplete);
        projectAutoComplete.setText("All");
        selectedProjectString = "All";

        timeAutoComplete = filterDialogLayout.findViewById(R.id.matrixTimeAutoComplete);
        timeAutoComplete.setText("All");
        selectedTimeString = "All";

        timesHash.put("Today", 0);
        timesHash.put("This week", 7);
        timesHash.put("This Month", 31);
        timesHash.put("All", 0);

        times.clear();
        for (String t: timesHash.keySet()){
            times.add(t);
        }

        //Setup time dropdown list
        adapterTime = new ArrayAdapter<String>(getActivity(), R.layout.list_categories, times);
        timeAutoComplete.setAdapter(adapterTime);

        filterBuilder.setPositiveButton("Filter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filterDialog.dismiss();

                if (!selectedProjectString.equals("All") || !selectedTimeString.equals("All")){
                    cancelFilter.setVisible(true);
                } else {
                    cancelFilter.setVisible(false);
                }

                plotTasks(filterTasks(allTasks));
            }
        });

        filterBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filterDialog.dismiss();
            }
        });

        filterDialog = filterBuilder.create();

        filterDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                //Why does this code make it lose the options
                //projectAutoComplete.setText(selectedProjectString);
                Button completeButton = filterDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (completeButton != null){
                    completeButton.setTextColor(Color.WHITE);
                    completeButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue));
                } else {
                    Log.i("AHS", "FILTER BUTTON IS NULL");
                }
            }
        });

        //Set up project auto complete text box click listener
        projectAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedProjectString = adapterView.getItemAtPosition(i).toString();


            }
        });

        timeAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTimeString = adapterView.getItemAtPosition(i).toString();
            }
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (Task t: tasksToUpdate) {
            //taskViewModel.updateTask(t);
            matrixViewModel.updateTask(t);
        }

        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.GONE){
            navBar.setVisibility(View.VISIBLE);
        }
    }

    private int getQuadrant(float X, float Y){

        int quadrant;

        if (X < screenCenterX && Y < screenCenterY) {
            quadrant = 1; // top-left
        } else if (X >= screenCenterX && Y < screenCenterY) {
            quadrant = 2; // top-right
        } else if (X < screenCenterX && Y >= screenCenterY) {
            quadrant = 3; // bottom-left
        } else {
            quadrant = 4; // bottom-right
        }

        return quadrant;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                Log.i("AHS", "Back arrow pressed");
                for (Task t: tasksToUpdate) {
                    //taskViewModel.updateTask(t);
                    matrixViewModel.updateTask(t);
                }
                NavHostFragment.findNavController(MatrixFragment.this)
                        .navigate(R.id.action_navigation_matrix_to_navigation_home);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String checkDigit(int number) { //https://stackoverflow.com/questions/38191945/android-timepicker-dialog-returns-no-preceding-zeros/38196212
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public void plotTasks(List<Task> tasks){
        //Delete all textviews before plotting new ones
        for(TextView t: textViewTasks.keySet()){
            matrixView.removeView(t);
        }
        //Clear hashmap of all entries when tasks are updated
        textViewTasks.clear();

        for (Task t: tasks) {
            //Log.i("AHS", "Matrix " + t.getTitle().toString());

            TextView newTextView = new TextView(getActivity());
            newTextView.setText(t.getTitle());
            newTextView.setMaxLines(2);
            newTextView.setEllipsize(TextUtils.TruncateAt.END);
            newTextView.setHorizontallyScrolling(false);
            newTextView.setMaxWidth(300);
            newTextView.setMinWidth(50);
            newTextView.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
            newTextView.setPadding(16,16,16,16);
            newTextView.setTextColor(Color.BLACK);

            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(10);
            newTextView.setFilters(filterArray);

            matrixView.addView(newTextView);

            newTextView.setOnTouchListener(new OnDragTouchListener(newTextView, (View) newTextView.getParent(), onDragActionListener));

            //Position task once the layout has been inflated
            ViewTreeObserver textViewTreeObserver = newTextView.getViewTreeObserver();
            textViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = newTextView.getWidth();
                    int height = newTextView.getHeight();

                    if (t.getPosY() == -1 || t.getPosX() == -1){
                        newTextView.setX(screenCenterX - width/2);
                        newTextView.setY(screenCenterY - height/2);
                    } else {
                        newTextView.setX(t.getPosX());
                        newTextView.setY(t.getPosY());
                    }


                    //Unload listener when done
                    newTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            //Add textView to the hashmap with corresponding task
            textViewTasks.put(newTextView, t);

        }
    }

    private List<Task> filterTasks(List<Task> tasks){

        List<Task> filteredByProject = new ArrayList<>();

        //If all is selected we arent filtering project
        if (selectedProjectString.equals("All")){
            Log.i("AHS", "Not filtering projects");
            filteredByProject = tasks;
        } else {
            //Get the project from the selected project string
            Log.i("AHS", "filtering projects");
            for (Project p : userProjects){
                if (p.getName().equals(selectedProjectString)){
                    selectedProject = p;
                }
            }
            Log.i("AHS", "Project is : " + selectedProjectString);

            //Add all tasks belonging to the selected project to the arraylist
            for (Task t: tasks){
                if (t.getProjectId() == selectedProject.getId()){
                    filteredByProject.add(t);
                }
            }
        }

        //If all is selected we arent filtering time
        List<Task> filteredByProjectAndTime = new ArrayList<>();
        if (selectedTimeString.equals("All")){
            Log.i("AHS", "Not filtering time");
            filteredByProjectAndTime = filteredByProject;
        } else {
            //Get selected time frame value
            Log.i("AHS", "filtering time");
            int timeScale = 0;
            for (String t: timesHash.keySet()){
                if (t.equals(selectedTimeString)){
                    timeScale = timesHash.get(t);

                }
            }

            //Filter through all tasks and check if the difference between the deadline date and today is greater
            //than the selected time value
            for (Task t: filteredByProject){
                String taskDate = t.getDeadlineDate();
                LocalDate taskDateLocal = LocalDate.parse(taskDate,dateFormat);
                long daysApart = ChronoUnit.DAYS.between(currentDate, taskDateLocal);
                Log.i("AHS", "Title: " + t.getTitle() + " taskDate: " + taskDate + " Days apart: " +
                        String.valueOf(daysApart) + "TimeScale: " + String.valueOf(timeScale));
                if (daysApart <= timeScale){
                    filteredByProjectAndTime.add(t);
                }
            }


        }

        return filteredByProjectAndTime;
    }
}