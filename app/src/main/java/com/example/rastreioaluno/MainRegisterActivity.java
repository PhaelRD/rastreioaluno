package com.example.rastreioaluno;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ProgressBar; // Import ProgressBar
import android.view.View; // Import View for overlay
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.os.Handler; // Import Handler for delaying

public class MainRegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText registerEmailEditText;
    private EditText registerPasswordEditText;
    private EditText registerConfirmPasswordEditText;
    private Button registerButton;
    private ImageButton backToLoginButton;
    private ProgressBar loadingSpinner; // ProgressBar for loading
    private View loadingOverlay; // Transparent overlay for blocking interaction

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        registerEmailEditText = findViewById(R.id.register_email);
        registerPasswordEditText = findViewById(R.id.register_password);
        registerConfirmPasswordEditText = findViewById(R.id.register_confirm_password);
        registerButton = findViewById(R.id.register_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);
        loadingSpinner = findViewById(R.id.loading_spinner); // ProgressBar
        loadingOverlay = findViewById(R.id.loading_overlay); // Transparent overlay

        // Action for the back to login button with animation
        backToLoginButton.setOnClickListener(v -> {
            // Start the animation for transition
            Intent intent = new Intent(MainRegisterActivity.this, MainActivity.class); // MainActivity is the login screen
            startActivity(intent);

            // Set the left-to-right animation
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            finish(); // Finish the register screen
        });

        // Action for the register button
        registerButton.setOnClickListener(v -> {
            String email = registerEmailEditText.getText().toString();
            String password = registerPasswordEditText.getText().toString();
            String confirmPassword = registerConfirmPasswordEditText.getText().toString();
            registerUser(email, password, confirmPassword);
        });
    }


    private void registerUser(String email, String password, String confirmPassword) {
        // Check if email, password, or confirmPassword are empty
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(MainRegisterActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(MainRegisterActivity.this, "As senhas não coincidem.", Toast.LENGTH_LONG).show();
            return;
        }

        // Show loading overlay and progress spinner
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String fullUid = firebaseUser.getUid();
                        User user = new User(fullUid, email);

                        mDatabase.child("users").child(fullUid).setValue(user)
                                .addOnCompleteListener(saveTask -> {
                                    // Hide loading UI after 1.5 seconds minimum delay
                                    new Handler().postDelayed(() -> {
                                        hideLoadingUI();
                                        if (saveTask.isSuccessful()) {
                                            // Apply right-to-left animation
                                            Intent intent = new Intent(MainRegisterActivity.this, HomeActivity.class);
                                            startActivity(intent);

                                            // Set the right-to-left animation
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                                            finish(); // Finish the register screen
                                        } else {
                                            Toast.makeText(MainRegisterActivity.this, "Erro ao salvar dados do usuário.", Toast.LENGTH_LONG).show();
                                        }
                                    }, 1500); // Delay of 1.5 seconds
                                });
                    } else {
                        // Hide loading UI after 1.5 seconds minimum delay
                        new Handler().postDelayed(() -> {
                            hideLoadingUI();
                            handleFirebaseAuthError(task.getException());
                        }, 1500); // Delay of 1.5 seconds
                    }
                });
    }



    private void hideLoadingUI() {
        // Hide the loading overlay and progress spinner
        loadingOverlay.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
    }

    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();
            String message;

            switch (errorCode) {
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    message = "Este e-mail já está em uso.";
                    break;
                case "ERROR_WEAK_PASSWORD":
                    message = "A senha é muito fraca.";
                    break;
                default:
                    message = "Erro: " + authException.getMessage();
                    break;
            }
            Toast.makeText(MainRegisterActivity.this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainRegisterActivity.this, "Erro inesperado: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static class User {
        public String fullUid;
        public String shortUserId;
        public String email;

        public User() {}

        public User(String fullUid, String email) {
            this.fullUid = fullUid;
            this.shortUserId = fullUid.length() >= 6 ? fullUid.substring(0, 6) : fullUid;
            this.email = email;
        }
    }
}
