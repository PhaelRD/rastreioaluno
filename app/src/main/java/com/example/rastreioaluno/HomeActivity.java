package com.example.rastreioaluno;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Switch locationSwitch;
    private TextView userIdTextView;
    private Spinner userTypeSpinner;
    private EditText nameEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        userIdTextView = findViewById(R.id.user_id_text_view);
        locationSwitch = findViewById(R.id.location_switch);
        userTypeSpinner = findViewById(R.id.user_type_spinner);
        nameEditText = findViewById(R.id.name_edit_text);
        saveButton = findViewById(R.id.save_button);

        // Exibir o shortUserId
        String userId = mAuth.getCurrentUser().getUid();
        String shortUserId = userId.length() >= 6 ? userId.substring(0, 6) : userId;
        userIdTextView.setText("User ID: " + shortUserId);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        // Verificar se as informações do usuário estão preenchidas e atualizar o switch
        checkUserInfo();

        saveButton.setOnClickListener(v -> saveUserInfo());

        locationSwitch.setOnCheckedChangeListener(this::onLocationSwitchChanged);

        // Verificar permissões e iniciar o rastreamento
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void checkUserInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserData userData = dataSnapshot.getValue(UserData.class);
                    if (userData != null) {
                        // Preencher campos com informações existentes
                        nameEditText.setText(userData.name);
                        int spinnerPosition = ((ArrayAdapter<String>)userTypeSpinner.getAdapter()).getPosition(userData.userType);
                        userTypeSpinner.setSelection(spinnerPosition);

                        // Habilitar o switch de localização
                        locationSwitch.setEnabled(true);
                    } else {
                        // Se não existir, desabilitar o switch de localização
                        locationSwitch.setEnabled(false);
                    }
                } else {
                    // Se não existir, desabilitar o switch de localização
                    locationSwitch.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Erro ao verificar informações do usuário: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        String userType = userTypeSpinner.getSelectedItem().toString();
        String name = nameEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, insira seu nome.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar um objeto UserData com o tipo de usuário e nome
        UserData userData = new UserData(userType, name);

        // Atualizar o banco de dados do Firebase com as informações do usuário
        mDatabase.child("users").child(userId).child("info").setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HomeActivity.this, "Informações salvas com sucesso.", Toast.LENGTH_SHORT).show();
                    locationSwitch.setEnabled(true); // Habilitar o switch após salvar as informações
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Falha ao salvar informações: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void onLocationSwitchChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationSwitch.setChecked(false); // Desmarcar o switch se a permissão não for concedida
                Toast.makeText(this, "Permissão de localização necessária.", Toast.LENGTH_SHORT).show();
            }
        } else {
            stopLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Atualizar a cada 10 segundos
        locationRequest.setFastestInterval(5000); // Atualizar a cada 5 segundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationInFirebase(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Toast.makeText(this, "Erro ao solicitar atualizações de localização: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback);
            } catch (SecurityException e) {
                Toast.makeText(this, "Erro ao parar atualizações de localização: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateLocationInFirebase(Location location) {
        String userId = mAuth.getCurrentUser().getUid();

        // Criar um objeto LocationData com a latitude e longitude
        LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());

        // Atualizar o banco de dados do Firebase com a localização do usuário
        mDatabase.child("users").child(userId).child("location").setValue(locationData)
                .addOnSuccessListener(aVoid -> {
                    // Localização atualizada com sucesso
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Falha ao atualizar localização: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Classe para armazenar dados do usuário
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

    // Classe para armazenar dados de localização
    private static class LocationData {
        public double latitude;
        public double longitude;

        public LocationData() {
            // Construtor padrão necessário para deserialização do Firebase
        }

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}