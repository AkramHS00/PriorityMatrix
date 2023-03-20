package com.akram.prioritymatrix.user;

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
import android.widget.TextView;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private BottomNavigationView navBar;
    private boolean restoreNav;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginViewModel = new ViewModelProvider(getActivity()).get(LoginViewModel.class);

        //Hide navbar
        restoreNav = true;
        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.VISIBLE){
            navBar.setVisibility(View.GONE);
        }

        //Set views
        Button loginBtn = (Button) getView().findViewById(R.id.loginBtn);
        TextView userName = getView().findViewById(R.id.username);
        TextView password = getView().findViewById(R.id.password);
        TextView signUp = getView().findViewById(R.id.signUp);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check username and password fields are not empty
                if (userName.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty()){

                    Toast.makeText(getActivity(), "Please fill in username and password fields.", Toast.LENGTH_SHORT).show();

                } else {
                    //Set currently logged in user and navigate back to map with username as bundle
                    User currentUser = loginViewModel.getUser(userName.getText().toString(), password.getText().toString());
                    if (currentUser != null) {
                        ((MainActivity) getActivity()).setCurrentUser(currentUser);

                        Log.i("AHS", "Current user id = " + currentUser.getId() + "Current username = " + currentUser.getUserName() + "Current user password = " + currentUser.getPassword());
                        Bundle bundle = new Bundle();
                        bundle.putString("userName", currentUser.getUserName().toString());
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_navigation_login_to_navigation_home, bundle);
                    } else {
                        Toast.makeText(getActivity(), "Invalid login details.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        //Navigate to sign up screen
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreNav = false;
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_navigation_login_to_signUpFragment);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //Show navbar again unless navigating to sign up screen
        if (restoreNav){
            BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
            navBar.setVisibility(View.VISIBLE);
        }

    }
}