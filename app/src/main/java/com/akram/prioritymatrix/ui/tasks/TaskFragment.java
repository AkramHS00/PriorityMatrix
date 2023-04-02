package com.akram.prioritymatrix.ui.tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskFragment extends Fragment {

    private TaskAdapter adapter;

    TaskViewModel taskViewModel;
    private User currentUser;

    private List<Task> userTasks = new ArrayList<>();

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

                currentUser = ((MainActivity) getActivity()).getCurrentUser();
                if (currentUser == null) {
                    menuLogoutIcon.setVisible(false);
                    menuLoginIcon.setVisible(true);
                    menuWelcomeIcon.setVisible(false);
                } else {
                    menuLogoutIcon.setVisible(true);
                    menuLoginIcon.setVisible(false);
                    menuWelcomeIcon.setVisible(true);
                    menuWelcomeIcon.setTitle("Welcome, " + currentUser.getName());
                }

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.logout) {
                    ((MainActivity) getActivity()).logout();
                    NavHostFragment.findNavController(TaskFragment.this)
                            .navigate(R.id.action_navigation_home_to_loginFragment);
                    Toast.makeText(getActivity(), "Logout button was pressed.", Toast.LENGTH_SHORT).show();
                } else if (menuItem.getItemId() == R.id.login) {
                    NavHostFragment.findNavController(TaskFragment.this)
                            .navigate(R.id.action_navigation_home_to_loginFragment);
                    Toast.makeText(getActivity(), "Login button was pressed.", Toast.LENGTH_SHORT).show();
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

        //Temporary button to navigate to the matrix screen
        FloatingActionButton tempFab = getView().findViewById(R.id.tempMatrixFab);
        tempFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TaskFragment.this)
                        .navigate(R.id.action_navigation_home_to_navigation_matrix);
            }
        });

        //Temporary button to set all tasks to incomplete again
        FloatingActionButton tempArchiveFab = getView().findViewById(R.id.tempArchiveFab);
        tempArchiveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser != null) {
                    for (Task t : userTasks) {
                        if (t.getComplete() == true) {
                            t.setComplete(false);
                            taskViewModel.updateTask(t);
                        }
                    }

                }

            }
        });


        RecyclerView recyclerView = getView().findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        final TaskAdapter adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        if (currentUser != null) {

            taskViewModel.getOrderedUserTasks(currentUser.getUserName().toString()).observe(getActivity(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {
                    userTasks = tasks;
                    List<Task> outstandingTasks = new ArrayList<>();
                    for (Task t : tasks) {
                        if (t.getComplete() == false) {
                            outstandingTasks.add(t);
                        }
                    }
                    Log.i("AHS", "Big task updated!");
                    //adapter.setTasks(outstandingTasks);

                    List<Task> prioritisedTasks = prioritiseTasks(outstandingTasks);
                    adapter.setTasks(prioritisedTasks);
                }
            });
        }

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
                Log.i("AHS", "Task completed! " + task.getTitle());
                task.setComplete(true);
                taskViewModel.updateTask(task);
                Toast.makeText(getActivity(), "Task archived.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private List<Task> prioritiseTasks(List<Task> tasks) {
        List<String> matrixCategoryOrder = Arrays.asList("Do", "Schedule", "Delegate", "Delete"); //This is the order we will prioritise our tasks by (same as elsewhere in app)
        List<Task> prioritisedTasks = tasks;
        Collections.sort(prioritisedTasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {

                int deadlineImportance = Integer.compare(Integer.valueOf(t1.getDeadlineDate()), Integer.valueOf(t2.getDeadlineDate()));
                Log.i("AHS", "Deadline importance of t1: " + t1.getDeadlineDate() + " t2: " + t2.getDeadlineDate() + " = " + deadlineImportance);

                if (deadlineImportance == 0){
                    //Check which quadrant they belong to and compare
                    int t1Category = matrixCategoryOrder.indexOf(t1.getCategory());
                    int t2Category = matrixCategoryOrder.indexOf(t2.getCategory());

                    //If this returns 0 they are in the same category and we therefore need to prioritise by its importance
                    int categoryImportance = Integer.compare(t1Category, t2Category);

                    if (categoryImportance == 0){
                        if (Float.compare(t1.getPosY(), t2.getPosY()) != 0){
                            //If they are the same category, the task with greater importance is prioritised
                            return Float.compare(t1.getPosY(), t2.getPosY());
                        } else {
                            //If category and Importance is the same, tasks are prioritised by urgency
                            // * by -1 as X values increases as we move to the right of the screen
                            return Float.compare(t1.getPosX() * -1, t2.getPosX() * -1);
                        }

                    }
                    return categoryImportance; //This will return if the categories are different
                }

                return deadlineImportance;
            }
        });

        return prioritisedTasks;
    }


}