package com.example.checkin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.checkin.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    public EditText etname, etmatric, etphone, etemail;
    Button proceedBtn, quitBtn;
    private FusedLocationProviderClient fusedLocationClient;
    public double latitude;
    public double longitude;
    public DatabaseReference databaseReference;


    ActivityMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        etname = findViewById(R.id.etname);
        etmatric = findViewById(R.id.etmatric);
        etphone = findViewById(R.id.etphone);
        etemail = findViewById(R.id.etemail);

        proceedBtn = findViewById(R.id.checkinBtn);
        proceedBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                Intent intent = new Intent(MainActivity.this, CheckInPage.class);
                intent.putExtra("name", etname.getText().toString().trim());
                intent.putExtra("matric", etmatric.getText().toString().trim());
                intent.putExtra("phone", etphone.getText().toString().trim());
                intent.putExtra("email", etemail.getText().toString().trim());
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Please fill all the details to proceed", Toast.LENGTH_SHORT).show();
            }
        });


        quitBtn = findViewById(R.id.quitBtn);
        quitBtn.setOnClickListener(v -> {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    private boolean validateInputs() {
        String name = etname.getText().toString().trim();
        String matric = etmatric.getText().toString().trim();
        String phone = etphone.getText().toString().trim();
        String email = etemail.getText().toString().trim();

        return !name.isEmpty() && !matric.isEmpty() && !phone.isEmpty() && !email.isEmpty();
    }

    public void sendUserDataAndLocationToCheckInPage() {
        String name = etname.getText().toString().trim();
        String matric = etmatric.getText().toString().trim();
        String phone = etphone.getText().toString().trim();
        String email = etemail.getText().toString().trim();

        CheckInData checkInData = new CheckInData(name, matric, phone, email, "", latitude, longitude, "");
        databaseReference = FirebaseDatabase.getInstance().getReference("checkin"); // Ensure the reference is initialized

        databaseReference.push().setValue(checkInData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Check-in successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, CheckInPage.class);
                    intent.putExtra("name", name);
                    intent.putExtra("matric", matric);
                    intent.putExtra("phone", phone);
                    intent.putExtra("email", email);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to check in", Toast.LENGTH_SHORT).show();
                });
    }


    public void getLocationAndSendData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        sendUserDataAndLocationToCheckInPage();

                    } else {
                        Toast.makeText(MainActivity.this, "Unable to get location. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                }) ;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSendData();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
