package com.akram.prioritymatrix.ui.lists;

import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.TaskAdapter;
import com.akram.prioritymatrix.ui.tasks.TaskFragment;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;

import java.util.List;

public class ProjectTaskFragment extends Fragment {

    User currentUser;
    ProjectTaskViewModel projectTaskViewModel;
    Project currentProject;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        projectTaskViewModel =
                new ViewModelProvider(this).get(ProjectTaskViewModel.class);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();

        Bundle bundle = getArguments();
        currentProject = (Project) bundle.getSerializable("Project");

        if(currentProject != null){
            Log.i("AHS", currentProject.getName().toString());
            ((MainActivity) getActivity()).setActionBarTitle(currentProject.getName());

        } else {
            Log.i("AHS", "Current project is null");
        }


        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.project_task_menu, menu);

            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.editProject) {
                    if(currentProject != null){
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Project", currentProject);
                        NavHostFragment.findNavController(ProjectTaskFragment.this)
                                .navigate(R.id.action_projectTaskFragment_to_addProjectFragment, bundle);

                    }

                }


                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);





        return inflater.inflate(R.layout.fragment_project_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);






        RecyclerView recyclerView = getView().findViewById(R.id.project_task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        final TaskAdapter adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        if( currentUser != null && currentProject != null){

            projectTaskViewModel.getProjectTasks(currentProject.getId()).observe(getActivity(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {
                    for (Task t: tasks) {
                        Log.i("AHS", t.getTitle().toString());
                    }
                    adapter.setTasks(tasks);
                }
            });
        }

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Task", task);
                NavHostFragment.findNavController(ProjectTaskFragment.this)
                        .navigate(R.id.action_projectTaskFragment_to_navigation_detail_task, bundle);
            }

            @Override
            public void completeTask(Task task) {
                Log.i("AHS", "Task completed! " + task.getTitle());
                task.setComplete(true);
                projectTaskViewModel.updateTask(task);
                Toast.makeText(getActivity(), "Task archived.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}