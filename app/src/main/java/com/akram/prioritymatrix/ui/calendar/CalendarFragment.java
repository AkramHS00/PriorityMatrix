package com.akram.prioritymatrix.ui.calendar;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;
import static com.kizitonwose.calendar.core.ExtensionsKt.firstDayOfWeekFromLocale;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

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
import com.akram.prioritymatrix.database.Task;
import com.akram.prioritymatrix.database.User;
import com.akram.prioritymatrix.databinding.FragmentCalendarBinding;
import com.akram.prioritymatrix.ui.tasks.TaskAdapter;
import com.akram.prioritymatrix.ui.tasks.TaskFragment;
import com.akram.prioritymatrix.ui.tasks.TaskViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.kizitonwose.calendar.compose.CalendarMonthsKt;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.lang.reflect.Array;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;




public class CalendarFragment extends Fragment {

    CalendarViewModel calendarViewModel;
    private User currentUser;
    private List<Task> userTasks = new ArrayList<>();
    private List<Task> selectedDateTasks = new ArrayList<>();

    private FragmentCalendarBinding binding;
    private CalendarView calendarView;
    private TextView dateSelectedText;
    //private View taskDot;

    private YearMonth currentMonth = YearMonth.now();
    private YearMonth startMonth = currentMonth.minusMonths(100);  // Adjust as needed
    private YearMonth endMonth = currentMonth.plusMonths(100);  // Adjust as needed
    private DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
    private LocalDate selectedDate = null;

    private List<DayOfWeek> daysOfWeek = new ArrayList<>();

    private  DateTimeFormatter sameYearFormatter = DateTimeFormatter.ofPattern("MMMM");
    private DateTimeFormatter differentYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
    private DateTimeFormatter selectedDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy");
    private DateTimeFormatter taskDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private CalendarTaskAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarViewModel =
                new ViewModelProvider(this).get(CalendarViewModel.class);

        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if (navBar != null){
            navBar.setVisibility(View.VISIBLE);
        }

        currentUser = ((MainActivity) getActivity()).getCurrentUser();

        calendarView = getView().findViewById(R.id.calendar);
        dateSelectedText = getView().findViewById(R.id.dateSelectedText);
        //taskDot = getView().findViewById(R.id.taskDot);

        firstDayOfWeek = firstDayOfWeekFromLocale();
        daysOfWeek = daysOfWeek();

        RecyclerView recyclerView = getView().findViewById(R.id.calendar_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new CalendarTaskAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CalendarTaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Task task) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Task", task);
                NavHostFragment.findNavController(CalendarFragment.this)
                        .navigate(R.id.action_navigation_calendar_to_navigation_detail_task, bundle);
            }
        });


        if( currentUser != null){

            calendarViewModel.getUserTasks(currentUser.getUserName().toString()).observe(getActivity(), new Observer<List<Task>>() {
                @Override
                public void onChanged(List<Task> tasks) {
                    userTasks = tasks;
                    for (Task t: userTasks){
                        String taskDate = t.getDeadlineDate();
                        LocalDate localTaskDate = LocalDate.parse(taskDate, taskDateFormatter);
                        calendarView.notifyDateChanged(localTaskDate);
                    }
                }
            });
        }

        // Set the day binder to customize day views
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {

            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                container.day = day;

                if (day.getPosition() == DayPosition.MonthDate){
                    container.textView.setVisibility(View.VISIBLE);

                    if (day.getDate().compareTo(LocalDate.now()) == 0){
                        container.textView.setTextColor(Color.BLUE);
                        container.textView.setBackgroundResource(R.drawable.current_date_circle);
                        //container.taskDot.setVisibility(View.INVISIBLE);
                        //Log.i("AHS", "Test date is : " + taskDateFormatter.format(day.getDate()));

                        container.taskDoDot.setVisibility(View.GONE);
                        container.taskScheduleDot.setVisibility(View.GONE);
                        container.taskDelegateDot.setVisibility(View.GONE);
                        container.taskDeleteDot.setVisibility(View.GONE);

                    } else if (day.getDate().equals(selectedDate) ){ //If equal to selected date
                        container.textView.setTextColor(Color.WHITE);
                        container.textView.setBackgroundResource(R.drawable.selected_date_cricle);
                        //container.taskDot.setVisibility(View.INVISIBLE);

                        container.taskDoDot.setVisibility(View.GONE);
                        container.taskScheduleDot.setVisibility(View.GONE);
                        container.taskDelegateDot.setVisibility(View.GONE);
                        container.taskDeleteDot.setVisibility(View.GONE);

                    } else {
                        //container.textView.setTextColor(Color.BLACK);
                        container.textView.setBackground(null);
                        //container.taskDot.setVisibility(View.INVISIBLE); //Check if events on this date

                        container.taskDoDot.setVisibility(View.GONE);
                        container.taskScheduleDot.setVisibility(View.GONE);
                        container.taskDelegateDot.setVisibility(View.GONE);
                        container.taskDeleteDot.setVisibility(View.GONE);

                        for (Task t: userTasks){
                            //Log.i("AHS", "calender loop : " +t.getTitle());
                            if (t.getDeadlineDate().equals(taskDateFormatter.format(day.getDate()))){
                                //container.taskDot.setVisibility(View.VISIBLE);
                                Log.i("AHS", "Task " + t.getTitle() + " is on " + t.getDeadlineDate() + " with category: " + t.getCategory());
                                switch (t.getCategory()){
                                    case "Do":
                                        container.taskDoDot.setVisibility(View.VISIBLE);
                                        break;
                                    case "Schedule":
                                        container.taskScheduleDot.setVisibility(View.VISIBLE);
                                        break;
                                    case "Delegate":
                                        container.taskDelegateDot.setVisibility(View.VISIBLE);
                                        break;
                                    case "Delete":
                                        container.taskDeleteDot.setVisibility(View.VISIBLE);
                                        break;

                                }
                            }
                        }

                    }

                } else {
                    container.textView.setVisibility(View.INVISIBLE);
                    //container.taskDot.setVisibility(View.INVISIBLE);

                    container.taskDoDot.setVisibility(View.GONE);
                    container.taskScheduleDot.setVisibility(View.GONE);
                    container.taskDelegateDot.setVisibility(View.GONE);
                    container.taskDeleteDot.setVisibility(View.GONE);
                }
            }
        });

        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return null;
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, CalendarMonth calendarMonth) {

                /*if (container.titlesContainer.getTag() == null) {
                    container.titlesContainer.setTag(currentMonth);
                    for (int i = 0; i < container.titlesContainer.getChildCount(); i++) {
                        View child = container.titlesContainer.getChildAt(i);
                        if (child instanceof TextView) {
                            TextView textView = (TextView) child;
                            DayOfWeek dayOfWeek = daysOfWeek.get(i);
                            String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                            textView.setText(title);
                        }
                    }
                }*/


            }
        });

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        ViewGroup titlesContainer = getView().findViewById(R.id.dayTitlesContainer);
        //List<DayOfWeek> daysOfWeekList = new ArrayList<>(Arrays.asList(DayOfWeek.values()));

        for (int i = 0; i < titlesContainer.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) titlesContainer.getChildAt(i);
            //Log.i("AHS", i + ": linear layout");
            for (int p = 0; p < linearLayout.getChildCount(); p++) {
                //Log.i("AHS", p + ": linear layout child");
                if (linearLayout.getChildAt(p) instanceof TextView){
                    TextView textView = (TextView) linearLayout.getChildAt(p);
                    DayOfWeek dayOfWeek = daysOfWeek.get(i);
                    //Log.i("AHS", i + ": days of week = " + String.valueOf(dayOfWeek));
                    String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                    //Log.i("AHS", i + " Day: " + title);
                    textView.setText(title);
                }
            }

        }

        calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
            @Override
            public Unit invoke(CalendarMonth calendarMonth) {
                if (calendarMonth.getYearMonth().getYear() == currentMonth.getYear()){
                    ((MainActivity) getActivity()).setActionBarTitle(sameYearFormatter.format(calendarMonth.getYearMonth()));
                } else {
                    ((MainActivity) getActivity()).setActionBarTitle(differentYearFormatter.format(calendarMonth.getYearMonth()));
                }
                selectDate(calendarMonth.getYearMonth().atDay(1));
                setSelectedDateTasks(calendarMonth.getYearMonth().atDay(1));
                //calendarView.notifyDateChanged(calendarMonth.getYearMonth().atDay(1));

                if (calendarMonth.getYearMonth().toString().equals(currentMonth.toString()) ){
                    selectDate(LocalDate.now());
                    setSelectedDateTasks(LocalDate.now());
                }

                return null;
            }
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class DayViewContainer extends ViewContainer {
        public TextView textView;
        public CalendarDay day;
        public CalendarView calendarView;
        //public View taskDot;
        public View taskDoDot;
        public View taskScheduleDot;
        public View taskDelegateDot;
        public View taskDeleteDot;

        public DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
            //taskDot = view.findViewById(R.id.taskDot);
            taskDoDot = view.findViewById(R.id.taskDoDot);
            taskScheduleDot = view.findViewById(R.id.taskScheduleDot);
            taskDelegateDot = view.findViewById(R.id.taskDelegateDot);
            taskDeleteDot = view.findViewById(R.id.taskDeleteDot);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectDate(day.getDate());
                }
            });
        }
    }

    class MonthViewContainer extends ViewContainer {

        public ViewGroup titlesContainer;

        public MonthViewContainer(@NonNull View view) {
            super(view);

            titlesContainer = (ViewGroup) view.findViewById(R.id.dayTitlesContainer);
        }
    }

    private void selectDate(LocalDate date){
        if (selectedDate != date){
            LocalDate oldDate = selectedDate;
            selectedDate = date;
            //Log.i("AHS", "Selected date is: " + date.getDayOfMonth());
            calendarView.notifyDateChanged(date);
            if(oldDate != null){
                calendarView.notifyDateChanged(oldDate);
            }
            dateSelectedText.setText(selectedDateFormatter.format(date));

            setSelectedDateTasks(date);

        }
    }

    private void setSelectedDateTasks(LocalDate date){
        //Get tasks for new date and send it to adapter
        selectedDateTasks.clear();
        for (Task t: userTasks){
            if (t.getDeadlineDate().equals(taskDateFormatter.format(date))){
                //Log.i("AHS", "Tasks on date = " + t.getTitle());
                selectedDateTasks.add(t);
            }
        }
        adapter.setTasks(selectedDateTasks);
    }
}