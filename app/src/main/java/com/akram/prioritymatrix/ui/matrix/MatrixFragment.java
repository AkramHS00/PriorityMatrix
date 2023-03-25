package com.akram.prioritymatrix.ui.matrix;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.OnDragTouchListener;
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

    Button matrixDo;
    Button matrixSchedule;
    Button matrixDelegate;
    Button matrixDelete;

    float screenWidth;
    float screenHeight;
    float screenCenterX;
    float screenCenterY;

    OnDragTouchListener.OnDragActionListener onDragActionListener = new OnDragTouchListener.OnDragActionListener() {
        @Override
        public void onDragStart(View view) {

            Log.i("AHS", "Drag started");
            view.bringToFront();
        }

        @Override
        public void onDragEnd(View view) {
            float x = view.getX();
            float y = view.getY();
            Log.i("AHS", "Drag ended with view dropped at X: " + x + " Y: " + y);

            float centerX = view.getX() + view.getWidth() / 2;
            float centerY = view.getY() + view.getHeight() / 2;

            int droppedQuadrant = getQuadrant(centerX, centerY);

            switch (droppedQuadrant){
                case 1:
                    Log.i("AHS", "Do");
                    break;
                case 2:
                    Log.i("AHS", "Schedule");
                    break;
                case 3:
                    Log.i("AHS", "Delegate");
                    break;
                case 4:
                    Log.i("AHS", "Delete");
                    break;

            }

            Log.i("AHS", String.valueOf(droppedQuadrant));
        }

        @Override
        public void onLongPress(View view) {
            Log.i("AHS", "View was long pressed: " + view.toString());
        }
    };



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

        matrixDo = getView().findViewById(R.id.matrixDo);
        matrixSchedule = getView().findViewById(R.id.matrixSchedule);
        matrixDelegate = getView().findViewById(R.id.matrixDelegate);
        matrixDelete = getView().findViewById(R.id.matrixDelete);


        if( currentUser != null){

            taskViewModel.getOrderedUserTasks(currentUser.getUserName().toString()).observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {
                    for (Task t: tasks) {
                        //Log.i("AHS", "Matrix " + t.getTitle().toString());

                        /*String text = t.getTitle();
                        String[] lines = new String[(text.length() + 9) / 10];
                        for (int i = 0; i < lines.length; i++) {
                            int start = i * 10;
                            int end = Math.min(start + 10, text.length());
                            lines[i] = text.substring(start, end);
                        }
                        if (text.length() > 30) {
                            lines[2] = lines[2].substring(0, Math.min(lines[2].length(), 3)) + "...";
                        }
                        String newText = TextUtils.join("\n", lines);*/



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

    private int getQuadrant(float X, float Y){

        int quadrant;

        screenWidth = matrixView.getWidth();
        screenHeight = matrixView.getHeight();
        screenCenterX = screenWidth / 2;
        screenCenterY = screenHeight / 2;

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

}