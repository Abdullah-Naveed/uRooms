/*
 * Similar to the log in activity, except this time it registers a user for the first time.
 */


package com.halaltokens.halaltokens.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.halaltokens.halaltokens.R;

public class RegistrationScreen extends AppCompatActivity implements View.OnClickListener {

    EditText editEmail, editPassword;
    private FirebaseAuth firebaseAuth;
    private LottieAnimationView signUpProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);

        editEmail = findViewById(R.id.edit_text_email);
        editPassword = findViewById(R.id.edit_text_password);
        signUpProgress = findViewById(R.id.sign_up_progress);

        firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.reg_button).setOnClickListener(this);
        findViewById(R.id.already_user_button).setOnClickListener(this);

        editPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                hideKeyboard(editPassword);
                registerUser();
                return true;
            }
            return false;
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reg_button:
                registerUser();
                break;

            case R.id.already_user_button:
                finish();
                openLoginActivity();
                break;
        }
    }


    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }


    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
            return;
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please enter a valid UCD email");
            editEmail.requestFocus();
            return;
        }

        if (!email.toLowerCase().contains("ucd.ie") && !email.toLowerCase().contains("ucdconnect.ie")) {
            editEmail.setError("Please enter a valid UCD email");
            editEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Password should be at least 6 characters long");
            editPassword.requestFocus();
            return;
        }

        signUpProgress.playAnimation();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            signUpProgress.cancelAnimation();
            signUpProgress.setVisibility(View.GONE);
            if (task.isSuccessful()) {

                Toast.makeText(getApplicationContext(), "User Registration Complete\nEmail Verification Sent", Toast.LENGTH_SHORT).show();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                firebaseUser.sendEmailVerification();

                Intent i = new Intent(RegistrationScreen.this, LoginScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            } else {
                Toast.makeText(getApplicationContext(), "An Error Occurred", Toast.LENGTH_SHORT).show();

                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(getApplicationContext(), "This Email Address is already in Use.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}


