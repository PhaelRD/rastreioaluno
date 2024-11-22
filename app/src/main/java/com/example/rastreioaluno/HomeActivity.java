package com.example.rastreioaluno;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SwitchMaterial locationSwitch;
    private TextView userIdTextView;
    private ImageButton shareButton; // Referência ao botão de compartilhamento
    private TextView locationWarningTextView;
    private Spinner userTypeSpinner;
    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        userIdTextView = findViewById(R.id.user_id_text_view);
        shareButton = findViewById(R.id.share_button); // Inicialize o botão de compartilhamento
        locationWarningTextView = findViewById(R.id.location_warning_text_view);
        locationSwitch = findViewById(R.id.location_switch);
        userTypeSpinner = findViewById(R.id.user_type_spinner);
        nameEditText = findViewById(R.id.name_edit_text);

        findViewById(R.id.save_button).setOnClickListener(v -> saveUserInfo());
        findViewById(R.id.logout_button).setOnClickListener(v -> logout());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        checkUserInfo();

        locationSwitch.setOnCheckedChangeListener(this::onLocationSwitchChanged);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isSwitchChecked = sharedPreferences.getBoolean("locationSwitchState", false);
        locationSwitch.setChecked(isSwitchChecked);

        if (isSwitchChecked && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Configuração do listener para o botão de compartilhamento
        shareButton.setOnClickListener(v -> shareUserId());
    }

    private void shareUserId() {
        String userId = userIdTextView.getText().toString();
        if (!userId.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("User ID", userId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "ID do usuário copiado para a área de transferência!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "ID do usuário não disponível.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onLocationSwitchChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationService();

                String userId = mAuth.getCurrentUser().getUid();
                String shortUserId = userId.length() >= 6 ? userId.substring(0, 6) : userId;
                userIdTextView.setText("User ID: " + shortUserId);
                userIdTextView.setVisibility(TextView.VISIBLE);
                shareButton.setVisibility(ImageButton.VISIBLE); // Torna o botão de compartilhamento visível
                locationWarningTextView.setVisibility(TextView.VISIBLE);
            } else {
                locationSwitch.setChecked(false);
                Toast.makeText(this, "Permissão de localização necessária.", Toast.LENGTH_SHORT).show();
            }
        } else {
            stopLocationService();
            userIdTextView.setVisibility(TextView.GONE);
            shareButton.setVisibility(ImageButton.GONE); // Oculta o botão de compartilhamento
            locationWarningTextView.setVisibility(TextView.GONE);
        }

        saveSwitchState(isChecked);
    }

    private void saveUserInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        String userType = userTypeSpinner.getSelectedItem().toString();
        String name = nameEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, insira seu nome.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserData userData = new UserData(userType, name);

        mDatabase.child("users").child(userId).child("info").setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeActivity.this, "Informações salvas com sucesso.", Toast.LENGTH_SHORT).show();
                    locationSwitch.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Falha ao salvar informações: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkUserInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserData userData = dataSnapshot.getValue(UserData.class);
                    if (userData != null) {
                        nameEditText.setText(userData.name);
                        int spinnerPosition = ((ArrayAdapter<String>)userTypeSpinner.getAdapter()).getPosition(userData.userType);
                        userTypeSpinner.setSelection(spinnerPosition);
                        locationSwitch.setEnabled(true);
                    } else {
                        locationSwitch.setEnabled(false);
                    }
                } else {
                    locationSwitch.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Erro ao verificar informações do usuário: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("locationSwitchState", isChecked);
        editor.apply();
    }

    private void logout() {
        // Desativa o switch e salva seu estado
        locationSwitch.setChecked(false);
        saveSwitchState(false);

        // Desconecta o usuário do Firebase
        mAuth.signOut();

        // Redireciona para a MainActivity e finaliza a HomeActivity
        Intent mainIntent = new Intent(HomeActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);
    }

    private void stopLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (locationSwitch.isChecked()) {
                    startLocationService();
                }
            } else {
                locationSwitch.setChecked(false);
                Toast.makeText(this, "Permissão de localização é necessária para rastreamento.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class UserData {
        public String userType;
        public String name;

        public UserData() {
            // Construtor padrão necessário para deserialização do Firebase
        }

        public UserData(String userType, String name) {
            this.userType = userType;
            this.name = name;
        }
    }
}
