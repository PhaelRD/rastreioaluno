package com.example.rastreioaluno;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialize o Firebase App
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Inicializando o Realtime Database

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.register_button);
        loginButton = findViewById(R.id.login_button);

        registerButton.setOnClickListener(v -> registerUser(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString()
        ));
        loginButton.setOnClickListener(v -> loginUser(emailEditText.getText().toString(), passwordEditText.getText().toString()));
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro bem-sucedido, adicionar o usuário ao Realtime Database
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String fullUid = firebaseUser.getUid();

                        // Criar objeto do usuário
                        User user = new User(fullUid, email);

                        // Salvar o usuário no Realtime Database
                        mDatabase.child("users").child(fullUid).setValue(user)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        // Redirecionar para HomeActivity
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish(); // Finaliza a MainActivity para que o usuário não possa voltar para ela.
                                    } else {
                                        Toast.makeText(MainActivity.this, "Falha ao salvar dados do usuário: " + saveTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
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

    // Classe de modelo para o usuário
    public static class User {
        public String fullUid;
        public String shortUserId;
        public String email;

        public User() {
            // Construtor vazio necessário para o Firebase
        }

        public User(String fullUid, String email) {
            this.fullUid = fullUid;
            this.shortUserId = fullUid.length() >= 6 ? fullUid.substring(0, 6) : fullUid;
            this.email = email;
        }
    }
}
