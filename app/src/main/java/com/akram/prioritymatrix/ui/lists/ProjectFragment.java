package com.akram.prioritymatrix.ui.lists;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Project;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.databinding.FragmentListBinding;
import com.akram.prioritymatrix.ui.tasks.TaskAdapter;
import com.akram.prioritymatrix.ui.tasks.TaskFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ProjectFragment extends Fragment {

    private FragmentListBinding binding;

    private ProjectViewModel projectViewModel;
    private User currentUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        projectViewModel =
                new ViewModelProvider(this).get(ProjectViewModel.class);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();
        FloatingActionButton addProjectFab = getView().findViewById(R.id.addProjectFab);

        if (currentUser == null){
            addProjectFab.setVisibility(View.INVISIBLE);
        }

        addProjectFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ProjectFragment.this)
                        .navigate(R.id.action_navigation_list_to_addProjectFragment);
            }
        });

        RecyclerView recyclerView = getView().findViewById(R.id.project_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        final ProjectAdapter adapter = new ProjectAdapter();
        recyclerView.setAdapter(adapter);

        if( currentUser != null){

            projectViewModel.getUserProjects(currentUser.getUserName().toString()).observe(getActivity(), new Observer<List<Project>>() {
                @Override
                public void onChanged(List<Project> projects) {
                    for (Project p: projects) {
                        Log.i("AHS", p.getName().toString());
                    }
                    adapter.setProjects(projects);
                }
            });
        }

        adapter.setOnItemClickListener(new ProjectAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Project project) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Project", project);
                NavHostFragment.findNavController(ProjectFragment.this)
                        .navigate(R.id.action_navigation_list_to_addProjectFragment, bundle);
            }

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}