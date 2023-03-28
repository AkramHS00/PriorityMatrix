package com.akram.prioritymatrix.ui.matrix;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.OnDragTouchListener;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.ui.tasks.TaskFragment;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
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

    HashMap<TextView, Task> textViewTasks = new HashMap<TextView, Task>();
    ArrayList<Task> tasksToUpdate = new ArrayList<Task>();

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
        }
    };



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

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

            taskViewModel.getOutstandingUserTasks(currentUser.getUserName().toString()).observe(getViewLifecycleOwner(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {
                    //Clear hashmap of all entries when tasks are updated
                    textViewTasks.clear();
                    for (Task t: tasks) {
                        //Log.i("AHS", "Matrix " + t.getTitle().toString());

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
                    taskViewModel.updateTask(t);
                }
                NavHostFragment.findNavController(MatrixFragment.this)
                        .navigate(R.id.action_navigation_matrix_to_navigation_home);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}