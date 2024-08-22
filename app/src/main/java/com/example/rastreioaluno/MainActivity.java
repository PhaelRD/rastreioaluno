package com.example.rastreioaluno;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);

        registerButton.setOnClickListener(v -> registerUser(emailEditText.getText().toString(), passwordEditText.getText().toString()));
        loginButton.setOnClickListener(v -> loginUser(emailEditText.getText().toString(), passwordEditText.getText().toString()));
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro bem-sucedido, redirecionar para HomeActivity
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza a MainActivity para que o usuário não possa voltar para ela.
                    } else {
                        // Falha no registro, capturar o erro e exibir mensagem ao usuário
                        handleFirebaseAuthError(task.getException());
                    }
                });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login bem-sucedido, redirecionar para HomeActivity
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza a MainActivity para que o usuário não possa voltar para ela.
                    } else {
                        // Falha no login, capturar o erro e exibir mensagem ao usuário
                        handleFirebaseAuthError(task.getException());
                    }
                });
    }

    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(MainActivity.this, "O formato do e-mail está inválido.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    Toast.makeText(MainActivity.this, "Este e-mail já está em uso.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_WEAK_PASSWORD":
                    Toast.makeText(MainActivity.this, "A senha é muito fraca. Por favor, escolha uma senha mais forte.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(MainActivity.this, "Usuário não encontrado. Verifique suas credenciais.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_WRONG_PASSWORD":
                    Toast.makeText(MainActivity.this, "Senha incorreta. Tente novamente.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_USER_DISABLED":
                    Toast.makeText(MainActivity.this, "Esta conta foi desativada.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Erro no login/registro: " + authException.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toast.makeText(MainActivity.this, "Erro inesperado: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
