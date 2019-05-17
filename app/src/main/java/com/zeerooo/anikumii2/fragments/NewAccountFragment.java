package com.zeerooo.anikumii2.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.zeerooo.anikumii2.Anikumii;
import com.zeerooo.anikumii2.R;
import com.zeerooo.anikumii2.activities.MainActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Created by ZeeRooo on 29/01/18
 */

public class NewAccountFragment extends Fragment {
    private TextInputEditText userName, userEmail, userPassword;
    private String nameStr, emailStr, passwordStr;
    private FloatingActionButton registarFAB;

    public NewAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().findViewById(R.id.act_login_rootView).setBackgroundColor(Color.WHITE);
            getActivity().findViewById(R.id.create_an_account).setVisibility(View.GONE);
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_account, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getView() != null && getActivity() != null) {

            userName = getView().findViewById(R.id.newAcc_user_name);
            userEmail = getView().findViewById(R.id.newAcc_user_mail);
            userPassword = getView().findViewById(R.id.newAcc_user_password);
            registarFAB = getView().findViewById(R.id.new_acc_register);
            registarFAB.setOnClickListener((View view) -> click());

            Button close = getView().findViewById(R.id.close_registerFragment);
            close.setOnClickListener((View view) -> {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                }
            });
        }
    }

    private void click() {
        String email = userEmail.getText().toString();
        if (email.contains("@"))
            emailStr = email;
        else if (!email.isEmpty())
            userEmail.setError(getString(R.string.error_invalid_email));
        else
            userEmail.setError(getString(R.string.error_field_required));

        String password = userPassword.getText().toString();
        if (password.length() > 4)
            passwordStr = password;
        else if (!password.isEmpty())
            userPassword.setError(getString(R.string.error_invalid_password));
        else
            userPassword.setError(getString(R.string.error_field_required));

        String user = userName.getText().toString();
        if (!user.isEmpty())
            nameStr = user;
        else
            userName.setError(getString(R.string.error_field_required));

        if (nameStr != null && passwordStr != null && emailStr != null)
            new task().execute(new String[]{nameStr, emailStr, passwordStr});
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() != null && getArguments() != null) {
            getActivity().findViewById(R.id.create_an_account).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.act_login_rootView).setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));
        }
    }

    private class task extends AsyncTask<String[], Void, Boolean> {
        @Override
        protected Boolean doInBackground(String[]... array) {
            try {
                Jsoup.connect("https://animeflv.net/auth/sign_up")
                        .userAgent(Anikumii.userAgent)
                        .data("username", array[0][0], "email", array[0][1], "password", array[0][2], "password_confirm", array[0][2])
                        .method(Connection.Method.POST)
                        .execute();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return getView() != null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                registarFAB.hide();
                getView().findViewById(R.id.register_success).setVisibility(View.VISIBLE);
            }
        }
    }
}
