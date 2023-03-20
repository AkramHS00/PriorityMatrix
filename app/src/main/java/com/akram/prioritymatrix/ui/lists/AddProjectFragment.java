package com.akram.prioritymatrix.ui.lists;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.AddTaskFragment;
import com.akram.prioritymatrix.ui.tasks.AddTaskViewModel;

public class AddProjectFragment extends Fragment {

    User currentUser;
    Project currentProject;

    EditText editName;
    Button saveProjectBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AddProjectViewModel addProjectViewModel = new ViewModelProvider(this).get(AddProjectViewModel.class);
        currentUser = ((MainActivity) getActivity()).getCurrentUser();

        editName = getView().findViewById(R.id.editName);
        saveProjectBtn = getView().findViewById(R.id.saveProjectBtn);

        Bundle bundle = getArguments();
        currentProject = (Project) bundle.getSerializable("Project");

        if (currentProject != null){
            editName.setText(currentProject.getName().toString());
        }

        saveProjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editName.getText().toString().trim().isEmpty()){
                    Toast.makeText(getActivity(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
                } else {
                    //Check if we are updating project
                    if (currentProject != null){

                        Project newProject = new Project(editName.getText().toString(), currentUser.getName());
                        newProject.setId(currentProject.getId());
                        addProjectViewModel.updateProject(newProject);

                    } else {

                        Project newProject = new Project(editName.getText().toString(), currentUser.getName());
                        addProjectViewModel.insertProject(newProject);

                    }

                    Toast.makeText(getActivity(), "New project created successfully.", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(AddProjectFragment.this)
                            .navigate(R.id.action_addProjectFragment_to_navigation_list);

                }


            }
        });

    }
}