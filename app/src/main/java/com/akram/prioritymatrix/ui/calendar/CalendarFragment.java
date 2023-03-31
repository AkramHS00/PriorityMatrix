package com.akram.prioritymatrix.ui.calendar;

import static com.kizitonwose.calendar.core.ExtensionsKt.daysOfWeek;
import static com.kizitonwose.calendar.core.ExtensionsKt.firstDayOfWeekFromLocale;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.databinding.FragmentCalendarBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class DayViewContainer extends ViewContainer {
    public TextView textView;
    public CalendarView calendarView;

    public DayViewContainer(View view) {
        super(view);
        textView = view.findViewById(R.id.calendarDayText);

    }
}

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarView calendarView;

    YearMonth currentMonth = YearMonth.now();
    YearMonth startMonth = currentMonth.minusMonths(100);  // Adjust as needed
    YearMonth endMonth = currentMonth.plusMonths(100);  // Adjust as needed
    DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

    List<DayOfWeek> daysOfWeek = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CalendarView calendarView = getView().findViewById(R.id.calendar);

        firstDayOfWeek = firstDayOfWeekFromLocale();
        daysOfWeek = daysOfWeek();

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
            }
        });

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        ViewGroup titlesContainer = getView().findViewById(R.id.dayTitlesContainer);
        //List<DayOfWeek> daysOfWeekList = new ArrayList<>(Arrays.asList(DayOfWeek.values()));

        for (int i = 0; i < titlesContainer.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) titlesContainer.getChildAt(i);
            Log.i("AHS", i + ": linear layout");
            for (int p = 0; p < linearLayout.getChildCount(); p++) {
                Log.i("AHS", p + ": linear layout child");
                if (linearLayout.getChildAt(p) instanceof TextView){
                    TextView textView = (TextView) linearLayout.getChildAt(p);
                    DayOfWeek dayOfWeek = daysOfWeek.get(i);
                    Log.i("AHS", i + ": days of week = " + String.valueOf(dayOfWeek));
                    String title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
                    Log.i("AHS", i + " Day: " + title);
                    textView.setText(title);
                }
            }

        }



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}