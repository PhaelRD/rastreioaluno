package com.example.rastreioaluno;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
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

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Switch locationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationSwitch = findViewById(R.id.location_switch);

        locationSwitch.setOnCheckedChangeListener(this::onLocationSwitchChanged);

        // Verificar permissões e iniciar o rastreamento
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
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

        // Atualizar o banco de dados do Firebase com a localização
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
                // Permissão concedida
                if (locationSwitch.isChecked()) {
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(this, "Permissão de localização necessária para compartilhar sua localização.", Toast.LENGTH_LONG).show();
                locationSwitch.setChecked(false); // Desmarcar o switch se a permissão não for concedida
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    // Classe de modelo para dados de localização
    public static class LocationData {
        public double latitude;
        public double longitude;

        public LocationData() {
            // Construtor vazio necessário para o Firebase
        }

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}