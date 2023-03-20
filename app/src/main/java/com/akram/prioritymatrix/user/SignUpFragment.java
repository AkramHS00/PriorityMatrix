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
import android.widget.EditText;
import android.widget.Toast;

import com.akram.prioritymatrix.MainActivity;
import com.akram.prioritymatrix.R;
import com.akram.prioritymatrix.database.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SignUpFragment extends Fragment {

    private SignUpViewModel signUpViewModel;
    private boolean restoreNav;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signUpViewModel = new ViewModelProvider(getActivity()).get(SignUpViewModel.class);

        //Hide navbar
        restoreNav = false;
        BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
        if(navBar.getVisibility() == View.VISIBLE){
            navBar.setVisibility(View.GONE);
        }

        //Get views
        Button signUpBtn = (Button) getView().findViewById(R.id.signUpBtn);
        EditText userName = getView().findViewById(R.id.newUsername);
        EditText password = getView().findViewById(R.id.newPassword);
        EditText name = getView().findViewById(R.id.name);
        EditText reNewPassword = getView().findViewById(R.id.reNewPassword);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check fields are not empty
                if (userName.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty() ||
                        name.getText().toString().trim().isEmpty() || reNewPassword.getText().toString().trim().isEmpty()){

                    Toast.makeText(getActivity(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    //Check to ensure password and re-enter password are equal
                } else if (!password.getText().toString().trim().equals(reNewPassword.getText().toString().trim())){
                    Toast.makeText(getActivity(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    //Check if username is already taken
                    if(signUpViewModel.isTaken(userName.getText().toString())){
                        Toast.makeText(getActivity(), "Username is taken, please choose another.", Toast.LENGTH_SHORT).show();
                        Log.i("AHS", "Username: " + userName.getText() + " password: " + password.getText());
                    } else {
                        //Create user and add to database, set the current user to new user
                        User newUser = new User(name.getText().toString(), userName.getText().toString(), password.getText().toString());
                        signUpViewModel.insert(newUser);
                        Log.i("AHS", "New user with Name: " + newUser.getName() + "Id: " + newUser.getId() + " Username: " + newUser.getUserName());
                        ((MainActivity) getActivity()).setCurrentUser(newUser);
                        restoreNav = true;
                        NavHostFragment.findNavController(SignUpFragment.this)
                                .navigate(R.id.action_navigation_sign_up_to_navigation_home2);
                    }
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(restoreNav){
            BottomNavigationView navBar = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
            navBar.setVisibility(View.VISIBLE);
        }

    }
}