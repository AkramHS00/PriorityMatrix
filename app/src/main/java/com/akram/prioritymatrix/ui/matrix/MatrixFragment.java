package com.akram.prioritymatrix.ui.matrix;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MatrixFragment extends Fragment {

    ConstraintLayout matrixView;
    TaskViewModel taskViewModel;
    User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.VISIBLE){
            navBar.setVisibility(View.GONE);
        }

        taskViewModel =
                new ViewModelProvider(this).get(TaskViewModel.class);



        return inflater.inflate(R.layout.fragment_matrix, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((MainActivity) getActivity()).getCurrentUser();
        matrixView = getView().findViewById(R.id.matrixView);

        if( currentUser != null){

            taskViewModel.getOrderedUserTasks(currentUser.getUserName().toString()).observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {
                    for (Task t: tasks) {
                        Log.i("AHS", "Matrix " + t.getTitle().toString());
                        TextView newTextView = new TextView(getActivity());
                        newTextView.setText(t.getTitle());
                        matrixView.addView(newTextView);

                    }
                }
            });

        }



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.GONE){
            navBar.setVisibility(View.VISIBLE);
        }
    }
}